package android.tony.tony.sliderbar

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent

class TonyImageView : AppCompatImageView {

    var existActionDown : Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs : AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                existActionDown = true
            }
        }
        return super.onTouchEvent(event)
    }

    fun resetMotionAction() {
        existActionDown = false
    }
}
