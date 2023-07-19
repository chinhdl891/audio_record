package com.example.audiorecord

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View

class WaveFormView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()
    private var radius = 6f
    private var w = 9f
    private var sw = 0f
    private var d = 6f
    private var sh = 400f
    private var maxSpike = 0

    init {

        paint.color = Color.RED
        sw = resources.displayMetrics.widthPixels.toFloat()
        maxSpike = (sw / (w + d)).toInt()
    }


    fun addAmplitude(amp: Float) {

        var norm = Math.min(amp.toInt() / 7, 400).toFloat()
        Log.d("TAG", "addAmplitude() called with: amp = $norm")
        amplitudes.add(norm)
        spikes.clear()
        var amps = amplitudes.takeLast(maxSpike)
        for (i in amps.indices) {
            var left = sw - i * (w + d)
            val top = sh / 2 - amps[i] / 2
            val right = left + w
            val bottom = top + amps[i]
            spikes.add(RectF(left, top, right, bottom))

        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        canvas?.drawRoundRect(RectF(20f, 30f, 20 + 30f, 30f + 60f), 6f, 6f, paint)
        spikes.forEach {
            canvas?.drawRoundRect(it, radius, radius, paint)
        }

    }

    fun clearData() {
        amplitudes.clear()
        spikes.clear()
        invalidate()
    }

}