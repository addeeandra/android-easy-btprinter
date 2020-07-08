package com.inibukanadit.easybtprinter.data

import android.bluetooth.BluetoothDevice

interface DeviceStorage {

    fun store(device: BluetoothDevice)

    fun remove(device: BluetoothDevice)

    fun remove(address: String)

    fun all(): List<BluetoothDevice>

}