package com.rwb.androidusbotgserial

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var serial:TextView
    private lateinit var workerThread : Thread
    private lateinit var usbManager:UsbManager
    private lateinit var usbDevice:UsbDevice


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Plumb in the view model
        serial = findViewById<TextView>(R.id.serial)

        // Start setting up USB.
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        if(usbManager.deviceList.count() > 1){
            serial.append("Found " + usbManager.deviceList.count().toString() + " devices; don't know which one to pick.")
            Toast.makeText(this, "Found " + usbManager.deviceList.count().toString() + " devices; don't know which one to pick.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        else if(usbManager.deviceList.count() == 0) {
            serial.append("No USB devices.")
            Toast.makeText(this, "No USB device found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        usbDevice = usbManager.deviceList.map { z -> z.value }.first()
        serial.append("Found: " + usbDevice.deviceName)

        if(usbManager.accessoryList != null){
            for(a in usbManager.accessoryList){
                serial.append("device: " + a.description )
            }
        } else {
            serial.append("No accessories.")
        }

        // USB setup now continues in onResume.
    }


    private fun startWorker() {
        workerThread = Thread(Runnable {
            val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val usbDeviceConnection :UsbDeviceConnection = usbManager.openDevice(usbDevice)

            val usbInterface:UsbInterface = usbDevice.getInterface(0)
            val ep:UsbEndpoint = usbInterface.getEndpoint(0)

            usbDeviceConnection.claimInterface(usbInterface, true)


            var buffer: ByteArray = ByteArray(1024)

            usbDeviceConnection.bulkTransfer(ep, buffer, 1024, 0)

//            while (!Thread.currentThread().isInterrupted  && bts?.isConnected ?: false) {
//                try {
//                    val bytesAvailable: Int = bts?.inputStream?.available() ?: 0
//                    if (bytesAvailable > 0) {
//                        val buffer = ByteArray(bytesAvailable)
//                        bts?.inputStream?.read(buffer)
//                        val data = String(buffer, Charset.defaultCharset())
//
//                        runOnUiThread {
//                            serial.append(data)
//                        }
//                    }
//                } catch (ex: IOException) {
//                    Toast.makeText(this, "Bluetooth connection lost.", Toast.LENGTH_LONG).show()
//                    //bluetoothButton?.visibility = View.VISIBLE
//                    break;
//                }
//            }
        })
        workerThread.start()
    }

    override fun onResume() {
        super.onResume()

        if(!usbManager.hasPermission(usbDevice))
        {
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbDevicePermissionReceiver, filter)
            usbManager.requestPermission(usbDevice, permissionIntent)
        } else {
            startWorker()
        }
    }



    override fun onPause() {
        super.onPause()
        // stop the worker.
    }
}

private const val ACTION_USB_PERMISSION = "com.rwb.androidusbotgserial.USB_DEVICE_PERMISSION"

private val usbDevicePermissionReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_USB_PERMISSION == intent.action) {
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                // ^^ Not a

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    startWorker()
                } else {
                    Log.d("com.rwb.androidusbotgserial", "permission denied for device $device")
                }
            }
        }
    }
}
