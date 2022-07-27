package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.SelectPDFAdapter
import com.document.pdfscanner.interfaces.ItemSelectClickListener
import com.document.pdfscanner.model.BackList
import com.document.pdfscanner.model.DataListMerge
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.*
import com.document.pdfscanner.ulti.Utils.showDialogSweet
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_organize_images.*
import kotlinx.android.synthetic.main.activity_select_pdf.*
import org.jetbrains.anko.runOnUiThread
import cn.pedant.SweetAlert.SweetAlertDialog
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import java.io.File
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

class SelectPDFActivity : BaseActivity(), ItemSelectClickListener {
    override fun onOpenPDF(itemPDFModel: ItemPDFModel) {}

    override fun onSplitClick(itemPDFModel: ItemPDFModel) {
        dialogSplit(itemPDFModel)
    }

    override fun onExtractTextClick(itemPDFModel: ItemPDFModel) {
    }

    override fun onExtractImageClick(itemPDFModel: ItemPDFModel) {
        showDialogExtractImage(itemPDFModel)
    }

    override fun onPDFToImageClick(itemPDFModel: ItemPDFModel) {
        startActivity(
            Intent(
                this,
                ConvertPdf::class.java
            ).putExtra("path", itemPDFModel.path).putExtra("key", ConstantSPKey.KEY_PDF_TO_IMAGE)
        )
    }

    override fun onCompressClick(itemPDFModel: ItemPDFModel) {
        showDialogCompressPDF(itemPDFModel)
    }

    override fun onRemovePageClick(itemPDFModel: ItemPDFModel) {
    }

