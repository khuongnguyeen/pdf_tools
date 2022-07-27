package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.document.pdfscanner.App
import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.RecyclerViewAdapter
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.FileStorage
import io.me.ndk.adsconfig.LovinInterstitialAds
import kotlinx.android.synthetic.main.activity_recent_file.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RecentFileActivity : AppCompatActivity() {

    internal var allFilesAdapter: RecyclerViewAdapter? = null

    val data = mutableListOf<Any>()
    lateinit var lovinInterstitialAds: LovinInterstitialAds

    private fun updateRecyclerView() {
        allFilesAdapter = RecyclerViewAdapter(this, data,lovinInterstitialAds)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_recentfiles.layoutManager = layoutManager
        recycler_recentfiles.adapter = allFilesAdapter
        recycler_recentfiles.isNestedScrollingEnabled = false
        allFilesAdapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_file)
        lovinInterstitialAds = LovinInterstitialAds(this)
        setSupportActionBar(toolbar_organize_pages)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        updateRecyclerView()

    }

    override fun onResume() {
        super.onResume()
        GetFile().execute()
    }





    @SuppressLint("StaticFieldLeak")
    inner class GetFile : AsyncTask<Void, Void, ArrayList<ItemPDFModel>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<ItemPDFModel> {

            var dataItem = mutableListOf<ItemPDFModel>()
            val fileStorage = FileStorage()
            val file = File(Environment.getExternalStorageDirectory().toString())
            if (Build.VERSION.SDK_INT >= 29) {
                dataItem = fileStorage.allPdfs(this@RecentFileActivity)
            } else {
                dataItem =  fileStorage.getList(file)
            }
            return dataItem
        }

        override fun onPostExecute(result: ArrayList<ItemPDFModel>?) {
            onPost(result)
        }

    }


    private fun onPost(arr: ArrayList<ItemPDFModel>?) {
        updateRecyclerView(arr!!)
    }



    fun updateRecyclerView(arr: ArrayList<ItemPDFModel>) {
        data.clear()
        data.addAll(arr)
        if (allFilesAdapter!= null) allFilesAdapter!!.notifyDataSetChanged()
    }




}