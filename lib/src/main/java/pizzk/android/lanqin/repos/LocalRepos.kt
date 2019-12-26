package pizzk.android.lanqin.repos

import pizzk.android.lanqin.db.LanQinDatabase
import pizzk.android.lanqin.db.LogDao
import pizzk.android.lanqin.db.LogTextEntity
import java.lang.Exception

/**本地存储仓库*/
internal object LocalRepos {

    fun save(logs: List<LogTextEntity>, db: LanQinDatabase): Int {
        return try {
            val dao: LogDao = db.log()
            val ids: List<Long> = dao.insertAll(logs)
            logs.forEachIndexed { index: Int, e: LogTextEntity -> e.id = ids[index] }
            ids.filter { it > 0 }.size
        } catch (e: Exception) {
            0
        }
    }
}