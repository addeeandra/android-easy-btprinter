package com.inibukanadit.easybtprinter.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.util.BTPrinterUtil
import com.inibukanadit.easybtprinter.receiver.BTPrinterActionReceiver
import com.inibukanadit.easybtprinter.ui.discovery.DeviceDiscoveryListFragment
import com.inibukanadit.easybtprinter.ui.stored.StoredDeviceListFragment
import kotlinx.android.synthetic.main.activity_btprinter.*

class BTPrinterActivity : AppCompatActivity() {

    lateinit var viewModel: BTPrinterViewModel

    private val mBTActionReceiver by lazy { BTPrinterActionReceiver(viewModel) }

    private val mStoredDeviceListFragment by lazy { StoredDeviceListFragment() }
    private val mDeviceDiscoveryListFragment by lazy { DeviceDiscoveryListFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btprinter)

        toolbar?.title = intent?.getStringExtra(Intent.EXTRA_TITLE) ?: getString(R.string.app_name)

        viewModel = ViewModelProvider(this).get(BTPrinterViewModel::class.java)
        onPrepareObservers()

        val btActionFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(mBTActionReceiver, btActionFilter)

        showStoredDeviceListPage()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBTActionReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        BTPrinterUtil.handleActivityResult(this, requestCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        BTPrinterUtil.handlePermissionsResult(this, requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onPrepareObservers() {
        viewModel.onStartDiscoveryEvent.observe(this) {
            BTPrinterUtil.checkRequiredPermissionsThenRun(this) {
                BTPrinterUtil.startBTDeviceDiscovery(this)
            }
        }
        viewModel.openDiscoveryPageEvent.observe(this) {
            showDiscoveryDeviceListPage()
        }
    }

    private fun showStoredDeviceListPage() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.page, mStoredDeviceListFragment.apply {
                this.viewModel = this@BTPrinterActivity.viewModel
            })
            .commit()
    }

    private fun showDiscoveryDeviceListPage() {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.page, mDeviceDiscoveryListFragment.apply {
                this.viewModel = this@BTPrinterActivity.viewModel
            })
            .commit()
    }

}