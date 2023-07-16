package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.bumptech.glide.Glide
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.CategoryItem
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.google.android.material.imageview.ShapeableImageView

class AdapterCategory(
    private var context: Context,
    var onClickItemCategory: ((CategoryItem) -> Unit)
) : RecyclerView.Adapter<AdapterCategory.CategoryViewHolder>() {

    private var listCategory = AppConstants.getListCategory(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterCategory.CategoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterCategory.CategoryViewHolder, position: Int) {
        holder.bind(listCategory[position])
        holder.itemView.setOnClickListener {
            onClickItemCategory.invoke(listCategory[position])
        }
    }

    override fun getItemCount(): Int {
        return listCategory.size
    }

    inner class CategoryViewHolder(
        private var view: View
    ) : RecyclerView.ViewHolder(view) {
        private val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        private val imgThumbCategory: ShapeableImageView = view.findViewById(R.id.imgThumbCategory)
        val cardView: CardView = view.findViewById(R.id.cardView)

        fun bind(item: CategoryItem) {
            tvCategory.text = item.cateTitle
            Glide.with(context)
                .load(item.thumb)
                .into(imgThumbCategory)
            cardView.setCardBackgroundColor(Color.parseColor(item.defColor))
        }
    }

}