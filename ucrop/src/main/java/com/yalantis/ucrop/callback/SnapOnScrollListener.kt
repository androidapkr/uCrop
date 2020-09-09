package com.yalantis.ucrop.callback


import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION)
}

fun SnapHelper.getSnapView(recyclerView: RecyclerView): View? {
    val layoutManager = recyclerView.layoutManager ?: return null
    return findSnapView(layoutManager) ?: return null
}


interface OnSnapPositionChangeListener {
    fun onSnapPositionChange(position: Int, view: View?)
}