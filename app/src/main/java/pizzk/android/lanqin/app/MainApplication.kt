package pizzk.android.lanqin.app

import android.app.Application
import org.jetbrains.anko.doAsync
import pizzk.android.lanqin.LanQin
import pizzk.android.lanqin.db.LogTextEntity

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        doAsync {
            val host = "http://192.168.0.104:8055"
            val config = LanQin.Config("001", "alpha", host, 7, BuildConfig.DEBUG)
            LanQin.init(this@MainApplication, config)
            LanQin.uploads()
            val logs: List<LogTextEntity> = LanQin.logs(page = 0, size = 1) ?: return@doAsync
            //本地无数据时重置清理数据库
            if (logs.isEmpty()) LanQin.cleanLogs()
        }
    }
}
