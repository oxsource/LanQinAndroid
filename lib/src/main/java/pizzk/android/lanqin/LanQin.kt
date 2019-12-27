package pizzk.android.lanqin

import android.app.Application
import pizzk.android.lanqin.db.LanQinDatabase
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import pizzk.android.lanqin.repos.CloudRepos
import pizzk.android.lanqin.repos.LocalRepos
import pizzk.android.lanqin.utils.HashUtils
import java.util.*

/**
 * 蓝芩APP性能及BUG采集SDK
 */
object LanQin {
    data class Config(
        val appId: String,
        val channel: String,
        /**上送服务器地址，不合规的地址将不进行上送*/
        val host: String,
        /**最大缓存时间*/
        val expireDays: Int,
        var debug: Boolean = true
    )

    private lateinit var config: Config
    private lateinit var app: Application

    /**
     * 初始化蓝芩SDK
     */
    fun init(context: Application, cfg: Config) {
        app = context
        config = cfg
    }

    fun config() = config

    fun app() = app

    /**使用数据库进行操作*/
    private fun <T> database(then: (LanQinDatabase) -> T): T? {
        var instance: LanQinDatabase? = null
        return try {
            val db: LanQinDatabase = LanQinDatabase.create(app())
            instance = db
            then(db)
        } catch (e: Exception) {
            null
        } finally {
            instance?.close()
        }
    }

    /**
     * 存储及上送日志
     */
    fun upload(vararg entity: LanQinEntity): Boolean {
        val es: List<LanQinEntity> = entity.toList()
        val logs: List<LogTextEntity> = try {
            es.map { e: LanQinEntity ->
                val log = LogTextEntity()
                val content: String = JsonUtils.json(e)
                e.hash = HashUtils.sha(content)
                log.content = JsonUtils.json(e)
                log.time = e.happenTime
                return@map log
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        val saved: Boolean = database { LocalRepos.save(logs, it) > 0 } ?: false
        if (CloudRepos.save(es) <= 0 || !saved) return false
        database { it.log().deleteAll(logs) }
        return true
    }

    /**
     * 上送未同步的日志
     */
    fun uploads() {
        val logs: List<LogTextEntity> = logs(page = 0, size = 5) ?: return
        if (logs.isEmpty()) return
        //有效与无效日志分组
        val now: Long = Date().time
        val expire: Int = config.expireDays * 24 * 60 * 60
        val invalid: (LogTextEntity) -> Boolean = { e: LogTextEntity -> now - e.time >= expire }
        val group: Map<Boolean, List<LogTextEntity>> = logs.groupBy(invalid)
        //移除无效日志
        val bad: List<LogTextEntity> = group[true] ?: emptyList()
        if (bad.isNotEmpty()) database { it.log().deleteAll(bad) }
        //上传有效日志
        val good: List<LogTextEntity> = group[false] ?: emptyList()
        val es: List<LanQinEntity> = good.mapNotNull { JsonUtils.parse<LanQinEntity>(it.content) }
        if (CloudRepos.save(es) <= 0) return
        database { it.log().deleteAll(good) }
    }

    /**
     * 查询本地未上送日志
     */
    fun logs(page: Int, size: Int): List<LogTextEntity>? {
        if (page < 0 || size < 0) return null
        return database { it.log().queryAll(page, size) }
    }

    /**
     * 清空本地所有日志
     */
    fun cleanLogs() {
        LanQinDatabase.truncate(app(), LogTextEntity.TABLE)
    }
}