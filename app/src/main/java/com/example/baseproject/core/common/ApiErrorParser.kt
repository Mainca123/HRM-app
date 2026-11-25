package com.example.baseproject.core.common

import com.google.gson.JsonParser
import retrofit2.Response

/**
 * Helper to extract human-readable error messages from Retrofit HTTP error responses
 * matching the ASP.NET Core ApiResponse JSON envelope.
 */
object ApiErrorParser {

    fun <T> extractErrorMessage(response: Response<T>, defaultMessage: String): String {
        val bodyMsg = response.body()?.let { body ->
            try {
                val field = body::class.java.getDeclaredField("message")
                field.isAccessible = true
                field.get(body) as? String
            } catch (_: Exception) {
                null
            }
        }
        if (!bodyMsg.isNullOrBlank()) return bodyMsg

        val rawError = try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }

        if (!rawError.isNullOrBlank()) {
            try {
                val json = JsonParser.parseString(rawError).asJsonObject
                val msg = json.get("message")?.takeIf { !it.isJsonNull }?.asString
                if (!msg.isNullOrBlank()) return msg

                val errors = json.getAsJsonArray("errors")
                if (errors != null && errors.size() > 0) {
                    val firstErr = errors[0]?.asString
                    if (!firstErr.isNullOrBlank()) return firstErr
                }
            } catch (_: Exception) {
                // Non-JSON error body
            }
        }

        return when (response.code()) {
            400 -> "$defaultMessage (Dữ liệu không hợp lệ)"
            401 -> "Phiên đăng nhập đã hết hạn hoặc không hợp lệ"
            403 -> "Bạn không có quyền thực hiện thao tác này"
            404 -> "Không tìm thấy dữ liệu yêu cầu"
            in 500..599 -> "Lỗi máy chủ (${response.code()}). Vui lòng thử lại sau."
            else -> defaultMessage
        }
    }
}
