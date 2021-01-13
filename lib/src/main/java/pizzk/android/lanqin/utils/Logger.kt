package pizzk.android.lanqin.utils

import android.util.Log
import pizzk.android.lanqin.LanQin
import pizzk.android.lanqin.entity.LanQinEntity
import java.io.File

object Logger {
    private const val TAG = "Logger@LanQin"
    private val SHADOW_FILE = "LanQin${File.separator}shadow.log"
    private val COMMON_FILE = "LanQin${File.separator}common.log"

    internal fun e(s: String) {
        if (!LanQin.config().debug) return
        Log.e(TAG, s)
    }

    private fun write(path: String, s: String, append: Boolean) {
        try {
            val cache = LanQin.app().externalCacheDir ?: return
            val file = File(cache, path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            Log.e(TAG, "log to file: " + file.absolutePath)
            if (append) file.appendText(s) else file.writeText(s)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun shadow(entities: List<LanQinEntity>) {
        if (!LanQin.config().debug) return
        if (entities.isEmpty()) return
        val json: String = JsonUtils.json(entities)
        write(SHADOW_FILE, json, append = false)
    }

    fun write(s: String, append: Boolean) {
        write(COMMON_FILE, s, append)
    }

    fun truncate() {
        try {
            val cache = LanQin.app().externalCacheDir ?: return
            listOf(COMMON_FILE, SHADOW_FILE).map {
                File(cache, it)
            }.filter(File::exists).forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}