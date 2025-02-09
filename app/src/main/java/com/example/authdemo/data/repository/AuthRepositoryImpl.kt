package com.example.authdemo.data.repository

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.example.authdemo.data.account.AuthAuthenticator
import com.example.authdemo.data.remote.AuthApi
import com.example.authdemo.domain.model.AuthToken
import com.example.authdemo.domain.model.User
import com.example.authdemo.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val context: Context,
    private val api: AuthApi
) : AuthRepository {

    private val accountManager = AccountManager.get(context)

    suspend fun exchangeCodeForToken(
        code: String,
        redirectUri: String,
        clientId: String
    ): Result<AuthToken> = withContext(Dispatchers.IO) {
        try {
            val response = api.getToken(
                code = code,
                redirectUri = redirectUri,
                clientId = clientId
            )
            val token = AuthToken(
                accessToken = response.access_token,
                tokenType = response.token_type,
                expiresIn = response.expires_in,
                userName = response.user_name
            )

            // Create or update account
            val account = Account(token.userName, AuthAuthenticator.ACCOUNT_TYPE)
            if (accountManager.getAccountsByType(AuthAuthenticator.ACCOUNT_TYPE).isEmpty()) {
                accountManager.addAccountExplicitly(account, null, null)
            }
            accountManager.setAuthToken(account, AuthAuthenticator.AUTH_TOKEN_TYPE, token.accessToken)

            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserInfo(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val account = accountManager.getAccountsByType(AuthAuthenticator.ACCOUNT_TYPE).firstOrNull()
                ?: return@withContext Result.failure(Exception("No account found"))

            val token = accountManager.peekAuthToken(account, AuthAuthenticator.AUTH_TOKEN_TYPE)
                ?: return@withContext Result.failure(Exception("No token found"))

            val response = api.getUserInfo("Bearer $token")
            Result.success(User(response.username, response.name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        val accounts = accountManager.getAccountsByType(AuthAuthenticator.ACCOUNT_TYPE)
        accounts.forEach { account ->
            accountManager.removeAccount(account, null, null)
        }
    }

    override fun isLoggedIn(): Boolean {
        return accountManager.getAccountsByType(AuthAuthenticator.ACCOUNT_TYPE).isNotEmpty()
    }
} 