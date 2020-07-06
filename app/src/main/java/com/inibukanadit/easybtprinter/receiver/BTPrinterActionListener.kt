package com.inibukanadit.easybtprinter.receiver

import android.bluetooth.BluetoothDevice

interface BTPrinterActionListener {

    fun onDeviceFound(device: BluetoothDevice)

    fun onDiscoveryStarted()

    fun onDiscoveryFinished()

}