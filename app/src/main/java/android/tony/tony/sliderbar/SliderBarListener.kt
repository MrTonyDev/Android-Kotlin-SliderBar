package android.tony.tony.sliderbar

interface SliderBarListener {
    fun onLeftChanged(value: Float)
    fun onRightChanged(value: Float)
    fun onBothLeftAndRightChanged(left: Float, right: Float)
    fun onCurrentLineChanged(value: Float, fromUser: Boolean)
}