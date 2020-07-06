package com.inibukanadit.easybtprinter.ui.discovery

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inibukanadit.easybtprinter.common.util.DiffUtil
import com.inibukanadit.easybtprinter.databinding.ListDeviceBinding
import com.inibukanadit.easybtprinter.databinding.ListDeviceDiscoveryFooterBinding
import com.inibukanadit.easybtprinter.listeners.OnDeviceDiscoveryListFooterClickListener

class DeviceDiscoveryListAdapter(
    private val mFooterClickListener: OnDeviceDiscoveryListFooterClickListener
) : ListAdapter<BluetoothDevice, DeviceDiscoveryListAdapter.ViewHolder>(DiffUtil.ofBluetoothDevice()) {

    var isDiscovering: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(itemCount - 1)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FOOTER -> {
                val binding = ListDeviceDiscoveryFooterBinding.inflate(inflater)
                FooterViewHolder(binding)
            }
            else -> {
                val binding = ListDeviceBinding.inflate(inflater)
                NormalViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is NormalViewHolder -> holder.bind(getItem(position))
            is FooterViewHolder -> holder.bind(isDiscovering, mFooterClickListener)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) VIEW_TYPE_FOOTER else VIEW_TYPE_NORMAL
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class NormalViewHolder(
        private val binding: ListDeviceBinding
    ) : ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            binding.data = device
            binding.executePendingBindings()
        }
    }

    inner class FooterViewHolder(
        private val binding: ListDeviceDiscoveryFooterBinding
    ) : ViewHolder(binding.root) {
        fun bind(
            isDiscovering: Boolean,
            footerClickListener: OnDeviceDiscoveryListFooterClickListener
        ) {
            binding.listener = footerClickListener
            binding.discovering = isDiscovering
            binding.executePendingBindings()
        }
    }

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_FOOTER = 1
    }

}