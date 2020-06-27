package com.rwb.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt
import kotlin.random.Random

// In kotlin static methods are made by leaving them floating outside, or by doing companion object. What a piece of shit.
fun startService(context:Context?)  {
    val intent = Intent(context, TheService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context!!.startForegroundService(intent)
    } else {
        context!!.startService(intent)
    }
}

// Automatically start on boot.
// https://stackoverflow.com/questions/7690350/android-start-service-on-boot
class StartTheService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        startService(context)
    }
}

class RestartTheService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        startService(context)
    }
}

// POCO. Or is it POKO for Kotlin?
class ServiceState {
    var current: Int = 1
    var previous: ArrayList<Int> = arrayListOf<Int>()

    fun add() {
        Log.i("RWB", "ServiceState.add")
        val r = Random(current).nextFloat() * 0.4f + 0.8f
        val next = (current.toFloat() * r).roundToInt()
        current = next
        previous.add(next)
        if (previous.count() > 24) {
            previous.removeAt(0)
        }
    }

    fun save(sharedPrefs: SharedPreferences) {
        Log.i("RWB", "ServiceState.save")
        val e = sharedPrefs.edit()
        e.putInt("current", current)
        e.putString("previous", previous.joinToString { "," })
        e.commit()
    }

    companion object { // This is Kotlin shit for 'static'.
        fun load(sharedPrefs: SharedPreferences): ServiceState {
            Log.i("RWB", "ServiceState.load")
            val state = ServiceState()
            state.current = sharedPrefs.getInt("current", 1)
            val previous = sharedPrefs.getString("prevoius", "")
            if (previous != null && previous!!.length > 0) {
                state.previous = ArrayList(previous.split(",").map { z -> z.toInt() })
            } else {
                state.previous = arrayListOf<Int>()
            }
            return state
        }
    }
}

class TheService() : Service() {
    private lateinit var state : ServiceState
    lateinit var handler : Handler

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("RWB", "TheService.onStartCommand")
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        state = ServiceState.load(sharedPrefs)

        // Run the runner that does the doing.
        handler = Handler(Looper.getMainLooper())
        handler.post(doThings)

        // Bye. Thanks for coming.
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.i("RWB", "TheService.onDestroy")
        handler.removeCallbacks(doThings)
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        state.save(sharedPrefs)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Fuck know what this is.
    }

    private val doThings = object : Runnable {
        override fun run() {
            state.add()
            handler.postDelayed(this, 128)
        }
    }
}
