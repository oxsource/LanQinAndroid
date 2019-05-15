package pizzk.android.lanqin.utils

import com.fasterxml.jackson.databind.ObjectMapper

internal object JsonUtils {
    fun json(obj: Any?): String {
        return try {
            val mapper = ObjectMapper()
            mapper.writeValueAsString(obj)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    inline fun <reified T> parse(json: String): T? {
        return try {
            val mapper = ObjectMapper()
            mapper.readValue<T>(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}