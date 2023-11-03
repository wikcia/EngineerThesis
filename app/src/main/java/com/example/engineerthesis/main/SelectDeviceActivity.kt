package com.example.engineerthesis.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.engineerthesis.R

class SelectDeviceActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        // Bluetooth Setup
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var pairedDevices : Set<BluetoothDevice> = setOf()


        checkPermissions()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {

            // Here you can continue and access paired Bluetooth devices
             pairedDevices = bluetoothAdapter.bondedDevices

        } else {
            // Ask the user for Bluetooth permissions and location access
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        }

        val deviceList = ArrayList<Any>()
        if (pairedDevices.isNotEmpty()) {
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                val deviceInfoModel = DeviceInfoModel(deviceName, deviceHardwareAddress)
                deviceList.add(deviceInfoModel)
            }
            // Display paired devices using recyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDevice)
            recyclerView.layoutManager = LinearLayoutManager(this)
            val deviceListAdapter = DeviceListAdapter(this, deviceList)
            recyclerView.adapter = deviceListAdapter
            recyclerView.itemAnimator = DefaultItemAnimator()
        } else {
            val view = findViewById<View>(R.id.recyclerViewDevice)
            val snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("OK") {}
            snackbar.show()
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        val grantedPermissions = mutableListOf<String>()

        for (permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permission)
            }
        }

        // You can perform some action based on the powers granted
        for (permission in grantedPermissions) {
            Log.d("Granted Permission", permission)
        }
    }

}
