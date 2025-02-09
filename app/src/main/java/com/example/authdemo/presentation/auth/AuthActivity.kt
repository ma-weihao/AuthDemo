package com.example.authdemo.presentation.auth

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.authdemo.data.remote.AuthApi
import com.example.authdemo.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthActivity : ComponentActivity() {

    companion object {
        private const val CLIENT_ID = "your_client_id"
        private const val REDIRECT_URI = "authdemo://oauth/callback"
        private const val AUTH_ENDPOINT = "http://10.0.2.2:3000/oauth/authorize"
    }

    private lateinit var accountManager: AccountManager
    private lateinit var authRepository: AuthRepositoryImpl
    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var resultBundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get AccountAuthenticatorResponse from intent
        accountAuthenticatorResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        accountAuthenticatorResponse?.onRequestContinued()

        accountManager = AccountManager.get(this)

        // Initialize API and Repository
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // Android emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(AuthApi::class.java)
        authRepository = AuthRepositoryImpl(this, api)

        // Check if this is an OAuth callback
        if (intent?.data?.scheme == "authdemo") {
            handleOAuthCallback(intent.data)
            return
        }

        setContent {
            OAuthLoginScreen(
                onStartAuth = { startOAuthFlow() },
                onError = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun startOAuthFlow() {
        val authUri = Uri.parse(AUTH_ENDPOINT).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "profile")
            .build()

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, authUri)
    }

    private fun handleOAuthCallback(uri: Uri?) {
        uri?.getQueryParameter("code")?.let { code ->
            lifecycleScope.launch {
                try {
                    val token = authRepository.exchangeCodeForToken(
                        code = code,
                        redirectUri = REDIRECT_URI,
                        clientId = CLIENT_ID
                    )
                    token.fold(
                        onSuccess = { authToken ->
                            val bundle = Bundle().apply {
                                putString(AccountManager.KEY_ACCOUNT_NAME, authToken.userName)
                                putString(AccountManager.KEY_ACCOUNT_TYPE, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
                                putString(AccountManager.KEY_AUTHTOKEN, authToken.accessToken)
                            }
                            setAccountAuthenticatorResult(bundle)
                            finish()
                        },
                        onFailure = { error ->
                            Toast.makeText(this@AuthActivity, error.message, Toast.LENGTH_LONG).show()
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@AuthActivity, "Authentication failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setAccountAuthenticatorResult(result: Bundle) {
        resultBundle = result
    }

    override fun finish() {
        if (accountAuthenticatorResponse != null) {
            if (resultBundle != null) {
                accountAuthenticatorResponse?.onResult(resultBundle)
            } else {
                accountAuthenticatorResponse?.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            }
            accountAuthenticatorResponse = null
        }
        super.finish()
    }
}

@Composable
fun OAuthLoginScreen(
    onStartAuth: () -> Unit,
    onError: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onStartAuth,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with OAuth")
        }
    }
} 