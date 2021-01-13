package pizzk.android.lanqin.app

import android.app.Application
import pizzk.android.lanqin.LanQin
import pizzk.android.lanqin.db.LogTextEntity

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        val config = LanQin.Config("001", "alpha", "", 7, BuildConfig.DEBUG)
        val config = LanQin.Config("alpha", BuildConfig.DEBUG)
        LanQin.init(this@MainApplication, config)
        LanQin.withCrashHandler(seconds = 5)
        LanQin.asyncTask {
            LanQin.uploads()
            val logs: List<LogTextEntity> = LanQin.logs(page = 0, size = 1) ?: return@asyncTask
            //本地无数据时重置清理数据库
            if (logs.isEmpty()) LanQin.cleanLogs()
        }
    }
}
