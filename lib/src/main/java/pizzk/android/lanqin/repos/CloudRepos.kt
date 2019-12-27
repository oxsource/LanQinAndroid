package pizzk.android.lanqin.repos

import okhttp3.Call
import okhttp3.Response
import pizzk.android.lanqin.api.LanQinApi
import pizzk.android.lanqin.api.LanQinHttp
import pizzk.android.lanqin.api.LanQinHttpResult
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import java.lang.Exception

/**云存储仓库*/
internal object CloudRepos {

    fun save(entities: List<LanQinEntity>): Int {
        if (entities.isEmpty()) return 0
        return try {
            val json: String = JsonUtils.json(entities)
            val call: Call = LanQinHttp.post(LanQinApi.UPLOAD_LOG, json)
            val response: Response = call.execute()
            val result: LanQinHttpResult = LanQinHttp.results(response)
            if (result.successful()) entities.size else 0
        } catch (e: Exception) {
            0
        }
    }
}