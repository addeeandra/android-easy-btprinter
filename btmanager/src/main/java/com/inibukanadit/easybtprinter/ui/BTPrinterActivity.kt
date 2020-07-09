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
import com.inibukanadit.easybtprinter.common.lifecycle.BTViewModelFactory
import com.inibukanadit.easybtprinter.common.toast
import com.inibukanadit.easybtprinter.common.util.BTPrinterUtil
import com.inibukanadit.easybtprinter.data.DevicePreferenceStorage
import com.inibukanadit.easybtprinter.receiver.BTPrinterActionReceiver
import com.inibukanadit.easybtprinter.ui.discovery.DeviceDiscoveryListFragment
import com.inibukanadit.easybtprinter.ui.stored.StoredDeviceListFragment
import kotlinx.android.synthetic.main.activity_btprinter.*

class BTPrinterActivity : AppCompatActivity() {

    val viewModelProvider by lazy { ViewModelProvider(this, BTViewModelFactory(mDeviceStorage)) }
    val viewModel: BTPrinterViewModel by lazy { viewModelProvider.get(BTPrinterViewModel::class.java) }

    private val mDeviceStorage by lazy { DevicePreferenceStorage(this) }
    private val mBTActionReceiver by lazy { BTPrinterActionReceiver(viewModel) }

    private val mStoredDeviceListTitle by lazy { intent.getStringExtra(TITLE_FAVORITE) ?: getString(R.string.title_favorite_device) }
    private val mStoredDeviceListFragment by lazy { StoredDeviceListFragment() }

    private val mDeviceDiscoveryListTitle by lazy { intent.getStringExtra(TITLE_DISCOVERY) ?: getString(R.string.title_discovery) }
    private val mDeviceDiscoveryListFragment by lazy { DeviceDiscoveryListFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btprinter)

        onPrepareObservers()
        showStoredDeviceListPage()
        BTPrinterUtil.checkRequiredBluetoothPermissions(this)

        val btActionFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(mBTActionReceiver, btActionFilter)

        supportFragmentManager.addOnBackStackChangedListener {
            when (supportFragmentManager.backStackEntryCount) {
                0 -> toolbar?.title = mStoredDeviceListTitle
                1 -> toolbar?.title = mDeviceDiscoveryListTitle
            }
        }
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
        viewModel.openDeviceDiscoveryPageEvent.observe(this) {
            showDiscoveryDeviceListPage(it)
        }
        viewModel.openStoredDeviceListPageEvent.observe(this) {
            showStoredDeviceListPage(it)
        }
        viewModel.openDiscoveryDeviceActionDialogEvent.observe(this) {
            showDiscoveryDeviceActionDialog(it)
        }
        viewModel.openStoredDeviceActionDialogEvent.observe(this) {
            showStoredDeviceActionDialog(it)
        }
        viewModel.openPreviousPage.observe(this) {
            super.onBackPressed()
        }
    }

    private fun showStoredDeviceListPage(shouldToBackStack: Boolean = false) {
        toolbar?.title = mStoredDeviceListTitle
        supportFragmentManager
            .beginTransaction()
            .apply { if (shouldToBackStack) addToBackStack(StoredDeviceListFragment::class.java.canonicalName) }
            .replace(R.id.page, mStoredDeviceListFragment.apply {
                this.viewModel = this@BTPrinterActivity.viewModel
            })
            .commit()
    }

    private fun showDiscoveryDeviceListPage(shouldToBackStack: Boolean = false) {
        BTPrinterUtil.startDiscovery(this)
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
            .apply { if (shouldToBackStack) addToBackStack(DeviceDiscoveryListFragment::class.java.canonicalName) }
            .replace(R.id.page, mDeviceDiscoveryListFragment.apply {
                this.viewModel = this@BTPrinterActivity.viewModel
            })
            .commit()
    }

    private fun showDiscoveryDeviceActionDialog(device: BluetoothDevice) {
        AlertDialog
            .Builder(this)
            .setCancelable(false)
            .setTitle(
                getString(R.string.ask_save_as_saved_device)
                    .format(device.name ?: device.address)
            )
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setItems(R.array.bt_discover_actions) { _, which ->
                when (which) {
                    0 -> viewModel.testPrint(device, BTPrinterUtil.getTestPrintContent())
                    1 -> viewModel.saveAsStoredDevice(device)
                }
            }
            .show()
    }

    private fun showStoredDeviceActionDialog(device: BluetoothDevice) {
        AlertDialog
            .Builder(this)
            .setCancelable(false)
            .setTitle(device.name ?: device.address)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setItems(R.array.bt_stored_actions) { _, which ->
                when (which) {
                    0 -> viewModel.testPrint(device, BTPrinterUtil.getTestPrintContent())
                    1 -> viewModel.deleteStoredDevice(device)
                }
            }
            .show()
    }

    companion object {
        const val TITLE_FAVORITE = "TITLE_FAVORITE"
        const val TITLE_DISCOVERY = "TITLE_DISCOVERY"
    }

}