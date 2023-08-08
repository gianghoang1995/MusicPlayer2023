package com.musicplayer.mp3player.playermusic.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.top = space
        outRect.bottom = space

        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.left = space
            outRect.right = space / 2
        } else if (parent.getChildLayoutPosition(view) % 2 == 0) {
            outRect.right = space / 2
            outRect.left = space / 2
        } else if (parent.getChildLayoutPosition(view) % 2 != 0) {
            outRect.left = space / 2
            outRect.right = space / 2
        } else {
            outRect.right = space
        }
    }
}