package com.inibukanadit.easybtprinter.common.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inibukanadit.easybtprinter.data.DeviceStorage
import com.inibukanadit.easybtprinter.ui.BTPrinterViewModel
import com.inibukanadit.easybtprinter.ui.dialog.BTPrinterDialogViewModel

class BTViewModelFactory(private val mStorage: DeviceStorage) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            BTPrinterViewModel::class.java -> BTPrinterViewModel(mStorage) as T
            BTPrinterDialogViewModel::class.java -> BTPrinterDialogViewModel(mStorage) as T
            else -> modelClass.newInstance()
        }
    }
}