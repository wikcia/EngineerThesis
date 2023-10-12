package com.example.engineerthesis

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
import androidx.core.app.ActivityCompat

class SelectDeviceActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        // Bluetooth Setup
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var pairedDevices : Set<BluetoothDevice> = setOf()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Tutaj możesz kontynuować i uzyskać dostęp do sparowanych urządzeń Bluetooth
             pairedDevices = bluetoothAdapter.bondedDevices
            // Pozostała część kodu
        } else {
            // Poproś użytkownika o uprawnienia Bluetooth i dostępu do lokalizacji
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
}
