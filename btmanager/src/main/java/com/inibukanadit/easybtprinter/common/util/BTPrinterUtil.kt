package com.inibukanadit.easybtprinter.common.util

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.inibukanadit.easybtprinter.R
import com.inibukanadit.easybtprinter.common.isGranted
import com.inibukanadit.easybtprinter.common.toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object BTPrinterUtil {

    const val RC_PERMISSION_FOR_DISCOVERY = 31
    const val RC_ENABLE_BLUETOOTH_FOR_DISCOVERY = 32
    const val KEY_SHOULD_REDISCOVER = "SHOULD_REDISCOVER"

    private val defaultBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val defaultUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    private val socketConnections: MutableMap<String, ConnectedThread> = mutableMapOf()

    private val mRequiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun checkRequiredBluetoothPermissions(context: Activity) {
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
    }

    fun startDiscovery(
        activityForAutoEnable: Activity? = null,
        shouldRediscoverAfterEnabled: Boolean = true
    ): Boolean {
        if (defaultBluetoothAdapter == null) return false

        if (!defaultBluetoothAdapter.isEnabled) {
            if (activityForAutoEnable != null) {
                requestToEnableBluetooth(activityForAutoEnable, shouldRediscoverAfterEnabled)
            }
            return false
        }

        if (defaultBluetoothAdapter.isDiscovering) {
            defaultBluetoothAdapter.cancelDiscovery()
        }

        return defaultBluetoothAdapter.startDiscovery()
    }

    fun cancelDiscovery() {
        defaultBluetoothAdapter?.cancelDiscovery()
    }

    fun isDiscovering(): Boolean {
        return defaultBluetoothAdapter?.isDiscovering == true
    }

    fun requestToEnableBluetooth(context: Activity, shouldAutoDiscover: Boolean = false) {
        if (defaultBluetoothAdapter == null) return

        if (!defaultBluetoothAdapter.isEnabled) {
            val requestEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (requestEnableIntent.resolveActivity(context.packageManager) != null) {
                ActivityCompat.startActivityForResult(
                    context,
                    requestEnableIntent,
                    RC_ENABLE_BLUETOOTH_FOR_DISCOVERY,
                    Bundle().apply { putBoolean(KEY_SHOULD_REDISCOVER, shouldAutoDiscover) }
                )
            } else {
                context.toast(R.string.notice_bluetooth_unsupported)
            }
        }
    }

    fun immediatePrintToDevice(device: BluetoothDevice, message: String) {
        Log.i("BTPrinter", "Direct Printing to ${device.address}")
        connectToDevice(device, true)
        writeMessageToDevice(device, message)
        disconnectFromDevice(device)
    }

    fun immediatePrintToAddress(address: String, message: String) {
        defaultBluetoothAdapter?.getRemoteDevice(address)
            ?.let { device -> immediatePrintToDevice(device, message) }
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        Log.i("BTPrinter", "Disconnecting from ${device.address}")
        socketConnections[device.address]?.close()
        Log.i("BTPrinter", "Disconnected from ${device.address}")
    }

    fun disconnectFromAddress(address: String) {
        defaultBluetoothAdapter?.getRemoteDevice(address)
            ?.let { device -> disconnectFromDevice(device) }
    }

    fun connectToAddress(address: String, secure: Boolean = false) {
        defaultBluetoothAdapter?.getRemoteDevice(address)
            ?.let { device -> connectToDevice(device, secure) }
    }

    fun connectToDevice(device: BluetoothDevice, secure: Boolean = false) {
        Log.i("BTPrinter", "Connecting to ${device.address}")

        if (socketConnections[device.address]?.isConnected() == true) return

        try {
            requestToPairWithBTDevice(device, secure).also { sock ->
                sock.connect()
                socketConnections[device.address] = ConnectedThread(sock)
                    .also { conn -> conn.start() }
            }
            Log.i("BTPrinter", "Connected to ${device.address}")
        } catch (e: IOException) {
            Log.i(
                "BTPrinter",
                "Connecting to ${device.address} has error ${e.message} .. closing socket"
            )
            /* connection error */
            try {
                socketConnections[device.address]?.close()
            } catch (e: IOException) {
                Log.i(
                    "BTPrinter",
                    "Connecting to ${device.address} has error ${e.message} when closing socket"
                )
                /* close error */
            }
            // clear cached
            socketConnections.remove(device.address)
            Log.i("BTPrinter", "Connecting to ${device.address} has failed with errors above")
        }
    }

    fun requestToPairWithBTDevice(
        device: BluetoothDevice,
        secure: Boolean = false
    ): BluetoothSocket {
        defaultBluetoothAdapter?.cancelDiscovery()

        val socket = if (secure) device.createRfcommSocketToServiceRecord(defaultUUID)
        else device.createInsecureRfcommSocketToServiceRecord(defaultUUID)

        return socket
    }

    fun writeMessageToAddress(address: String, message: String) {
        defaultBluetoothAdapter?.getRemoteDevice(address)
            ?.let { device -> writeMessageToDevice(device, message) }
    }

    fun writeMessageToDevice(device: BluetoothDevice, message: String) {
        Log.i("BTPrinter", "Writing message to ${device.address}")
        if (socketConnections.containsKey(device.address)) {
            socketConnections[device.address]?.also { connection ->
                connection.write(message.toByteArray())
                Log.i("BTPrinter", "Written message to ${device.address} has succeed")
            } ?: Log.i("BTPrinter", "Writing message to ${device.address} has null stream")
        }
    }

    class ContentWriterThread(
        private val mmConnectedThread: ConnectedThread,
        private val mmContent: String
    ) : Thread() {

        override fun run() {
            mmConnectedThread.write(mmContent.toByteArray())
        }

    }

    class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)
        private val mmIsConnected: AtomicBoolean = AtomicBoolean(false)

        override fun run() {
            Log.i("BTPrinter", "Running connection thread ..")
            mmIsConnected.set(true)
            while (true) {
                try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    mmIsConnected.set(false)
                    Log.w("BTPrinter", "Input stream disconnected in connection thread")
                    break
                }
            }
            Log.i("BTPrinter", "Run finished at connection thread ..")
        }

        fun isConnected(): Boolean {
            return mmIsConnected.get()
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.w("BTPrinter", "Couldn't send data to other device")
            }
        }

        fun close() {
            mmSocket.close()
            mmIsConnected.set(false)
        }

    }

}