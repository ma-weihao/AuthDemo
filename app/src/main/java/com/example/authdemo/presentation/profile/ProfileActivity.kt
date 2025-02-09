package com.example.authdemo.presentation.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.authdemo.data.remote.AuthApi
import com.example.authdemo.data.repository.AuthRepositoryImpl
import com.example.authdemo.domain.model.User
import com.example.authdemo.presentation.auth.AuthActivity
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize API and Repository
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // Android emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(AuthApi::class.java)
        authRepository = AuthRepositoryImpl(this, api)

        // Check if user is logged in
        if (!authRepository.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContent {
            ProfileScreen(
                authRepository = authRepository,
                onLogout = {
                    lifecycleScope.launch {
                        authRepository.logout()
                        startActivity(Intent(this@ProfileActivity, AuthActivity::class.java))
                        finish()
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(
    authRepository: AuthRepositoryImpl,
    onLogout: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val result = authRepository.getUserInfo()
            result.fold(
                onSuccess = { userInfo ->
                    user = userInfo
                    error = null
                },
                onFailure = { e ->
                    error = e.message
                }
            )
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("Logout")
            }
        } else if (user != null) {
            Text(
                "Welcome ${user!!.name}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                user!!.username,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
} 