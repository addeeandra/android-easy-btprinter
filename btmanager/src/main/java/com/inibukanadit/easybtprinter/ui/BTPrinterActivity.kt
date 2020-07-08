package com.inibukanadit.easybtprinter.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.toast
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

        BTPrinterUtil.checkRequiredBluetoothPermissions(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBTActionReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BTPrinterUtil.RC_ENABLE_BLUETOOTH_FOR_DISCOVERY &&
            resultCode == Activity.RESULT_OK &&
            data?.getBooleanExtra(BTPrinterUtil.KEY_SHOULD_REDISCOVER, false) == true
        ) {
            BTPrinterUtil.startDiscovery()
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == BTPrinterUtil.RC_PERMISSION_FOR_DISCOVERY) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                toast(R.string.notice_permissions_required)
                finish()
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onPrepareObservers() {
        viewModel.openDiscoveryPageEvent.observe(this) {
            showDiscoveryDeviceListPage()
        }
        viewModel.openBluetoothActionDialogEvent.observe(this) {
            showBluetoothActionDialog(it)
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
        BTPrinterUtil.startDiscovery(this)
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.page, mDeviceDiscoveryListFragment.apply {
                this.viewModel = this@BTPrinterActivity.viewModel
            })
            .commit()
    }

    private fun showBluetoothActionDialog(device: BluetoothDevice) {
        AlertDialog
            .Builder(this)
            .setCancelable(false)
            .setTitle(
                getString(R.string.ask_save_as_saved_device)
                    .format(device.name ?: device.address)
            )
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setItems(R.array.bt_action_chooser) { _, which ->
                when (which) {
                    0 -> viewModel.testPrint(device, "WOWOWOWOWOWOWOWO\n\nTEST PRINT HERE\nwkwkwk\n\n")
                    1 -> viewModel.saveDevice(device)
                }
            }
            .show()
    }

}