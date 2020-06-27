package com.rwb.service

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, TheService::class.java)
        serviceIntent.action = "com.rwb.service.THE_SERVICE"
        startService(serviceIntent)
    }
}