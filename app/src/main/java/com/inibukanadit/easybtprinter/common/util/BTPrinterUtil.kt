package com.inibukanadit.easybtprinter.common.util

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.isGranted
import com.inibukanadit.easybtprinter.common.toast

object BTPrinterUtil {

    private const val RC_PERMISSION_FOR_DISCOVERY = 31
    private const val RC_ENABLE_BLUETOOTH_FOR_DISCOVERY = 32
    private const val KEY_SHOULD_AUTO_DISCOVER = "SHOULD_AUTO_DISCOVER"

    private val defaultBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val mRequiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun handleActivityResult(
        context: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent? = null
    ): Boolean {
        if (requestCode == RC_ENABLE_BLUETOOTH_FOR_DISCOVERY &&
            resultCode == Activity.RESULT_OK &&
            data?.getBooleanExtra(KEY_SHOULD_AUTO_DISCOVER, false) == true
        ) {
            startBTDeviceDiscovery(context, true)
            return true
        }
        return false
    }

    fun handlePermissionsResult(
        context: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (defaultBluetoothAdapter == null) return false
        if (requestCode == RC_PERMISSION_FOR_DISCOVERY) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                context.toast(R.string.notice_permissions_required)
                context.finish()
                return true
            }
            return true
        }
        return false
    }

    fun checkRequiredPermissionsThenRun(context: Activity, block: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val grants = mRequiredPermissions.map { context.isGranted(it) }
            if (grants.any { !it }) {
                ActivityCompat.requestPermissions(
                    context,
                    mRequiredPermissions,
                    RC_PERMISSION_FOR_DISCOVERY
                )
                return
            }
        }
        block()
    }

    fun startBTDeviceDiscovery(
        activity: Activity,
        shouldStopIfDisabled: Boolean = false,
        shouldAutoDiscoverOnEnabled: Boolean = true
    ): Boolean {
        if (defaultBluetoothAdapter == null) return false

        if (!defaultBluetoothAdapter.isEnabled) {
            if (!shouldStopIfDisabled) requestToEnableBTAdapter(
                activity,
                shouldAutoDiscoverOnEnabled
            )
            return false
        }

        if (defaultBluetoothAdapter.isDiscovering) {
            defaultBluetoothAdapter.cancelDiscovery()
        }

        return defaultBluetoothAdapter.startDiscovery()
    }

    fun requestToEnableBTAdapter(context: Activity, shouldAutoDiscover: Boolean = false) {
        if (defaultBluetoothAdapter == null) return

        if (!defaultBluetoothAdapter.isEnabled) {
            val requestEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (requestEnableIntent.resolveActivity(context.packageManager) != null) {
                ActivityCompat.startActivityForResult(
                    context,
                    requestEnableIntent,
                    RC_ENABLE_BLUETOOTH_FOR_DISCOVERY,
                    Bundle().apply {
                        putBoolean(KEY_SHOULD_AUTO_DISCOVER, shouldAutoDiscover)
                    }
                )
            } else {
                context.toast(R.string.notice_bluetooth_unsupported)
            }
        }
    }

    fun requestToPairWithBTDevice(device: BluetoothDevice) {
    }

}