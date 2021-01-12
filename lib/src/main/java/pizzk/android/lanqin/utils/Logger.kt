package pizzk.android.lanqin.utils

import android.util.Log
import pizzk.android.lanqin.LanQin
import java.io.File

internal object Logger {
    private const val TAG = "logger@LanQin"

    fun e(s: String) {
        if (!LanQin.config().debug) return
        Log.e(TAG, s)
    }

    fun file(s: String) {
        if (!LanQin.config().debug) return
        try {
            val cacheFile = LanQin.app().externalCacheDir ?: return
            val file = File(cacheFile, "LanQin${File.separator}log.txt")
            if (!file.parentFile.exists()) {
                file.mkdirs()
            }
            file.writeText(s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}