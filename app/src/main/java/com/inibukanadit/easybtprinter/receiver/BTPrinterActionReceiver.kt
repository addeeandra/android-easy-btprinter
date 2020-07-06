package com.inibukanadit.easybtprinter.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BTPrinterActionReceiver(
    private val mBTPrinterActionListener: BTPrinterActionListener
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                intent
                    .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    ?.let { mBTPrinterActionListener.onDeviceFound(it) }
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                mBTPrinterActionListener.onDiscoveryStarted()
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                mBTPrinterActionListener.onDiscoveryFinished()
            }
        }
    }

}