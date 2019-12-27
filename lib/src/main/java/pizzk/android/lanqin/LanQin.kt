package pizzk.android.lanqin

import android.app.Application
import pizzk.android.lanqin.db.LanQinDatabase
import pizzk.android.lanqin.db.LogDao
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.db.truncate
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import pizzk.android.lanqin.repos.CloudRepos
import pizzk.android.lanqin.repos.LocalRepos
import pizzk.android.lanqin.utils.HashUtils
import java.lang.Exception
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
    private lateinit var db: LanQinDatabase

    /**
     * 初始化蓝芩SDK
     */
    fun init(context: Application, cfg: Config) {
        app = context
        config = cfg
        db = LanQinDatabase.create(context)
    }

    fun config() = config

    fun app() = app

    /**
     * 存储及上送日志
     */
    fun upload(vararg entity: LanQinEntity): Boolean {
        try {
            val es: List<LanQinEntity> = entity.toList()
            val logs: List<LogTextEntity> = es.map { e: LanQinEntity ->
                val log = LogTextEntity()
                val content: String = JsonUtils.json(e)
                e.hash = HashUtils.sha(content)
                log.content = JsonUtils.json(e)
                log.time = e.happenTime
                return@map log
            }

            val saved: Boolean = LocalRepos.save(logs, db) > 0
            if (CloudRepos.save(es) <= 0) return false
            if (saved) db.log().deleteAll(logs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 上送未同步的日志
     */
    fun uploads() {
        try {
            val dao: LogDao = db.log()
            val logs: List<LogTextEntity> = dao.queryAll(page = 0, size = 5)
            if (logs.isEmpty()) return
            val currentMills: Long = Date().time
            val maxExpire: Int = config.expireDays * 24 * 60 * 60
            //移除过期日志
            val expireOuts: List<LogTextEntity> =
                logs.filter { log: LogTextEntity -> currentMills - log.time >= maxExpire }
            if (expireOuts.isNotEmpty()) dao.deleteAll(expireOuts)
            //上送日志
            val expireIns: List<LogTextEntity> =
                logs.filter { log: LogTextEntity -> currentMills - log.time < maxExpire }
            val entities: List<LanQinEntity> = expireIns.map { log: LogTextEntity ->
                val json: String = log.content
                return@map JsonUtils.parse<LanQinEntity>(json)
            }.filterNotNull()
            if (entities.isEmpty() || CloudRepos.save(entities) <= 0) return
            dao.deleteAll(expireIns)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 查询本地未上送日志
     */
    fun logs(page: Int, size: Int): List<LogTextEntity>? = try {
        db.log().queryAll(page, size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    /**
     * 清空本地所有日志
     */
    fun cleanLogs() {
        db.truncate(LogTextEntity.TABLE)
    }
}