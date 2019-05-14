package pizzk.android.lanqin.api

internal object LanQinApi {
    const val UPLOAD_LOG: String = "/app/log"
}

internal data class LanQinHttpResult(
    var code: Int = 0,
    var msg: String = ""
) {

    fun successful(): Boolean = code == SUCCESS_CODE

    companion object {
        const val SUCCESS_CODE = 1
    }
}