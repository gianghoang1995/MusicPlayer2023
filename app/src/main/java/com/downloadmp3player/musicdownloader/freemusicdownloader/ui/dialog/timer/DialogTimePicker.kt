package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.dialog.timer

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.WindowManager
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.service.MusicPlayerService
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants
import com.downloadmp3player.musicdownloader.freemusicdownloader.widget.TimePickerView

object DialogTimePicker {
    fun showDialogTimePicker(context: Context, listener: DialogTimePickerCallback) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_picktime)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER or Gravity.BOTTOM
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        window.attributes = wlp
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val timePicker: TimePickerView = dialog.findViewById(R.id.timePicker)
        timePicker.setTimePickerListener(object : TimePickerView.OnTimePickerListener {
            override fun onTimePicker(time: String, millis: Long) {
                val intent = Intent(context, MusicPlayerService::class.java)
                intent.action = AppConstants.SET_TIMER
                intent.putExtra(AppConstants.SET_TIMER, millis)
                intent.putExtra(AppConstants.TIMER_TEXT, time)
                context.startService(intent)
                dialog.dismiss()
            }

            override fun onCancelPick() {
                dialog.dismiss()
            }
        })
        dialog.show()
    }
}