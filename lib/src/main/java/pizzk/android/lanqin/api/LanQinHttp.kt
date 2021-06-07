package pizzk.android.lanqin.api

import okhttp3.*
import pizzk.android.lanqin.utils.JsonUtils
import pizzk.android.lanqin.LanQin
import java.io.File
import java.util.concurrent.TimeUnit

/**蓝芩Http客户端*/
internal object LanQinHttp {
    /**超时设置*/
    private const val READ_TS = 30L
    private const val WRITE_TS = 45L
    private const val CONN_TS = 45L

    /**JSON媒体类型*/
    private const val TEXT_MEDIA = "text/xml"
    private const val JSON_MEDIA = "application/json"

    private val client: OkHttpClient by lazy {
        return@lazy OkHttpClient.Builder()
            .readTimeout(READ_TS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TS, TimeUnit.SECONDS)
            .connectTimeout(CONN_TS, TimeUnit.SECONDS)
            .build()
    }

    fun post(path: String, json: String): Call {
        val jsonMedia: MediaType? = MediaType.parse(JSON_MEDIA)
        val body: RequestBody = RequestBody.create(jsonMedia, json)
        val url = "${LanQin.config().host}$path"
        val request: Request = Request.Builder().url(url).post(body).build()
        return client.newCall(request)
    }

    fun post(path: String, file: File): Call {
        val content = RequestBody.create(MediaType.parse(TEXT_MEDIA), file)
        val body = MultipartBody.Builder()
            .addFormDataPart("file", file.name, content)
            .build()
        val url = "${LanQin.config().host}$path"
        val request: Request = Request.Builder().url(url).post(body).build()
        return client.newCall(request)
    }

    fun results(response: Response?): LanQinHttpResult {
        val result = LanQinHttpResult()
        val rsp: Response = response ?: return result
        if (!rsp.isSuccessful) return result
        val json: String = rsp.body()?.string() ?: ""
        return JsonUtils.parse<LanQinHttpResult>(json) ?: result
    }
}