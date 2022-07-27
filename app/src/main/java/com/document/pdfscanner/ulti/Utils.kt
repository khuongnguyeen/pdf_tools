@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
package com.document.pdfscanner.ulti

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView

import com.document.pdfscanner.BuildConfig
import com.document.pdfscanner.R
import com.document.pdfscanner.model.ItemPDFModel
import com.shockwave.pdfium.PdfiumCore
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import cn.pedant.SweetAlert.SweetAlertDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

object Utils {
    const val TAG = "Utils"
    fun isFileNameValid(str: String): Boolean {
        val trim = str.trim { it <= ' ' }
        return !TextUtils.isEmpty(trim) && trim.matches(Regex("[a-zA-Z0-9-_ ]*"))
    }

    fun showDialogSweet(context: Context, type: Int, string: String){
        SweetAlertDialog(context, type).apply {
            contentText = string
            show()
        }
    }

    private fun getFileInfoFromFile(file: File): ItemPDFModel {
        val fileInfo = ItemPDFModel(
            name = file.name,
            path = file.path,
            size = formatFileSize(file.length())
        )
        val lastDotIndex = file.name.lastIndexOf(".")
        if (lastDotIndex > 0) {
            val fileSuffix = file.name.substring(lastDotIndex + 1)
            //            fileInfo.setSuffix(fileSuffix);
        }
        return fileInfo
    }


