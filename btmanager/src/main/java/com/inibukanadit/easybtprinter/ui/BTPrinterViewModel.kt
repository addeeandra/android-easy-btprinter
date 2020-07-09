package com.inibukanadit.easybtprinter.ui

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inibukanadit.easybtprinter.common.lifecycle.event.LiveEvent
import com.inibukanadit.easybtprinter.common.lifecycle.event.MutableLiveEvent
import com.inibukanadit.easybtprinter.common.mutateList
import com.inibukanadit.easybtprinter.common.util.BTPrinterUtil
import com.inibukanadit.easybtprinter.data.DeviceStorage
import com.inibukanadit.easybtprinter.receiver.BTPrinterActionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BTPrinterViewModel(
    private val mDeviceStorage: DeviceStorage
) : ViewModel(), BTPrinterActionListener {

    /*
     * Listen to discovery status :
     * true = discovering
     * false = not discovering
     */
    private val _isDiscovering by lazy { MutableLiveEvent<Boolean>() }
    val isDiscovering: LiveEvent<Boolean> = _isDiscovering

    private val _openDeviceDiscoveryPageEvent by lazy { MutableLiveEvent<Boolean>() }
    val openDeviceDiscoveryPageEvent: LiveEvent<Boolean> = _openDeviceDiscoveryPageEvent

    private val _openStoredDeviceListPageEvent by lazy { MutableLiveEvent<Boolean>() }
    val openStoredDeviceListPageEvent: LiveEvent<Boolean> = _openStoredDeviceListPageEvent

    private val _openDiscoveryDeviceActionDialogEvent by lazy { MutableLiveEvent<BluetoothDevice>() }
    val openDiscoveryDeviceActionDialogEvent: LiveEvent<BluetoothDevice> = _openDiscoveryDeviceActionDialogEvent

    private val _openStoredDeviceActionDialogEvent by lazy { MutableLiveEvent<BluetoothDevice>() }
    val openStoredDeviceActionDialogEvent: LiveEvent<BluetoothDevice> = _openStoredDeviceActionDialogEvent

    private val _openPreviousPage by lazy { MutableLiveEvent<Unit>() }
    val openPreviousPage: LiveEvent<Unit> = _openPreviousPage

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
    private val _storedDeviceList by lazy {
        MutableLiveData<List<BluetoothDevice>>().apply {
            value = emptyList()
        }
    }
    val storedDeviceList: LiveData<List<BluetoothDevice>> = _storedDeviceList

    init {
        fetchStoredDevices()
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

    /**
     * Common actions
     */
    fun toggleDiscovery() {
        if (BTPrinterUtil.isDiscovering()) BTPrinterUtil.cancelDiscovery()
        else BTPrinterUtil.startDiscovery()
    }

    fun testPrint(device: BluetoothDevice, content: String) {
        Log.d("ViewModel", "Test print : $content")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ViewModel", "Starting immediate print ..")
            BTPrinterUtil.immediatePrintToDevice(device, content)
            Log.d("ViewModel", "Immediate print completed ..")
        }
    }

    /**
     * Stored devices
     */
    fun fetchStoredDevices() {
        _storedDeviceList.value = mDeviceStorage.all()
    }

    fun saveAsStoredDevice(device: BluetoothDevice) {
        mDeviceStorage.store(device)
        fetchStoredDevices()

        _openPreviousPage.put(Unit)
    }

    fun deleteStoredDevice(device: BluetoothDevice) {
        mDeviceStorage.remove(device)
        fetchStoredDevices()
    }

    /**
     * open page / ui related functions
     */
    fun openDiscoveryPage() {
        _openDeviceDiscoveryPageEvent.put(true)
    }

    fun openDiscoveryDeviceActionDialog(device: BluetoothDevice) {
        _openDiscoveryDeviceActionDialogEvent.put(device)
    }

    fun openStoredDeviceActionDialog(device: BluetoothDevice) {
        _openStoredDeviceActionDialogEvent.put(device)
    }

}