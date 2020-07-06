package com.inibukanadit.easybtprinter.common.util

import android.bluetooth.BluetoothDevice
import androidx.recyclerview.widget.DiffUtil

object DiffUtil {

    fun ofBluetoothDevice(): DiffUtil.ItemCallback<BluetoothDevice> {
        return object : DiffUtil.ItemCallback<BluetoothDevice>() {

            override fun areItemsTheSame(
                oldItem: BluetoothDevice,
                newItem: BluetoothDevice
            ): Boolean = oldItem.address == newItem.address

            override fun areContentsTheSame(
                oldItem: BluetoothDevice,
                newItem: BluetoothDevice
            ): Boolean = oldItem == newItem

        }
    }

}