package com.rwb.androidusbotgserial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class MainActivity : AppCompatActivity() {
    private lateinit var serial:TextView
    private lateinit var usbManager:UsbManager
    private lateinit var usbDevice:UsbDevice
    private lateinit var usbSerial: UsbSerialDevice

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

        // Now go to onResume.
    }


    fun startWorker() {
        //workerThread = Thread(Runnable {
        try {
            if (!this::usbSerial.isInitialized) {
                val usbDeviceConnection: UsbDeviceConnection = usbManager.openDevice(usbDevice)
                usbSerial = UsbSerialDevice.createUsbSerialDevice(usbDevice, usbDeviceConnection)
            }
            usbSerial.open()
            usbSerial.setBaudRate(9600)
            usbSerial.read {
                try {
                    this.runOnUiThread(Runnable {
                        serial.append(String(it, Charsets.UTF_16))
                    })
                }
                catch(e:Exception)
                {
                    Log.e("RWB", "It's fucked.", e)
                }
            }
        }
        catch(e:Exception)
        {
            Log.e("RWB", "It's fucked.", e)
        }
//        })
//        workerThread.start()
    }

    override fun onResume() {
        super.onResume()

        if(!usbManager.hasPermission(usbDevice))
        {
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            val br = UsbDevicePermissionReceiver(this)
            registerReceiver(br, filter)
            usbManager.requestPermission(usbDevice, permissionIntent)
        }
        else {
            startWorker()
        }
    }

    override fun onPause() {
        super.onPause()
        // stop the worker.
        usbSerial.close()
    }
}

private const val ACTION_USB_PERMISSION = "com.rwb.androidusbotgserial.USB_DEVICE_PERMISSION"

private class UsbDevicePermissionReceiver(val activity:MainActivity) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_USB_PERMISSION == intent.action) {
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                // ^^ Not a

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    activity.startWorker()
                } else {
                    Log.d("RWB", "permission denied for device $device")
                }
            }
        }
    }
}
