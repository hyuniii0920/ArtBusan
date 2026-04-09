package com.example.artbusan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArtworkAdapter(
    private val items: List<ArtworkItem>,
    private val onItemClick: (ArtworkItem) -> Unit
) : RecyclerView.Adapter<ArtworkAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        val item = items[position]
        holder.tvCategory.text = item.category
        holder.tvTitle.text = item.title
        holder.tvFloor.text = item.location
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
