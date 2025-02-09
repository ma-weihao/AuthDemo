package com.example.authdemo.domain.repository

import com.example.authdemo.domain.model.AuthToken
import com.example.authdemo.domain.model.User

interface AuthRepository {
    suspend fun getUserInfo(): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Boolean
} 