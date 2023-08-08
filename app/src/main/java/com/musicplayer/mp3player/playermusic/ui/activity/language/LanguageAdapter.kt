package com.musicplayer.mp3player.playermusic.ui.activity.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.mp3player.playermusic.equalizer.R
import com.musicplayer.mp3player.playermusic.equalizer.databinding.ItemLanguageBinding
import com.musicplayer.mp3player.playermusic.model.LanguageModel
import com.musicplayer.mp3player.playermusic.utils.AppConstants
import java.util.*

class LanguageAdapter internal constructor(
    private var context: Context,
    private var items: ArrayList<Any>,
    val itemClick: (LanguageModel) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedItemIndex: Int = 0
    private var timeLastClick: Long = 0

    inner class ViewHolder(binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var itemBinding: ItemLanguageBinding

        fun bind(obj: LanguageModel, position: Int) {
            itemBinding.imvFlag.setImageResource(obj.image)
            itemBinding.tvNameLanguage.text = obj.language
            if (obj.isSelected) {
                itemBinding.tvNameLanguage.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )

            } else {
                itemBinding.tvNameLanguage.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )


            }
        }

        init {
            itemBinding = binding
        }
    }

//    inner class AdsViewHolder(binding: ItemAdsLanguageBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        var itemBinding: ItemAdsLanguageBinding
//
//        fun bind(item: NativeAdsItem) {
//            itemBinding.nativeAdMediumView.showShimmer(false)
//            item.nativeAd?.let {
//                itemBinding.nativeAdMediumView.setNativeAd(
//                    it
//                )
//            }
//            itemBinding.nativeAdMediumView.isVisible = true
//        }
//
//        init {
//            itemBinding = binding
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        if (viewType == Co.TYPE_OBJECT) {
        val binding: ItemLanguageBinding =
            ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
//        } else {
//            val binding: ItemAdsLanguageBinding =
//                ItemAdsLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//            return AdsViewHolder(binding)
//        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

//        if (holder.itemViewType == Utils.TYPE_ADS) {
//            val adsHolder = holder as AdsViewHolder
//
//            var ite = items[position] as NativeAdsItem
//            adsHolder.bind(ite)
//
//        } else {
        val itemImageHolder = holder as ViewHolder

        var ite = items[position] as LanguageModel

        itemImageHolder.bind(ite, position)

        if (ite.isSelected) {
            selectedItemIndex = position

            holder.itemBinding.tvNameLanguage.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
            holder.itemBinding.layoutParent.setBackgroundResource(R.drawable.bg_language_selected)
            holder.itemBinding.imvSelect.setImageResource(R.drawable.ic_radio_selected)

        } else {
            holder.itemBinding.tvNameLanguage.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
            holder.itemBinding.layoutParent.setBackgroundResource(R.drawable.bg_gray)
            holder.itemBinding.imvSelect.setImageResource(R.drawable.ic_radio_not_select)


        }


        itemImageHolder.itemBinding.root.setOnClickListener {
            if (System.currentTimeMillis() - timeLastClick > 300) {
                if (position != selectedItemIndex) {
                    itemClick(ite)
                    var copyPosition = selectedItemIndex
                    var selectedItemIndex = position
                    (items[copyPosition] as LanguageModel).isSelected = false
                    (items[selectedItemIndex] as LanguageModel).isSelected = true
                    notifyDataSetChanged()

                }
                timeLastClick = System.currentTimeMillis()
            } else {
                return@setOnClickListener
            }
        }
//        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items?.get(position) is LanguageModel) {
            AppConstants.TYPE_OBJECT
        } else {
            AppConstants.TYPE_ADS
        }
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun setNativeAds(nativeAd: NativeAd?) {
//
//        val nativeAdsItem = NativeAdsItem(Utils.TYPE_ADS, nativeAd)
//        if (!checkListContainsAds()) {
//
//            items.add(1, nativeAdsItem)
//            notifyItemInserted(1)
//
//        }
//
//    }
//
//    private fun checkListContainsAds(): Boolean {
//
//        items.forEach {
//            if (it is NativeAdsItem) {
//                return true
//            }
//        }
//
//        return false
//    }

    fun getKeyLanguageSelected() : String{
        items.forEach {
            if (it is LanguageModel && it.isSelected){
                return it.code
            }
        }
        return "en"
    }


}