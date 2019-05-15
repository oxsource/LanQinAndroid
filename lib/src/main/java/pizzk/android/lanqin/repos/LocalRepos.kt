package pizzk.android.lanqin.repos

import pizzk.android.lanqin.db.LanQinDatabase
import pizzk.android.lanqin.db.LogDao
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import java.lang.Exception
import java.util.*

/**本地存储仓库*/
internal object LocalRepos {

    fun save(logs: List<LogTextEntity>, db: LanQinDatabase): Int {
        return try {
            val dao: LogDao = db.log()
            val ids: List<Long> = dao.insertAll(logs)
            ids.filter { it > 0 }.size
        } catch (e: Exception) {
            0
        }
    }
}