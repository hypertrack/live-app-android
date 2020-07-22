package com.hypertrack.backend

import android.os.Build
import android.util.Log
import okhttp3.*
import okhttp3.internal.userAgent

class AccessTokenInterceptor(private val accessTokenRepository: AccessTokenRepository): Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val token = accessTokenRepository.getAccessToken()
        val request = chain.request().newBuilder().addHeader("Authorization","Bearer $token").build()
        return chain.proceed(request)

    }

}

class UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val networkingLibrary = userAgent
        val request = chain.request().newBuilder()
            .addHeader("User-Agent",
                "LiveApp/${BuildConfig.VERSION_NAME} $networkingLibrary Android/${Build.VERSION.RELEASE}"
            )
            .build()
        return chain.proceed(request)

    }

}

class AccessTokenAuthenticator(private val accessTokenRepository: AccessTokenRepository) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = accessTokenRepository.getAccessToken()

        if (response.authenticatedWithSameToken(accessToken)) {
            return null
        }

        synchronized(this) {
            return try {
                var updatedToken = accessTokenRepository.getAccessToken()
                if (updatedToken == accessToken) {
                    updatedToken = accessTokenRepository.refreshToken()
                }
                response.request.newBuilder()
                    .addHeader(AUTH_HEADER_KEY, "Bearer $updatedToken")
                    .build()
            } catch (e: IllegalStateException) {
                Log.w("AccessToken", "Authentication call failed", e)
                null
            }
        }
    }
}

fun Response.authenticatedWithSameToken(token : String) : Boolean = header(AUTH_HEADER_KEY, "")?.endsWith(token)?:false