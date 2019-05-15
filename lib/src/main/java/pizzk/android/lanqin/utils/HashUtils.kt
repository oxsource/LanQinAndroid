package pizzk.android.lanqin.utils

import java.nio.charset.Charset
import java.security.MessageDigest

internal object HashUtils {

    fun sha(content: String): String {
        var dst: ByteArray? = null
        try {
            val alg = MessageDigest.getInstance("SHA-1")
            alg.update(content.toByteArray(Charset.forName("UTF-8")))
            dst = alg.digest()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dst ?: return ""
        return hex(dst)
    }

    private fun hex(bytes: ByteArray): String {
        val sbf = StringBuilder()
        bytes.forEachIndexed { index, _ ->
            val tp = (Integer.toHexString(bytes[index].toInt() and 0xFF))
            sbf.append(tp.padStart(2, '0'))
        }
        return sbf.toString()
    }
}