package com.inibukanadit.easybtprinter.ui.stored

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inibukanadit.easybtprinter.common.util.DiffUtil
import com.inibukanadit.easybtprinter.databinding.ListDeviceBinding
import com.inibukanadit.easybtprinter.databinding.ListDeviceStoredFooterBinding
import com.inibukanadit.easybtprinter.listeners.OnStoredDeviceListFooterClickListener

class StoredDeviceListAdapter(
    private val mFooterClickListener: OnStoredDeviceListFooterClickListener
) : ListAdapter<BluetoothDevice, StoredDeviceListAdapter.ViewHolder>(DiffUtil.ofBluetoothDevice()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FOOTER -> {
                val binding = ListDeviceStoredFooterBinding.inflate(inflater)
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
            is FooterViewHolder -> holder.bind(mFooterClickListener)
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
        private val binding: ListDeviceStoredFooterBinding
    ) : ViewHolder(binding.root) {
        fun bind(footerClickListener: OnStoredDeviceListFooterClickListener) {
            binding.listener = footerClickListener
            binding.executePendingBindings()
        }
    }

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_FOOTER = 1
    }

}