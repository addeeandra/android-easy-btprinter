package com.inibukanadit.easybtprinter.ui

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inibukanadit.easybtprinter.common.lifecycle.CombinedTransformation
import com.inibukanadit.easybtprinter.common.lifecycle.event.LiveEvent
import com.inibukanadit.easybtprinter.common.lifecycle.event.MutableLiveEvent
import com.inibukanadit.easybtprinter.common.mutateList
import com.inibukanadit.easybtprinter.common.util.BTPrinterUtil
import com.inibukanadit.easybtprinter.receiver.BTPrinterActionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BTPrinterViewModel : ViewModel(), BTPrinterActionListener {

    /*
     * Listen to discovery status :
     * true = discovering
     * false = not discovering
     */
    private val _isDiscovering by lazy { MutableLiveEvent<Boolean>() }
    val isDiscovering: LiveEvent<Boolean> = _isDiscovering

    private val _openDiscoveryPageEvent by lazy { MutableLiveEvent<Unit>() }
    val openDiscoveryPageEvent: LiveEvent<Unit> = _openDiscoveryPageEvent

    private val _openBluetoothActionDialogEvent by lazy { MutableLiveEvent<BluetoothDevice>() }
    val openBluetoothActionDialogEvent: LiveEvent<BluetoothDevice> = _openBluetoothActionDialogEvent

    /*
     * List of paired devices which obtained from bondedDevices function in adapter
     */
    private val _pairedDeviceList by lazy {
        MutableLiveData<List<BluetoothDevice>>().apply {
            value = emptyList()
        }
    }
    val pairedDeviceList: LiveData<List<BluetoothDevice>> = _pairedDeviceList

    /*
     * List of discovered devices which updated frequently on discovering
     */
    private val _discoveredDeviceList by lazy {
        MutableLiveData<List<BluetoothDevice>>().apply {
            value = emptyList()
        }
    }
    val discoveredDeviceList: LiveData<List<BluetoothDevice>> = _discoveredDeviceList

    /*
     * List of saved device addresses on test print
     */
    private val _savedDeviceAddressList by lazy {
        MutableLiveData<Set<String>>().apply {
            value = emptySet()
        }
    }
    val savedDeviceAddressList: LiveData<Set<String>> = _savedDeviceAddressList

    val storedDeviceList by lazy {
        CombinedTransformation<List<BluetoothDevice>>(savedDeviceAddressList, pairedDeviceList) {
            val addressList = it[0] as Set<String>? ?: return@CombinedTransformation emptyList()
            val deviceList =
                it[1] as List<BluetoothDevice>? ?: return@CombinedTransformation emptyList()

            deviceList.filter { device ->
                val address = (device as BluetoothDevice?)?.address
                val contained = addressList.contains(address)
                contained
            }
        }
    }

    override fun onDeviceFound(device: BluetoothDevice) {
        _discoveredDeviceList.mutateList {
            if (any { it.address == device.address }) return@mutateList
            add(device)
        }
    }

    override fun onDiscoveryStarted() {
        _discoveredDeviceList.mutateList { clear() }
        _isDiscovering.put(true)
    }

    override fun onDiscoveryFinished() {
        _isDiscovering.put(false)
    }

    fun toggleDiscovery() {
        if (BTPrinterUtil.isDiscovering()) BTPrinterUtil.cancelDiscovery()
        else BTPrinterUtil.startDiscovery()
    }

    fun testPrint(device: BluetoothDevice, content: String) {
        Log.d("ViewModel", "Test print : $content")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ViewModel", "Starting printing ..")
            BTPrinterUtil.immediatePrintToDevice(device, content)
            Log.d("ViewModel", "Printing completed ..")
        }
    }

    fun saveDevice(device: BluetoothDevice) {
        Log.d("ViewModel", "Save device : ${device.address}")
    }

    /**
     * open page / ui related functions
     */
    fun openDiscoveryPage() {
        _openDiscoveryPageEvent.put(Unit)
    }

    fun openBluetoothActionDialog(device: BluetoothDevice) {
        _openBluetoothActionDialogEvent.put(device)
    }

}