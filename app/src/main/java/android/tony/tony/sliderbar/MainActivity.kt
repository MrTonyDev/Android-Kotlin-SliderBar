package android.tony.tony.sliderbar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SliderBarListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sliderbar.scaleUp = 2f
        sliderbar.minDuration = 0.1f//from 0f -> 1f
        sliderbar.maxDuration = 0.8f
        sliderbar.formatText = "%.3f"
        sliderbar.setPosition(0.2f, 0.6f, 0.4f)
        sliderbar.listener = this
    }

    override fun onLeftChanged(value: Float) {
        Log.d("HoangNM", "onLeftChanged: " + value)
    }

    override fun onRightChanged(value: Float) {
        Log.d("HoangNM", "onRightChanged: " + value)
    }

    override fun onBothLeftAndRightChanged(left: Float, right: Float) {
        Log.d("HoangNM", "onBothLeftAndRightChanged: " + left + " --- " + right)
    }

    override fun onCurrentLineChanged(value: Float, fromUser: Boolean) {
        Log.d("HoangNM", "onCurrentLineChanged: " + value)
    }
}
