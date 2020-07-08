package com.inibukanadit.easybtprinter.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.inibukanadit.easybtprinter.common.util.BTPrinterUtil
import org.json.JSONArray

class DevicePreferenceStorage(context: Context) : DeviceStorage {

    private val mSharedPreferences =
        context.getSharedPreferences(BT_PRINTER_PREFS, Context.MODE_PRIVATE)

    @SuppressLint("ApplySharedPref")
    override fun store(device: BluetoothDevice) {
        val addresses = fetchAddressList().apply { add(device.address) }
        mSharedPreferences.edit().apply { putString(PREF_DEVICES, encode(addresses)) }.commit()
    }

    @SuppressLint("ApplySharedPref")
    override fun remove(device: BluetoothDevice) {
        val addresses = fetchAddressList()
        addresses.remove(device.address)
        mSharedPreferences.edit().apply { putString(PREF_DEVICES, encode(addresses)) }.commit()
    }

    @SuppressLint("ApplySharedPref")
    override fun remove(address: String) {
        val addresses = fetchAddressList()
        addresses.remove(address)
        mSharedPreferences.edit().apply { putString(PREF_DEVICES, encode(addresses)) }.commit()
    }

    override fun all(): List<BluetoothDevice> {
        return fetchAddressList().map { BTPrinterUtil.getRemoteDevice(it) }
    }

    private fun fetchAddressList(): MutableSet<String> {
        val savedDevices = mSharedPreferences.getString(PREF_DEVICES, null) ?: "[]"
        return decode(savedDevices)
    }

    private fun decode(content: String): MutableSet<String> {
        val data = JSONArray(content)
        val result = mutableSetOf<String>()
        for (i in 0 until data.length()) {
            result.add(data.getString(i))
        }
        return result
    }

    private fun encode(data: Set<String>): String {
        return JSONArray(data).toString()
    }

    companion object {
        const val BT_PRINTER_PREFS = "bt_storage"
        const val PREF_DEVICES = "DEVICES"
    }

}