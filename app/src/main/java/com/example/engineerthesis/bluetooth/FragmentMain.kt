package com.example.engineerthesis.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import com.example.engineerthesis.R
import com.example.engineerthesis.main.SelectDeviceActivity

class FragmentMain : Fragment() {

    private var deviceName: String? = null
    private lateinit var deviceAddress: String
    private lateinit var handler: Handler
    private lateinit var mmSocket: BluetoothSocket
    private lateinit var connectedThread: ConnectedThread
    private lateinit var createConnectThread: CreateConnectThread

    private val CONNECTING_STATUS = 1
    private val MESSAGE_READ = 2
    private val PERMISSION_REQUEST_CODE = 123

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main2, container, false)

        val buttonConnect = rootView.findViewById<ImageView>(R.id.imageView13)
        val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
        val progressBar = rootView.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE
        val textViewInfo = rootView.findViewById<TextView>(R.id.textViewInfo)

        val btnForward = rootView.findViewById<ImageView>(R.id.btnForward)
        val btnForwardImage = R.drawable.untitled_906
        val btnForwardImagePressed = R.drawable.t1
        btnForward.isEnabled = false

        val btnBack = rootView.findViewById<ImageView>(R.id.btnBack)
        val btnBackImage = R.drawable.untitled_904
        val btnBackImagePressed = R.drawable.t2
        btnBack.isEnabled = false

        val btnLeft = rootView.findViewById<ImageView>(R.id.btnLeft)
        val btnLeftImage = R.drawable.untitled_902
        val btnLeftImagePressed = R.drawable.t3
        btnLeft.isEnabled = false

        val btnRight = rootView.findViewById<ImageView>(R.id.btnRight)
        val btnRightImage = R.drawable.untitled_901
        val btnRightImagePressed = R.drawable.t4
        btnRight.isEnabled = false

        val btnStop = rootView.findViewById<ImageView>(R.id.btnStop)
        val btnStopImage = R.drawable.untitled_907
        val btnStopImagePressed = R.drawable.c2
        btnStop.isEnabled = false

        val seekBarSpeed = rootView.findViewById<SeekBar>(R.id.seekBarSpeed)
        seekBarSpeed.isEnabled = false

        deviceName = activity?.intent?.getStringExtra("deviceName")
        if (deviceName != null) {
            deviceAddress = activity?.intent?.getStringExtra("deviceAddress")!!

            toolbar.subtitle = "Connecting to $deviceName..."
            progressBar.visibility = View.VISIBLE
            buttonConnect.isEnabled = false

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            createConnectThread = CreateConnectThread(requireActivity(), requireContext(), bluetoothAdapter, deviceAddress)
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
                                seekBarSpeed.isEnabled = true
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
            val intent = Intent(requireContext(), SelectDeviceActivity::class.java)
            startActivity(intent)
        }


        /* Anonymous function */
        val handleButtonAction: (String) -> Unit = { command ->
            connectedThread.write(command)
        }

        val setupButton: (ImageView, Int, Int, String) -> Unit = { imageView, imagePressed, imageNormal, command ->
            imageView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        imageView.setImageResource(imagePressed)
                    }
                    MotionEvent.ACTION_UP -> {
                        imageView.setImageResource(imageNormal)
                        handleButtonAction(command)
                    }
                }
                true
            }
        }

        setupButton(btnForward, btnForwardImagePressed, btnForwardImage, "<go forward>")
        setupButton(btnBack, btnBackImagePressed, btnBackImage, "<go back>")
        setupButton(btnLeft, btnLeftImagePressed, btnLeftImage, "<turn left>")
        setupButton(btnRight, btnRightImagePressed, btnRightImage, "<turn right>")
        setupButton(btnStop, btnStopImagePressed, btnStopImage, "<stop>")

        val btnInfo = rootView.findViewById<ImageView>(R.id.imageView11)

        btnInfo.setOnClickListener {
            showAuthorInfoPopup()
        }

        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val command = "$progress"
                Log.d("Velocity ", command)
                connectedThread.write(command)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        return rootView
    }

    fun sendTextViaBluetooth(text : String){
        connectedThread.write(text)

        val context = activity?.applicationContext
        context?.let {
            Toast.makeText(it, "Sent text: $text", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAuthorInfoPopup() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_layout, null)
        dialogBuilder.setView(dialogView)

        val tvAuthorInfo = dialogView.findViewById<TextView>(R.id.tvAuthorInfo)
        tvAuthorInfo.text =
            "Author: Wiktoria Październiak\n You can download the entire application source code here: \n https://github.com/wikcia/EngineerThesis"

        dialogBuilder.setPositiveButton("OK") { _, _ -> }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (areAllPermissionsGranted(permissions, grantResults)) {
                    Log.d("Permissions granted", "==============Permissions granted===============")
                } else {
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
        AlertDialog.Builder(requireContext())
            .setTitle("No permissions")
            .setMessage("The app requires Bluetooth permissions and location access.")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */

    inner class CreateConnectThread(private val activity: Activity, private val context: Context, private val bluetoothAdapter: BluetoothAdapter, private val address: String) :
        Thread() {

        init {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            var tmp: BluetoothSocket? = null
            var uuid: UUID? = null

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                uuid = bluetoothDevice.uuids[0].uuid
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    ), PERMISSION_REQUEST_CODE
                )
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
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery()
                try {
                    mmSocket.connect()
                    Log.e("Status", "Device connected")
                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget()
                    // After a successful connection, set the BluetoothManager flag to true
                    BluetoothManager.setBluetoothConnected(true)
                } catch (connectException: IOException) {
                    try {
                        mmSocket.close()
                        Log.e("Status", "Cannot connect to device")
                        handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget()
                        // After a failed connection, set the BluetoothManager flag to false
                        BluetoothManager.setBluetoothConnected(false)
                    } catch (closeException: IOException) {
                        Log.e(TAG, "Could not close the client socket", closeException)
                    }
                    return
                }
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    ), PERMISSION_REQUEST_CODE
                )
            }
            bluetoothAdapter.cancelDiscovery()
            connectedThread = ConnectedThread(mmSocket)
            connectedThread.start()
        }

        fun cancel() {
            try {
                mmSocket.close()
                BluetoothManager.setBluetoothConnected(false)
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

//    override fun onBackPressed() {
//        if (::createConnectThread.isInitialized) {
//            createConnectThread.cancel()
//        }
//        val a = Intent(Intent.ACTION_MAIN)
//        a.addCategory(Intent.CATEGORY_HOME)
//        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(a)
//    }
}
