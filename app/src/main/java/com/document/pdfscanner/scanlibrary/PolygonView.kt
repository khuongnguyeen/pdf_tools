package com.document.pdfscanner.scanlibrary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.document.pdfscanner.R
import java.util.*
import kotlin.math.abs

/**
 * The View that is used to mark the corners and allow the user to change them.
 * @author Nalin Angrish, Jhansi.
 */
class PolygonView : FrameLayout {
    /** The context in which the view is made */

    /** The paint of the lines */
    private var paint: Paint? = null

    /** The pointers on the corners */
    private var pointer1: ImageView? = null

    /** The pointers on the corners */
    private var pointer2: ImageView? = null

    /** The pointers on the corners */
    private var pointer3: ImageView? = null

    /** The pointers on the corners */
    private var pointer4: ImageView? = null

    /** The pointers between the corners 1&3 */
    private var midPointer13: ImageView? = null

    /** The pointers between the corners 1&2 */
    private var midPointer12: ImageView? = null

    /** The pointers between the corners 3&4 */
    private var midPointer34: ImageView? = null

    /** The pointers between the corners 2&4 */
    private var midPointer24: ImageView? = null

    /** The polygonView object to show */
    private var polygonView: PolygonView? = null

    /**
     * The Constructor
     * @param context the context in which the view is made
     */
    constructor(context: Context) : super(context) {
        init()
    }

