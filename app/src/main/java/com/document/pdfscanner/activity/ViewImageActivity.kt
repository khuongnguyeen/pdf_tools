package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager

import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.ViewImageAdapter
import com.document.pdfscanner.model.ItemIMG
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.FileStorage
import com.document.pdfscanner.ulti.Utils
import kotlinx.android.synthetic.main.activity_view_image.*
import cn.pedant.SweetAlert.SweetAlertDialog
import java.io.File
import java.lang.Exception

class ViewImageActivity : BaseActivity() {
    lateinit var viewImageAdapter: ViewImageAdapter
    lateinit var sharedPreferences: SharedPreferences
    lateinit var mList: ArrayList<ItemIMG>
    var action = ""
    var uri = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        setSupportActionBar(tb_view_image)
        val mActionBar = supportActionBar
        mActionBar!!.setDisplayHomeAsUpEnabled(true)
        mActionBar.setTitle(R.string.view_image)
        getDataAction()
        GetFile().execute()
    }

    private fun getDataAction() {
        try {
            action = intent.getStringExtra(Action.ACTION_INTENT).toString()
            if (action == Action.PDF_TO_IMG) {
                uri = intent.getStringExtra(Action.IMG_FOLDER).toString()
            }
        } catch (e: Exception) {
            Utils.showDialogSweet(
                this,

                SweetAlertDialog.ERROR_TYPE,
                "Error Exception"
            )
        }
    }


    private fun initRecyclerView() {
        rv_view_image.setHasFixedSize(true)

        viewImageAdapter =
            ViewImageAdapter(this, mList)
        val layoutManager = GridLayoutManager(this, 1)
        rv_view_image.layoutManager = layoutManager

        rv_view_image.adapter = viewImageAdapter

        viewImageAdapter.notifyDataSetChanged()

    }

    @SuppressLint("StaticFieldLeak")
    inner class GetFile : AsyncTask<Void, Void, ArrayList<ItemIMG>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<ItemIMG> {
            return if (action == Action.PDF_TO_IMG) {
                val fileStorage = FileStorage()
                val file = File(uri)
                mList = fileStorage.getListFileIMG(file)
                mList
            } else {
                val fileStorage = FileStorage()
                val file = File(Environment.getExternalStorageDirectory().toString() + "/Pictures/")
                mList = fileStorage.getListFileIMG(file)
                mList
            }
        }

        override fun onPostExecute(result: ArrayList<ItemIMG>?) {
            initRecyclerView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.mn_home -> {
                startActivity(
                    Intent(
                        this,
                        HomeActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        }
        return true
    }

}
