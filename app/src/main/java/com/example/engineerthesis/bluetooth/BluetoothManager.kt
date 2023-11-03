package com.example.engineerthesis.bluetooth

/* The code uses the Singleton design pattern to manage the state of the BluetoothManager object */
object BluetoothManager {
    private var isBluetoothConnected = false

    // setter
    fun setBluetoothConnected(connected: Boolean) {
        isBluetoothConnected = connected
    }

    //getter
    fun getBluetoothConnected(): Boolean {
        return isBluetoothConnected
    }
}