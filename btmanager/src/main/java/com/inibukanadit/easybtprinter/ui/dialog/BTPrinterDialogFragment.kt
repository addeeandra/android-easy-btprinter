package com.inibukanadit.easybtprinter.ui.dialog

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.lifecycle.BTViewModelFactory
import com.inibukanadit.easybtprinter.common.util.RecyclerUtil
import com.inibukanadit.easybtprinter.data.DevicePreferenceStorage
import com.inibukanadit.easybtprinter.listeners.OnDeviceItemClickListener
import com.inibukanadit.easybtprinter.listeners.OnDevicePrintingListFooterClickListener
import com.inibukanadit.easybtprinter.ui.BTPrinterActivity
import kotlinx.android.synthetic.main.fragment_device_list.*

class BTPrinterDialogFragment : DialogFragment(), OnDeviceItemClickListener,
    OnDevicePrintingListFooterClickListener {

    val viewModelProvider by lazy { ViewModelProvider(this, BTViewModelFactory(mDeviceStorage)) }
    val viewModel: BTPrinterDialogViewModel by lazy { viewModelProvider.get(BTPrinterDialogViewModel::class.java) }

    private val mDeviceStorage by lazy { DevicePreferenceStorage(requireActivity()) }
    private val mDeviceListAdapter by lazy { BTPrinterListAdapter(this, this) }

    private val mContent by lazy { arguments?.getString(KEY_CONTENT) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onPrepareDeviceList()
        onPrepareObservers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.fetchStoredDevices()
    }

    private fun onPrepareDeviceList() {
        rv_devices.addItemDecoration(RecyclerUtil.getDivider(requireContext()))
        rv_devices.layoutManager = RecyclerUtil.getLayoutManager(requireContext())
        rv_devices.adapter = mDeviceListAdapter
    }

    private fun onPrepareObservers() {
        viewModel.storedDeviceList.observe(this, Observer {
            mDeviceListAdapter.submitList(it)
        })
        viewModel.isPrinting.observe(this, Observer {
            if (mDeviceListAdapter.isPrinting != it) {
                mDeviceListAdapter.isPrinting = it
            }
        })
        viewModel.onContentPrintedEvent.observe(this) {
            dismiss()
        }
    }

    override fun onDeviceClick(device: BluetoothDevice) {
        mContent?.let { content -> viewModel.chooseDeviceAndPrint(device, content + "\n\n\n") }
    }

    override fun onFooterClickToAddNewDevice() {
        startActivityForResult(Intent(requireActivity(), BTPrinterActivity::class.java), 0)
    }

    companion object {
        const val KEY_CONTENT = "CONTENT"
    }

}