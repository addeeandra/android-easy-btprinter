package com.inibukanadit.easybtprinter.ui.stored

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.util.RecyclerUtil
import com.inibukanadit.easybtprinter.listeners.OnDeviceItemClickListener
import com.inibukanadit.easybtprinter.listeners.OnStoredDeviceListFooterClickListener
import com.inibukanadit.easybtprinter.ui.BTPrinterViewModel
import kotlinx.android.synthetic.main.fragment_device_list.*

class StoredDeviceListFragment : Fragment(), OnStoredDeviceListFooterClickListener,
    OnDeviceItemClickListener {

    lateinit var viewModel: BTPrinterViewModel

    private val mStoredDeviceListAdapter by lazy { StoredDeviceListAdapter(this, this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onPrepareListView()
        onPrepareObservers()
    }

    private fun onPrepareListView() {
        rv_devices.setHasFixedSize(true)
        rv_devices.addItemDecoration(RecyclerUtil.getDivider(requireContext()))
        rv_devices.layoutManager = RecyclerUtil.getLayoutManager(requireContext())
        rv_devices.adapter = mStoredDeviceListAdapter
    }

    private fun onPrepareObservers() {
        viewModel.storedDeviceList.observe(this, Observer { devices ->
            mStoredDeviceListAdapter.submitList(devices)
        })
    }

    override fun onFooterClickAndOpenDiscovery() {
        viewModel.openDiscoveryPage()
    }

    override fun onDeviceClick(device: BluetoothDevice) {
        viewModel.openStoredDeviceActionDialog(device)
    }

}