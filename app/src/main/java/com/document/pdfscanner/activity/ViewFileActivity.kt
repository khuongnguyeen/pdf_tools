package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.document.pdfscanner.App
import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.SelectPDFAdapter
import com.document.pdfscanner.interfaces.ItemSelectClickListener
import com.document.pdfscanner.model.BackList
import com.document.pdfscanner.model.DataListMerge
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.ConstantSPKey
import com.document.pdfscanner.ulti.FileStorage
import com.document.pdfscanner.ulti.Utils
import kotlinx.android.synthetic.main.activity_view_file.*
import kotlinx.android.synthetic.main.activity_view_file.tv_loaddata
import java.io.File

class ViewFileActivity : BaseActivity(), ItemSelectClickListener {
    override fun onSplitClick(itemPDFModel: ItemPDFModel) {}
    override fun onExtractTextClick(itemPDFModel: ItemPDFModel) {
    }

    override fun onExtractImageClick(itemPDFModel: ItemPDFModel) {

    }

    override fun onPDFToImageClick(itemPDFModel: ItemPDFModel) {

    }

    override fun onCompressClick(itemPDFModel: ItemPDFModel) {

    }

    override fun onRemovePageClick(itemPDFModel: ItemPDFModel) {

    }

    override fun onOpenPDF(itemPDFModel: ItemPDFModel) {
                val bundle = Bundle()
                bundle.putSerializable(ConstantSPKey.INFO_PDF, itemPDFModel)
                startActivity(Intent(this@ViewFileActivity, ReadPDFActivity::class.java).putExtras(bundle))

    }

    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private lateinit var nativeAdLayout: FrameLayout
    private var nativeAd: MaxAd? = null
    lateinit var sharedPreferences: SharedPreferences
    var arr = ArrayList<DataListMerge>()
    var action = ""
    var getI = ""
    lateinit var selectPDFAdapter: SelectPDFAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_file)
        getDataIntent()
        setSupportActionBar(tb_view_file)
        val mab = supportActionBar
        mab!!.setDisplayHomeAsUpEnabled(true)
        GetFile().execute()
        sharedPreferences =
            getSharedPreferences(ConstantSPKey.ACTIVITY_SETTING_KEY, Context.MODE_PRIVATE)

        nativeAdLayout = findViewById(R.id.native_ad_layout)

        val adView: View = LayoutInflater.from(this).inflate(R.layout.loading_ad_big, null)
        nativeAdLayout.removeAllViews()
        nativeAdLayout.addView(adView)
        nativeAdLoader = MaxNativeAdLoader(getString(R.string.applovin_small_native_ids), this)

        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }

                // Save ad to be rendered later.
                nativeAd = ad
                showAd()
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {

                nativeAdLayout.removeAllViews()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }
        })

        loadAd()
    }



    fun loadAd() {
        nativeAdLoader.loadAd()
    }

    fun showAd() {
        val adView = createNativeAdView()
        // Render the ad separately
        nativeAdLoader.render(adView, nativeAd)
        nativeAdLayout.removeAllViews()
        nativeAdLayout.addView(adView)
    }

    private fun createNativeAdView(): MaxNativeAdView {
        val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(R.layout.native_small_ad_view)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_textView)
            .setIconImageViewId(R.id.icon_image_view)
            .setOptionsContentViewGroupId(R.id.options_view)
            .setCallToActionButtonId(R.id.cta_button)
            .build()
        return MaxNativeAdView(binder, this)
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

    @SuppressLint("StaticFieldLeak")
    inner class GetFile : AsyncTask<Void, Void, ArrayList<ItemPDFModel>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<ItemPDFModel> {
            val fileStorage = FileStorage()
            val file: File

            val path = intent.getStringExtra(Action.PATH_A_FOLDER)
            file = File(path)
            fileStorage.getListFileByFolder(file).forEach {
                BackList.instance!!.list2.add(DataListMerge(it))
            }
            arr = BackList.instance!!.list2

            return fileStorage.getListFilePDF(file)
        }

        override fun onPostExecute(result: ArrayList<ItemPDFModel>?) {
            onPost(arr)
        }

    }

    private fun onPost(arr: ArrayList<DataListMerge>) {
        if (tv_loaddata.visibility == View.VISIBLE) {
            tv_loaddata.visibility = View.INVISIBLE
        }
        updateRecyclerView(arr)
    }

    fun updateRecyclerView(arr: ArrayList<DataListMerge>) {
        recycler_view_file.setHasFixedSize(true)
        var mColor = 0
        val gridLayout = false
        mColor = 0

        selectPDFAdapter =
            SelectPDFAdapter(this, arr, gridLayout, mColor, action)
        if (!gridLayout) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recycler_view_file.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(this, 3)
            recycler_view_file.layoutManager = layoutManager
        }
        recycler_view_file.adapter = selectPDFAdapter

        selectPDFAdapter.notifyDataSetChanged()
    }

    @SuppressLint("RestrictedApi")
    fun getDataIntent() {
        try {

            action = intent.getStringExtra(Action.ACTION_INTENT).toString()
            getI = intent.getStringExtra(Action.ST_IT).toString()
        } catch (e: Exception) {
            Log.d("exxx", "Error " + e.message)
        }
    }

    override fun onDestroy() {
        BackList.instance!!.list2.clear()
        if (nativeAd != null)
            nativeAdLoader.destroy(nativeAd)
        nativeAdLoader.destroy()
        super.onDestroy()
    }
}
