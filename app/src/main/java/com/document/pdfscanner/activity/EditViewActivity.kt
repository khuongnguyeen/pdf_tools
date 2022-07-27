package com.document.pdfscanner.activity

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.document.pdfscanner.App
import com.document.pdfscanner.R
import com.document.pdfscanner.internal.LockedHScrollView
import com.document.pdfscanner.internal.Statics
import com.document.pdfscanner.scanlibrary.ProgressDialogFragment
import com.document.pdfscanner.scanlibrary.ResultFragment
import com.document.pdfscanner.scanlibrary.ScanConstants
import com.document.pdfscanner.ulti.Utils
import java.io.File
import java.util.*


class EditViewActivity : AppCompatActivity(), View.OnClickListener {
    var finishB: ImageView? = null
    var dir: File? = null
    var scrollzone: LockedHScrollView? = null
    var fragList: MutableList<ResultFragment> = ArrayList()
    var progressDialogFragment: ProgressDialogFragment? = ProgressDialogFragment("Creating PDF..")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_view)
        main()
    }

    fun main() {
        finishB = findViewById(R.id.finishB)
        scrollzone = findViewById(R.id.scrollzone)

        finishB?.setOnClickListener(this)
        val sessionDir = application.getSharedPreferences("PDFtools", MODE_PRIVATE)
            .getString("sessionName", "hello")
        dir = File(filesDir, sessionDir)
        val manager = supportFragmentManager
        Thread {
            for (i in dir!!.listFiles().indices) {
                Log.e("khuong___", "___________________________________${dir!!.listFiles().size}")
                val transaction = manager.beginTransaction()
                val imageFile = dir!!.listFiles()[i]
                val args = Bundle()
                args.putParcelable(ScanConstants.SCANNED_RESULT, Uri.fromFile(imageFile))
                args.putString(ScanConstants.SCAN_FILE, imageFile.absolutePath)
                val result = ResultFragment(dir!!.listFiles(), i)
                result.arguments = args
                val l = LinearLayout(this@EditViewActivity)
                l.id = View.generateViewId()
                transaction.add(l.id, result, "result-$i")
                val param = LinearLayout.LayoutParams(
                    resources.displayMetrics.widthPixels,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                l.layoutParams = param
                (findViewById<View>(R.id.viewList) as LinearLayout).addView(l, i)
                findViewById<View>(R.id.viewList).invalidate()
                fragList.add(result)
                transaction.commit()
            }
        }.start()
        val animator = ObjectAnimator.ofInt(scrollzone, "scrollX", dir!!.listFiles().size)
        animator.duration = 2000
        animator.start()
    }

    private fun askName() {
        var s = ""
        val builder = AlertDialog.Builder(this)
        val sb = StringBuilder()
        sb.append("Camera_scanner_PDF_")
        sb.append(System.currentTimeMillis())
        val sb2 = sb.toString()
        val f = resources.displayMetrics.density
        val editText = EditText(this)
        editText.setText(sb2)
        editText.setSelectAllOnFocus(true)
        builder.setTitle(R.string.enter_file_name)
            .setPositiveButton(R.string.ok, null as DialogInterface.OnClickListener?)
            .setNegativeButton(R.string.cancel, null as DialogInterface.OnClickListener?)
        val create = builder.create()
        val i = (24.0f * f).toInt()
        create.setView(editText, i, (8.0f * f).toInt(), i, (f * 5.0f).toInt())
        create.show()
        create.getButton(-1).setOnClickListener {
            showProgressDialog("Converting to PDF...")
            Thread {
                var shouldClose = true
                try {
                    if (Statics.isAvailable(editText.text.toString())) {
                        for (frag in fragList) {
                            if (!frag.deleted) {
                                frag.finish()
                            }
                        }
                        s = Statics.createPdf(application, editText.text.toString())
                    } else {
                        runOnUiThread {
                            Utils.showDialogSweet(
                                this,
                                SweetAlertDialog.ERROR_TYPE,
                                "A PDF with this name already exists. Please try again with a different name."
                            )
                        }
                        shouldClose = false
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Utils.showDialogSweet(
                            this,
                            SweetAlertDialog.ERROR_TYPE,
                            "Couldn't create PDF, Please try again"
                        )
                    }
                    e.printStackTrace()
                    shouldClose = false
                }
                dismissDialog()
                if (shouldClose) {
                    runOnUiThread {
                        create.dismiss()

                        val i = Intent(
                            this@EditViewActivity,
                            DoneCameraPdfActivity::class.java
                        ).putExtra("DoneCameraPdfActivity", "$s")
                        startActivity(i)
                        finish()

                    }
                }
            }.start()
        }
    }

    override fun onClick(v: View) {
        if (v == finishB) {
            askName()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun delete(fragment: Fragment) {
        val index = fragList.indexOf(fragment)
        val transaction = supportFragmentManager.beginTransaction()
        (findViewById<View>(R.id.viewList) as LinearLayout).removeViewAt(index)
        transaction.remove(fragment)
        fragList.remove(fragment)
        transaction.commit()
        if (fragList.size == 0) { //all images have been deleted
            onBackPressed()
        }
    }


    @Synchronized
    fun showProgressDialog(message: String?) {
        if (progressDialogFragment != null && progressDialogFragment!!.isVisible) {
            progressDialogFragment?.dismiss()
        }
        progressDialogFragment = null
        progressDialogFragment = ProgressDialogFragment(message!!)
        val fm = supportFragmentManager
        progressDialogFragment!!.show(fm, ProgressDialogFragment::class.java.toString())
    }

    @Synchronized
    fun dismissDialog() {
        progressDialogFragment?.dismiss()
    }
}