package com.example.engineerthesis

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.UUID
import android.Manifest
import android.app.Activity

class MainActivity : AppCompatActivity() {

    private var deviceName: String? = null
    private lateinit var deviceAddress: String
    private lateinit var handler: Handler
    private lateinit var mmSocket: BluetoothSocket
    private lateinit var connectedThread: ConnectedThread
    private lateinit var createConnectThread: CreateConnectThread

    private val CONNECTING_STATUS = 1
    private val MESSAGE_READ = 2
    private val PERMISSION_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonConnect = findViewById<Button>(R.id.buttonConnect)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE
        val textViewInfo = findViewById<TextView>(R.id.textViewInfo)
        val buttonToggle = findViewById<Button>(R.id.buttonToggle)
        buttonToggle.isEnabled = false
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setBackgroundColor(getColor(R.color.colorOff))

        deviceName = intent.getStringExtra("deviceName")
        if (deviceName != null) {
            deviceAddress = intent.getStringExtra("deviceAddress")!!

            toolbar.subtitle = "Connecting to $deviceName..."
            progressBar.visibility = View.VISIBLE
            buttonConnect.isEnabled = false

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            createConnectThread = CreateConnectThread(this,this, bluetoothAdapter, deviceAddress)
            createConnectThread.start()
        }

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    CONNECTING_STATUS -> {
                        when (msg.arg1) {
                            1 -> {
                                toolbar.subtitle = "Connected to $deviceName"
                                progressBar.visibility = View.GONE
                                buttonConnect.isEnabled = true
                                buttonToggle.isEnabled = true
                            }
                            -1 -> {
                                toolbar.subtitle = "Device fails to connect"
                                progressBar.visibility = View.GONE
                                buttonConnect.isEnabled = true
                            }
                        }
                    }
                    MESSAGE_READ -> {
                        val arduinoMsg = msg.obj.toString()
                        when (arduinoMsg.lowercase(Locale.ROOT)) {
                            "led is turned on" -> {
                                imageView.setBackgroundColor(getColor(R.color.colorOn))
                                textViewInfo.text = "Arduino Message : $arduinoMsg"
                            }
                            "led is turned off" -> {
                                imageView.setBackgroundColor(getColor(R.color.colorOff))
                                textViewInfo.text = "Arduino Message : $arduinoMsg"
                            }
                        }
                    }
                }
            }
        }

        buttonConnect.setOnClickListener {
            val intent = Intent(this@MainActivity, SelectDeviceActivity::class.java)
            startActivity(intent)
        }

        buttonToggle.setOnClickListener {
            var cmdText: String? = null
            when (buttonToggle.text.toString().lowercase(Locale.ROOT)) {
                "turn on" -> {
                    buttonToggle.text = "Turn Off"
                    cmdText = "<turn on>"
                }
                "turn off" -> {
                    buttonToggle.text = "Turn On"
                    cmdText = "<turn off>"
                }
            }
            connectedThread.write(cmdText!!)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Uprawnienia zostały udzielone, możesz teraz wykonywać operacje Bluetooth
                } else {
                    // Uprawnienia nie zostały udzielone, obsłuż to odpowiednio
                    showPermissionDeniedMessage()
                }
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        // Wyświetl komunikat informujący użytkownika o konieczności udzielenia uprawnień
        // Przykład z użyciem AlertDialog:
        AlertDialog.Builder(this)
            .setTitle("Brak uprawnień")
            .setMessage("Aplikacja wymaga udzielenia uprawnień Bluetooth i dostępu do lokalizacji.")
            .setPositiveButton("OK") { dialog, which ->

            }
            //.setNegativeButton("Anuluj", null)
            .show()
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */

    inner class CreateConnectThread(private val activity: Activity, private val context: Context, private val bluetoothAdapter: BluetoothAdapter, private val address: String) : Thread() {

        init {

            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            var tmp: BluetoothSocket? = null
            var uuid: UUID? = null

            /* Sprawdzamy czy uzytkownik nadal nam uprawnienia */
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Tutaj możesz wykonać operacje Bluetooth
                 uuid = bluetoothDevice.uuids[0].uuid
            } else {
                // Poproś użytkownika o uprawnienia Bluetooth i dostępu do lokalizacji
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }

            try {
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                Log.e(TAG, "Socket's create() method failed", e)
            }
            if (tmp != null) {
                mmSocket = tmp
            }
        }


        override fun run() {
            /* Sprawdzamy czy uzytkownik nadal nam uprawnienia */
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                bluetoothAdapter.cancelDiscovery()
//                try {
//                    mmSocket.connect()
//                    Log.e("Status", "Device connected")
//                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget()
//                } catch (connectException: IOException) {
//                    try {
//                        mmSocket.close()
//                        Log.e("Status", "Cannot connect to device")
//                        handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget()
//                    } catch (closeException: IOException) {
//                        Log.e(TAG, "Could not close the client socket", closeException)
//                    }
//                    return
//                }
            } else {
                // Poproś użytkownika o uprawnienia Bluetooth i dostępu do lokalizacji
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
            bluetoothAdapter.cancelDiscovery()
            try {
                mmSocket.connect()
                Log.e("Status", "Device connected")
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget()
            } catch (connectException: IOException) {
                try {
                    mmSocket.close()
                    Log.e("Status", "Cannot connect to device")
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget()
                } catch (closeException: IOException) {
                    Log.e(TAG, "Could not close the client socket", closeException)
                }
                return
            }
            connectedThread = ConnectedThread(mmSocket)
            connectedThread.run()
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */

    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private val mmSocket: BluetoothSocket = socket
        private val mmInStream: InputStream
        private val mmOutStream: OutputStream

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn!!
            mmOutStream = tmpOut!!
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes = 0
            while (true) {
                try {
                    buffer[bytes] = mmInStream.read().toByte()
                    var readMessage: String
                    if (buffer[bytes] == '\n'.toByte()) {
                        readMessage = String(buffer, 0, bytes)
                        Log.e("Arduino Message", readMessage)
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget()
                        bytes = 0
                    } else {
                        bytes++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        fun write(input: String) {
            val bytes = input.toByteArray()
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("Send Error", "Unable to send message", e)
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }
    }

    override fun onBackPressed() {
        if (::createConnectThread.isInitialized) {
            createConnectThread.cancel()
        }
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }
}
