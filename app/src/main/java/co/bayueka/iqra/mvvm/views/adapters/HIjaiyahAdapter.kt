package co.bayueka.iqra.mvvm.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.bayueka.iqra.databinding.ItemHijaiyahBinding
import co.bayueka.iqra.mvvm.models.HijaiyahModel

class HIjaiyahAdapter: ListAdapter<HijaiyahModel, HIjaiyahAdapter.ViewHolder>(DiffCallback()) {

    private lateinit var listener: OnListener

    fun setListener(listener: OnListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HIjaiyahAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemHijaiyahBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: HIjaiyahAdapter.ViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
    }

    inner class ViewHolder(private val binding: ItemHijaiyahBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(model: HijaiyahModel) {
            binding.apply {
                txtHijaiyah.text = model.title
                imgHijaiyah.setImageResource(model.img)
                itemView.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onClick(adapterPosition, getItem(adapterPosition))
                    }
                }
            }
        }
    }

    interface OnListener {
        fun onClick(position: Int, model: HijaiyahModel)
    }

    class DiffCallback: DiffUtil.ItemCallback<HijaiyahModel>() {
        override fun areItemsTheSame(oldItem: HijaiyahModel, newItem: HijaiyahModel) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: HijaiyahModel, newItem: HijaiyahModel) =
            oldItem == newItem

    }
}