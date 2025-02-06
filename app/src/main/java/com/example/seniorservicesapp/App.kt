package com.example.seniorservicesapp

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // ✅ Force Firebase to initialize globally
        FirebaseApp.initializeApp(this)

        // ✅ Store global application context
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }

    fun getContext(): Context = instance.applicationContext
}
