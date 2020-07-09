package com.inibukanadit.easybtprinter.ui.dialog

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inibukanadit.easybtprinter.common.util.DiffUtil
import com.inibukanadit.easybtprinter.databinding.ListDeviceBinding
import com.inibukanadit.easybtprinter.databinding.ListDevicePrintingFooterBinding
import com.inibukanadit.easybtprinter.listeners.OnDeviceItemClickListener
import com.inibukanadit.easybtprinter.listeners.OnDevicePrintingListFooterClickListener

class BTPrinterListAdapter(
    private val mOnFooterClickListener: OnDevicePrintingListFooterClickListener,
    private val mOnDeviceItemClickListener: OnDeviceItemClickListener
) : ListAdapter<BluetoothDevice, BTPrinterListAdapter.ViewHolder>(DiffUtil.ofBluetoothDevice()) {

    var isPrinting: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(itemCount - 1)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FOOTER -> {
                val binding = ListDevicePrintingFooterBinding.inflate(inflater)
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
            is NormalViewHolder -> holder.bind(getItem(position), mOnDeviceItemClickListener)
            is FooterViewHolder -> holder.bind(isPrinting, mOnFooterClickListener)
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
        fun bind(device: BluetoothDevice, listener: OnDeviceItemClickListener) {
            binding.data = device
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    inner class FooterViewHolder(
        private val binding: ListDevicePrintingFooterBinding
    ) : ViewHolder(binding.root) {
        fun bind(
            isPrinting: Boolean,
            footerClickListener: OnDevicePrintingListFooterClickListener
        ) {
            binding.listener = footerClickListener
            binding.printing = isPrinting
            binding.executePendingBindings()
        }
    }

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_FOOTER = 1
    }

}