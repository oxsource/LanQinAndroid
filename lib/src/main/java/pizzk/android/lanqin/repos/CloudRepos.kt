package pizzk.android.lanqin.repos

import okhttp3.Call
import okhttp3.Response
import pizzk.android.lanqin.LanQin
import pizzk.android.lanqin.api.LanQinApi
import pizzk.android.lanqin.api.LanQinHttp
import pizzk.android.lanqin.api.LanQinHttpResult
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import pizzk.android.lanqin.utils.Logger
import java.lang.Exception

/**云存储仓库*/
internal object CloudRepos {

    fun save(entities: List<LanQinEntity>): Int {
        return try {
            val json: String = JsonUtils.json(entities)
            Logger.e("cloud save: $json")
            if (LanQin.config().host.isEmpty()) {
                Logger.file(json)
                return entities.size
            }
            val call: Call = LanQinHttp.post(LanQinApi.UPLOAD_LOG, json)
            val response: Response = call.execute()
            val result: LanQinHttpResult = LanQinHttp.results(response)
            if (result.successful()) entities.size else 0
        } catch (e: Exception) {
            Logger.e("cloud save exp: ${e.message}")
            0
        }
    }
}