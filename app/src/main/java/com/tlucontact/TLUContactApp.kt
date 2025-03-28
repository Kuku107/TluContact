package com.tlucontact

import android.app.Application
import com.google.firebase.FirebaseApp

class TLUContactApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}