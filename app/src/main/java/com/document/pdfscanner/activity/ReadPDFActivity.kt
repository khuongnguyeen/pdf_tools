package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.document.pdfscanner.R
import android.view.LayoutInflater
import android.view.View
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.ConstantSPKey

import android.util.Log
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_read_pdf.*
import java.io.File
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import com.github.barteksc.pdfviewer.util.FitPolicy
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders

import com.github.barteksc.pdfviewer.listener.OnDrawListener
import com.document.pdfscanner.BuildConfig
//import com.pdftools.pdfreader.pdfviewer.bottomsheet.BottomSheetFragment
import com.document.pdfscanner.model.Data
import com.document.pdfscanner.ulti.Utils
import com.document.pdfscanner.viewmodel.DataViewModel
import kotlinx.android.synthetic.main.custom_actionbar.*
import cn.pedant.SweetAlert.SweetAlertDialog
import java.lang.NullPointerException


class ReadPDFActivity : BaseActivity(), OnErrorListener, OnPageChangeListener,
    OnLoadCompleteListener, OnDrawListener {
    override fun onLayerDrawn(
        canvas: Canvas?,
        pageWidth: Float,
        pageHeight: Float,
        displayedPage: Int
    ) {

    }

    var uri: Uri? = null
    lateinit var prefManager: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var title: TextView
    lateinit var txpageNumber: TextView
    lateinit var close: ImageView
    lateinit var menu: ImageView
    lateinit var gtPageNumberInput: EditText
    lateinit var item: ItemPDFModel
    lateinit var data: Data
    var number: Int = 1
    var isFullScreen = false
    var isDestroy = false
    lateinit var path: String
    var mActionBar: ActionBar? = null
    var isLoadSuccess = true
    var progressDialog: AlertDialog? = null

    var valueZoom = 1.0f
    var maxZoom = 10.0f
    var minZoom = 1.0f

    override fun onError(t: Throwable?) {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }
        isLoadSuccess = false

        Utils.showDialogSweet(
            this,
            SweetAlertDialog.ERROR_TYPE,
            "The file could not be found. It may have been deleted or renamed"
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onPageChanged(page: Int, pageCount: Int) {

        txpageNumber.text = (page + 1).toString() + "/" + pageCount

    }

    override fun loadComplete(nbPages: Int) {
        number = nbPages
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }
        if (uri == null) {
            val dataViewModel: DataViewModel =
                ViewModelProviders.of(this).get(DataViewModel(application)::class.java)
            dataViewModel.selectDataByPath(item.path!!).observe(this
            ) { t ->
                if (t!!.isEmpty()) {
                    data = Data()
                    data = Data(item.size, item.name, item.path)
                    dataViewModel.insertData(data)
                }
            }
        }
        isLoadSuccess = true
    }

    override fun onStop() {
        super.onStop()
        if ( progressDialog!=null && progressDialog!!.isShowing){
            progressDialog!!.cancel();
        }
    }
    private fun hideProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog?.dismiss()
            }
        }
    }


    @SuppressLint("NewApi", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_pdf)

        prefManager = getSharedPreferences(ConstantSPKey.ACTIVITY_SETTING_KEY, Context.MODE_PRIVATE)
        editor = prefManager.edit()

        if (prefManager.getInt(ConstantSPKey.VALUE_A_V, 0) >= 3) {
            editor.putInt(ConstantSPKey.VALUE_A_V, 0)
            editor.apply()
        } else {
            //   layout_load_av.visibility=View.GONE
            hideProgressDialog()
        }
        if (!prefManager.getBoolean(ConstantSPKey.HIDE_ZOOM_MODE, false)) {
            pdf_zoom.visibility = View.VISIBLE
            ivC.setImageDrawable(getDrawable(R.drawable.ic_zoom_plus))
        } else {
            ivC.setImageDrawable(getDrawable(R.drawable.ic_zoom_am))
            pdf_zoom.visibility = View.INVISIBLE
        }
        if (!prefManager.getBoolean(ConstantSPKey.NIGHT_MODE, false)) {
            ivB.setImageDrawable(getDrawable(R.drawable.ic_sun))

        } else {
            ivB.setImageDrawable(getDrawable(R.drawable.ic_moon))

        }
        if (!prefManager.getBoolean(ConstantSPKey.HORIZONTAL_SCROLL_MODE, false)) {
            ivA.setImageDrawable(getDrawable(R.drawable.ic_change_ve))
        } else {
            ivA.setImageDrawable(getDrawable(R.drawable.ic_change_ho))
        }
        if (!prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false)) {
            ivD.setImageDrawable(getDrawable(R.drawable.ic_list_tick))
        } else {
            ivD.setImageDrawable(getDrawable(R.drawable.ic_list_num))
        }

        setSupportActionBar(tb_read)
        mActionBar = supportActionBar
        mActionBar!!.setDisplayShowHomeEnabled(false)
        mActionBar!!.setDisplayShowTitleEnabled(false)

        val mInflater = LayoutInflater.from(this)

        val mCustomView = mInflater.inflate(R.layout.custom_actionbar, null)
        title = mCustomView.findViewById(R.id.title_actionbar)
        close = mCustomView.findViewById(R.id.close_actionbar)
        txpageNumber = mCustomView.findViewById(R.id.position_page_display)
        menu = mCustomView.findViewById(R.id.pdf_menu)
        gtPageNumberInput = mCustomView.findViewById(R.id.position_page_input)
        close.setOnClickListener { finish() }

        menu.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val popupMenu = PopupMenu(this@ReadPDFActivity, menu)
                popupMenu.menuInflater.inflate(R.menu.menu_read_pdf, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { p0 ->
                    when (p0!!.itemId) {
                        R.id.rd_fullScreen -> {
                            fullScreen()
                        }
                        R.id.rd_shareFile -> {
                            if (isLoadSuccess) {
                                shareFile()
                            } else {
                                Utils.showDialogSweet(
                                    this@ReadPDFActivity,
                                    SweetAlertDialog.ERROR_TYPE,
                                    "Can't share when loading file fails"
                                )
                            }
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        })
        gtPageNumberInput.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event!!.action == KeyEvent.ACTION_DOWN) {
                gtThePageNumber()
                true
            } else {
                false
            }
        }
        mActionBar!!.customView = mCustomView
        mActionBar!!.setDisplayShowCustomEnabled(true)
        getDataBundle()
        zoom_in.setOnClickListener {
            if (isLoadSuccess) {
                zoomIn()
            } else {

                Utils.showDialogSweet(
                    this@ReadPDFActivity,
                    SweetAlertDialog.ERROR_TYPE,
                    "Can't zoom when loading file fails"
                )
            }
        }
        zoom_out.setOnClickListener {
            if (isLoadSuccess) {
                zoomOut()
            } else {
                Utils.showDialogSweet(
                    this@ReadPDFActivity,
                    SweetAlertDialog.ERROR_TYPE,
                    "Can't zoom when loading file fails"
                )
            }
        }

        ivE.setOnClickListener {
            startActivity(Intent(this@ReadPDFActivity, HomeActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        ivA.setOnClickListener {
            if (!prefManager.getBoolean(ConstantSPKey.HORIZONTAL_SCROLL_MODE, false)) {
                ivA.setImageDrawable(getDrawable(R.drawable.ic_change_ho))
                editor.putBoolean(ConstantSPKey.HORIZONTAL_SCROLL_MODE, true)
                editor.apply()
            } else {
                ivA.setImageDrawable(getDrawable(R.drawable.ic_change_ve))
                editor.putBoolean(ConstantSPKey.HORIZONTAL_SCROLL_MODE, false)
                editor.apply()
            }
            if (uri == null) {
                getPDF(path)
            } else {
                loadPDFFromUri(uri)
            }
        }
        ivB.setOnClickListener {
            if (!prefManager.getBoolean(ConstantSPKey.NIGHT_MODE, false)) {
                ivB.setImageDrawable(getDrawable(R.drawable.ic_moon))
                editor.putBoolean(ConstantSPKey.NIGHT_MODE, true)
                editor.apply()
                recreate()
            } else {
                ivB.setImageDrawable(getDrawable(R.drawable.ic_sun))
                editor.putBoolean(ConstantSPKey.NIGHT_MODE, false)
                editor.apply()
                recreate()
            }

            if (uri == null) {
                getPDF(path)
            } else {
                loadPDFFromUri(uri)
            }
        }
        ivC.setOnClickListener {
            if (!prefManager.getBoolean(ConstantSPKey.HIDE_ZOOM_MODE, false)) {
                ivC.setImageDrawable(getDrawable(R.drawable.ic_zoom_am))
                pdf_zoom.visibility = View.INVISIBLE
                editor.putBoolean(ConstantSPKey.HIDE_ZOOM_MODE, true)
                editor.apply()
            } else {
                ivC.setImageDrawable(getDrawable(R.drawable.ic_zoom_plus))
                pdf_zoom.visibility = View.VISIBLE
                editor.putBoolean(ConstantSPKey.HIDE_ZOOM_MODE, false)
                editor.apply()
            }
        }
        ivD.setOnClickListener {
            if (!prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false)) {
                ivD.setImageDrawable(getDrawable(R.drawable.ic_list_num))
                editor.putBoolean(ConstantSPKey.SWIPE_MODE, true)
                editor.apply()
            } else {
                ivD.setImageDrawable(getDrawable(R.drawable.ic_list_tick))
                editor.putBoolean(ConstantSPKey.SWIPE_MODE, false)
                editor.apply()
            }
            if (uri == null) {
                getPDF(path)
            } else {
                loadPDFFromUri(uri)
            }
        }
    }

    private fun gtThePageNumber() {
        try {
            val pagerNumber = position_page_input.text.toString().toInt()
            if (pagerNumber > 0 && pagerNumber <= pdfView.pageCount) {
                pdfView.jumpTo(pagerNumber - 1)
            } else {
                Utils.showDialogSweet(
                    this,
                    SweetAlertDialog.ERROR_TYPE,
                    "error page number"
                )
            }
        }catch (e:Exception){
            e.printStackTrace()
            Utils.showDialogSweet(
                this,
                SweetAlertDialog.ERROR_TYPE,
                getString(R.string.invalid_number)
            )
        }
    }

    private fun shareFile() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            val fileUri =
                FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    File(item.path)
                )
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share " + title.text.toString() + ".pdf"
                )
            )
        } catch (unused: Exception) {
            Utils.showDialogSweet(
                this,
                SweetAlertDialog.ERROR_TYPE,
                "Don't have any application to share"
            )
        }

    }

    private fun fullScreen() {
        if (!isFullScreen) {
            mActionBar!!.hide()
            layout_tool.visibility = View.GONE
            isFullScreen = true

        } else {
            mActionBar!!.show()
            layout_tool.visibility = View.VISIBLE
            isFullScreen = false
        }
    }

    private fun getPDF(path: String) {
        pdfView.fromFile(File(path))
            .onLoad(this).onError(this).onPageChange(this)
            .autoSpacing(prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false)).onDraw(this)
            .nightMode(prefManager.getBoolean(ConstantSPKey.NIGHT_MODE, false)).enableSwipe(true)
            .pageFling(prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false))
            .swipeHorizontal(prefManager.getBoolean(ConstantSPKey.HORIZONTAL_SCROLL_MODE, false))
            .pageSnap(prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false)).spacing(8)
            .password(null).enableAnnotationRendering(true).pageFitPolicy(FitPolicy.BOTH)
            .fitEachPage(true).enableAntialiasing(true).load()
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun getDataBundle() {
        val intente = intent.extras
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        try {
            uri = intent.data
            if (uri == null) {
                item = intente!!.get(ConstantSPKey.INFO_PDF) as ItemPDFModel
                title.text = item.name
                path = item.path!!
                getPDF(path)
                Log.d("exxee", "Error " + " path")
            } else {
                try {
                    val action = intent!!.action
                    val type = intent.type
                    if (Intent.ACTION_VIEW == action && type!!.endsWith("pdf")) {
                        loadPDFFromUri(uri)
                    }
                    Log.d("exxee", "Error " + " uri")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.d("exxee", "Error " + e.message)
        }
    }

    private fun loadPDFFromUri(uri: Uri?) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        try {
            val name = getFileName(uri!!)
            title.text = name
            path = uri.path.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pdfView.fromUri(uri)
            .onLoad(this).onError(this).onPageChange(this)
            .autoSpacing(prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false)).onDraw(this)
            .nightMode(prefManager.getBoolean(ConstantSPKey.NIGHT_MODE, false)).enableSwipe(true)
            .pageFling(prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false))
            .swipeHorizontal(prefManager.getBoolean(ConstantSPKey.HORIZONTAL_SCROLL_MODE, false))
            .pageSnap(prefManager.getBoolean(ConstantSPKey.SWIPE_MODE, false)).spacing(8)
            .password(null).enableAnnotationRendering(true).pageFitPolicy(FitPolicy.BOTH)
            .fitEachPage(true).enableAntialiasing(true).load()

    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var str = "";
        val query = contentResolver.query(uri, null, null, null, null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    str = query.getString(query.getColumnIndex("_display_name"));
                }
            } catch (th: Throwable) {
                query.close()
                throw th;
            }
        }
        query?.close()

        return str
    }

    override fun onBackPressed() {
        if (isFullScreen) {
            mActionBar!!.show()
            layout_tool.visibility = View.VISIBLE
            isFullScreen = false
        } else {
            finish()
        }
    }

    private fun zoomIn() {
        valueZoom = pdfView.zoom

        try {
            valueZoom += 1.0f
            if (valueZoom < maxZoom && !isDestroy) {
                pdfView.zoomWithAnimation(valueZoom)
            } else {
                valueZoom = maxZoom
                pdfView.zoomWithAnimation(maxZoom)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun zoomOut() {
        try {

            valueZoom = pdfView.zoom
            valueZoom -= 1.0f
            if (valueZoom > minZoom && !isDestroy) {
                pdfView.zoomWithAnimation(valueZoom)
            } else {
                valueZoom = minZoom
                pdfView.zoomWithAnimation(minZoom)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: NullPointerException) {

            Utils.showDialogSweet(
                this,
                SweetAlertDialog.ERROR_TYPE,
                "Error " + e.message
            )
        }

    }

    override fun onDestroy() {
        isDestroy = true
        super.onDestroy()
    }
}
