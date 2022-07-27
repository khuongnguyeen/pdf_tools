package com.document.pdfscanner.scanlibrary

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.scanlibrary.ScanActivity
import com.document.pdfscanner.R
import com.document.pdfscanner.activity.EditViewActivity
import com.document.pdfscanner.ulti.Utils.createContrast
import com.document.pdfscanner.ulti.Utils.getBitmap
import com.document.pdfscanner.ulti.Utils.toGrayscale
import kotlinx.android.synthetic.main.result_layout.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ResultFragment(val list: Array<File>?, val position: Int) : Fragment() {

    private var scannedImageView: ImageView? = null
    private var BWModeB: ImageView? = null
    private var grayModeB: ImageView? = null
    private var magicColorB: ImageView? = null
    private var originalB: ImageView? = null
    private var ani_view: LottieAnimationView? = null

    private var original: Bitmap? = null

    private var transformed: Bitmap? = null
    private var trans1: Bitmap? = null
    private var trans2: Bitmap? = null
    private var trans3: Bitmap? = null
    private var trans4: Bitmap? = null
    private var rotoriginal: Bitmap? = null

    private var view2: View? = null
    private var filter: ConstraintLayout? = null
    private var ibFilter: RadioButton? = null
    var originalButton: Button? = null
    var magicColorButton: Button? = null
    var grayModeButton: Button? = null
    var bwButton: Button? = null
    var rotcButton: ImageButton? = null
    var delButton: ImageButton? = null

    @JvmField
    var deleted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view2 = inflater.inflate(R.layout.result_layout, null)
        init()
        if (list?.size!! > 1 && position == 0) {
            ani_view?.visibility = View.VISIBLE
            try {
                Handler(Looper.myLooper()!!).postDelayed({
                    ani_view?.visibility = View.INVISIBLE
                }, 2000)
            } catch (ex: Exception) {
                print(ex.toString())
            }
        }
        return view2
    }

    private fun init() {
        scannedImageView = view2!!.findViewById(R.id.scannedImage)
        BWModeB = view2!!.findViewById(R.id.BWModeB)
        grayModeB = view2!!.findViewById(R.id.grayModeB)
        originalB = view2!!.findViewById(R.id.originalB)
        magicColorB = view2!!.findViewById(R.id.magicColorB)
        originalButton = view2!!.findViewById(R.id.original)
        ibFilter = view2!!.findViewById(R.id.ib_filter)
        filter = view2!!.findViewById(R.id.filter)
        ani_view = view2!!.findViewById(R.id.ani_view)

        ibFilter!!.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                filter!!.visibility = View.VISIBLE
            } else {
                filter!!.visibility = View.INVISIBLE
            }
        }

        var check = false

        ibFilter!!.setOnClickListener {
            if (check) {
                ibFilter!!.isChecked = false
                check = false
            } else {
                check = true
                ibFilter!!.isChecked = true
            }
        }




        originalB?.setOnClickListener(OriginalButtonClickListener())
        magicColorButton = view2!!.findViewById(R.id.magicColor)
        magicColorB?.setOnClickListener(MagicColorButtonClickListener())
        grayModeButton = view2!!.findViewById(R.id.grayMode)
        grayModeB?.setOnClickListener(GrayButtonClickListener())
        bwButton = view2!!.findViewById(R.id.BWMode)
        BWModeB?.setOnClickListener(BWButtonClickListener())
        rotcButton = view2!!.findViewById(R.id.rotcButton)
        rotcButton?.setOnClickListener(RotButtonClickListener())
        delButton = view2!!.findViewById(R.id.delete)
        delButton?.setOnClickListener(DeleteButtonListener())
        bitmap
    }

    private val bitmap: Unit
        get() {
            val uri = uri
            onReceiveBitmap(getBitmap(activity, uri))
        }

    private val uri: Uri?
        get() = arguments?.getParcelable(ScanConstants.SCANNED_RESULT)

    private fun setScannedImage(scannedImage: Bitmap?) {
        scannedImageView!!.setImageBitmap(scannedImage)
    }

    private inner class BWButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            showProgressDialog(resources.getString(R.string.applying_filter))
            AsyncTask.execute {
                try {
                    transformed = createContrast(rotoriginal!!, 50.0)
//                    transformed = ScanActivity.getBWBitmap(rotoriginal)
                } catch (e: OutOfMemoryError) {
                    activity?.runOnUiThread {
                        transformed = original
                        scannedImageView!!.setImageBitmap(original)
                        e.printStackTrace()
                        dismissDialog()
                        onClick(v)
                    }
                }
                activity?.runOnUiThread {
                    scannedImageView!!.setImageBitmap(transformed)
                    dismissDialog()
                }
            }
        }
    }

    private inner class MagicColorButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            showProgressDialog(resources.getString(R.string.applying_filter))
            AsyncTask.execute {
                try {
                    transformed = ScanActivity.getMagicColorBitmap(rotoriginal)
                } catch (e: OutOfMemoryError) {
                    activity?.runOnUiThread {
                        transformed = original
                        scannedImageView!!.setImageBitmap(original)
                        e.printStackTrace()
                        dismissDialog()
                        onClick(v)
                    }
                }
                activity?.runOnUiThread {
                    scannedImageView!!.setImageBitmap(transformed)
                    dismissDialog()
                }
            }
        }
    }

    private inner class OriginalButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            try {
                showProgressDialog(resources.getString(R.string.applying_filter))
                transformed = rotoriginal
                scannedImageView!!.setImageBitmap(rotoriginal)
                dismissDialog()
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                dismissDialog()
            }
        }
    }

    private inner class GrayButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            showProgressDialog(resources.getString(R.string.applying_filter))
            AsyncTask.execute {
                try {
                    transformed = toGrayscale(rotoriginal!!)
                } catch (e: OutOfMemoryError) {
                    activity?.runOnUiThread {
                        transformed = original
                        scannedImageView!!.setImageBitmap(original)
                        e.printStackTrace()
                        dismissDialog()
                        onClick(v)
                    }
                }
                activity?.runOnUiThread {
                    scannedImageView!!.setImageBitmap(transformed)
                    dismissDialog()
                }
            }
        }
    }

    private inner class RotButtonClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            showProgressDialog(resources.getString(R.string.applying_filter))
            AsyncTask.execute {
                try {
                    val imageViewBitmap = (scannedImageView!!.drawable as BitmapDrawable).bitmap
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    rotoriginal = Bitmap.createBitmap(
                        rotoriginal!!,
                        0,
                        0,
                        rotoriginal!!.width,
                        rotoriginal!!.height,
                        matrix,
                        true
                    )
                    transformed = Bitmap.createBitmap(
                        imageViewBitmap,
                        0,
                        0,
                        imageViewBitmap.width,
                        imageViewBitmap.height,
                        matrix,
                        true
                    )

                    filter()
                } catch (e: OutOfMemoryError) {
                    activity?.runOnUiThread {
                        transformed = original
                        scannedImageView!!.setImageBitmap(original)
                        e.printStackTrace()
                        dismissDialog()
                        onClick(v)
                    }
                }
                activity?.runOnUiThread {
                    scannedImageView!!.setImageBitmap(transformed)
                    dismissDialog()
                }
            }
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
            // noinspection ConstantConditions
            val imageFile = File(uri!!.path)
            if (imageFile.exists()) {
                imageFile.delete()
            }
            (activity as EditViewActivity).delete(this@ResultFragment)
        }
    }

    @Synchronized
    private fun showProgressDialog(message: String?) {
        if (progressDialogFragment != null && progressDialogFragment!!.isVisible) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment!!.dismissAllowingStateLoss()
        }
        progressDialogFragment = null
        progressDialogFragment = ProgressDialogFragment(message!!)
        val fm = childFragmentManager
        progressDialogFragment!!.show(fm, ProgressDialogFragment::class.java.toString())
    }

    /**
     * A function to dismiss the progress dialog.
     */
    @Synchronized
    private fun dismissDialog() {
        progressDialogFragment?.dismiss()
    }

    /**
     * A function to prepare the image for compiling the images into a PDF.
     */
    fun finish() {
        try {
            val file = File(Objects.requireNonNull(arguments?.getString(ScanConstants.SCAN_FILE)))
            val stream = FileOutputStream(file)
            transformed!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            original!!.recycle()
//            transformed!!.recycle()
//            rotoriginal!!.recycle()
//            trans1!!.recycle()
//            trans2!!.recycle()
//            trans3!!.recycle()
//            trans4!!.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * A function to receive the requested bitmap
     * @param bitmap the requested bitmap
     */
    fun onReceiveBitmap(bitmap: Bitmap?) {
        original = bitmap
        transformed = bitmap
        rotoriginal = bitmap
        setScannedImage(bitmap)
        filter()

    }

    fun filter() {
        AsyncTask.execute {
            try {
                trans1 = createContrast(rotoriginal!!, 50.0)
//                trans1 = ScanActivity.getBWBitmap(rotoriginal)
                trans2 = toGrayscale(rotoriginal!!)
                trans3 = ScanActivity.getMagicColorBitmap(rotoriginal)
                trans4 = original
            } catch (e: OutOfMemoryError) {
                activity?.runOnUiThread {
                    trans1 = original
                    trans2 = original
                    trans3 = original
                }
            }
            activity?.runOnUiThread {
                BWModeB!!.setImageBitmap(trans1)
                grayModeB!!.setImageBitmap(trans2)
                magicColorB!!.setImageBitmap(trans3)
                originalB!!.setImageBitmap(rotoriginal)
            }
        }

    }

    companion object {
        /** The Fragment to show loading when any operation is being performed */
        private var progressDialogFragment: ProgressDialogFragment? = null
    }
}