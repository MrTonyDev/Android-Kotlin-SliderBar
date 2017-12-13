package android.tony.tony.sliderbar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sliderbar.minDuration = 0.1f//from 0f -> 1f
        sliderbar.maxDuration = 0.5f
    }
}
