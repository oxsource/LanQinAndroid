package pizzk.android.lanqin.repos

import pizzk.android.lanqin.db.LogDao
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.main.JsonFormat
import pizzk.android.lanqin.main.LanQin
import java.lang.Exception
import java.util.*

/**本地存储仓库*/
object LocalRepos : LanQinRepos() {

    override fun save(entities: List<LanQinEntity>): Int {
        return try {
            val dao: LogDao = LanQin.db()?.log() ?: return 0
            val now = Date()
            val logs: List<LogTextEntity> = entities.map { e ->
                val log = LogTextEntity()
                log.setSynced(LogTextEntity.NOT_SYNC_CODE)
                log.content = JsonFormat.json(e)
                log.time = now.time
                return@map log
            }
            val ids: List<Int> = dao.insertAll(logs)
            ids.filter { it > 0 }.size
        } catch (e: Exception) {
            0
        }
    }
}