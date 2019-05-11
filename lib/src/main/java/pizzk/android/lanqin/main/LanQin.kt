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
        doAsync { db = LanQinDatabase.create(context) }
    }

    fun config() = config

    fun app() = app

    fun db(): LanQinDatabase? = db

    /**
     * 存储及上送日志
     */
    fun log(entity: LanQinEntity) {
        try {
            val logId: Int = LocalRepos.save(listOf(entity))
            val flag: Boolean = CloudRepos.save(listOf(entity)) > 0
            if (!flag) return
            val dao: LogDao = db()?.log() ?: return
            val item: LogTextEntity = dao.query(logId) ?: return
            item.setSynced(LogTextEntity.SYNCED_CODE)
            dao.update(item)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 上送未同步的日志
     */
    fun logs() {
        try {
            val dao: LogDao = db()?.log() ?: return
            val syncs: Array<Int> = arrayOf(LogTextEntity.NOT_SYNC_CODE)
            val logs: List<LogTextEntity> = dao.query(page = 0, size = 5, syncs = syncs)
            if (logs.isEmpty()) return
            val entities: List<LanQinEntity> = logs.map { log ->
                val json: String = log.content
                return@map JsonFormat.parse<LanQinEntity>(json)
            }.filterNotNull()
            val flag: Boolean = CloudRepos.save(entities) > 0
            if (!flag) return
            logs.forEach { e -> e.setSynced(LogTextEntity.SYNCED_CODE) }
            dao.updateAll(logs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}