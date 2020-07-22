package com.hypertrack.backend

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface AccessTokenRepository {
    fun refreshToken(): String
    fun getAccessToken(): String
    suspend fun refreshTokenAsync() : String
    fun  getConfig() : Any
}

class BasicAuthAccessTokenRepository(
    private val authUrl: String,
    val deviceId: String,
    private val userName: String,
    private val userPwd: String = "",
    private var token: String? = null
) : AccessTokenRepository {

    private val okHttpClient : OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor(UserAgentInterceptor()).build()
    }

    private val request: Request by lazy {
        Request.Builder()
            .url(authUrl)
            .header(AUTH_HEADER_KEY, Credentials.basic(userName, userPwd))
            .post("""{"device_id": "$deviceId"}""".toRequestBody(MEDIA_TYPE_JSON))
            .build()
    }

    override fun getAccessToken(): String = token?:refreshToken()

    override fun refreshToken(): String {
        Log.v(TAG, "Refreshing token $token for user $userName for deviceId $deviceId")

        okHttpClient
            .newCall(request)
            .execute()
            .use { response -> token = getTokenFromResponse(response)}

        Log.v(TAG, "Updated bearer token $token" )
        return token ?: ""
    }

    override suspend fun refreshTokenAsync(): String =
        suspendCoroutine { cont ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to get ")
                    cont.resume("")
                }

                override fun onResponse(call: Call, response: Response) {
                    cont.resume(getTokenFromResponse(response))
                }
            })
        }

    private fun getTokenFromResponse(response: Response) : String {
        Log.d(TAG, "Getting token from response $response")
        if (response.isSuccessful) {
            response.body?.let {
                try {
                    val responseObject = Gson().fromJson(it.string(), AuthCallResponse::class.java)
                    return responseObject.accessToken
                } catch (ignored: JsonSyntaxException) {
                    Log.w(TAG, "Can't deserialize auth response ${it.string()}")
                }
            }
        } else {
            Log.w(TAG, "Failed to refresh token $response")
        }
        return ""
    }

    override fun getConfig() : BasicAuthAccessTokenConfig {
        return BasicAuthAccessTokenConfig(authUrl, deviceId, userName, userPwd, token)
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
        const val TAG = "AccessTokenRepo"
    }
}

private data class AuthCallResponse(
    @SerializedName("access_token") val accessToken:String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class BasicAuthAccessTokenConfig(
    val authUrl: String,
    val deviceId: String,
    val userName: String,
    val userPwd: String = "",
    var token: String? = null
)

const val AUTH_HEADER_KEY = "Authorization"