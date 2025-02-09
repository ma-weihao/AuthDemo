package com.example.authdemo.domain.model

data class AuthToken(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val userName: String
) 