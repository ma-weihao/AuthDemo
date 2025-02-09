package com.example.authdemo.data.account

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.authdemo.presentation.auth.AuthActivity

class AuthAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    companion object {
        const val ACCOUNT_TYPE = "com.example.authdemo"
        const val AUTH_TOKEN_TYPE = "OAuth"
        const val KEY_ERROR_MESSAGE = "ERROR_MESSAGE"
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, AuthActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val am = AccountManager.get(context)

        // If the caller requested an authToken type we don't support, return an error
        if (authTokenType != AUTH_TOKEN_TYPE) {
            return Bundle().apply {
                putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType")
            }
        }

        // Extract the stored token
        val authToken = am.peekAuthToken(account, authTokenType)

        // If we have a token, return it
        if (!authToken.isNullOrEmpty()) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        }

        // If we get here, then we need to re-authenticate
        val intent = Intent(context, AuthActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            putExtra(AccountManager.KEY_ACCOUNT_NAME, account?.name)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
        throw UnsupportedOperationException()
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
        return Bundle().apply {
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
        }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return if (AUTH_TOKEN_TYPE == authTokenType) "OAuth Access Token" else ""
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        return Bundle().apply {
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        }
    }
} 