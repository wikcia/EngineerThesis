package com.example.engineerthesis.bluetooth

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
import android.widget.Toast
import com.example.engineerthesis.R

/**
 * Uwaga! To działa tylko wtedy kiedy ręcznie przyznamy aplikacji uprawnienia BLUETOOTH_CONNECT i BLUETOOTH_SCAN
 */
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

        val buttonConnect = findViewById<ImageView>(R.id.imageView13)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE
        val textViewInfo = findViewById<TextView>(R.id.textViewInfo)
        val btnInfo = findViewById<ImageView>(R.id.imageView11)


        val btnForward = findViewById<ImageView>(R.id.btnForward)
        btnForward.isEnabled = false
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.isEnabled = false
        val btnLeft = findViewById<ImageView>(R.id.btnLeft)
        btnLeft.isEnabled = false
        val btnRight = findViewById<ImageView>(R.id.btnRight)
        btnRight.isEnabled = false
        val btnStop = findViewById<ImageView>(R.id.btnStop)
        btnStop.isEnabled = false

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
                                btnForward.isEnabled = true
                                btnBack.isEnabled = true
                                btnLeft.isEnabled = true
                                btnRight.isEnabled = true
                                btnStop.isEnabled = true
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
                        when (arduinoMsg.toLowerCase()) {
                            "car is going forward" -> {
                                textViewInfo.text = "Arduino Message : $arduinoMsg"
                            }
                            "car is going back" -> {
                               textViewInfo.text = "Arduino Message : $arduinoMsg"
                            }
                            "car is turning right" -> {
                                textViewInfo.text = "Arduino Message : $arduinoMsg"
                            }
                            "car is turning left" -> {
                               textViewInfo.text = "Arduino Message : $arduinoMsg"
                            }
                            "car is stopped" -> {
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

        fun handleButtonAction(command: String) {
            connectedThread.write(command)
        }

        btnForward.setOnClickListener {
            handleButtonAction("<go forward>")
        }

        btnBack.setOnClickListener {
            handleButtonAction("<go back>")
        }

        btnRight.setOnClickListener {
            handleButtonAction("<turn right>")
        }

        btnLeft.setOnClickListener {
            handleButtonAction("<turn left>")
        }

        btnStop.setOnClickListener {
            handleButtonAction("<stop>")
        }

        btnInfo.setOnClickListener {
            // Show a dialog with author information
            showAuthorInfoPopup()
        }
    }
    private fun showAuthorInfoPopup() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_layout, null)
        dialogBuilder.setView(dialogView)

        val tvAuthorInfo = dialogView.findViewById<TextView>(R.id.tvAuthorInfo)
        tvAuthorInfo.text = "Author: Wiktoria Październiak\n You can download the entire application source code here: \n https://github.com/wikcia/EngineerThesis"

        dialogBuilder.setPositiveButton("OK") { _, _ ->
            // Dismiss the dialog if the OK button is clicked
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (areAllPermissionsGranted(permissions, grantResults)) {
                    // Uprawnienia zostały udzielone, możesz teraz wykonywać operacje Bluetooth
                    Log.d("Permissions granted","==============Permissions granted===============")
                } else {
                    // Uprawnienia nie zostały udzielone, obsłuż to odpowiednio
                    showPermissionDeniedMessage()
                }
            }
        }
    }

    private fun areAllPermissionsGranted(permissions: Array<String>, grantResults: IntArray): Boolean {
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
    private fun showPermissionDeniedMessage() {
        // Display a message informing the user that permissions are required
        AlertDialog.Builder(this)
            .setTitle("No permissions")
            .setMessage("The app requires Bluetooth permissions and location access.")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */

    inner class CreateConnectThread(private val activity: Activity, private val context: Context, private val bluetoothAdapter: BluetoothAdapter, private val address: String) : Thread() {

        init {

            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            var tmp: BluetoothSocket? = null
            var uuid: UUID? = null

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                // here you can perform bluetooth operation
                 uuid = bluetoothDevice.uuids[0].uuid

            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_CODE)
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
            /* checking if user has granted us permissions */
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
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
            } else {
                // ask user to grant bluetooth and location permissions
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_CODE)

            }
            bluetoothAdapter.cancelDiscovery()
            connectedThread = ConnectedThread(mmSocket)
            connectedThread.start()
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
            } catch (_: IOException) {
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
