package com.inibukanadit.easybtprinter.ui.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.util.RecyclerUtil
import com.inibukanadit.easybtprinter.listeners.OnDeviceDiscoveryListFooterClickListener
import com.inibukanadit.easybtprinter.ui.BTPrinterViewModel
import kotlinx.android.synthetic.main.fragment_device_list.*

class DeviceDiscoveryListFragment : Fragment(), OnDeviceDiscoveryListFooterClickListener {

    lateinit var viewModel: BTPrinterViewModel

    private val mDeviceDiscoveryListAdapter: DeviceDiscoveryListAdapter by lazy {
        DeviceDiscoveryListAdapter(this)
    }

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
        rv_devices.adapter = mDeviceDiscoveryListAdapter
    }

    private fun onPrepareObservers() {
        viewModel.discoveredDeviceList.observe(this, Observer {
            mDeviceDiscoveryListAdapter.submitList(it)
        })
        viewModel.isDiscovering.observe(this) { isDiscovering ->
            // notify only if state changed
            if (mDeviceDiscoveryListAdapter.isDiscovering != isDiscovering) {
                mDeviceDiscoveryListAdapter.isDiscovering = isDiscovering
            }
        }
    }

    override fun onFooterClickAndRediscover() {
        viewModel.startDiscovery()
    }

}