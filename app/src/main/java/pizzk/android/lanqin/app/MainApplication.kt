package pizzk.android.lanqin.app

import android.app.Application
import pizzk.android.lanqin.main.LanQin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = LanQin.Config("com.pizzk.demo", "alpha", "localhost", BuildConfig.DEBUG)
        LanQin.init(this, config)
    }
}