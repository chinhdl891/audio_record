package com.example.audiorecord

import android.os.Handler
import android.os.Looper

class Time(listen: OnTimeTickListener) {

    interface OnTimeTickListener {
        fun onTimeTick(duration: String)
    }

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var duration = 0L
    private var delay = 100L

    init {

        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listen.onTimeTick(format())
        }
    }

    fun start() {
        handler.postDelayed(runnable, delay)
    }

    fun pause() {
        handler.removeCallbacks(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0L

    }

    fun format(): String {
        val milis = duration % 1000
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hour = (duration / (1000 * 60 * 60))
        val formatted = if (hour > 0) {
            "%02d:%02d:%02d.%02d".format(hour, minutes, seconds, milis / 10)
        } else {
            "%02d:%02d.%02d".format(minutes, seconds, milis / 10)
        }
        return formatted
    }
}