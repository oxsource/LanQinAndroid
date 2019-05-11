package pizzk.android.lanqin.main

import com.fasterxml.jackson.databind.ObjectMapper

object JsonFormat {
    fun json(obj: Any?): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(obj)
    }

    inline fun <reified T> parse(json: String): T? {
        val mapper = ObjectMapper()
        return mapper.readValue<T>(json, T::class.java)
    }
}