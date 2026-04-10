package com.example.vaultguard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VaultGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
