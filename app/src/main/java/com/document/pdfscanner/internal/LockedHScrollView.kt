package com.document.pdfscanner.internal

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class LockedHScrollView : HorizontalScrollView {
    var x1 = 0f
    var x2 = 0f

    private val distance = 200f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun init() {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    fun scrollLeft() {
        val x = resources.displayMetrics.widthPixels
        scrollBy(-x, 0) // scroll by the amount of the screen's width
    }

    fun scrollRight() {
        val x = resources.displayMetrics.widthPixels
        scrollBy(x, 0) // scroll by the amount of the screen's width
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            x1 = event.x
        } else if (event.action == MotionEvent.ACTION_UP) {
            x2 = event.x
            if (x2 - x1 > 0) {             //Right swipe
                if (x2 - x1 >= distance) {        // Validate that the swipe was long enough
                    scrollLeft()
                }
            } else if (x1 - x2 > 0) {        // Left swipe
                if (x1 - x2 >= distance) {        // Validate that the swipe was long enough
                    scrollRight()
                }
            }
        }
        return true
    }
}