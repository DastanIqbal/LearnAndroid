package com.dastanapps.moshi

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import org.junit.Test


@Keep
@JsonClass(generateAdapter = true)
data class ClassBResponseData(
    @Json(name = "message")
    val message: String?,
    @Json(name = "status")
    val status: String?,
    @Json(name = "token")
    val token: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class ClassAResponse<Data : ClassBResponseData>(
    @Json(name = "data")
    val `data`: Data?,
    @Json(name = "status")
    val status: String?
) {
}

class Scenario1Test {

    private val jsonString = """
        {
            "status": "success",
            "data": {
                "token": "token",
                "message": "Your request is accepted",
                "status": "1"
            }
        }
    """.trimIndent()

    private val moshiExt by lazy { MoshiExt()}

    @Test
    fun verify_type() {

        val type1 = Types.newParameterizedType(
            ClassAResponse::class.java,
            ClassBResponseData::class.java
        )

        val jsonAdapter: JsonAdapter<ClassAResponse<ClassBResponseData>> = moshiExt.moshi.adapter(type1)

        val response = moshiExt.jsonToClass(jsonString,jsonAdapter)

        println(response)
    }
}