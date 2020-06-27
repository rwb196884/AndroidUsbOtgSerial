# Android USB OTG serial proof of concept

A simple Android application to receive serial data (_e.g.,_ from an Arduino) over USB OTB.

I had initially thought of _Solution1_: to use Bluetooth. Trying to reduce cost and eliminate components
I'd arrived at _Solution 2_: serial over USB OTG.
https://code.google.com/archive/p/android-serialport-api/wikis/android_to_rs232_guideline.wiki

## Solution 1

There's quite a bit of paraphernalia one may do around Bluetooth pairing, device selection,
and whatnot.

However, it basically comes down to having a socket. It goes something like this:
```
class MainActivity : AppCompatActivity() {
    private lateinit var serial:TextView
    private lateinit var workerThread : Thread
    private lateinit var bts:BluetoothSocket


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Plumb in the view model
        serial = findViewById<TextView>(R.id.serial)

        val btm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bta = btm?.adapter
        val btd = bta.bondedDevices.first{z -> z.name == "chosenDevice"}
        bts = btd.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        bts.connect()


        workerThread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted  && bts?.isConnected ?: false) {
                try {
                    val bytesAvailable: Int = bts?.inputStream?.available() ?: 0
                    if (bytesAvailable > 0) {
                        val buffer = ByteArray(bytesAvailable)
                        bts?.inputStream?.read(buffer)
                        val data = String(buffer, Charset.defaultCharset())

                        runOnUiThread {
                            serial.append(data)
                        }
                    }
                } catch (ex: IOException) {
                    Toast.makeText(this, "Bluetooth connection lost.", Toast.LENGTH_LONG).show()
                    //bluetoothButton?.visibility = View.VISIBLE
                    break;
                }
            }
        })
        workerThread?.start()
    }
}```

Note that BLE won't work because the data rate is very low, and because it's a bastard to program.

## Solution 2

From https://developer.android.com/guide/topics/connectivity/usb/host:
* Add to `Manifext.xml`
* Create `device_filter.xml`



## Solution  3/4

Need to find hardware to get from CAN to UART/GPIO.
