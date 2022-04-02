package co.bayueka.iqra.mvvm.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ItemBismillahirrahmanirrahimBinding
import co.bayueka.iqra.databinding.ItemIqraBinding
import co.bayueka.iqra.mvvm.models.IqraModel

class IqraAdapter: ListAdapter<IqraModel, RecyclerView.ViewHolder>(DiffCallback()) {

    private val VIEW_IQRA = 1
    private val VIEW_BISMILLAH = 0

    private lateinit var listener: OnListener

    fun setListener(listener: OnListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_IQRA -> ViewHolder(ItemIqraBinding.inflate(inflater, parent, false))
            else -> BismillahHolder(ItemBismillahirrahmanirrahimBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)
        if (viewHolder.itemViewType == VIEW_IQRA) {
            val holder = viewHolder as ViewHolder
            holder.bind(model)
        }
    }

    inner class ViewHolder(private val binding: ItemIqraBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(model: IqraModel) {
            binding.apply {
                txtHalaman.text = String.format(binding.root.context.resources.getString(R.string.halaman), model.halaman)
                imgIqra.setImageResource(model.img)
                itemView.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onClick(adapterPosition, getItem(adapterPosition))
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).halaman.equals("bismillah")) {
            VIEW_BISMILLAH
        } else {
            VIEW_IQRA
        }
    }

    inner class BismillahHolder(binding: ItemBismillahirrahmanirrahimBinding): RecyclerView.ViewHolder(binding.root)

    interface OnListener {
        fun onClick(position: Int, model: IqraModel)
    }

    class DiffCallback: DiffUtil.ItemCallback<IqraModel>() {
        override fun areItemsTheSame(oldItem: IqraModel, newItem: IqraModel) =
            oldItem.halaman == newItem.halaman

        override fun areContentsTheSame(oldItem: IqraModel, newItem: IqraModel) =
            oldItem == newItem

    }
}