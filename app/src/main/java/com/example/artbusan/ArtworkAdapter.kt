package com.example.artbusan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.artbusan.data.Museum

class ArtworkAdapter(
    private val onItemClick: (Museum) -> Unit
) : ListAdapter<Museum, ArtworkAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Museum>() {
        override fun areItemsTheSame(oldItem: Museum, newItem: Museum) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Museum, newItem: Museum) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvArtworkTitle)
        val tvFloor: TextView = view.findViewById(R.id.tvFloor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artwork_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvCategory.text = item.category
        holder.tvTitle.text = item.title
        holder.tvFloor.text = item.location
        holder.imgThumbnail.load(item.imageUrl.ifEmpty { null }) {
            placeholder(R.drawable.ic_stamp)
            error(R.drawable.ic_stamp)
        }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }
}
