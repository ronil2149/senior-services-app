package com.example.seniorservicesapp

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }

    fun getContext(): Context = instance.applicationContext
}
