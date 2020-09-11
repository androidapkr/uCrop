package com.yalantis.ucrop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.card.MaterialCardView
import com.yalantis.ucrop.adapter.LayoutSizeAdapter
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.callback.OnSnapPositionChangeListener
import com.yalantis.ucrop.callback.SizeLayoutSnapOnScrollListener
import com.yalantis.ucrop.callback.attachSizeLayoutSnapHelperWithListener
import com.yalantis.ucrop.util.*
import com.yalantis.ucrop.util.BitmapLoadUtils.calculateMaxBitmapSize
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView
import kotlinx.android.synthetic.main.activity_layout_size.*
import java.util.*
import kotlin.math.min
import android.view.ViewTreeObserver.OnGlobalLayoutListener as OnGlobalLayoutListener1

open class LayoutSizeActivity : AppCompatActivity(), LayoutSizeAdapter.OnLayoutSizeListener {
    private lateinit var layoutSizeAdapter: LayoutSizeAdapter
    private var viewWidth = 0
    private var viewHeight = 0
    private var oldView: View? = null
    private var isSaving = false

    private val DEFAULT_COMPRESS_QUALITY = 100
    private val DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG
    private val ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42

    private var mActiveControlsWidgetColor: Int = 0
    private var mInActiveControlsWidgetColor: Int = 0
    private var mRootViewBackgroundColor: Int = 0
    private var mRootViewBackgroundSurfaceColor: Int = 0

    private var mCompressFormat = DEFAULT_COMPRESS_FORMAT
    private var mCompressQuality = DEFAULT_COMPRESS_QUALITY

    private lateinit var mGestureCropImageView: GestureCropImageView
    private lateinit var mOverlayView: OverlayView

    private var mBlockingView: FrameLayout? = null

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {

        val win: Window = activity.window

        val winParams: WindowManager.LayoutParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (Build.VERSION.SDK_INT in 19..20)
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true)
//        if (Build.VERSION.SDK_INT >= 19)
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true)
//            window.statusBarColor = Color.TRANSPARENT
//            window.navigationBarColor = Color.TRANSPARENT
//        }
//        android:fitsSystemWindows="false"

        setContentView(R.layout.activity_layout_size)

