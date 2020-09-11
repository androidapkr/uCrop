package com.yalantis.ucrop.util

import android.content.Context
import android.content.res.Resources
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.nshmura.snappysmoothscroller.SnapType
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager

const val PREFIX_X = " x "

fun Context.getResColor(color: Int): Int = ContextCompat.getColor(this, color)

fun AppCompatActivity.getResColor(color: Int): Int = ContextCompat.getColor(this, color)

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun AppCompatActivity.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun AppCompatActivity.getNavigationBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun DoubleArray.resizeInRangeOf(mainWidth: Double, mainHeight: Double): DoubleArray = run {

    var newWidth: Double
    var newHeight: Double

    //imageWidth: Double, imageHeight: Double

    // if width grater than height
    if ((this[0] / this[1]) >= 1) {
        newWidth = mainWidth
        newHeight = this[1] * (mainWidth / this[0])
        if (newHeight > mainHeight) {
            newWidth *= (mainHeight / newHeight)
            newHeight *= (mainHeight / newHeight)
        }
    } else {
        newWidth = this[0] * (mainHeight / this[1])
        newHeight = mainHeight
        if (newWidth > mainWidth) {
            newHeight *= (mainWidth / newWidth)
            newWidth *= (mainWidth / newWidth)
        }
    }
    return doubleArrayOf(newWidth, newHeight)
}

fun TextInputEditText.checkLength80(): Boolean {
    return if (this.text != null && this.text!!.isNotEmpty()) {
        this.text!!.toString().checkLength80()
    } else {
        false
    }
}

fun String.checkLength80(): Boolean {
    return (this.length >= 2 && this.toInt() >= 80)
}

fun AppCompatActivity.getCenterSnapHorizontalLayoutManager(): SnappyLinearLayoutManager = run {
    return getCenterSnapLayoutManager(LinearLayoutManager.HORIZONTAL)
}

fun AppCompatActivity.getCenterSnapLayoutManager(orientation: Int): SnappyLinearLayoutManager = run {
    val layoutManager = SnappyLinearLayoutManager(this)
    layoutManager.orientation = orientation
    layoutManager.setSnapType(SnapType.CENTER)
    layoutManager.setSnapDuration(300)
    layoutManager.setSeekDuration(300)
    layoutManager.setSnapInterpolator(DecelerateInterpolator())
    return layoutManager
}