    /**
     * The Constructor
     * @param context the context in which the view is made
     * @param attrs the attributes of the view.
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * The main constructor of the view.
     * It creates the corner pointers, the mid-corner pointers, and other required elements.
     */
    private fun init() {
        polygonView = this
        pointer1 = getImageView(0, 0)
        pointer2 = getImageView(width, 0)
        pointer3 = getImageView(0, height)
        pointer4 = getImageView(width, height)
        midPointer13 = getImageView(0, height / 2)
        midPointer13!!.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer3))
        midPointer12 = getImageView(0, width / 2)
        midPointer12!!.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer2))
        midPointer34 = getImageView(0, height / 2)
        midPointer34!!.setOnTouchListener(MidPointTouchListenerImpl(pointer3, pointer4))
        midPointer24 = getImageView(0, height / 2)
        midPointer24!!.setOnTouchListener(MidPointTouchListenerImpl(pointer2, pointer4))
        addView(pointer1)
        addView(pointer2)
        addView(midPointer13)
        addView(midPointer12)
        addView(midPointer34)
        addView(midPointer24)
        addView(pointer3)
        addView(pointer4)
        initPaint()
    }

    /**
     * A function to set the paint colour to the required value (here blue)
     */
    private fun initPaint() {
        paint = Paint()
        paint!!.color = Color.rgb(255, 0, 73)
        paint!!.strokeWidth = 2f
        paint!!.isAntiAlias = true
    }
    /**
     * A function to get the points of the corners of the polygonView
     * @return a List of Integer-Point pairs
     */
    /**
     * function to set points on the Image (public candidate)
     * @param pointFMap list of Integer-Point pairs for all the points to be shown
     */
    var points: Map<Int, PointF>
        get() {
            val points: MutableList<PointF> = ArrayList()
            points.add(PointF(pointer1!!.x, pointer1!!.y))
            points.add(PointF(pointer2!!.x, pointer2!!.y))
            points.add(PointF(pointer3!!.x, pointer3!!.y))
            points.add(PointF(pointer4!!.x, pointer4!!.y))
            return getOrderedPoints(points)
        }
        set(pointFMap) {
            if (pointFMap.size == 4) {
                setPointsCoordinates(pointFMap)
            }
        }

    /**
     * A function to sort the points
     * @param points the Points to be ordered
     * @return a list of Integer-Point pairs
     */
    fun getOrderedPoints(points: List<PointF>): Map<Int, PointF> {
        val centerPoint = PointF()
        val size = points.size
        for (pointF in points) {
            centerPoint.x += pointF.x / size
            centerPoint.y += pointF.y / size
        }
        val orderedPoints: MutableMap<Int, PointF> = HashMap()
        for (pointF in points) {
            var index = -1
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3
            }
            orderedPoints[index] = pointF
        }
        return orderedPoints
    }

    /**
     * function to set points on the Image
     * @param pointFMap list of Integer-Point pairs for all the points to be shown
     */
    private fun setPointsCoordinates(pointFMap: Map<Int, PointF>) {
        pointer1!!.x = pointFMap[0]!!.x
        pointer1!!.y = pointFMap[0]!!.y
        pointer2!!.x = pointFMap[1]!!.x
        pointer2!!.y = pointFMap[1]!!.y
        pointer3!!.x = pointFMap[2]!!.x
        pointer3!!.y = pointFMap[2]!!.y
        pointer4!!.x = pointFMap[3]!!.x
        pointer4!!.y = pointFMap[3]!!.y
    }

    /**
     * A function to draw lines from one point to the other and place the mid-corner pointers on the correct place.
     * @param canvas the canvas to draw on.
     */
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(
            pointer1!!.x + pointer1!!.width / 2,
            pointer1!!.y + pointer1!!.height / 2,
            pointer3!!.x + pointer3!!.width / 2,
            pointer3!!.y + pointer3!!.height / 2,
            paint!!
        )
        canvas.drawLine(
            pointer1!!.x + pointer1!!.width / 2,
            pointer1!!.y + pointer1!!.height / 2,
            pointer2!!.x + pointer2!!.width / 2,
            pointer2!!.y + pointer2!!.height / 2,
            paint!!
        )
        canvas.drawLine(
            pointer2!!.x + pointer2!!.width / 2,
            pointer2!!.y + pointer2!!.height / 2,
            pointer4!!.x + pointer4!!.width / 2,
            pointer4!!.y + pointer4!!.height / 2,
            paint!!
        )
        canvas.drawLine(
            pointer3!!.x + pointer3!!.width / 2,
            pointer3!!.y + pointer3!!.height / 2,
            pointer4!!.x + pointer4!!.width / 2,
            pointer4!!.y + pointer4!!.height / 2,
            paint!!
        )
        midPointer13!!.x = pointer3!!.x - (pointer3!!.x - pointer1!!.x) / 2
        midPointer13!!.y = pointer3!!.y - (pointer3!!.y - pointer1!!.y) / 2
        midPointer24!!.x = pointer4!!.x - (pointer4!!.x - pointer2!!.x) / 2
        midPointer24!!.y = pointer4!!.y - (pointer4!!.y - pointer2!!.y) / 2
        midPointer34!!.x = pointer4!!.x - (pointer4!!.x - pointer3!!.x) / 2
        midPointer34!!.y = pointer4!!.y - (pointer4!!.y - pointer3!!.y) / 2
        midPointer12!!.x = pointer2!!.x - (pointer2!!.x - pointer1!!.x) / 2
        midPointer12!!.y = pointer2!!.y - (pointer2!!.y - pointer1!!.y) / 2
    }

    /**
     * A function to create an imageView representing a corner at a point
     * @param x the x position of the point to place the imageView at
     * @param y the y position of the point to place the imageView at
     * @return the found ImageView
     */
    private fun getImageView(x: Int, y: Int): ImageView {
        val imageView = ImageView(context)
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = layoutParams
        imageView.setImageResource(R.drawable.circle)
        imageView.x = x.toFloat()
        imageView.y = y.toFloat()
        imageView.setOnTouchListener(TouchListenerImpl())
        return imageView
    }

    /**
     * A function to check whether the shape is valid (i.e., the shape is a quadrilateral and has four corners)
     * @param pointFMap the list of Integer-Point pair of the corners of the shape
     * @return is no. of points = 4 ?
     */
    fun isValidShape(pointFMap: Map<Int, PointF>): Boolean {
        return pointFMap.size == 4
    }

    /**
     * An OnTouchListener to listen the touches on a mid-corner pointer.
     */
    private inner class MidPointTouchListenerImpl internal constructor(
        private val mainPointer1: ImageView?,
        private val mainPointer2: ImageView?
    ) : OnTouchListener {
        var DownPT = PointF() // Record Mouse Position When Pressed Down
        var StartPT = PointF() // Record Start Position of 'img'
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.action
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - DownPT.x, event.y - DownPT.y)
                    if (abs(mainPointer1!!.x - mainPointer2!!.x) > abs(
                            mainPointer1.y - mainPointer2.y
                        )
                    ) {
                        if (mainPointer2.y + mv.y + v.height < polygonView!!.height && mainPointer2.y + mv.y > 0) {
                            v.setX((StartPT.y + mv.y) as Float)
                            StartPT = PointF(v.x, v.y)
                            mainPointer2.setY((mainPointer2.y + mv.y) )
                        }
                        if (mainPointer1.y + mv.y + v.height < polygonView!!.height && mainPointer1.y + mv.y > 0) {
                            v.setX((StartPT.y + mv.y))
                            StartPT = PointF(v.x, v.y)
                            mainPointer1.setY((mainPointer1.y + mv.y))
                        }
                    } else {
                        if (mainPointer2.x + mv.x + v.width < polygonView!!.width && mainPointer2.x + mv.x > 0) {
                            v.setX((StartPT.x + mv.x))
                            StartPT = PointF(v.x, v.y)
                            mainPointer2.setX((mainPointer2.x + mv.x))
                        }
                        if (mainPointer1.x + mv.x + v.width < polygonView!!.width && mainPointer1.x + mv.x > 0) {
                            v.setX((StartPT.x + mv.x))
                            StartPT = PointF(v.x, v.y)
                            mainPointer1.setX((mainPointer1.x + mv.x))
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.x
                    DownPT.y = event.y
                    StartPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    val color: Int = if (isValidShape(points)) {
                        Color.rgb(255, 0, 73)
                    } else {
                        Color.rgb(255, 255, 255)
                    }
                    paint!!.color = color
                }
                else -> {
                }
            }
            polygonView!!.invalidate()
            return true
        }
    }

    /**
     * An OnTouchListener to listen the touches on a corner pointer.
     */
    private inner class TouchListenerImpl : OnTouchListener {
        var DownPT = PointF() // Record Mouse Position When Pressed Down
        var StartPT = PointF() // Record Start Position of 'img'
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.action
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - DownPT.x, event.y - DownPT.y)
                    if (StartPT.x + mv.x + v.width < polygonView!!.width && StartPT.y + mv.y + v.height < polygonView!!.height && StartPT.x + mv.x > 0 && StartPT.y + mv.y > 0) {
                        v.setX((StartPT.x + mv.x))
                        v.setY((StartPT.y + mv.y))
                        StartPT = PointF(v.x, v.y)
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.x
                    DownPT.y = event.y
                    StartPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    color = if (isValidShape(points)) {
                        Color.parseColor("#ED2991")
                    } else {
                        Color.parseColor("#FFFFFF")
                    }
                    paint!!.color = color
                }
                else -> {
                }
            }
            polygonView!!.invalidate()
            return true
        }
    }
}