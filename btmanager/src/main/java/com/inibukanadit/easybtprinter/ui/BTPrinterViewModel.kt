package com.inibukanadit.easybtprinter.ui

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inibukanadit.easybtprinter.common.lifecycle.CombinedTransformation
import com.inibukanadit.easybtprinter.common.lifecycle.event.LiveEvent
import com.inibukanadit.easybtprinter.common.lifecycle.event.MutableLiveEvent
import com.inibukanadit.easybtprinter.common.mutateList
import com.inibukanadit.easybtprinter.receiver.BTPrinterActionListener

class BTPrinterViewModel : ViewModel(), BTPrinterActionListener {

    /*
     * Listen to discovery status :
     * true = discovering
     * false = not discovering
     */
    private val _isDiscovering by lazy { MutableLiveEvent<Boolean>() }
    val isDiscovering: LiveEvent<Boolean> = _isDiscovering

    private val _onStartDiscoveryEvent by lazy { MutableLiveEvent<Unit>() }
    val onStartDiscoveryEvent: LiveEvent<Unit> = _onStartDiscoveryEvent

    private val _openDiscoveryPageEvent by lazy { MutableLiveEvent<Unit>() }
    val openDiscoveryPageEvent: LiveEvent<Unit> = _openDiscoveryPageEvent

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

    fun startDiscovery() {
        _onStartDiscoveryEvent.put(Unit)
    }

    fun openDiscoveryPage() {
        _openDiscoveryPageEvent.put(Unit)
    }

}