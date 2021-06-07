package pizzk.android.lanqin.repos

import okhttp3.Call
import okhttp3.Response
import pizzk.android.lanqin.api.LanQinApi
import pizzk.android.lanqin.api.LanQinHttp
import pizzk.android.lanqin.api.LanQinHttpResult
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import pizzk.android.lanqin.utils.Logger
import java.io.File
import java.lang.Exception

/**云存储仓库*/
internal object CloudRepos {

    fun save(entities: List<LanQinEntity>): Int {
        if (entities.isEmpty()) return 0
        return try {
            val json: String = JsonUtils.json(entities)
            Logger.d("cloud save: $json")
            val call: Call = LanQinHttp.post(LanQinApi.UPLOAD_LOG, json)
            val response: Response = call.execute()
            val result: LanQinHttpResult = LanQinHttp.results(response)
            if (result.successful()) entities.size else 0
        } catch (e: Exception) {
            Logger.e("cloud save exp: ${e.message}")
            0
        }
    }

    fun save(file: File): Boolean {
        if (!file.exists() || file.length() <= 0) return false
        return try {
            Logger.d("cloud save file: ${file.absolutePath}")
            val call: Call = LanQinHttp.post(LanQinApi.UPLOAD_JOURNAL, file)
            val response: Response = call.execute()
            val result: LanQinHttpResult = LanQinHttp.results(response)
            if (!result.successful()) throw Exception(result.msg)
            Logger.d("cloud save file success.")
            return true
        } catch (e: Exception) {
            Logger.e("cloud save file exp: ${e.message}")
            false
        }
    }
}