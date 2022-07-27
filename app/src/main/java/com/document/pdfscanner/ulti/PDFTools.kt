package com.document.pdfscanner.ulti


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.exifinterface.media.ExifInterface

import com.airbnb.lottie.LottieAnimationView
import com.document.pdfscanner.R
import com.document.pdfscanner.activity.HomeActivity
import com.document.pdfscanner.activity.ReadPDFActivity
import com.document.pdfscanner.activity.ViewFileActivity
import com.document.pdfscanner.activity.ViewImageActivity
import com.document.pdfscanner.model.ImagePage
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.Utils.shareFile
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.parser.PdfImageObject
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import cn.pedant.SweetAlert.SweetAlertDialog
import com.document.pdfscanner.App
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*

@Suppress("DEPRECATION")
class PDFTools {
    val TAG = "PDFTools"
    private var btnOpenFile: ImageView? = null
    private var share_file: ImageView? = null
    private var preview: ImageView? = null
    private var cardView5: CardView? = null
    lateinit var homeProgressView: ImageView
    lateinit var imageBg: ImageView
    lateinit var currentAction: TextView
    lateinit var mProgressView: ConstraintLayout
    lateinit var clAds: ConstraintLayout
    lateinit var percent: TextView
    lateinit var progressBar: ProgressBar
    lateinit var animationView: LottieAnimationView


