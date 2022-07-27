package com.document.pdfscanner.activity


import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.OrganizeMergePDFAdapter
import com.document.pdfscanner.model.BackList
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.Utils
import kotlinx.android.synthetic.main.activity_organize_merge_pdf.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.document.pdfscanner.App
import java.io.File
import java.util.*

class OrganizeMergePDFActivity : BaseActivity() {
    internal var REQUEST_CODE_ADD_FILE = 62
    internal val TAG = OrganizeMergePDFActivity::class.java.simpleName
    private var allPdfDocuments: String? = null
    private var allPdfPictureDir: String? = null
    private var closeInfo: AppCompatImageView? = null
    internal var closeMoreInfo: OnClickListener = OnClickListener {
        info_tap_more_options.visibility = View.GONE
        info_tap_more_options.animate()
            .translationY((-info_tap_more_options.height).toFloat())
            .alpha(0.0f).setListener(object : AnimatorListener {
                override fun onAnimationCancel(animator: Animator) {}

                override fun onAnimationRepeat(animator: Animator) {}

                override fun onAnimationStart(animator: Animator) {}

                override fun onAnimationEnd(animator: Animator) {
                    info_tap_more_options.visibility = View.GONE
                    val edit = sharedPreferences.edit()
                    edit.putBoolean(ORGANIZE_MERGE_PAGES_TIP, false)
                    edit.apply()
                }
            })
    }
    var mPDFFiles: MutableList<File> = ArrayList()
    lateinit var organizePagesAdapter: OrganizeMergePDFAdapter
    private var pdfFilePaths: ArrayList<String>? = null
    lateinit var sharedPreferences: SharedPreferences
//    lateinit var sharedPreferences2: SharedPreferences
    internal var showOrganizePagesTip: Boolean = false

    @SuppressLint("StaticFieldLeak")
    inner class LoadPageThumbnails : AsyncTask<ArrayList<String>, Void, Void>() {

        public override fun onPreExecute() {
            super.onPreExecute()
        }

        public override fun doInBackground(vararg listArr: ArrayList<String>): Void? {
            val list = listArr[0]
            val size = list.size
            for (i in 0 until size) {
                val str = list[i]
                if (!Utils.isThumbnailPresent(this@OrganizeMergePDFActivity, str)) {
                    Utils.generatePDFThumbnail(this@OrganizeMergePDFActivity, str)
                }
                mPDFFiles.add(File(str))
            }
            return null
        }

        @SuppressLint("RestrictedApi")
        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            organizePagesAdapter = OrganizeMergePDFAdapter(
                this@OrganizeMergePDFActivity,
                mPDFFiles
            )
            recycler_view_organize_pages.layoutManager = GridLayoutManager(
                this@OrganizeMergePDFActivity,
                3,
                RecyclerView.VERTICAL,
                false
            )
            progress_bar_organize_pages.visibility = View.GONE
            recycler_view_organize_pages.adapter = organizePagesAdapter
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(15, 0) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    viewHolder2: RecyclerView.ViewHolder
                ): Boolean {
                    val adapterPosition = viewHolder.adapterPosition
                    val adapterPosition2 = viewHolder2.adapterPosition
                    mPDFFiles.add(
                        adapterPosition,
                        mPDFFiles.removeAt(adapterPosition2)
                    )
                    organizePagesAdapter.notifyItemMoved(
                        adapterPosition2,
                        adapterPosition
                    )
                    val str = TAG
                    val sb = StringBuilder()
                    sb.append("moved from ")
                    sb.append(adapterPosition)
                    sb.append(" to position ")
                    sb.append(adapterPosition2)
                    Log.d(str, sb.toString())
                    return true
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                }
            }).attachToRecyclerView(recycler_view_organize_pages)
        }
    }



    var mActionBar: ActionBar? = null
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_organize_merge_pdf)

        val file = Environment.getExternalStorageDirectory().toString()
        val sb = StringBuilder()
        sb.append(file)
        sb.append("/Pictures/PDF/tmp/")
        this.allPdfPictureDir = sb.toString()
        val sb2 = StringBuilder()
        sb2.append(file)
        sb2.append("/Documents/PDF/")
        this.allPdfDocuments = sb2.toString()
        setSupportActionBar(tb_org_merge)
        mActionBar = supportActionBar
        mActionBar!!.setDisplayHomeAsUpEnabled(true)
        mActionBar!!.setTitle(R.string.organize_files)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        showOrganizePagesTip =
            sharedPreferences.getBoolean(ORGANIZE_MERGE_PAGES_TIP, true)

        closeInfo = findViewById<View>(R.id.info_close) as AppCompatImageView
        closeInfo!!.setOnClickListener(this.closeMoreInfo)

        getList()
        if (this.showOrganizePagesTip) {
            info_tap_more_options.visibility = View.VISIBLE
        } else {
            info_tap_more_options.visibility = View.GONE
        }
        LoadPageThumbnails().execute(pdfFilePaths)

    }

    private fun getList() {
        var arr = ArrayList<String>()
        BackList.instance!!.list.forEach {
            if (it.click == true) {
                arr.add(it.itemPDFModel?.path!!)
            }
        }
        pdfFilePaths = arr
    }


    public override fun onDestroy() {
        super.onDestroy()
        Utils.deleteFiles(this.allPdfPictureDir)
        val str = this.TAG
        val sb = StringBuilder()
        sb.append("Deleting temp dir ")
        sb.append(this.allPdfPictureDir)
        Log.d(str, sb.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tool, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun mergePDFFiles(list: List<File>) {
        val arrayList = ArrayList<String>()
        for (absolutePath in list) {
            arrayList.add(absolutePath.absolutePath)
        }
        val builder = AlertDialog.Builder(this)
        val sb = StringBuilder()
        sb.append("Merged")
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
            val obj = editText.text.toString()
            if (Utils.isFileNameValid(obj)) {

                        create.dismiss()
                        startActivity(
                            Intent(this@OrganizeMergePDFActivity, ConvertPdf::class.java).putExtra("key", Action.MERGE_PDF)
                                .putStringArrayListExtra(Action.ACTION_LST, arrayList)
                                .putExtra(Action.N_VALUE, obj)
                        )

            } else {
                editText.error = this@OrganizeMergePDFActivity.getString(R.string.invalid_file_name)
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_check -> {
                organizePagesAdapter.finishActionMode()
                if (organizePagesAdapter.pdFsToMerge.size >= 2) {
                    mergePDFFiles(organizePagesAdapter.pdFsToMerge)
                }else
                    Utils.showDialogSweet(
                        this,
                        SweetAlertDialog.WARNING_TYPE,
                        getString(R.string.at_least_two_files)
                    )
            }

        }
        return true
    }

    companion object {
        var ORGANIZE_MERGE_PAGES_TIP = "prefs_organize_merge_pages"
    }
}
