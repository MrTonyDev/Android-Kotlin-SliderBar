package android.tony.tony.sliderbar

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.sliderbar.view.*


class SliderBar : FrameLayout {
    private var vc: ViewConfiguration? = null
    private lateinit var set : ConstraintSet
    var isVibrated = false
    var formatText = "%.2f"
    var totalDuration = 1f//default
    var minDuration = 0f
    var maxDuration = 0f
    var scaleUp = 2f
    var listener: SliderBarListener? = null
    private lateinit var vibrator: Vibrator
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs : AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    fun setViewVisibility(visibility: Int, @IdRes vararg viewIds: Int) {
        for (id in viewIds) {
            set.setVisibility(id, visibility)
        }
        set.applyTo(root)
        set.clone(root)
    }

    fun setPosition(left: Float, right: Float, current: Float) {
        moveArrowLeft(left)
        moveArrowRight(right)
        //set current line
        set.setHorizontalBias(R.id.currentLine, current)
        set.applyTo(root)
        set.clone(root)
    }

    fun setLeftBgColor(@ColorInt color: Int){
        bgLeft.setBackgroundColor(color)
    }

    fun setRightBgColor(@ColorInt color: Int){
        bgRight.setBackgroundColor(color)
    }

    fun setMidBgColor(@ColorInt color: Int){
        bgMid.setBackgroundColor(color)
    }

    fun setLeftImageRes(@DrawableRes resId: Int){
        bgLeft.setImageResource(resId)
    }

    fun setRightImageRes(@DrawableRes resId: Int){
        bgRight.setImageResource(resId)
    }

    fun setMidImageRes(@DrawableRes resId: Int){
        bgMid.setImageResource(resId)
    }

    private fun init() {
        inflate(context, R.layout.sliderbar, this)
        vc = ViewConfiguration.get(context)
        set = ConstraintSet()
        set.clone(root)

        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun vibrate() {
        if (isVibrated) {
            vibrator.vibrate(500)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        getLocationOnScreen(locations)
        arrowLeft.pivotY = (arrowLeft.height/2).toFloat()
        arrowRight.pivotY = (arrowRight.height/2).toFloat()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                arrowLeft.resetMotionAction()
                arrowRight.resetMotionAction()
                bgMid.resetMotionAction()
                currentLine.resetMotionAction()
                return false
            }
            MotionEvent.ACTION_MOVE -> return true
            else -> return false
        }
    }

