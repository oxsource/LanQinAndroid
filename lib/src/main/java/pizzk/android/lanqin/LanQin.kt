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
    fun upload(entity: LanQinEntity): Boolean {
        try {
            val logs = listOf(entity).map { e ->
                val log = LogTextEntity()
                val content = JsonUtils.json(e)
                e.hash = HashUtils.sha(content)
                log.content = JsonUtils.json(e)
                log.time = e.happenTime
                return@map log
            }
            if (CloudRepos.save(listOf(entity)) > 0) return true
            if (LocalRepos.save(logs, db) > 0) return true
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
            val currentMills = Date().time
            val maxExpire = 7 * 24 * 60 * 60
            //移除过期日志
            val expireOuts = logs.filter { log -> currentMills - log.time >= maxExpire }
            dao.insertAll(expireOuts)
            //上送日志
            val expireIns = logs.filter { log -> currentMills - log.time < maxExpire }
            val entities: List<LanQinEntity> = expireIns.map { log ->
                val json: String = log.content
                return@map JsonUtils.parse<LanQinEntity>(json)
            }.filterNotNull()
            if (CloudRepos.save(entities) <= 0) return
            dao.deleteAll(logs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 查询本地未上送日志
     */
    fun logs(page: Int, size: Int): List<LogTextEntity> = try {
        db.log().queryAll(page, size)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /**
     * 清空本地所有日志
     */
    fun cleanLogs() {
        db.truncate(LogTextEntity.TABLE)
    }
}