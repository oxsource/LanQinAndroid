package pizzk.android.lanqin.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

internal object JsonUtils {

    private fun getMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }

    fun json(obj: Any?): String {
        return try {
            getMapper().writeValueAsString(obj)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    inline fun <reified T> parse(json: String): T? {
        return try {
            getMapper().readValue<T>(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}