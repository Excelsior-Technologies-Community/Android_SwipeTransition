package com.ext.android_swipe_transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.GestureDetectorCompat

class SwipeTransitionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Swipe properties
    private var swipeDirection: SwipeDirection = SwipeDirection.ALL
    private var transitionDuration: Int = 300
    private var swipeThreshold: Float = 0.25f
    private var enableSwipe: Boolean = true

    // Visual properties
    private var overlayColor: Int = Color.parseColor("#40000000")
    private var showIndicator: Boolean = true
    private var indicatorColor: Int = Color.WHITE
    private var indicatorSize: Float = 60f
    private var cardElevation: Float = 16f

    // Image stack
    private val imageList = mutableListOf<Int>()
    private var currentIndex = 0

    // Views
    private var currentImageView: ImageView? = null
    private var nextImageView: ImageView? = null
    private var backgroundImageView: ImageView? = null

    // Animation properties
    private var currentOffsetX: Float = 0f
    private var currentOffsetY: Float = 0f
    private var transitionProgress: Float = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gestureDetector: GestureDetectorCompat
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var isAnimating: Boolean = false
    private var startX: Float = 0f
    private var startY: Float = 0f

    // Listener
    var onSwipeListener: OnSwipeListener? = null

    enum class SwipeDirection {
        HORIZONTAL, VERTICAL, ALL
    }

    interface OnSwipeListener {
        fun onSwipeStart(currentIndex: Int)
        fun onSwipeProgress(progress: Float, currentIndex: Int)
        fun onSwipeComplete(direction: String, oldIndex: Int, newIndex: Int)
        fun onSwipeCancel(currentIndex: Int)
        fun onLastImage()
    }

    init {
        setWillNotDraw(false)
        clipChildren = false

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SwipeTransitionView,
            0, 0
        ).apply {
            try {
                swipeDirection = SwipeDirection.values()[
                    getInt(R.styleable.SwipeTransitionView_swipeDirection, 2)
                ]
                transitionDuration = getInt(R.styleable.SwipeTransitionView_transitionDuration, 300)
                swipeThreshold = getFloat(R.styleable.SwipeTransitionView_swipeThreshold, 0.25f)
                enableSwipe = getBoolean(R.styleable.SwipeTransitionView_enableSwipe, true)
                overlayColor = getColor(R.styleable.SwipeTransitionView_overlayColor, overlayColor)
                showIndicator = getBoolean(R.styleable.SwipeTransitionView_showIndicator, true)
                indicatorColor = getColor(R.styleable.SwipeTransitionView_indicatorColor, indicatorColor)
                indicatorSize = getDimension(R.styleable.SwipeTransitionView_indicatorSize, indicatorSize)
                cardElevation = getDimension(R.styleable.SwipeTransitionView_swipeElevation, cardElevation)
            } finally {
                recycle()
            }
        }

        gestureDetector = GestureDetectorCompat(context, SwipeGestureListener())
        setupImageViews()
    }

    private fun setupImageViews() {
        // Background image (3rd in stack)
        backgroundImageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0.6f
        }
        addView(backgroundImageView)

        // Next image (2nd in stack)
        nextImageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0.85f
        }
        addView(nextImageView)

        // Current image (top of stack)
        currentImageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        addView(currentImageView)
    }

    fun setImages(images: List<Int>) {
        imageList.clear()
        imageList.addAll(images)
        currentIndex = 0
        updateImageViews()
    }

    fun addImage(imageResId: Int) {
        imageList.add(imageResId)
        if (imageList.size == 1) {
            updateImageViews()
        }
    }

    private fun updateImageViews() {
        if (imageList.isEmpty()) return

        // Current image
        if (currentIndex < imageList.size) {
            currentImageView?.setImageResource(imageList[currentIndex])
            currentImageView?.alpha = 1f
        }

        // Next image
        if (currentIndex + 1 < imageList.size) {
            nextImageView?.setImageResource(imageList[currentIndex + 1])
            nextImageView?.alpha = 0.85f
            nextImageView?.scaleX = 0.96f
            nextImageView?.scaleY = 0.96f
        } else {
            nextImageView?.setImageDrawable(null)
        }

        // Background image
        if (currentIndex + 2 < imageList.size) {
            backgroundImageView?.setImageResource(imageList[currentIndex + 2])
            backgroundImageView?.alpha = 0.6f
            backgroundImageView?.scaleX = 0.92f
            backgroundImageView?.scaleY = 0.92f
        } else {
            backgroundImageView?.setImageDrawable(null)
        }

        resetPosition()
    }

    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (enableSwipe && !isAnimating && currentIndex < imageList.size) {
                startX = e.x
                startY = e.y
                onSwipeListener?.onSwipeStart(currentIndex)
                return true
            }
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!enableSwipe || isAnimating || currentIndex >= imageList.size) return false

            when (swipeDirection) {
                SwipeDirection.HORIZONTAL -> {
                    currentOffsetX -= distanceX
                    currentOffsetY = 0f
                    transitionProgress = Math.abs(currentOffsetX) / width.toFloat()
                }
                SwipeDirection.VERTICAL -> {
                    currentOffsetY -= distanceY
                    currentOffsetX = 0f
                    transitionProgress = Math.abs(currentOffsetY) / height.toFloat()
                }
                SwipeDirection.ALL -> {
                    currentOffsetX -= distanceX
                    currentOffsetY -= distanceY

                    val distance = Math.sqrt(
                        (currentOffsetX * currentOffsetX + currentOffsetY * currentOffsetY).toDouble()
                    ).toFloat()
                    val maxDistance = Math.sqrt(
                        (width * width + height * height).toDouble()
                    ).toFloat()
                    transitionProgress = (distance / maxDistance).coerceIn(0f, 1f)
                }
            }

            updateCardTransforms()
            onSwipeListener?.onSwipeProgress(transitionProgress, currentIndex)
            invalidate()
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (!enableSwipe || isAnimating) return false

            val velocityThreshold = 1500f

            when (swipeDirection) {
                SwipeDirection.HORIZONTAL -> {
                    if (Math.abs(velocityX) > velocityThreshold) {
                        completeSwipeInDirection(velocityX > 0, true)
                        return true
                    }
                }
                SwipeDirection.VERTICAL -> {
                    if (Math.abs(velocityY) > velocityThreshold) {
                        completeSwipeInDirection(velocityY > 0, false)
                        return true
                    }
                }
                SwipeDirection.ALL -> {
                    if (Math.abs(velocityX) > velocityThreshold || Math.abs(velocityY) > velocityThreshold) {
                        val isHorizontal = Math.abs(velocityX) > Math.abs(velocityY)
                        completeSwipeInDirection(
                            if (isHorizontal) velocityX > 0 else velocityY > 0,
                            isHorizontal
                        )
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun updateCardTransforms() {
        // Update current card position
        currentImageView?.translationX = currentOffsetX
        currentImageView?.translationY = currentOffsetY

        // Subtle rotation based on position
        val rotation = when {
            Math.abs(currentOffsetX) > Math.abs(currentOffsetY) ->
                (currentOffsetX / width.toFloat()) * 10f
            else ->
                (currentOffsetY / height.toFloat()) * 8f
        }
        currentImageView?.rotation = rotation

        // Fade out current card slightly
        currentImageView?.alpha = 1f - (transitionProgress * 0.3f).coerceIn(0f, 0.3f)

        // Scale and fade in next card
        val nextScale = 0.96f + (transitionProgress * 0.04f)
        nextImageView?.scaleX = nextScale
        nextImageView?.scaleY = nextScale
        nextImageView?.alpha = 0.85f + (transitionProgress * 0.15f)

        // Scale background card
        val bgScale = 0.92f + (transitionProgress * 0.04f)
        backgroundImageView?.scaleX = bgScale
        backgroundImageView?.scaleY = bgScale
        backgroundImageView?.alpha = 0.6f + (transitionProgress * 0.25f)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (enableSwipe && !isAnimating && transitionProgress > 0) {
                handleSwipeEnd()
            }
        }

        return handled || super.onTouchEvent(event)
    }

    private fun handleSwipeEnd() {
        // Determine swipe direction and check if threshold met
        when (swipeDirection) {
            SwipeDirection.HORIZONTAL -> {
                val threshold = width * swipeThreshold
                if (Math.abs(currentOffsetX) > threshold) {
                    completeSwipeInDirection(currentOffsetX > 0, true)
                } else {
                    returnToStart()
                }
            }
            SwipeDirection.VERTICAL -> {
                val threshold = height * swipeThreshold
                if (Math.abs(currentOffsetY) > threshold) {
                    completeSwipeInDirection(currentOffsetY > 0, false)
                } else {
                    returnToStart()
                }
            }
            SwipeDirection.ALL -> {
                val threshold = Math.min(width, height) * swipeThreshold
                val distance = Math.sqrt(
                    (currentOffsetX * currentOffsetX + currentOffsetY * currentOffsetY).toDouble()
                ).toFloat()

                if (distance > threshold) {
                    val isHorizontal = Math.abs(currentOffsetX) > Math.abs(currentOffsetY)
                    completeSwipeInDirection(
                        if (isHorizontal) currentOffsetX > 0 else currentOffsetY > 0,
                        isHorizontal
                    )
                } else {
                    returnToStart()
                }
            }
        }
    }

    private fun completeSwipeInDirection(positive: Boolean, isHorizontal: Boolean) {
        if (currentIndex >= imageList.size - 1) {
            onSwipeListener?.onLastImage()
            returnToStart()
            return
        }

        isAnimating = true

        val targetOffsetX = if (isHorizontal) {
            if (positive) width.toFloat() * 1.5f else -width.toFloat() * 1.5f
        } else currentOffsetX

        val targetOffsetY = if (!isHorizontal) {
            if (positive) height.toFloat() * 1.5f else -height.toFloat() * 1.5f
        } else currentOffsetY

        animateSwipeOut(targetOffsetX, targetOffsetY, positive, isHorizontal) {
            val direction = when {
                isHorizontal && positive -> "RIGHT"
                isHorizontal && !positive -> "LEFT"
                !isHorizontal && positive -> "DOWN"
                else -> "UP"
            }

            val oldIndex = currentIndex
            currentIndex++

            onSwipeListener?.onSwipeComplete(direction, oldIndex, currentIndex)

            if (currentIndex < imageList.size) {
                updateImageViews()
            } else {
                onSwipeListener?.onLastImage()
            }

            isAnimating = false
        }
    }

    private fun returnToStart() {
        isAnimating = true

        animateReturn {
            onSwipeListener?.onSwipeCancel(currentIndex)
            isAnimating = false
        }
    }

    private fun animateSwipeOut(targetX: Float, targetY: Float, positive: Boolean, isHorizontal: Boolean, onEnd: () -> Unit) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            val startX = currentOffsetX
            val startY = currentOffsetY
            val startRotation = currentImageView?.rotation ?: 0f
            val targetRotation = if (isHorizontal) {
                (targetX / width.toFloat()) * 20f
            } else {
                (targetY / height.toFloat()) * 15f
            }

            duration = transitionDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float

                currentOffsetX = startX + (targetX - startX) * progress
                currentOffsetY = startY + (targetY - startY) * progress
                transitionProgress = progress

                currentImageView?.translationX = currentOffsetX
                currentImageView?.translationY = currentOffsetY
                currentImageView?.rotation = startRotation + (targetRotation - startRotation) * progress
                currentImageView?.alpha = 1f - progress

                val nextScale = 0.96f + (progress * 0.04f)
                nextImageView?.scaleX = nextScale
                nextImageView?.scaleY = nextScale
                nextImageView?.alpha = 0.85f + (progress * 0.15f)

                val bgScale = 0.92f + (progress * 0.04f)
                backgroundImageView?.scaleX = bgScale
                backgroundImageView?.scaleY = bgScale
                backgroundImageView?.alpha = 0.6f + (progress * 0.4f)

                onSwipeListener?.onSwipeProgress(progress, currentIndex)
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })

            start()
        }
    }

    private fun animateReturn(onEnd: () -> Unit) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            val startX = currentOffsetX
            val startY = currentOffsetY
            val startRotation = currentImageView?.rotation ?: 0f

            duration = (transitionDuration * 0.6f).toLong()
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float

                currentImageView?.translationX = startX * progress
                currentImageView?.translationY = startY * progress
                currentImageView?.rotation = startRotation * progress
                currentImageView?.alpha = 1f - (progress * 0.3f)

                val nextScale = 0.96f + (progress * 0.04f)
                nextImageView?.scaleX = nextScale
                nextImageView?.scaleY = nextScale
                nextImageView?.alpha = 0.85f + (progress * 0.15f)

                val bgScale = 0.92f + (progress * 0.04f)
                backgroundImageView?.scaleX = bgScale
                backgroundImageView?.scaleY = bgScale

                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    resetPosition()
                    onEnd()
                }
            })

            start()
        }
    }

    private fun resetPosition() {
        currentOffsetX = 0f
        currentOffsetY = 0f
        transitionProgress = 0f

        currentImageView?.apply {
            translationX = 0f
            translationY = 0f
            rotation = 0f
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }

        nextImageView?.apply {
            scaleX = 0.96f
            scaleY = 0.96f
            alpha = 0.85f
        }

        backgroundImageView?.apply {
            scaleX = 0.92f
            scaleY = 0.92f
            alpha = 0.6f
        }

        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (transitionProgress > 0) {
            drawCardShadow(canvas)
            drawOverlay(canvas)
        }

        if (showIndicator && transitionProgress > 0.15f) {
            drawSwipeIndicator(canvas)
        }
    }

    private fun drawCardShadow(canvas: Canvas) {
        val shadowAlpha = (transitionProgress * 80).toInt().coerceIn(0, 80)

        canvas.save()
        canvas.translate(currentOffsetX, currentOffsetY)

        shadowPaint.shader = RadialGradient(
            width / 2f, height / 2f,
            Math.max(width, height).toFloat() * 0.8f,
            Color.argb(shadowAlpha, 0, 0, 0),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shadowPaint)
        canvas.restore()
    }

    private fun drawOverlay(canvas: Canvas) {
        paint.color = overlayColor
        paint.alpha = (transitionProgress * Color.alpha(overlayColor) * 0.8f).toInt()

        canvas.save()
        canvas.translate(currentOffsetX, currentOffsetY)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.restore()
    }

    private fun drawSwipeIndicator(canvas: Canvas) {
        paint.color = indicatorColor
        paint.alpha = ((transitionProgress - 0.15f) / 0.85f * 255).toInt().coerceIn(0, 255)
        paint.style = Paint.Style.FILL

        val centerX = width / 2f + currentOffsetX
        val centerY = height / 2f + currentOffsetY

        val path = Path()
        val arrowSize = indicatorSize

        if (Math.abs(currentOffsetX) > Math.abs(currentOffsetY)) {
            if (currentOffsetX > 0) {
                // Right arrow
                path.moveTo(centerX + arrowSize / 2, centerY)
                path.lineTo(centerX - arrowSize / 2, centerY - arrowSize / 2)
                path.lineTo(centerX - arrowSize / 2, centerY + arrowSize / 2)
            } else {
                // Left arrow
                path.moveTo(centerX - arrowSize / 2, centerY)
                path.lineTo(centerX + arrowSize / 2, centerY - arrowSize / 2)
                path.lineTo(centerX + arrowSize / 2, centerY + arrowSize / 2)
            }
        } else {
            if (currentOffsetY > 0) {
                // Down arrow
                path.moveTo(centerX, centerY + arrowSize / 2)
                path.lineTo(centerX - arrowSize / 2, centerY - arrowSize / 2)
                path.lineTo(centerX + arrowSize / 2, centerY - arrowSize / 2)
            } else {
                // Up arrow
                path.moveTo(centerX, centerY - arrowSize / 2)
                path.lineTo(centerX - arrowSize / 2, centerY + arrowSize / 2)
                path.lineTo(centerX + arrowSize / 2, centerY + arrowSize / 2)
            }
        }

        path.close()
        canvas.drawPath(path, paint)
    }

    fun getCurrentIndex() = currentIndex
    fun getImageCount() = imageList.size
    fun hasNextImage() = currentIndex < imageList.size - 1

    fun goToNext() {
        if (hasNextImage()) {
            completeSwipeInDirection(true, true)
        }
    }

    fun reset() {
        currentIndex = 0
        updateImageViews()
    }
}