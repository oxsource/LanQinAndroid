package pizzk.android.lanqin.main

import android.app.Application
import org.jetbrains.anko.doAsync
import pizzk.android.lanqin.db.LanQinDatabase
import pizzk.android.lanqin.db.LogDao
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.repos.CloudRepos
import pizzk.android.lanqin.repos.LocalRepos
import java.lang.Exception

/**
 * 蓝芩APP性能及BUG采集SDK
 */
object LanQin {
    data class Config(
        val appId: String,
        val channel: String,
        /**上送服务器地址，不合规的地址将不进行上送*/
        val host: String,
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
            if (CloudRepos.save(listOf(entity)) > 0) return true
            if (LocalRepos.save(listOf(entity), db) > 0) return true
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
            val entities: List<LanQinEntity> = logs.map { log ->
                val json: String = log.content
                return@map JsonFormat.parse<LanQinEntity>(json)
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
    fun localLogs(page: Int, size: Int): List<LogTextEntity> = try {
        db.log().queryAll(page, size)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}