    fun formatFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        fileSizeString = when {
            fileS < 1024 -> {
                df.format(fileS.toDouble()) + "B"
            }
            fileS < 1048576 -> {
                df.format(fileS.toDouble() / 1024) + "KB"
            }
            fileS < 1073741824 -> {
                df.format(fileS.toDouble() / 1048576) + "MB"
            }
            else -> {
                df.format(fileS.toDouble() / 1073741824) + "GB"
            }
        }
        return fileSizeString
    }


    fun getFileMedia(context: Context?): MutableList<ItemPDFModel> {
        val mDataList = mutableListOf<ItemPDFModel>()
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.DATA
        )
        val select = "(" + MediaStore.Files.FileColumns.DATA + " LIKE '%.pdf'" + ")"

        val contentResolver = Objects.requireNonNull(context)?.contentResolver
        var cursor = contentResolver?.query(
            MediaStore.Files.getContentUri("external"),
            columns,
            select,
            null,
            null
        )
        val columnIndexOrThrowData =
            cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        while (cursor.moveToNext()) {
            val path = cursor.getString(columnIndexOrThrowData)
            val document = getFileInfoFromFile(File(path))
            mDataList.add(document)
        }
        cursor.close()
        cursor = null
        return mDataList
    }



    fun loadFileCoroutine(
        context: Context?,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?,
        mDataList: MutableList<Any>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mDataList.clear()
               val data2 = getFileMedia(context).sortWith { row1, row2 ->
                    if (File(row1.path!!).lastModified() < File(row2.path!!).lastModified()) {
                        return@sortWith 1
                    }
                    if (File(row1.path!!).lastModified() == File(row2.path!!).lastModified()) return@sortWith 0
                    -1
                }
                mDataList.addAll(listOf(data2))
                withContext(Dispatchers.Main) {
                    if (mDataList.size == 0 || mDataList.isEmpty()) {
                        if (adapter!=null) adapter!!.notifyDataSetChanged()
                    } else {
                        if (mDataList.size>10) {
                            for (i in mDataList.size - 1 downTo  10 ){
                                mDataList.removeAt(i)
                            }
                        }
                        if (adapter!=null) adapter!!.notifyDataSetChanged()
                    }
                }
            } catch (ex: Exception) {
                Log.e("getLeagues", "$ex")
            }
        }
    }



    fun deleteFiles(str: String?) {
        val file = File(str)
        if (file.exists() && file.isDirectory) {
            val sb = StringBuilder()
            sb.append("find ")
            sb.append(str)
            sb.append(" -xdev -mindepth 1 -delete")
            try {
                Runtime.getRuntime().exec(sb.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun isThumbnailPresent(context: Context, str: String?): Boolean {
        val name = File(str).name
        val sb = StringBuilder()
        sb.append(context.cacheDir)
        sb.append("/Thumbnails/")
        val sb2 = sb.toString()
        val sb3 = StringBuilder()
        sb3.append(sb2)
        sb3.append(removeExtension(name))
        sb3.append(".jpg")
        return File(sb3.toString()).exists()
    }

    @JvmStatic
    fun removeExtension(str: String): String {
        var str = str
        val lastIndexOf = str.lastIndexOf(System.getProperty("file.separator"))
        if (lastIndexOf != -1) {
            str = str.substring(lastIndexOf + 1)
        }
        val lastIndexOf2 = str.lastIndexOf(".")
        return if (lastIndexOf2 == -1) {
            str
        } else str.substring(0, lastIndexOf2)
    }

    fun generatePDFThumbnail(context: Context, str: String?) {
        val pdfiumCore = PdfiumCore(context)
        val file = File(str)
        val name = file.name
        try {
            val newDocument = pdfiumCore.newDocument(
                context.contentResolver.openFileDescriptor(
                    Uri.fromFile(file), PDPageLabelRange.STYLE_ROMAN_LOWER
                )
            )
            val sb = StringBuilder()
            sb.append(context.cacheDir)
            sb.append("/Thumbnails/")
            val sb2 = sb.toString()
            val file2 = File(sb2)
            if (!file2.exists()) {
                file2.mkdirs()
            }
            val sb3 = StringBuilder()
            sb3.append(sb2)
            sb3.append(removeExtension(name))
            sb3.append(".jpg")
            val sb4 = sb3.toString()
            val str2 = TAG
            val sb5 = StringBuilder()
            sb5.append("Generating thumb img ")
            sb5.append(sb4)
            Log.d(str2, sb5.toString())
            val fileOutputStream = FileOutputStream(sb4)
            pdfiumCore.openPage(newDocument, 0)
            val pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, 0) / 2
            val pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, 0) / 2
            try {
                val createBitmap =
                    Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.RGB_565)
                pdfiumCore.renderPageBitmap(
                    newDocument,
                    createBitmap,
                    0,
                    0,
                    0,
                    pageWidthPoint,
                    pageHeightPoint,
                    true
                )
                createBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)
            } catch (e: OutOfMemoryError) {
                Utils.showDialogSweet(
                    context,
                    SweetAlertDialog.ERROR_TYPE,
                    context.getString(R.string.failed_low_memory)
                )
                e.printStackTrace()
            }
            pdfiumCore.closeDocument(newDocument)
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    @JvmStatic
    fun getImageUriFromPath(toString: String): Uri {
        return Uri.fromFile(File(toString.replace(".pdf", ".jpg")))
    }

    fun getBitmap(ctx: Context?, uri: Uri?): Bitmap? {
        return Compressor(ctx)
            .setQuality(100)
            .compressToBitmap(File(uri?.path))
    }

    fun createContrast(src: Bitmap, value: Double): Bitmap? {
        // image size
        val width = src.width
        val height = src.height
        // create output bitmap
        val bmOut = Bitmap.createBitmap(width, height, src.config)
        // color information
        var A: Int
        var R: Int
        var G: Int
        var B: Int
        var pixel: Int
        // get contrast value
        val contrast = ((100 + value) / 100).pow(2.0)

        // scan through all pixels
        for (x in 0 until width) {
            for (y in 0 until height) {
                // get pixel color
                pixel = src.getPixel(x, y)
                A = Color.alpha(pixel)
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel)
                R = (((R / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
                if (R < 0) {
                    R = 0
                } else if (R > 255) {
                    R = 255
                }
                G = Color.red(pixel)
                G = (((G / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
                if (G < 0) {
                    G = 0
                } else if (G > 255) {
                    G = 255
                }
                B = Color.red(pixel)
                B = (((B / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
                if (B < 0) {
                    B = 0
                } else if (B > 255) {
                    B = 255
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B))
            }
        }
        return bmOut
    }

    fun toGrayscale(srcImage: Bitmap): Bitmap {
        val bmpGrayscale: Bitmap =
            Bitmap.createBitmap(srcImage.width, srcImage.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(srcImage, 0f, 0f, paint)
        return bmpGrayscale
    }

    fun convertUnits(nb: Double?): String {
        val numberFormat = DecimalFormat("#.0")
        val numb = nb!! / 1024.0
        return if (numb >= 1000.0) {

            numberFormat.format(numb / 1024.0) + " Mb"
        } else {
            numberFormat.format(numb) + " Kb"
        }
    }

    fun shareFile(context: Context, uri: Uri) {
        val item = File(uri.path)
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            val sb = StringBuffer()
            sb.append("application/")
            shareIntent.type = sb.toString()
            val fileUri: Uri
            fileUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                File(item.path)
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            context.startActivity(Intent.createChooser(shareIntent, "Share " + item.name))
        } catch (unused: java.lang.Exception) {
            showDialogSweet(
                context,
                SweetAlertDialog.ERROR_TYPE,
                "Don't have any application to share"
            )
        }
    }



}