    @SuppressLint("StaticFieldLeak")
    inner class PdfToImage(context: Context, apath: String, constraintLayout: ConstraintLayout) :
        AsyncTask<Void, Int, Void>() {
        lateinit var allPdf: String
        var mContext: Context
        var numberPage: Int = 0
        var path: String
        lateinit var pdfiumCore: PdfiumCore
        lateinit var pdfDocument: PdfDocument

        init {
            mContext = context
            path = apath
            mProgressView = constraintLayout
            initProgressView()

        }

        override fun doInBackground(vararg p0: Void?): Void? {
            var voidR: Void?
            var i: Int
            var str: String
            var i2: Int
            val name = File(path).name
            val arrayList = ArrayList<String>()
            val arrayList2 = ArrayList<String>()
            val sbd = StringBuilder()
            sbd.append(Environment.getExternalStorageDirectory().toString())
            sbd.append("/Pictures/PDFToImage/")
            sbd.append(Utils.removeExtension(name))
            sbd.append("/")
            allPdf = sbd.toString()
            val file = File(allPdf)
            if (!file.exists()) {
                file.mkdirs()
            }
            pdfiumCore = PdfiumCore(mContext)
            try {
                pdfDocument = pdfiumCore.newDocument(
                    mContext.contentResolver.openFileDescriptor(
                        Uri.fromFile(File(path)), PDPageLabelRange.STYLE_ROMAN_LOWER
                    )
                )
                numberPage = this.pdfiumCore.getPageCount(this.pdfDocument)
                progressBar.max = numberPage
                var i3 = 0
                while (i3 < numberPage && !isCancelled) {
                    val sb2 = StringBuilder()
                    sb2.append(allPdf)
                    sb2.append(Utils.removeExtension(name))
                    sb2.append(R.string.page)
                    val i4 = i3 + 1
                    sb2.append(i4)
                    sb2.append(".jpg")
                    val ss2 = sb2.toString()
                    val fileOutputStream = FileOutputStream(ss2)

                    val sb3 = StringBuilder()
                    sb3.append(R.string.page_to_image)
                    sb3.append(sb3)
                    Log.d(TAG, sb3.toString())
                    pdfiumCore.openPage(pdfDocument, i3)
                    val pageWidthPoint = pdfiumCore.getPageWidthPoint(pdfDocument, i3) * 2
                    val pageHeightPoint =
                        pdfiumCore.getPageHeightPoint(pdfDocument, i3) * 2
                    try {
                        val createBitmap = Bitmap.createBitmap(
                            pageWidthPoint,
                            pageHeightPoint,
                            Bitmap.Config.ARGB_8888
                        )
                        i2 = 1
                        str = ss2
                        i = i4
                        try {
                            pdfiumCore.renderPageBitmap(
                                pdfDocument,
                                createBitmap,
                                i3,
                                0,
                                0,
                                pageWidthPoint,
                                pageHeightPoint,
                                true
                            )
                            createBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fileOutputStream)
                        } catch (e: OutOfMemoryError) {
                            e.printStackTrace()
                        }

                    } catch (e2: OutOfMemoryError) {
                        e2.printStackTrace()
                        str = ss2
                        i = i4
                        i2 = 1
                        Utils.showDialogSweet(
                            mContext,
                            SweetAlertDialog.ERROR_TYPE,
                            mContext.getString(R.string.failed_low_memory)
                        )
                        arrayList.add(str)
                        arrayList2.add("image/jpg")
                        val numArr = arrayOfNulls<Int>(i2)
                        numArr[0] = Integer.valueOf(i)
                        publishProgress(*numArr)
                    }
                    arrayList.add(str)
                    arrayList2.add("image/jpg")
                    val numArr2 = arrayOfNulls<Int>(i2)
                    numArr2[0] = Integer.valueOf(i)
                    publishProgress(*numArr2)
                    i3 = i
                }
                pdfiumCore.closeDocument(pdfDocument)
                return try {
                    MediaScannerConnection.scanFile(
                        mContext,
                        arrayList.toTypedArray(),
                        arrayList2.toTypedArray(),
                        null
                    )
                    null
                } catch (unused: Exception) {
                    voidR = null
                    voidR
                }
            } catch (unused2: Exception) {
                voidR = null
                return voidR
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            updateProgressPercent(values[0]!!, numberPage)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            currentAction.text = ""
            processingFinished(mContext, "", "", allPdf)
            openImageDirectory(
                mContext,
                mContext.getString(R.string.open_directory),
                this.allPdf
            )
            share_file?.visibility= View.GONE
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class SplitPDF : AsyncTask<Void, Int, Void> {


        var mContext: Context
        private var numPages: Int = 0
        private var pdfPath: String? = null
        private var splitAt = 0
        private var splitFrom = 0
        private var splitTo = 0
        private var splittedPdfDocumentDir: String = ""

        constructor(context: Context, str: String, constraintLayout: ConstraintLayout) {
            mContext = context
            pdfPath = str
            mProgressView = constraintLayout
            initProgressView()
        }

        constructor(
            context: Context,
            str: String,
            constraintLayout: ConstraintLayout,
            int1: Int,
            int2: Int
        ) {
            mContext = context
            pdfPath = str
            mProgressView = constraintLayout
            initProgressView()
            splitFrom = int1
            splitTo = int2


        }

        constructor(context: Context, str: String, constraintLayout: ConstraintLayout, int1: Int) {
            mContext = context
            pdfPath = str
            mProgressView = constraintLayout
            initProgressView()
            splitAt = int1


        }


        override fun doInBackground(vararg p0: Void?): Void? {
            val z: Boolean = PreferenceManager.getDefaultSharedPreferences(this.mContext)
                .getBoolean(ConstantSPKey.GRID_MODE, false)
            val file = File(pdfPath)
            val name = file.name
            val sb = StringBuilder()
            sb.append(Environment.getExternalStorageDirectory())
            sb.append("/Documents/PDF/split")
            sb.append(Utils.removeExtension(name))
            sb.append(System.currentTimeMillis())
            sb.append("/")
            splittedPdfDocumentDir = sb.toString()
            val file2 = File(splittedPdfDocumentDir)
            if (!file2.exists()) {
                file2.mkdirs()
            }
            PDFBoxResourceLoader.init(mContext)
            try {
                val load = PDDocument.load(file)
                val splitter = Splitter()
                if (splitFrom != 0 && splitTo != 0) {
                    splitter.setStartPage(this.splitFrom)
                    splitter.setEndPage(this.splitTo)
                } else if (splitAt != 0) {
                    splitter.setSplitAtPage(this.splitAt)
                }
                val split = splitter.split(load)
                numPages = split.size
                progressBar.max = numPages
                val listIterator = split.listIterator()
                val arrayList = ArrayList<String>()
                val arrayList2 = ArrayList<String>()
                removeProgressBarIndeterminate(mContext, progressBar)
                var i = 1
                if (splitFrom != 0 && splitTo != 0) i = splitFrom
                while (listIterator.hasNext() && !isCancelled) {
                    val sb2 = StringBuilder()
                    sb2.append(this.splittedPdfDocumentDir)
                    sb2.append(Utils.removeExtension(name))
                    sb2.append("_page_")
                    sb2.append(i)
                    sb2.append(".pdf")
                    val sb3 = sb2.toString()
                    val pDDocument = listIterator.next()
                    pDDocument.save(sb3)
                    pDDocument.close()
                    arrayList.add(sb3)
                    arrayList2.add("application/pdf")
                    if (z) {
                        Utils.generatePDFThumbnail(this.mContext, sb3)
                    }
                    val sb4 = StringBuilder()
                    sb4.append(R.string.created)
                    sb4.append(sb3)
                    publishProgress(Integer.valueOf(i))
                    i++
                }
                load.close()
                if (!isCancelled) {
                    @Suppress("UNCHECKED_CAST")
                    MediaScannerConnection.scanFile(
                        this.mContext,
                        arrayList.toArray(arrayOfNulls<String>(arrayList.size)) as Array<String>,
                        arrayList2.toArray(
                            arrayOfNulls<String>(arrayList2.size)
                        ) as Array<String>,
                        null
                    )
                } else {
                    deleteFiles(splittedPdfDocumentDir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.isIndeterminate = true
            currentAction.setText(R.string.splitting)
            mProgressView.visibility = View.VISIBLE
        }


        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            updateProgressPercent(values[0], numPages)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            currentAction.setText("")
            processingFinished(mContext, "", "", splittedPdfDocumentDir)
            setupOpenPath(
                mContext, mContext.getString(R.string.open_directory),
                splittedPdfDocumentDir.toString(), false
            )


        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ExtractImages(
        internal var mContext: Context,
        internal var pdfPath: String,
        internal var compressionQuality: Int,
        constraintLayout: ConstraintLayout
    ) : AsyncTask<Void, Int, Void>() {
        var allPdfPictureDir: String = ""
        internal var xrefSize: Int = 0

        init {
            mProgressView = constraintLayout
            initProgressView()

        }

        public override fun onPreExecute() {
            super.onPreExecute()
            currentAction.setText(R.string.extracting)
            mProgressView.visibility = View.VISIBLE
        }

        public override fun doInBackground(vararg voidArr: Void): Void? {
            var str: String = ""
            var e: Exception
            val arrayList = ArrayList<String>()
            val arrayList2 = ArrayList<String>()
            val str2 = ""
            val name = File(this.pdfPath).name
            val sb = StringBuilder()
            sb.append(Environment.getExternalStorageDirectory())
            sb.append("/Pictures/PDFExtractImage/")
            sb.append(Utils.removeExtension(name))
            sb.append("/")
            this.allPdfPictureDir = sb.toString()
            val file = File(this.allPdfPictureDir)
            if (!file.exists()) {
                file.mkdirs()
            }
            try {
                val pdfReader = PdfReader(this.pdfPath)
                this.xrefSize = pdfReader.xrefSize
                this@PDFTools.progressBar.max = this.xrefSize
                var str3 = str2
                var i = 1
                var i2 = 0
                while (i2 < this.xrefSize && !isCancelled) {
                    val pdfObject = pdfReader.getPdfObject(i2)
                    if (pdfObject != null) {
                        if (pdfObject.isStream) {
                            val pRStream = pdfObject as PRStream
                            val pdfObject2 = pRStream.get(PdfName.SUBTYPE)
                            if (pdfObject2 == null || !pdfObject2.toString()
                                    .equals(PdfName.IMAGE.toString())
                            ) {
                                str = str3
                            } else {
                                try {
                                    val imageAsBytes = PdfImageObject(pRStream).imageAsBytes
                                    val decodeByteArray = BitmapFactory.decodeByteArray(
                                        imageAsBytes,
                                        0,
                                        imageAsBytes.size
                                    )
                                    if (decodeByteArray != null) {
                                        val sb2 = StringBuilder()
                                        sb2.append(this.allPdfPictureDir)
                                        sb2.append("image-")
                                        sb2.append(i)
                                        sb2.append(".jpg")
                                        str = sb2.toString()
                                        try {
                                            decodeByteArray.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                this.compressionQuality,
                                                FileOutputStream(str)
                                            )
                                            val `access$600` = TAG
                                            val sb3 = StringBuilder()
                                            sb3.append(R.string.image_extracted)
                                            sb3.append(this.allPdfPictureDir)
                                            sb3.append("image-")
                                            sb3.append(i)
                                            sb3.append(".jpg")
                                            Log.d(`access$600`, sb3.toString())
                                            pRStream.clear()
                                            if (!decodeByteArray.isRecycled) {
                                                decodeByteArray.recycle()
                                            }
                                            i++
                                        } catch (e2: Exception) {
                                            e = e2
                                            e.printStackTrace()
                                            arrayList.add(str)
                                            arrayList2.add("image/jpg")
                                            publishProgress(*arrayOf(Integer.valueOf(i2 + 1)))
                                            str3 = str
                                        }

                                    }
                                } catch (e3: Exception) {
                                    str = str3
                                    e = e3
                                    e.printStackTrace()
                                    arrayList.add(str)
                                    arrayList2.add("image/jpg")
                                    publishProgress(Integer.valueOf(i2 + 1))
                                }

                            }
                            arrayList.add(str)
                            arrayList2.add("image/jpg")
                            publishProgress(Integer.valueOf(i2 + 1))
                            str3 = str
                        }
                    }
                    i2++
                }
                MediaScannerConnection.scanFile(
                    this.mContext,
                    arrayList.toTypedArray(),
                    arrayList2.toTypedArray(),
                    null
                )
            } catch (e4: Exception) {
                e4.printStackTrace()
            }

            return null
        }


        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            updateProgressPercent(values[0], xrefSize)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            percent.setText(R.string.hundred_percent)
            progressBar.progress = this.xrefSize
            currentAction.setText("")
            processingFinished(mContext, "", "", allPdfPictureDir)

            share_file?.visibility= View.GONE
            openImageDirectory(
                mContext,
                mContext.getString(R.string.open_directory),
                allPdfPictureDir
            )
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class SaveOrganizedPages(
        private val mContext: Context,
        private val imagePages: List<ImagePage>,
        private val newFileName: String,
        constraintLayout: ConstraintLayout
    ) : AsyncTask<Void, Int, Void>() {
        private var organizedPages: ArrayList<Int> = ArrayList()
        private var generatedPDFPath: String? = null
        private var numPages = 0
        private var allPdfDocuments = ""

        init {
            mProgressView = constraintLayout
            initProgressView()
        }

        public override fun onPreExecute() {
            super.onPreExecute()
            progressBar.max = this.numPages
            currentAction.setText(R.string.converting)
            mProgressView.visibility = View.VISIBLE
        }

        public override fun doInBackground(vararg voidArr: Void): Void? {
            val z =
                PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getBoolean(ConstantSPKey.GRID_MODE, false)
            try {
                imagePages.forEach {
                    organizedPages.add(it.pageNumber)
                }
                val sb = StringBuilder()
                sb.append(Environment.getExternalStorageDirectory())
                sb.append("/Documents/PDF/ImageToPDF/")
                this.allPdfDocuments = sb.toString()

                val file = File(allPdfDocuments)
                val sbb = StringBuilder()
                sbb.append(allPdfDocuments)
                sbb.append(newFileName)
                sbb.append(".pdf")
                this.generatedPDFPath = sbb.toString()
                if (!file.exists()) {
                    file.mkdirs()
                }
                PDFBoxResourceLoader.init(mContext)
                val pDDocument = PDDocument()
                numPages = organizedPages.size
                var i = 0
                while (i < this.numPages && !isCancelled) {
                    val path =
                        imagePages[i].imageUri
                    val rotateBitmap = rotateBitmap(
                        ImageUtils.instant!!.getCompressedBitmap(path),
                        ExifInterface(path!!).getAttributeInt("Orientation", 0)
                    )
                    val width = rotateBitmap!!.width.toFloat()
                    val height = rotateBitmap.height.toFloat()
                    val pDPage = PDPage(PDRectangle(width, height))
                    pDDocument.addPage(pDPage)
                    // val img = PDImageXObject.createFromFile(File(path),pDDocument)
                    val img = JPEGFactory.createFromImage(pDDocument, rotateBitmap)
                    val pDPageContentStream =
                        PDPageContentStream(pDDocument, pDPage)
                    pDPageContentStream.drawImage(img, 0.0f, 0.0f, width, height)
                    pDPageContentStream.close()
                    i++
                    publishProgress(i)
                }
                pDDocument.save(this.generatedPDFPath!!)
                pDDocument.close()
                if (z) {
                    Utils.generatePDFThumbnail(
                        mContext,
                        generatedPDFPath
                    )
                }
                MediaScannerConnection.scanFile(
                    mContext,
                    arrayOf(generatedPDFPath!!),
                    arrayOf("application/pdf"),
                    null
                )
                val str = TAG
                val sb2 = StringBuilder()
                sb2.append("Page order")
                sb2.append(this.organizedPages)
                Log.d(str, sb2.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            updateProgressPercent(values[0], this.numPages)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            currentAction.setText("")
            processingFinished(mContext, "", "", allPdfDocuments)
            setupOpenPath(
                mContext,
                mContext.getString(R.string.open_file),
                generatedPDFPath.toString(),
                true
            )
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class CompressPDFImproved(
        internal var mContext: Context,
        internal var pdfPath: String,
        internal var compressionQuality: Int,
        constraintLayout: ConstraintLayout
    )
        : AsyncTask<Void, Int, Void>() {
        lateinit var allPdfDocumentDir: String
        internal var compressedFileLength: Long? = null
        internal lateinit var compressedFileSize: String
        internal lateinit var compressedPDF: String
        internal var isEcrypted = false
        internal lateinit var reducedPercent: String
        internal var uncompressedFileLength: Long? = null
        internal lateinit var uncompressedFileSize: String
        internal var xrefSize: Int = 0

        init {
            mProgressView = constraintLayout
            initProgressView()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            currentAction.setText(R.string.compressing)
            mProgressView.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg voidArr: Void): Void? {

            val file = File(this.pdfPath)
            val name = file.name
            this.uncompressedFileLength = java.lang.Long.valueOf(file.length())
            this.uncompressedFileSize =
                Formatter.formatShortFileSize(this.mContext, this.uncompressedFileLength!!.toLong())
            val sb = StringBuilder()
            sb.append(Environment.getExternalStorageDirectory())
            sb.append("/Documents/PDF/")
            this.allPdfDocumentDir = sb.toString()
            val sb2 = StringBuilder()
            sb2.append(allPdfDocumentDir)
            sb2.append(Utils.removeExtension(name))
            sb2.append("-Compressed.pdf")
            compressedPDF = sb2.toString()
            val file2 = File(this.allPdfDocumentDir)
            if (!file2.exists()) {
                file2.mkdirs()
            }
            try {
                val pdfReader = PdfReader(this.pdfPath)
                if (pdfReader.isEncrypted) {
                    this.isEcrypted = true
                    return null
                }
                this.xrefSize = pdfReader.xrefSize
                progressBar.max = this.xrefSize
                var i = 0
                while (i < this.xrefSize && !isCancelled) {
                    val pdfObject = pdfReader.getPdfObject(i)
                    if (pdfObject != null) {
                        if (pdfObject.isStream) {
                            val pRStream = pdfObject as PRStream
                            val pdfObject2 = pRStream.get(PdfName.SUBTYPE)
                            if (pdfObject2 != null && pdfObject2.toString() == PdfName.IMAGE.toString()) {
                                try {
                                    val compressedBitmap = ImageUtils.instant!!
                                        .getCompressedBitmap(PdfImageObject(pRStream).imageAsBytes)
                                    val byteArrayOutputStream = ByteArrayOutputStream()
                                    compressedBitmap!!.compress(
                                        Bitmap.CompressFormat.JPEG,
                                        this.compressionQuality,
                                        byteArrayOutputStream
                                    )
                                    pRStream.setData(
                                        byteArrayOutputStream.toByteArray(),
                                        false,
                                        9
                                    )
                                    pRStream.put(PdfName.FILTER, PdfName.DCTDECODE)
                                    byteArrayOutputStream.close()
                                    pRStream.clear()
                                    pRStream.setData(
                                        byteArrayOutputStream.toByteArray(),
                                        false,
                                        0
                                    )
                                    pRStream.put(PdfName.TYPE, PdfName.XOBJECT)
                                    pRStream.put(PdfName.SUBTYPE, PdfName.IMAGE)
                                    pRStream.put(PdfName.FILTER, PdfName.DCTDECODE)
                                    pRStream.put(
                                        PdfName.WIDTH,
                                        PdfNumber(compressedBitmap.width)
                                    )
                                    pRStream.put(
                                        PdfName.HEIGHT,
                                        PdfNumber(compressedBitmap.height)
                                    )
                                    pRStream.put(PdfName.BITSPERCOMPONENT, PdfNumber(8))
                                    pRStream.put(PdfName.COLORSPACE, PdfName.DEVICERGB)
                                    if (!compressedBitmap.isRecycled) {
                                        compressedBitmap.recycle()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                            publishProgress(Integer.valueOf(i + 1))
                        }
                    }
                    i++
                }
                pdfReader.removeUnusedObjects()
                val pdfStamper = PdfStamper(pdfReader, FileOutputStream(compressedPDF))
                pdfStamper.setFullCompression()
                pdfStamper.close()
                compressedFileLength =
                    java.lang.Long.valueOf(File(compressedPDF).length())
                compressedFileSize = Formatter.formatShortFileSize(
                    mContext,
                    compressedFileLength!!.toLong()
                )
                val sb3 = StringBuilder()
                sb3.append(100 - (this.compressedFileLength!!.toLong() * 100 / this.uncompressedFileLength!!.toLong()).toInt())
                sb3.append("%")
                reducedPercent = sb3.toString()
                MediaScannerConnection.scanFile(
                    this.mContext,
                    arrayOf(compressedPDF),
                    arrayOf("application/pdf"),
                    null
                )
                return null
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
            return null
        }


        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            updateProgressPercent(values[0], this.xrefSize)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (!this.isEcrypted) {
//                val sb = StringBuilder()
//                sb.append(this.mContext.getString(R.string.reduced_from))
//                sb.append(" ")
//                sb.append(this.uncompressedFileSize)
//                sb.append(" ")
//                sb.append(this.mContext.getString(R.string.to))
//                sb.append(" ")
//                sb.append(this.compressedFileSize)
//                val sb2 = sb.toString()
                percent.setText(R.string.hundred_percent)
                progressBar.progress = this.xrefSize
                currentAction.text = ""
                processingFinished(mContext, reducedPercent, "", allPdfDocumentDir)
                setupOpenPath(
                    mContext,
                    mContext.getString(R.string.open_file),
                    compressedPDF,
                    true
                )
                return
            }
            closeProgressView(this.mContext)
            Utils.showDialogSweet(
                mContext,
                SweetAlertDialog.WARNING_TYPE,
                mContext.getString( R.string.file_protected_unprotect)
            )
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class MergePDFFiles(
        internal var mContext: Context,
        internal var pdfPaths: ArrayList<String>,
        internal var mergedFileName: String,
        constraintLayout: ConstraintLayout
    )
        : AsyncTask<Void, Int, Void>() {
        lateinit var allPdfMergedDir: String
        var mergeSuccess = true
        lateinit var mergedFilePath: String
        var numFiles: Int = 0

        init {
            PDFBoxResourceLoader.init(mContext)
            mProgressView = constraintLayout
            initProgressView()

        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.isIndeterminate = true
            currentAction.setText(R.string.merging)
            mProgressView.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val z = PreferenceManager.getDefaultSharedPreferences(this.mContext)
                .getBoolean(ConstantSPKey.GRID_MODE, false)
            val sb = StringBuilder()
            sb.append(Environment.getExternalStorageDirectory())
            sb.append("/Documents/PDF/Merged/")
            allPdfMergedDir = sb.toString()
            val file = File(allPdfMergedDir)
            if (!file.exists()) {
                file.mkdirs()
            }
            val sb2 = StringBuilder()
            sb2.append(allPdfMergedDir)
            sb2.append(mergedFileName)
            sb2.append(".pdf")
            mergedFilePath = sb2.toString()
            numFiles = pdfPaths.size
            progressBar.max = numFiles + 1
            PDFBoxResourceLoader.init(mContext)
            val pDFMergerUtility = PDFMergerUtility()
            pDFMergerUtility.destinationFileName = mergedFilePath
            removeProgressBarIndeterminate(mContext, progressBar)
            var i = 0
            while (i < this.numFiles && !isCancelled) {
                try {
                    var afile = File(pdfPaths[i])

                    pDFMergerUtility.addSource(afile)
                    i++
                    publishProgress(*arrayOf(Integer.valueOf(i)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    mergeSuccess = false
                }

            }
            pDFMergerUtility.mergeDocuments()
            publishProgress(numFiles + 1)
            if (isCancelled) {
                File(mergedFilePath).delete()
            }
            MediaScannerConnection.scanFile(
                this.mContext,
                arrayOf(mergedFilePath),
                arrayOf("application/pdf"),
                null
            )
            if (z) {
                Utils.generatePDFThumbnail(mContext, mergedFilePath)
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            updateProgressPercent(values[0], numFiles + 1)
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            currentAction.setText("")
            processingFinished(mContext, "", "", allPdfMergedDir)
            setupOpenPath(
                mContext,
                mContext.getString(R.string.open_file),
                mergedFilePath,
                true
            )
            if (!this.mergeSuccess) {
                Utils.showDialogSweet(
                    mContext,
                    SweetAlertDialog.ERROR_TYPE,
                    mContext.getString( R.string.merge_failed)
                )
            }
        }
    }


    fun deleteFiles(str: String) {
        if (File(str).exists()) {
            val sb = StringBuilder()
            sb.append("rm -r ")
            sb.append(str)
            try {
                Runtime.getRuntime().exec(sb.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun initProgressView() {
        percent = mProgressView.findViewById(R.id.percent)
        cardView5 = mProgressView.findViewById(R.id.cardView5)
        currentAction = mProgressView.findViewById(R.id.current_action)
        progressBar = mProgressView.findViewById(R.id.progress_bar)
        animationView = mProgressView.findViewById(R.id.ani_view)
        imageBg = mProgressView.findViewById(R.id.cl_bg)
        btnOpenFile = mProgressView.findViewById(R.id.open_file)
        clAds = mProgressView.findViewById(R.id.cl_ads)
        share_file = mProgressView.findViewById(R.id.share_file)
        preview = mProgressView.findViewById(R.id.preview)
        homeProgressView = mProgressView.findViewById(R.id.home_progress_view)
    }

    fun processingFinished(context: Context, str: String, str2: String, str3: String) {
        percent.visibility = View.GONE
        progressBar.visibility = View.INVISIBLE
        cardView5?.visibility = View.VISIBLE
        animationView.visibility = View.INVISIBLE
        imageBg.visibility = View.VISIBLE
        homeProgressView.visibility = View.VISIBLE
        btnOpenFile!!.visibility = View.VISIBLE

        val ss = "Save to $str3"
        if (!TextUtils.isEmpty(str)) {
            this.currentAction.text = ""
        }
    }

    fun closeProgressView(context: Context) {
        context as Activity
        context.finish()
    }


    fun updateProgressPercent(i: Int?, i2: Int) {
        val i3 = (i!!.toFloat() * 100.0f).toInt() / i2
        val ss = "$i3 %"
        percent.text = ss
        progressBar.progress = i
    }

    fun setupOpenPath(context: Context, str: String, str2: String, z: Boolean) {
        homeProgressView.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    HomeActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

        share_file?.setOnClickListener {
            val file = File(str2)
            shareFile(context, Uri.fromFile(File(file.absolutePath.toString())))
        }


        this.btnOpenFile!!.setOnClickListener {
                    if (z) {
                        val file = File(str2)
                        val itemPDFModel =
                            ItemPDFModel(
                                convertUnits(file.length().toDouble()),
                                file.name,
                                file.absolutePath
                            )
                        val bundle = Bundle()
                        bundle.putSerializable(ConstantSPKey.INFO_PDF, itemPDFModel)
                        context.startActivity(
                            Intent(
                                context,
                                ReadPDFActivity::class.java
                            ).putExtras(bundle)
                        )
                    } else {
                        val intent2 = Intent(context, ViewFileActivity::class.java)
                        intent2.putExtra(Action.ST_IT, Action.ST_A_FOLDER)
                        intent2.putExtra(Action.PATH_A_FOLDER, str2)
                        context.startActivity(intent2.putExtra(Action.ACTION_INTENT, Action.OPEN_PDF))
                    }

        }
    }

    fun openImageDirectory(context: Context, str: String, str2: String) {
        homeProgressView.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    HomeActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

        btnOpenFile!!.setOnClickListener {
                    val intent = Intent(context, ViewImageActivity::class.java)
                    intent.putExtra(Action.ACTION_INTENT, Action.PDF_TO_IMG)
                        .putExtra(Action.IMG_FOLDER, str2)
                    context.startActivity(intent)
                  
        }
    }

    fun removeProgressBarIndeterminate(context: Context, progressBar2: ProgressBar) {
        (context as Activity).runOnUiThread { progressBar2.isIndeterminate = false }
    }

    private fun convertUnits(nb: Double?): String {
        val numberFormat = DecimalFormat("#.0")
        val numb = nb!! / 1024.0
        return if (numb >= 1000.0) {

            numberFormat.format(numb / 1024.0) + " Mb"
        } else {
            numberFormat.format(numb) + " Kb"
        }
    }

    fun rotateBitmap(bitmap: Bitmap, i: Int): Bitmap? {
        val matrix = Matrix()
        when (i) {
            1 -> return bitmap
            2 -> matrix.setScale(-1.0f, 1.0f)
            3 -> matrix.setRotate(180.0f)
            4 -> {
                matrix.setRotate(180.0f)
                matrix.postScale(-1.0f, 1.0f)
            }
            5 -> {
                matrix.setRotate(90.0f)
                matrix.postScale(-1.0f, 1.0f)
            }
            6 -> matrix.setRotate(90.0f)
            7 -> {
                matrix.setRotate(-90.0f)
                matrix.postScale(-1.0f, 1.0f)
            }
            8 -> matrix.setRotate(-90.0f)
            else -> return bitmap
        }
        return try {
            val createBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            createBitmap
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }

    }
}