    private fun showDialogCompressPDF(itemPDFModel: ItemPDFModel) {
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setTitle(getString(R.string.compress_lv))
        // add a radio button list
        val mode = arrayOf(
            "Low",
            "Medium",
            "Hight"
        )
        var checkedItem = 2
        var value = 20
        builder.setSingleChoiceItems(mode, checkedItem) { _, which ->
            when (which) {
                0 -> {
                    checkedItem = 0
                    value = 70
                }
                1 -> {
                    checkedItem = 1
                    value = 50
                }
                2 -> {
                    checkedItem = 2
                    value = 20
                }

            }

        }
        builder.setPositiveButton(
            getString(R.string.compress)
        ) { _, _ ->
            startActivity(
                Intent(
                    this@SelectPDFActivity,
                    ConvertPdf::class.java
                ).putExtra("path", itemPDFModel.path).putExtra(
                    "key",
                    Action.COMPRESS_PDF
                ).putExtra(Action.DL_CP_VALUE, value.toString())
                    .putExtra("value", value.toString())
            )
        }
        builder.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        val dialog = builder.create()// create and show the alert dialog
        val view = (dialog).window
        view?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun showDialogExtractImage(itemPDFModel: ItemPDFModel) {
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setTitle(getString(R.string.image_quality))
        val mode = arrayOf(
            "Low",
            "Medium",
            "Hight"
        )
        var checkedItem = 1
        var value = 65
        builder.setSingleChoiceItems(mode, checkedItem) { _, which ->
            when (which) {
                0 -> {
                    checkedItem = 0
                    value = 30
                }
                1 -> {
                    checkedItem = 1
                    value = 65
                }
                2 -> {
                    checkedItem = 2
                    value = 100
                }
            }
        }
        builder.setPositiveButton(getString(R.string.extract)) { _, _ ->
            startActivity(
                Intent(
                    this@SelectPDFActivity,
                    ConvertPdf::class.java
                ).putExtra("path", itemPDFModel.path).putExtra("key", Action.EXTRACT_IMG)
                    .putExtra("value", value.toString())
            )
        }
        builder.setNegativeButton(getString(R.string.cancel)) { _, _ -> }


        val dialog = builder.create()
        dialog.setOnShowListener {
            val view = (dialog).window
            view?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialog.show()
    }

    private fun dialogSplit(itemPDFModel: ItemPDFModel) {
        var nb = 0
        try {

            val myDocument = PDDocument.load(File(itemPDFModel.path))
            nb = myDocument.numberOfPages
        } catch (e: Exception) {
            Log.d("ConvertPdfTo", "Error " + e.message)
        }
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setTitle(
            Html.fromHtml("<font color='#000000'>Split PDF</font>")
        )

        val layoutInflater = LayoutInflater.from(this).inflate(R.layout.dialog_split, null)

        val spn = layoutInflater.findViewById<AppCompatSpinner>(R.id.spn)
        val edAt = layoutInflater.findViewById<EditText>(R.id.at)
        val edFrom = layoutInflater.findViewById<EditText>(R.id.from)
        val edTo = layoutInflater.findViewById<EditText>(R.id.to)
        val tvNumPage = layoutInflater.findViewById<TextView>(R.id.tvNumberPagesDialog)
        val sb = StringBuilder()
        sb.append(getString(R.string.tv_number_of_page))
        sb.append(" ")
        sb.append(nb)
        tvNumPage.text = sb.toString()
        builder.setView(layoutInflater)
        var index = 0
        spn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        index = 0
                        edAt.visibility = View.INVISIBLE
                        edFrom.visibility = View.INVISIBLE
                        edTo.visibility = View.INVISIBLE
                    }
                    1 -> {
                        index = 1
                        edAt.visibility = View.VISIBLE
                        edFrom.visibility = View.INVISIBLE
                        edTo.visibility = View.INVISIBLE
                    }
                    2 -> {
                        index = 2
                        edAt.visibility = View.INVISIBLE
                        edFrom.visibility = View.VISIBLE
                        edTo.visibility = View.VISIBLE
                    }
                }
            }
        }
        builder.setNegativeButton("CANCEL", null)
        builder.setPositiveButton("OK", null)
        val dialog: AlertDialog = builder.create()

        dialog.setOnShowListener(object : DialogInterface.OnShowListener {
            override fun onShow(dl: DialogInterface?) {
                val btnPB: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val view = (dialog).window

                view?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                btnPB.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        Log.d("qeeeee", "sss")
                        when (index) {
                            0 -> {
                                startActivity(
                                    Intent(
                                        this@SelectPDFActivity,
                                        ConvertPdf::class.java
                                    ).putExtra("path", itemPDFModel.path).putExtra(
                                        "key",
                                        ConstantSPKey.KEY_SPLIT
                                    ).putExtra(Action.ACTION_INTENT, Action.DL_ALL)
                                )
                                dialog.cancel()
                            }
                            1 -> {
                                val vlAt = edAt.text.toString().toInt()
                                if (vlAt <= 0 || vlAt > nb) {
                                    edAt.error = getString(R.string.invalid_value)
                                } else {
                                    startActivity(
                                        Intent(
                                            this@SelectPDFActivity,
                                            ConvertPdf::class.java
                                        ).putExtra("path", itemPDFModel.path).putExtra(
                                            "key",
                                            ConstantSPKey.KEY_SPLIT
                                        ).putExtra(
                                            Action.ACTION_INTENT,
                                            Action.DL_AT
                                        ).putExtra(Action.DL_VALUE_AT, vlAt.toString())
                                    )
                                    dialog.cancel()
                                }
                            }
                            2 -> {
                                try {
                                    val vlFrom = edFrom.text.toString().toInt()
                                    val vlTo = edTo.text.toString().toInt()
                                    if (vlFrom in 1..nb) {
                                        if (vlTo <= 0 || vlTo > nb || vlTo <= vlFrom) {
                                            edTo.error = getString(R.string.invalid_value)
                                            return
                                        } else {
                                            startActivity(
                                                Intent(
                                                    this@SelectPDFActivity,
                                                    ConvertPdf::class.java
                                                ).putExtra("path", itemPDFModel.path)
                                                    .putExtra("key", ConstantSPKey.KEY_SPLIT)
                                                    .putExtra(
                                                        Action.ACTION_INTENT,
                                                        Action.DL_FROM_TO
                                                    )
                                                    .putExtra(
                                                        Action.DL_VALUE_FROM,
                                                        vlFrom.toString()
                                                    )
                                                    .putExtra(Action.DL_VALUE_TO, vlTo.toString())
                                            )
                                            dialog.cancel()
                                        }
                                        return
                                    }
                                    edFrom.error = getString(R.string.invalid_value)
                                    return
                                } catch (e: Exception) {
                                    Log.e("exxxx", "Error " + e.message)
                                }
                            }
                        }
                    }
                })
            }
        })
        dialog.show()

        val w = this.window.decorView.width - 350
        dialog.window?.setLayout(w, w)
    }

    var Isreset = false
    lateinit var sharedPreferences: SharedPreferences
    var start = 0L
    var mColor = 0
    var gridLayout = false
    var action = ""
    private var mFoodList = mutableListOf<DataListMerge>()
    private var mSearchList = mutableListOf<DataListMerge>()
    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private lateinit var nativeAdLayout: FrameLayout
    private var nativeAd: MaxAd? = null
    var selectPDFAdapter: SelectPDFAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_pdf)
        start = System.currentTimeMillis()
        getDataIntent()
        sharedPreferences = getSharedPreferences(
            ConstantSPKey.ACTIVITY_SETTING_KEY,
            Context.MODE_PRIVATE
        )
        tb_select_pdf.setTitle(R.string.select_file)
        tb_select_pdf.setNavigationOnClickListener {
            onBackPressed()
        }
        imageButton!!.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (!b) {
                val layoutManager = LinearLayoutManager(applicationContext)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recycler_view_select_file.layoutManager = layoutManager
            } else {
                val layoutManager = GridLayoutManager(applicationContext, 2)
                recycler_view_select_file.layoutManager = layoutManager
            }
            selectPDFAdapter!!.notifyDataSetChanged()
        }

        recycler_view_select_file.isNestedScrollingEnabled = false

        imageButton.setOnClickListener {
            imageButton!!.isChecked = !gridLayout
            gridLayout = !gridLayout
        }
        setSupportActionBar(tb_select_pdf)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        GetFile().execute()


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

    @SuppressLint("StaticFieldLeak")
    inner class GetFile : AsyncTask<Void, Void, ArrayList<DataListMerge>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<DataListMerge> {
            val fileStorage = FileStorage()
            val file = File(Environment.getExternalStorageDirectory().toString())
            if (Build.VERSION.SDK_INT >= 29) {
                fileStorage.allPdfs(this@SelectPDFActivity).forEach {
                    BackList.instance!!.list.add(DataListMerge(it))
                }
            } else {
                fileStorage.getList(file).forEach {
                    BackList.instance!!.list.add(DataListMerge(it))
                }
            }
            return BackList.instance!!.list
        }

        override fun onPostExecute(result: ArrayList<DataListMerge>?) {
            onPost(result)
        }

    }


    private fun onPost(arr: ArrayList<DataListMerge>?) {
        if (tv_loaddataSL.visibility == View.VISIBLE) {
            tv_loaddataSL.visibility = View.INVISIBLE
        }
        if (arr?.size==0) {
            runOnUiThread {
                SweetAlertDialog(this@SelectPDFActivity, SweetAlertDialog.WARNING_TYPE).apply {
                    setCanceledOnTouchOutside(false)
                    contentText = getString(R.string.no_file)
                    confirmText = "Ok"
                    setConfirmClickListener {
                        it.cancel()
                        finish()
                    }
                        .show()
                }
            }
        }
        updateRecyclerView(arr!!)
    }

    override fun onResume() {
        super.onResume()
        checkFile().execute()
        val editText = findViewById<EditText>(R.id.editTextSearch)
        editText.addTextChangedListener(textWatcher)
        editText.addTextChangedListener(textWatcher)

    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {
            search(s.toString().toLowerCase(Locale.ROOT))
        }
    }

    private fun search(searchTerm: String) {
        mSearchList.clear()
        for (item in mFoodList) {
            if (item.itemPDFModel!!.name!!.toLowerCase(Locale.ROOT).contains(searchTerm)) {
                mSearchList.add(item)
            }
        }
        if(selectPDFAdapter != null) selectPDFAdapter!!.notifyDataSetChanged()
    }

    inner class checkFile : AsyncTask<Void, Void, ArrayList<DataListMerge>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<DataListMerge> {
            val fileStorage = FileStorage()
            val file = File(Environment.getExternalStorageDirectory().toString())
            val lst = ArrayList<DataListMerge>()
            if (Build.VERSION.SDK_INT > 29) {
                fileStorage.allPdfs(this@SelectPDFActivity).forEach {
                    lst.add(DataListMerge(it))
                }
            } else {
                fileStorage.getList(file).forEach {
                    lst.add(DataListMerge(it))
                }
            }
            return BackList.instance!!.list
        }

        override fun onPostExecute(result: ArrayList<DataListMerge>?) {
            if (result!!.size != BackList.instance!!.list.size) {
                BackList.instance!!.list = result
                updateRecyclerView(result)
            }
        }
    }

    fun updateRecyclerView(arr: ArrayList<DataListMerge>) {

        recycler_view_select_file.setHasFixedSize(true)
        mColor = 0
        mSearchList.addAll(arr)
        mFoodList.addAll(arr)

        if (mSearchList.size>0){
            mSearchList.sortWith { row1, row2 ->
                if (File(row1.itemPDFModel!!.path!!).lastModified() < File(row2.itemPDFModel!!.path!!).lastModified()) {
                    return@sortWith 1
                }
                if (File(row1.itemPDFModel!!.path!!).lastModified() == File(row2.itemPDFModel!!.path!!).lastModified()) return@sortWith 0
                -1
            }
        }
        selectPDFAdapter = SelectPDFAdapter(this, mSearchList, gridLayout, mColor, action)
        if (!gridLayout) {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recycler_view_select_file.layoutManager = layoutManager
        } else {
            val layoutManager = GridLayoutManager(this, 2)
            recycler_view_select_file.layoutManager = layoutManager
        }
        recycler_view_select_file.adapter = selectPDFAdapter
        selectPDFAdapter!!.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_check -> {
                val lst = ArrayList<ItemPDFModel>()
                BackList.instance!!.list.forEach {
                    if (it.click == true) {
                        lst.add(it.itemPDFModel!!)
                    }
                }
                if (lst.size <= 0) {
                    try {
                        showDialogSweet(
                            this,
                            SweetAlertDialog.ERROR_TYPE,
                            "You have not selected the file to merge"
                        )
                    }catch (ex: RuntimeException){
                        Toast.makeText(this,"You have not selected the file to merge",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    startActivity(
                        Intent(
                            this@SelectPDFActivity,
                            OrganizeMergePDFActivity::class.java
                        )
                    )
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_tool, menu)
        val mResetButton = menu.findItem(R.id.action_check)
        mResetButton.isVisible = Isreset
        return true
    }

    override fun onDestroy() {
        BackList.instance!!.list.clear()
        if (nativeAd != null)
            nativeAdLoader.destroy(nativeAd)
        nativeAdLoader.destroy()
        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    fun getDataIntent() {
        try {
            action = intent.getStringExtra(Action.ACTION_INTENT).toString()
        } catch (e: Exception) {
            Log.d("exxx", "Error " + e.message)
        }
        Isreset = action == Action.MERGE_PDF

    }


}
