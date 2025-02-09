package com.example.authdemo.data.remote

import retrofit2.http.*

interface AuthApi {
    @POST("oauth/token")
    suspend fun getToken(
        @Query("grant_type") grantType: String = "authorization_code",
        @Query("code") code: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("client_id") clientId: String
    ): TokenResponse

    @GET("api/user")
    suspend fun getUserInfo(@Header("Authorization") token: String): UserResponse
}

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val user_name: String
)

data class UserResponse(
    val username: String,
    val name: String
) 