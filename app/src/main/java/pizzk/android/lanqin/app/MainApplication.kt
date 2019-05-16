package pizzk.android.lanqin.app

import android.app.Application
import org.jetbrains.anko.doAsync
import pizzk.android.lanqin.LanQin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        doAsync {
            val host = "http://192.168.0.104:8055"
            val config = LanQin.Config("001", "alpha", host, 7, BuildConfig.DEBUG)
            LanQin.init(this@MainApplication, config)
            //测试使用
            LanQin.cleanLogs()
        }
    }
}
