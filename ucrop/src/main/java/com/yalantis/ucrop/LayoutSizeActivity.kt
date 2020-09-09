package com.yalantis.ucrop

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.yalantis.ucrop.adapter.LayoutSizeAdapter
import com.yalantis.ucrop.callback.*
import com.yalantis.ucrop.util.PREFIX_X
import com.yalantis.ucrop.util.getCenterSnapHorizontalLayoutManager
import com.yalantis.ucrop.util.resizeInRangeOf
import com.yalantis.ucrop.util.toPx
import kotlinx.android.synthetic.main.activity_layout_size.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener as OnGlobalLayoutListener1

class LayoutSizeActivity : AppCompatActivity(), LayoutSizeAdapter.OnLayoutSizeListener {
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var layoutSizeAdapter: LayoutSizeAdapter
    private var isPortrait = true
    private var aspectRatio = 1f
    private var viewWidth = 0
    private var viewHeight = 0
    private var isManualScroll = true
    private var layoutSizePosition = 0
    private lateinit var snapHelper: LinearSnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_size)
        if (card_view_frame.viewTreeObserver.isAlive) {
            card_view_frame.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener1 {
                override fun onGlobalLayout() {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        card_view_frame.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        card_view_frame.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                    viewWidth = card_view_frame.width
                    viewHeight = card_view_frame.height
                }
            })
        }

        buttonSize.text = getString(R.string._1080_x_1080)
        snapHelper = LinearSnapHelper()

        isManualScroll = true
        layoutManager = getCenterSnapHorizontalLayoutManager()
        recyclerViewSize.layoutManager = layoutManager
        layoutSizeAdapter = LayoutSizeAdapter(this)
        recyclerViewSize.adapter = layoutSizeAdapter
        recyclerViewSize.setItemViewCacheSize(layoutSizeAdapter.size)

        recyclerViewSize.attachSizeLayoutSnapHelperWithListener(snapHelper, SizeLayoutSnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL, object : OnSnapPositionChangeListener {
            override fun onSnapping() {
            }

            override fun onSnapped() {
                if (!isManualScroll) {
                    layoutSizePosition = snapHelper.getSnapPosition(recyclerViewSize)
                    if (layoutSizePosition >= 0) {
                        recyclerViewSize.post {
                            layoutSizeAdapter.setSelected(layoutSizePosition)
                        }
                    }
                    animateSizeSnapLayout(snapHelper.getSnapView(recyclerViewSize), layoutSizeAdapter.list[layoutSizePosition].split(PREFIX_X))
                }
                isManualScroll = true
            }

            @SuppressLint("SetTextI18n")
            override fun onSnapPositionChange(position: Int, view: View?) {
                if (isManualScroll && recyclerViewSize.adapter != null) {
                    layoutSizePosition = position
                    val array = layoutSizeAdapter.list[position].split(PREFIX_X)
                    buttonSize.text = array[0] + PREFIX_X + array[1]
                    aspectRatio = array[0].toFloat() / array[1].toFloat()
                    isPortrait = true
                    animateSizeSnapLayout(view, array)
                }
            }
        })

        val paddingLeft = (applicationContext.resources.displayMetrics.widthPixels - 100.toPx()) / 2
        recyclerViewSize.setPadding(paddingLeft, recyclerViewSize.paddingTop, paddingLeft, recyclerViewSize.bottom)
        image_view_rotate_layout.setOnClickListener { rotateCanvasView(it) }
        adjustLayout(arrayListOf("2048", "2048"))
    }

    private fun rotateCanvasView(view: View) {
        if (layoutSizePosition == -1) return
        try {
            ObjectAnimator.ofFloat(view, View.ROTATION, 360f, 0f).setDuration(300).start()
            isPortrait = !isPortrait
            layoutSizeAdapter.list[layoutSizePosition].split(PREFIX_X)
            adjustLayout(layoutSizeAdapter.list[layoutSizePosition].split(PREFIX_X))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustLayout(array: List<String>) {
        val changeBounds = ChangeBounds()
        changeBounds.duration = 200
        TransitionManager.beginDelayedTransition(container, changeBounds)
        val paramCard = card_view_frame.layoutParams
        buttonSize.text = if (isPortrait) array[0] + PREFIX_X + array[1] else array[1] + PREFIX_X + array[0]
        aspectRatio = if (isPortrait) array[0].toFloat() / array[1].toFloat() else array[1].toFloat() / array[0].toFloat()
        val dimen = doubleArrayOf(if (isPortrait) array[0].toDouble() else array[1].toDouble(), if (isPortrait) array[1].toDouble() else array[0].toDouble()).resizeInRangeOf((viewWidth).toDouble(), (viewHeight).toDouble())
        paramCard.width = dimen[0].toInt()
        paramCard.height = dimen[1].toInt()
        card_view_frame.layoutParams = paramCard
    }

    private fun animateSizeSnapLayout(view: View?, array: List<String>) {
        if (view != null) {
            val dimen = doubleArrayOf(array[0].toDouble(), array[1].toDouble()).resizeInRangeOf((viewWidth).toDouble(), (viewHeight).toDouble())
        }
    }

    override fun onLayoutSizeSelected(position: Int, width: Int, height: Int) {
        layoutSizePosition = position
        recyclerViewSize.smoothScrollToPosition(position)

        recyclerViewSize.post {
            layoutSizeAdapter.setSelected(layoutSizePosition)
        }
    }

    lateinit var menu: Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ucrop_menu_activity, menu)
        this.menu = menu

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()

        return true
    }
}