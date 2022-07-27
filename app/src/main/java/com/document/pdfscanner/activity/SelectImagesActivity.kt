package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.SelectImagesAdapter
import com.document.pdfscanner.ulti.*
import kotlinx.android.synthetic.main.activity_select_images.*
import cn.pedant.SweetAlert.SweetAlertDialog
import java.io.File

import java.util.ArrayList

class SelectImagesActivity : BaseActivity(), SelectImagesAdapter.OnImageSelectedListener {
    lateinit var sharedPreferences: SharedPreferences
    internal var selectedListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>) {}

            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, j: Long) {

                when (i) {
                    0 -> {
                        LoadImages("/").execute(*arrayOfNulls(0))
                        return
                    }
                    1 -> {
                        LoadImages("/DCIM/").execute(*arrayOfNulls(0))
                        return
                    }
                    2 -> {
                        LoadImages("/Download/").execute(*arrayOfNulls(0))
                        return
                    }
                    3 -> {
                        LoadImages("/Pictures/").execute(*arrayOfNulls(0))
                        return
                    }
                    else -> return
                }
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

    @SuppressLint("StaticFieldLeak")
    inner class LoadImages(private val imageDir: String) : AsyncTask<Void, Void, Void>() {
        private var adapter: SelectImagesAdapter? = null

        public override fun onPreExecute() {
            super.onPreExecute()
            progress_bar_select_images.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val sb = StringBuilder()
            sb.append(Environment.getExternalStorageDirectory())
            sb.append(this.imageDir)
            val list = ArrayList<Uri>()
            val fileStorage = FileStorage()
            val file = File(sb.toString())
            fileStorage.getListFileIMG(file).forEach {
                list.add(Uri.parse(it.path))
            }


            if (list.size == 0){
                runOnUiThread {
                    SweetAlertDialog(this@SelectImagesActivity, SweetAlertDialog.WARNING_TYPE).apply {
                        contentText = getString(R.string.no_img)
                        confirmText = "Ok"
                        setConfirmClickListener {
                            it.cancel()
                        }
                            .show()
                    }
                }
            }


            adapter = SelectImagesAdapter(this@SelectImagesActivity, list)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            progress_bar_select_images.visibility = View.GONE
            recycler_view_select_images.layoutManager = GridLayoutManager(
                this@SelectImagesActivity,
                3,
                RecyclerView.VERTICAL,
                false
            )
            recycler_view_select_images.adapter = this.adapter
        }
    }

    @SuppressLint("ResourceType")
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_select_images)
        tb_select_image!!.title = ""
        tb_select_image!!.setNavigationOnClickListener { onBackPressed() }
        spinner_img_directories.setSelection(0)
        spinner_img_directories.onItemSelectedListener = this.selectedListener
    }


    companion object {
        val TAG = "SelectImagesActivity"
    }

    override fun onMultiSelectedPDF(arrayList: ArrayList<String>?) {
        val intent = Intent(this, OrganizeImagesActivity::class.java)
        intent.putStringArrayListExtra(Action.IMAGE_URIS, arrayList)
        startActivity(intent)

    }

}