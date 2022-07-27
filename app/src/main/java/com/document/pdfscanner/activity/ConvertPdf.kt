package com.document.pdfscanner.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import com.document.pdfscanner.R
import com.document.pdfscanner.model.ImagePage
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.ConstantSPKey
import com.document.pdfscanner.ulti.PDFTools
import com.document.pdfscanner.ulti.Utils
import kotlinx.android.synthetic.main.progress_view.*

class ConvertPdf : BaseActivity() {
    lateinit var path: String
    lateinit var key: String
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert_pdf_to_image)
        setBackground()

        sharedPreferences = getSharedPreferences(ConstantSPKey.ACTIVITY_SETTING_KEY, Context.MODE_PRIVATE)
        getData()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { p0, p1 ->
        if (p1.equals(ConstantSPKey.VALUE_COLOR) || p1.equals(ConstantSPKey.NIGHT_MODE)) {
//                isActivity=true
            //  setBackground()
        }
    }

    fun getData() {
        try {
            path = intent.getStringExtra("path").toString()
        } catch (e: Exception) {
            Log.d("exxxe", "Error " + e.message)
        }
        key = intent.getStringExtra("key").toString()
        when (key) {
            ConstantSPKey.KEY_PDF_TO_IMAGE -> {
                val pdfTools = PDFTools()
                pdfTools.javaClass
                pdfTools.PdfToImage(this, path, progress_vieww).execute()
            }
            ConstantSPKey.KEY_SPLIT -> {
                val dlAction = intent.getStringExtra(Action.ACTION_INTENT)
                val pdfTools = PDFTools()
                when (dlAction) {
                    Action.DL_ALL -> {
                        pdfTools.javaClass
                        pdfTools.SplitPDF(
                            this@ConvertPdf,
                            path,
                            progress_vieww
                        )
                            .execute()
                    }
                    Action.DL_AT -> {
                        val vlAt = intent.getStringExtra(Action.DL_VALUE_AT)?.toInt()

                        pdfTools.javaClass
                        pdfTools.SplitPDF(
                            this@ConvertPdf,
                            path,
                            progress_vieww,
                            vlAt!!
                        )
                            .execute()

                    }
                    Action.DL_FROM_TO -> {
                        val vlFrom = intent.getStringExtra(Action.DL_VALUE_FROM)?.toInt()
                        val vlTo = intent.getStringExtra(Action.DL_VALUE_TO)?.toInt()
                        pdfTools.javaClass
                        pdfTools.SplitPDF(
                            this@ConvertPdf,
                            path,
                            progress_vieww,
                            vlFrom!!,
                            vlTo!!
                        )
                            .execute()

                    }

                }

            }
            Action.EXTRACT_IMG -> {
                val value = intent.getStringExtra("value")?.toInt()
                val pdfTools = PDFTools()
                pdfTools.javaClass
                value?.let { pdfTools.ExtractImages(this, path, it, progress_vieww).execute() }
            }

            Action.COMPRESS_PDF -> {
                val pdfTools = PDFTools()
                val value = intent.getStringExtra(Action.DL_CP_VALUE)?.toInt()
                pdfTools.javaClass
                pdfTools.CompressPDFImproved(
                    this@ConvertPdf,
                    path,
                    value!!
                    ,
                    progress_vieww
                )
                    .execute()

            }
            Action.MERGE_PDF -> {
                val lst = intent.getStringArrayListExtra(Action.ACTION_LST)
                val name = intent.getStringExtra(Action.N_VALUE)
                val pdfTools = PDFTools()
                pdfTools.javaClass
                pdfTools.MergePDFFiles(this, lst!!, name!!, progress_vieww).execute()
            }
            Action.IMG_TO_PDF -> {
                val bd = intent.getBundleExtra(Action.IT_BD)
                val list = bd?.getSerializable(Action.BD_LST) as ArrayList<ImagePage>
                val obj = bd.getString(Action.BD_NN)
                val pdfTools = PDFTools()
                pdfTools.javaClass
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                pdfTools.SaveOrganizedPages(
                    this, list,
                    obj!!,
                    progress_vieww
                ).execute()
            }
        }
    }

    override fun onBackPressed() {}

    private fun setBackground() {
        sharedPreferences =
            getSharedPreferences(ConstantSPKey.ACTIVITY_SETTING_KEY, Context.MODE_PRIVATE)

    }


}




