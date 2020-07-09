package com.inibukanadit.easybtprinter.ui.dialog

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inibukanadit.easybtprinter.common.lifecycle.event.LiveEvent
import com.inibukanadit.easybtprinter.common.lifecycle.event.MutableLiveEvent
import com.inibukanadit.easybtprinter.common.util.BTPrinterUtil
import com.inibukanadit.easybtprinter.data.DeviceStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BTPrinterDialogViewModel(private val mDeviceStorage: DeviceStorage) : ViewModel() {

    private val _isPrinting by lazy { MutableLiveData<Boolean>() }
    val isPrinting: LiveData<Boolean> = _isPrinting

    private val _storedDeviceList by lazy { MutableLiveData<List<BluetoothDevice>>() }
    val storedDeviceList: LiveData<List<BluetoothDevice>> = _storedDeviceList

    private val _onContentPrintedEvent by lazy { MutableLiveEvent<Unit>() }
    val onContentPrintedEvent: LiveEvent<Unit> = _onContentPrintedEvent

    init {
        fetchStoredDevices()
    }

    fun fetchStoredDevices() {
        _storedDeviceList.value = mDeviceStorage.all()
    }

    fun chooseDeviceAndPrint(device: BluetoothDevice, content: String) {
        viewModelScope.launch {
            _isPrinting.value = true
            withContext(Dispatchers.IO) { BTPrinterUtil.immediatePrintToDevice(device, content) }
            _isPrinting.value = false
            _onContentPrintedEvent.put(Unit)
        }
    }

}