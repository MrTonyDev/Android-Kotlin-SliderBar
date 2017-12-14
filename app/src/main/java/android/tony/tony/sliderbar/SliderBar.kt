package android.tony.tony.sliderbar

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.sliderbar.view.*


class SliderBar : FrameLayout {
    var minDuration = 0f
    var maxDuration = 0f
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

    private var vc: ViewConfiguration? = null
    private lateinit var set : ConstraintSet

    fun init() {
        inflate(context, R.layout.sliderbar, this)
        vc = ViewConfiguration.get(context)
        set = ConstraintSet()
        set.clone(root)
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
                return false
            }
            MotionEvent.ACTION_MOVE -> return true
            else -> return false
        }
    }

    private var startX : Float = 0f
    val locations = IntArray(2)
    var originalLeftBias : Float = 0f
    var originalRightBias : Float = 0f
    var isMoving : Boolean = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val currentX = event?.rawX
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (arrowLeft.existActionDown) {
                    arrowLeft.animate().scaleY(3f).setDuration(200).withEndAction {
                        set.setScaleY(R.id.arrowLeft, 3f)
                        set.applyTo(root)
                        set.clone(root) }.start()
                }else if (arrowRight.existActionDown) {
                    arrowRight.animate().scaleY(3f).setDuration(200).withEndAction {
                        set.setScaleY(R.id.arrowRight, 3f)
                        set.applyTo(root)
                        set.clone(root)
                    }.start()
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
                    if (arrowLeft.existActionDown) {
                        if (bias > originalRightBias - minDuration) bias = originalRightBias - minDuration//prevent 'left' move over 'right' and rely on minDuration too
                        if (bias < originalRightBias - maxDuration) bias = originalRightBias - maxDuration
                        set.setHorizontalBias(R.id.arrowLeft, bias)
                        set.applyTo(root)
                    }else if (arrowRight.existActionDown) {
                        if (bias < originalLeftBias + minDuration) bias = originalLeftBias + minDuration//prevent 'right' move over 'left'
                        if (bias > originalLeftBias + maxDuration) bias = originalLeftBias + maxDuration
                        set.setHorizontalBias(R.id.arrowRight, bias)
                        set.applyTo(root)
                    }else if (bgMid.existActionDown) {
                        if (originalLeftBias == 0f && originalRightBias == 1f) {
                            return true
                        }
                        val deltaBias = currentDeltaX/width
                        var leftBias = originalLeftBias + deltaBias
                        var rightBias = originalRightBias + deltaBias

                        if (leftBias < 0) {
                            val differenceZero = leftBias
                            leftBias = 0f
                            set.setHorizontalBias(R.id.arrowLeft, leftBias)
                            set.setHorizontalBias(R.id.arrowRight, rightBias-differenceZero)
                            set.applyTo(root)
                            return true
                        }
                        if (rightBias > 1) {
                            val differenceOne = rightBias - 1f
                            rightBias = 1f

                            set.setHorizontalBias(R.id.arrowLeft, leftBias-differenceOne)
                            set.setHorizontalBias(R.id.arrowRight, rightBias)
                            set.applyTo(root)
                            return true
                        }

                        set.setHorizontalBias(R.id.arrowLeft, leftBias)
                        set.setHorizontalBias(R.id.arrowRight, rightBias)
                        set.applyTo(root)
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

}