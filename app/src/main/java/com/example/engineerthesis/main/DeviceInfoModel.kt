package com.example.engineerthesis.main

class DeviceInfoModel {
    var deviceName: String? = null
    var deviceHardwareAddress: String? = null

    constructor() {}

    constructor(deviceName: String, deviceHardwareAddress: String) {
        this.deviceName = deviceName
        this.deviceHardwareAddress = deviceHardwareAddress
    }
}