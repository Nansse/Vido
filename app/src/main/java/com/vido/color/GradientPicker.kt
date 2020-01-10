package com.vido.color

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class GradientPicker(val hue: Float, val range: Float, val width: Float, val callback: (GradientDrawable) -> Unit) {
    var value: Float = 0f
    var snapshot :Float = 0f
    var x1: Float = 0f
    val left = hue - 0.5f * range
    val right = hue + 0.5f * range
    init {
        update()
    }
    fun hueOf(hue: Float): Int {
        return Color.HSVToColor(floatArrayOf(hue, 0.3f, 1f))
    }

    fun register(x1: Float) {
        this.x1 = x1
        snapshot = value
    }
    fun clamp(x: Float): Float {
        if (x > 1f) {
            return 1f
        }
        else if (x < 1f) {
            return -1f
        }
        return x
    }
    fun move(x: Float) {
        val rate = 0.1f
        value = snapshot+  (x1 - x) / width// / width
        System.out.println(value)
        update()
    }
    fun update() {
        if (value > 0f) {
            callback(gradient(left + value*range, right))
        } else {
            callback(gradient(left, right + value*range))
        }
    }
    fun gradient(left: Float, right: Float): GradientDrawable {
        return GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(hueOf(left), hueOf(right)))
    }
}
