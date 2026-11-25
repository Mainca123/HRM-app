package com.example.baseproject.data.api

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.baseproject.App
import com.example.baseproject.ui.login.LoginActivity
import com.example.baseproject.utils.SpManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val spManager: SpManager
) : Interceptor {

    @Volatile
    private var lastUnauthorizedRedirectMs: Long = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        val token = spManager.getAccessToken()
        if (token.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        val path = originalRequest.url.encodedPath
        val isLoginRequest = path.endsWith("/login", ignoreCase = true)

        if (response.code == 401 && !isLoginRequest) {
            spManager.logout()
            val now = System.currentTimeMillis()
            if (now - lastUnauthorizedRedirectMs > 3000L) {
                lastUnauthorizedRedirectMs = now
                App.context?.let { ctx ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            ctx,
                            "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(ctx, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        ctx.startActivity(intent)
                    }
                }
            }
        }

        return response
    }
}
