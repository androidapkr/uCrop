package com.yalantis.ucrop.callback

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.card.MaterialCardView
import com.yalantis.ucrop.callback.OnSnapPositionChangeListener
import com.yalantis.ucrop.callback.getSnapPosition
import com.yalantis.ucrop.util.toPx

class SizeLayoutSnapOnScrollListener(
        private val snapHelper: SnapHelper,
        var behavior: Behavior = Behavior.NOTIFY_ON_SCROLL,
        var onSnapPositionChangeListener: OnSnapPositionChangeListener? = null
) : RecyclerView.OnScrollListener() {

    enum class Behavior {
        NOTIFY_ON_SCROLL,
        NOTIFY_ON_SCROLL_STATE_IDLE
    }

    private var snapPosition = RecyclerView.NO_POSITION

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onSnapPositionChangeListener?.onSnapped()
        } else {
            onSnapPositionChangeListener?.onSnapping()
        }
        if (behavior == Behavior.NOTIFY_ON_SCROLL_STATE_IDLE && newState == RecyclerView.SCROLL_STATE_IDLE) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) (recyclerView.getChildAt(i) as MaterialCardView).strokeWidth = 0

        val snapPosition = snapHelper.getSnapPosition(recyclerView)
        val snapPositionChanged = this.snapPosition != snapPosition
        if (snapPositionChanged) {
            val view: View? = snapHelper.getCenterSnapView(recyclerView)
            if (view != null) (view as MaterialCardView).strokeWidth = 2.toPx()
            onSnapPositionChangeListener?.onSnapPositionChange(snapPosition, view!!)
            this.snapPosition = snapPosition
        }
    }
}

fun SnapHelper.getCenterSnapView(recyclerView: RecyclerView): View? {
    val layoutManager = recyclerView.layoutManager ?: return null
    return findSnapView(layoutManager) ?: return null
}

fun RecyclerView.attachSizeLayoutSnapHelperWithListener(
        snapHelper: SnapHelper,
        behavior: SizeLayoutSnapOnScrollListener.Behavior = SizeLayoutSnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
        onSnapPositionChangeListener: OnSnapPositionChangeListener) {
    snapHelper.attachToRecyclerView(this)
    addOnScrollListener(SizeLayoutSnapOnScrollListener(snapHelper, behavior, onSnapPositionChangeListener))
}
