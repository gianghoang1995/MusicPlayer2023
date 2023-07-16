package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.downloadmp3player.musicdownloader.freemusicdownloader.R

class SuggestionsAdapter(
    private val context: Context,
    var onClickToItem: ((keyword: String?) -> Unit)? = null
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {
    private val lst: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        var layoutInflater = LayoutInflater.from(context)
        return SuggestionViewHolder(context, layoutInflater, parent)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.tvSuggest.text = lst[position]
        holder.itemView.setOnClickListener {
            onClickToItem?.invoke(lst[position])
        }
    }

    override fun getItemCount() = lst.size

    fun setListKeyword(list: ArrayList<String>) {
        lst.clear()
        lst.addAll(list)
        notifyDataSetChanged()
    }

    class SuggestionViewHolder(
        private val context: Context?,
        inflater: LayoutInflater,
        parent: ViewGroup
    ) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_suggestion, parent, false)) {
        var tvSuggest: TextView = itemView.findViewById(R.id.tvSuggestion)
    }

    interface OnSuggestionItemClickListener {
        fun onClickItemSuggestion(keyword: String)
    }
}