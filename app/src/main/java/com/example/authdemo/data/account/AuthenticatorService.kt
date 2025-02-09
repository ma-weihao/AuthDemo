package com.example.authdemo.data.account

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService : Service() {
    private lateinit var authenticator: AuthAuthenticator

    override fun onCreate() {
        super.onCreate()
        authenticator = AuthAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return authenticator.iBinder
    }
} 