    private var startX : Float = 0f
    private val locations = IntArray(2)
    private var originalLeftBias : Float = 0f
    private var originalRightBias : Float = 0f
    private var isMoving : Boolean = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val currentX = event?.rawX
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (arrowLeft.existActionDown) {
                    arrowLeft.animate().scaleY(scaleUp).setDuration(200).withEndAction {
                        set.setScaleY(R.id.arrowLeft, scaleUp)
                        set.applyTo(root)
                        set.clone(root) }.start()
                    vibrate()
                }else if (arrowRight.existActionDown) {
                    arrowRight.animate().scaleY(scaleUp).setDuration(200).withEndAction {
                        set.setScaleY(R.id.arrowRight, scaleUp)
                        set.applyTo(root)
                        set.clone(root)
                    }.start()
                    vibrate()
                }
                isMoving = false
                startX = event.rawX
                originalLeftBias = (arrowLeft.layoutParams as ConstraintLayout.LayoutParams).horizontalBias
                originalRightBias = (arrowRight.layoutParams as ConstraintLayout.LayoutParams).horizontalBias
            }
            MotionEvent.ACTION_MOVE -> {
                var startViewPos = locations[0]
                val currentDeltaX = (currentX?.minus(startX))
                if (Math.abs(currentDeltaX!!) > vc?.scaledTouchSlop!! || isMoving) {
                    isMoving = true
                    var bias = (currentX-startViewPos) / width
                    if (bias < 0) bias = 0f
                    if (bias > 1) bias = 1f
                    if (currentLine.existActionDown) {//current line
                        if (bias > originalRightBias) bias = originalRightBias//
                        if (bias < originalLeftBias) bias = originalLeftBias//limit in left and right arrow
                        set.setHorizontalBias(R.id.currentLine, bias)
                        set.applyTo(root)
                        listener?.onCurrentLineChanged(totalDuration*bias, true)
                    }else if (arrowLeft.existActionDown) {//left
                        if (bias > originalRightBias - minDuration) bias = originalRightBias - minDuration//prevent 'left' move over 'right' and rely on minDuration too
                        if (bias < originalRightBias - maxDuration) bias = originalRightBias - maxDuration
                        moveArrowLeft(bias)
                        set.applyTo(root)
                        listener?.onLeftChanged(totalDuration*bias)
                    }else if (arrowRight.existActionDown) {//right
                        if (bias < originalLeftBias + minDuration) bias = originalLeftBias + minDuration//prevent 'right' move over 'left'
                        if (bias > originalLeftBias + maxDuration) bias = originalLeftBias + maxDuration
                        moveArrowRight(bias)
                        set.applyTo(root)
                        listener?.onRightChanged(totalDuration*bias)
                    }else if (bgMid.existActionDown) {//both left and right
                        if (originalLeftBias == 0f && originalRightBias == 1f) {
                            return true
                        }
                        val deltaBias = currentDeltaX/width
                        var leftBias = originalLeftBias + deltaBias
                        var rightBias = originalRightBias + deltaBias

                        if (leftBias < 0) {
                            val differenceZero = leftBias
                            leftBias = 0f
                            moveBothLeftAndRight(leftBias, rightBias-differenceZero)
                            set.applyTo(root)
                            listener?.onBothLeftAndRightChanged(totalDuration*leftBias, totalDuration*(rightBias-differenceZero))
                            return true
                        }
                        if (rightBias > 1) {
                            val differenceOne = rightBias - 1f
                            rightBias = 1f
                            moveBothLeftAndRight(leftBias-differenceOne, rightBias)
                            set.applyTo(root)
                            listener?.onBothLeftAndRightChanged(totalDuration*(leftBias-differenceOne), totalDuration*rightBias)
                            return true
                        }
                        moveBothLeftAndRight(leftBias, rightBias)
                        set.applyTo(root)
                        listener?.onBothLeftAndRightChanged(totalDuration*leftBias, totalDuration*rightBias)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                isMoving = false
                if (arrowLeft.existActionDown) {
                    arrowLeft.animate().scaleY(1f).setDuration(200).withEndAction {
                        set.setScaleY(R.id.arrowLeft, 1f)
                        set.applyTo(root)
                        set.clone(root) }.start()
                }else if (arrowRight.existActionDown) {
                    arrowRight.animate().scaleY(1f).setDuration(200).withEndAction {
                        set.setScaleY(R.id.arrowRight, 1f)
                        set.applyTo(root)
                        set.clone(root)
                    }.start()
                }
            }
        }
        return true
    }

    private fun moveArrowLeft(bias : Float) {
        set.setHorizontalBias(R.id.tvTopLeft, bias)
        set.setHorizontalBias(R.id.tvBottomLeft, bias)
        set.setHorizontalBias(R.id.arrowLeft, bias)
        tvTopLeft.text = String.format(formatText, totalDuration*bias)
        tvBottomLeft.text = String.format(formatText, totalDuration*bias)
    }

    private fun moveArrowRight(bias : Float){
        set.setHorizontalBias(R.id.tvTopRight, bias)
        set.setHorizontalBias(R.id.tvBottomRight, bias)
        set.setHorizontalBias(R.id.arrowRight, bias)

        tvTopRight.text = String.format(formatText, totalDuration*bias)
        tvBottomRight.text = String.format(formatText, totalDuration*bias)
    }

    private fun moveBothLeftAndRight(leftBias: Float, rightBias: Float){
        moveArrowLeft(leftBias)
        moveArrowRight(rightBias)
    }

}