//        (app_bar_layout.layoutParams as ConstraintLayout.LayoutParams).topMargin = getStatusBarHeight()
//        (recyclerViewSize.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = getNavigationBarHeight()

        if (mCropView.viewTreeObserver.isAlive) {
            mCropView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener1 {
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mCropView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        mCropView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                    viewWidth = mCropView.width
                    viewHeight = mCropView.height
                }
            })
        }

        buttonSize.text = ""

        layoutSizeAdapter = LayoutSizeAdapter(this)
        recyclerViewSize.layoutManager = getCenterSnapHorizontalLayoutManager()
        recyclerViewSize.adapter = layoutSizeAdapter
        recyclerViewSize.setItemViewCacheSize(layoutSizeAdapter.size)
        recyclerViewSize.attachSizeLayoutSnapHelperWithListener(LinearSnapHelper(), SizeLayoutSnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL, object : OnSnapPositionChangeListener {
            override fun onSnapPositionChange(position: Int, view: View?) {
                if (oldView != null) (oldView as MaterialCardView).strokeWidth = 0
                view?.let {
                    (it as MaterialCardView).strokeWidth = 2.toPx()
                    oldView = it
                }
                val item = layoutSizeAdapter.list[position].split(PREFIX_X)
                if (position == 0) {
                    buttonSize.text = ""
                    mOverlayView.freestyleCropMode = OverlayView.FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH
                    mGestureCropImageView.targetAspectRatio = 0f
                } else {
                    buttonSize.text = item[0] + " " + PREFIX_X + " " + item[1]
                    mOverlayView.freestyleCropMode = OverlayView.FREESTYLE_CROP_MODE_DISABLE
                    mGestureCropImageView.targetAspectRatio = item[0].toFloat() / item[1].toFloat()
                }
                mGestureCropImageView.setImageToWrapCropBounds()
            }

            override fun onSnapped(snapPosition: Int) {
                layoutSizeAdapter.setSelected(snapPosition)
            }
        })

        val paddingLeft = (applicationContext.resources.displayMetrics.widthPixels - 100.toPx()) / 2
        recyclerViewSize.setPadding(paddingLeft, recyclerViewSize.paddingTop, paddingLeft, recyclerViewSize.bottom)

        mGestureCropImageView = mCropView.cropImageView
        mOverlayView = mCropView.overlayView

        mGestureCropImageView.setTransformImageListener(mImageListener)

        imageViewClose.setOnClickListener { stopProcessWithExit() }
        imageViewCrop.setOnClickListener { cropAndSaveImage() }

        setImageData(intent)

        addBlockingView()
    }

    private val mImageListener: TransformImageView.TransformImageListener = object : TransformImageView.TransformImageListener {
        override fun onLoadComplete() {
            mCropView.animate().alpha(1f).setDuration(300).setInterpolator(AccelerateInterpolator()).start()
            mBlockingView?.let { it.isClickable = false }
            imageViewCrop.setImageResource(R.drawable.ucrop_ic_done)
        }

        override fun onLoadFailure(e: Exception) {
            setResultError(e)
            finish()
        }

        override fun onRotate(currentAngle: Float) {
            setAngleText(currentAngle)
        }

        override fun onScale(currentScale: Float) {

        }
    }

    private fun setImageData(intent: Intent) {
        val inputUri = intent.getParcelableExtra<Uri>(UCrop.EXTRA_INPUT_URI)
        val outputUri = intent.getParcelableExtra<Uri>(UCrop.EXTRA_OUTPUT_URI)
        processOptions(intent)
        processWidget()

        if (inputUri != null && outputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri, outputUri)

                mGestureCropImageView.isScaleEnabled = true
                mGestureCropImageView.isRotateEnabled = false

            } catch (e: Exception) {
                setResultError(e)
                finish()
            }
        } else {
            setResultError(NullPointerException(getString(R.string.ucrop_error_input_data_is_absent)))
            finish()
        }
    }

    private fun processOptions(intent: Intent) {

        mActiveControlsWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_COLOR_CONTROLS_WIDGET_ACTIVE, getResColor(R.color.ucrop_color_active_controls_color))
        mInActiveControlsWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_COLOR_CONTROLS_WIDGET_INACTIVE, getResColor(R.color.ucrop_color_active_controls_color))

        mRootViewBackgroundColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, getResColor(R.color.ucrop_color_crop_background))
        mRootViewBackgroundSurfaceColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, getResColor(R.color.ucrop_color_crop_background))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setBackgroundDrawable(ColorDrawable(mRootViewBackgroundColor))
            window.statusBarColor = mRootViewBackgroundColor
            window.navigationBarColor = mRootViewBackgroundColor
        }

        // Bitmap compression options
        var compressFormat: Bitmap.CompressFormat? = null
        intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME)?.let {
            if (!TextUtils.isEmpty(it)) {
                compressFormat = Bitmap.CompressFormat.valueOf(it)
            }
        }
        mCompressFormat = compressFormat ?: DEFAULT_COMPRESS_FORMAT
        mCompressQuality = intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, DEFAULT_COMPRESS_QUALITY)

        // Crop image view options
        mGestureCropImageView.maxBitmapSize = intent.getIntExtra(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, calculateMaxBitmapSize(applicationContext))

        mGestureCropImageView.maxBitmapSize = min(mGestureCropImageView.maxBitmapSize, calculateMaxBitmapSize(applicationContext))

        mGestureCropImageView.setMaxScaleMultiplier(intent.getFloatExtra(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER))
        mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION).toLong())

        // Overlay view options
        mOverlayView.freestyleCropMode = OverlayView.FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH
        mOverlayView.setDimmedColor(intent.getIntExtra(UCrop.Options.EXTRA_DIMMED_LAYER_COLOR, getResColor(R.color.ucrop_color_default_dimmed)))
        mOverlayView.setCircleDimmedLayer(intent.getBooleanExtra(UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER))
        mOverlayView.setShowCropFrame(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME))
        mOverlayView.setCropFrameColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_COLOR, getResColor(R.color.ucrop_color_default_crop_frame)))
        mOverlayView.setCropFrameStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)))
        mOverlayView.setShowCropGrid(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_GRID, OverlayView.DEFAULT_SHOW_CROP_GRID))
        mOverlayView.setCropGridRowCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT))
        mOverlayView.setCropGridColumnCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT))
        mOverlayView.setCropGridColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLOR, getResColor(R.color.ucrop_color_default_crop_grid)))
        mOverlayView.setCropGridCornerColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_CORNER_COLOR, getResColor(R.color.ucrop_color_default_crop_grid)))
        mOverlayView.setCropGridStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH, resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)))

        mGestureCropImageView.targetAspectRatio = intent.getFloatExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, CropImageView.SOURCE_IMAGE_ASPECT_RATIO)

        val maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0)
        val maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0)
        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView.setMaxResultImageSizeX(maxSizeX)
            mGestureCropImageView.setMaxResultImageSizeY(maxSizeY)
        }
    }

    private fun processWidget() {

        imageViewPlaceholder.colorFilter = PorterDuffColorFilter(mInActiveControlsWidgetColor, PorterDuff.Mode.SRC_IN)

        rotateScrollWheel2.setScrollingListener(object : HorizontalProgressWheelView.ScrollingListener {
            override fun onScroll(delta: Float, totalDistance: Float) {
                mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT)
            }

            override fun onScrollEnd() {
                mGestureCropImageView.setImageToWrapCropBounds()
            }

            override fun onScrollStart() {
                mGestureCropImageView.cancelAllAnimations()
            }
        })
        rotateScrollWheel2.setMiddleLineColor(mActiveControlsWidgetColor)
        rotateScrollWheel2.setLineColor(mInActiveControlsWidgetColor)

        materialButtonReset2.setOnClickListener {
            mGestureCropImageView.postRotate(-mGestureCropImageView.currentAngle)
            mGestureCropImageView.setImageToWrapCropBounds()
        }
        materialButton90Angle2.setOnClickListener {
            mGestureCropImageView.postRotate(90f)
            mGestureCropImageView.setImageToWrapCropBounds()
        }
        textViewRotate.setTextColor(mActiveControlsWidgetColor)
    }

    private fun addBlockingView() {
        if (mBlockingView == null) {
            mBlockingView = FrameLayout(this)
            mBlockingView?.let {
                val lp = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                lp.startToStart = container.id
                lp.topToBottom = app_bar_layout.id
                lp.endToEnd = container.id
                lp.bottomToBottom = container.id
                it.layoutParams = lp
                it.isClickable = true
            }
        }
        container.addView(mBlockingView)
        animateSaveIcon()
    }

    private fun animateSaveIcon() {
        ContextCompat.getDrawable(applicationContext, R.drawable.ucrop_vector_loader_animated)?.let {
            try {
                it.mutate()
                it.colorFilter = PorterDuffColorFilter(mActiveControlsWidgetColor, PorterDuff.Mode.SRC_IN)
                imageViewCrop.setImageDrawable(it)
                (it as Animatable).start()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun cropAndSaveImage() {
        if (isSaving) return
        isSaving = true
        mBlockingView?.isClickable = true
        animateSaveIcon()
        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, object : BitmapCropCallback {

            override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
                mBlockingView?.isClickable = false
                isSaving = false
                setResultUri(resultUri, mGestureCropImageView.targetAspectRatio, offsetX, offsetY, imageWidth, imageHeight)
                finish()
            }

            override fun onCropCancelled() {
                mBlockingView?.isClickable = false
                isSaving = false
                setResultError(Exception("Operation Cancelled by user."))
                finish()
            }

            override fun onCropFailure(t: Throwable) {
                mBlockingView?.isClickable = false
                isSaving = false
                setResultError(t)
                finish()
            }
        })
    }

    protected fun setResultUri(uri: Uri?, resultAspectRatio: Float, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
        setResult(RESULT_OK, Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
        )
    }

    protected fun setResultError(throwable: Throwable?) {
        setResult(UCrop.RESULT_ERROR, Intent().putExtra(UCrop.EXTRA_ERROR, throwable))
    }

    private fun stopProcessWithExit() {
        if (!mGestureCropImageView.cancelCurrentTask()) {
            onBackPressed()
        }
    }

    private fun setAngleText(angle: Float) {
        if (textViewRotate != null) {
            textViewRotate.text = String.format(Locale.getDefault(), "%.1f°", angle)
        }
    }

    override fun onLayoutSizeSelected(position: Int, width: Int, height: Int, view: View?) {
        if (oldView != null) (oldView as MaterialCardView).strokeWidth = 0
        if (view != null) {
            (view as MaterialCardView).strokeWidth = 0
            oldView = view
        }
        recyclerViewSize.smoothScrollToPosition(position)
    }

    override fun onStop() {
        super.onStop()
        mGestureCropImageView.cancelAllAnimations()
    }

    override fun onDestroy() {
        container.removeAllViews()
        Runtime.getRuntime().gc()
        super.onDestroy()
    }
}