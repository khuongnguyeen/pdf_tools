package com.document.pdfscanner.scanlibrary

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.document.pdfscanner.R
import com.document.pdfscanner.activity.ScannerActivity
import com.document.pdfscanner.ulti.Utils.getBitmap
import com.scanlibrary.ScanActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ScanFragment : Fragment() {
    private var scanButton: ImageView? = null
    private var sourceImageView: ImageView? = null
    private var deleteView: ImageView? = null
    private var sourceFrame: FrameLayout? = null
    private var polygonView: PolygonView? = null
    private var view2: View? = null
    private var progressDialogFragment: ProgressDialogFragment? = null
    private var original: Bitmap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view2 = inflater.inflate(R.layout.scan_fragment_layout, null)
        init()
        return view2
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        sourceImageView = view2!!.findViewById(R.id.sourceImageView)
        scanButton = view2!!.findViewById(R.id.scanButton)
        scanButton!!.setOnClickListener(ScanButtonClickListener())
        sourceFrame = view2!!.findViewById(R.id.sourceFrame)
        polygonView = view2!!.findViewById(R.id.polygonView)
        deleteView = view2!!.findViewById(R.id.delete_2)
        deleteView!!.setOnClickListener (DeleteButtonListener())
        sourceFrame!!.post {
            bitmap
        }
    }

    private inner class DeleteButtonListener : View.OnClickListener {
        override fun onClick(v: View) {
            AlertDialog.Builder(activity!!)
                .setTitle("Delete?")
                .setMessage("Are you sure you want to delete the image?")
                .setPositiveButton("Delete") { _, _ -> delete() }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setIcon(R.drawable.ic_bin_button)
                .show()
        }

        private fun delete() {
            val imageFile = File(uri!!.path)
            if (imageFile.exists()) {
                imageFile.delete()
            }
            (activity as ScannerActivity).onScanFinish()
            (activity as ScannerActivity).loadCount()
            (activity as ScannerActivity).loadLastImage()

        }
    }

    private val bitmap: Unit
        get() {
            val uri = uri
            val img = getBitmap(activity, uri)
            onReceiveBitmap(img)
        }
    private val uri: Uri?
        get() = arguments?.getParcelable(ScanConstants.SELECTED_BITMAP)

    private fun setBitmap(original: Bitmap) {
        val scaledBitmap = scaledBitmap(original, sourceFrame!!.width, sourceFrame!!.height)
        sourceImageView!!.setImageBitmap(scaledBitmap)
        val tempBitmap = (sourceImageView!!.drawable as BitmapDrawable).bitmap
        val pointFs = getEdgePoints(tempBitmap)
        polygonView!!.points = pointFs
        polygonView!!.visibility = View.VISIBLE
        val padding = resources.getDimension(R.dimen.scanPadding).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            tempBitmap.width + 2 * padding,
            tempBitmap.height + 2 * padding
        )
        layoutParams.gravity = Gravity.CENTER
        polygonView!!.layoutParams = layoutParams
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        val pointFs = getContourEdgePoints(tempBitmap)
        return orderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        val points = ScanActivity.getPoints(tempBitmap)
        val x1 = points[0]
        val x2 = points[1]
        val x3 = points[2]
        val x4 = points[3]
        val y1 = points[4]
        val y2 = points[5]
        val y3 = points[6]
        val y4 = points[7]
        val pointFs: MutableList<PointF> = ArrayList()
        pointFs.add(PointF(x1, y1))
        pointFs.add(PointF(x2, y2))
        pointFs.add(PointF(x3, y3))
        pointFs.add(PointF(x4, y4))
        return pointFs
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        val outlinePoints: MutableMap<Int, PointF> = HashMap()
        outlinePoints[0] = PointF(0f, 0f)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0f)
        outlinePoints[2] = PointF(0f, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(
            tempBitmap.width.toFloat(), tempBitmap.height.toFloat()
        )
        return outlinePoints
    }

    private fun orderedValidEdgePoints(
        tempBitmap: Bitmap,
        pointFs: List<PointF>
    ): Map<Int, PointF> {
        var orderedPoints = polygonView!!.getOrderedPoints(pointFs)
        if (!polygonView!!.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    private inner class ScanButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val points = polygonView!!.points
            if (isScanPointsValid(points)) {
                ScanAsyncTask(points).execute()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun showErrorDialog() {
        val fragment =
            SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true)
        val fm = activity?.fragmentManager
        fragment.show(fm, SingleButtonDialogFragment::class.java.toString())
    }

    private fun isScanPointsValid(points: Map<Int, PointF>): Boolean {
        return points.size == 4
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()),
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun getScannedBitmap(original: Bitmap?, points: Map<Int, PointF>): Bitmap {
        val xRatio = original!!.width.toFloat() / sourceImageView!!.width
        val yRatio = original.height.toFloat() / sourceImageView!!.height
        val x1 = points[0]!!.x * xRatio
        val x2 = points[1]!!.x * xRatio
        val x3 = points[2]!!.x * xRatio
        val x4 = points[3]!!.x * xRatio
        val y1 = points[0]!!.y * yRatio
        val y2 = points[1]!!.y * yRatio
        val y3 = points[2]!!.y * yRatio
        val y4 = points[3]!!.y * yRatio
        return ScanActivity.getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4)
    }

    private inner class ScanAsyncTask internal constructor(
        private val points: Map<Int, PointF>
    ) : AsyncTask<Void?, Void?, Bitmap?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(getString(R.string.scanning))
        }

        override fun doInBackground(vararg param: Void?): Bitmap {
            val bitmap = getScannedBitmap(original, points)
            try {
                val s: OutputStream = FileOutputStream(
                    File(
                        Objects.requireNonNull(arguments?.getString(ScanConstants.SCAN_FILE))
                    )
                )
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, s)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            bitmap?.recycle()
            (activity as ScannerActivity).onScanFinish()
            dismissDialog()
        }
    }

    private fun showProgressDialog(message: String?) {
        progressDialogFragment = ProgressDialogFragment(message!!)
        progressDialogFragment!!.show(childFragmentManager, ProgressDialogFragment::class.java.toString())
    }

    private fun dismissDialog() {
        try {
            progressDialogFragment?.dismissAllowingStateLoss()
        }catch (ex:Exception){

        }
    }

    private fun onReceiveBitmap(bitmap: Bitmap?) {
        original = bitmap
        if (original != null) {
            setBitmap(original!!)
        }
    }
}