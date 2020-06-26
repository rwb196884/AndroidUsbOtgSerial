package com.rwb.androidusbotgserial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val serial = MutableLiveData<String>()

    fun receive(serialDataReceived:String){
        serial.value = serial.value + serialDataReceived
    }
}

class MainActivity : AppCompatActivity() {
    private val viewModel:MainActivityViewModel = MainActivityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Plumb in the view model
        viewModel.serial.observe(this, Observer {
            findViewById<TextView>(R.id.serial).text = it
        })

        // Do the USB stuff.

    }
}