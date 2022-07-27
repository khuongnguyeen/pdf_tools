package com.document.pdfscanner.internal

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicConvolve3x3
import android.util.Log
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.*
import java.util.*
import kotlin.math.roundToInt

object Statics {
    private const val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"

    private val PageWidth = PageSize.A3.width.roundToInt()


    fun randString(): String {
        val builder = StringBuilder()
        for (i in 0..9) {
            val index = (chars.length * Math.random()).toInt()
            builder.append(chars[index])
        }
        return builder.toString()
    }


    @Throws(IOException::class, DocumentException::class)
    fun createPdf(app: Application, name: String):String {
        val sb = StringBuilder()
        sb.append(Environment.getExternalStorageDirectory())
        sb.append("/PDFtools")
        val pdfFolder = File(sb.toString())
        Log.e("Test crash","Save ___________ file ___________ to__________ ${sb.toString()}")
        if (!pdfFolder.exists()) {
            println(pdfFolder.mkdir())
        }
        val pdf = File(pdfFolder, "$name.pdf")
        if (!pdf.exists()) {
            println(pdfFolder.createNewFile())
        }

        val path = pdf.path

        val sessionName = app.getSharedPreferences("PDFtools", Context.MODE_PRIVATE)
            .getString("sessionName", "hello")
        val imageFolder = File(app.filesDir, sessionName)
        val images: MutableList<Bitmap> = ArrayList()
        val files = imageFolder.listFiles()
        for (eachFile in files) {
            images.add(BitmapFactory.decodeFile(eachFile.absolutePath))
        }
        val doc = Document(PageSize.A4, 0f, 0f, 0f, 0f)
        PdfWriter.getInstance(doc, FileOutputStream(pdf)).compressionLevel = 9
        doc.open()
        doc.addAuthor("Adtrue - The Document Scanner")
        for (image in images) {
            val pw = PageWidth.toFloat()
            val iw = image.width.toFloat()
            val ih = image.height.toFloat()
            val rat = pw / iw
            val ph = rat * ih
            val page = Bitmap.createScaledBitmap(
                image,
                pw.toInt(),
                ph.toInt(),
                true
            ) // scale the bitmap so that the page width is standard (it looks clean and good)
            doc.pageSize = Rectangle(pw, ph)
            doc.newPage()
            val baos = ByteArrayOutputStream()
            doSharpen(app, doSharpen(app, page)).compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val img = Image.getInstance(baos.toByteArray())
            doc.add(img)
        }
        doc.close()

        MediaScannerConnection.scanFile(app,
            arrayOf(pdf.path),
            arrayOf("application/pdf"),
            null
        )

        val thumbnailFile = getThumbnail(app)
        val dataStorage = File(pdfFolder, ".data-internal")
        if (!dataStorage.exists()) {
            println(dataStorage.mkdir())
        }
        val thumbnail = File(dataStorage, "$name.jpg")
        if (!thumbnail.exists()) {
            thumbnail.createNewFile()
            copy(thumbnailFile, thumbnail)
        }
        clearData(app)
        return path
    }

    fun clearData(app: Application) {
        val folder = app.filesDir
        val deletable = File(
            folder,
            app.getSharedPreferences("PDFtools", Context.MODE_PRIVATE)
                .getString("sessionName", "hello")
        )
        try {
            for (file in deletable.listFiles()) {
                println(file.delete())
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        println(deletable.delete())
    }

    private fun getThumbnail(app: Application): File? {
        val folder = app.getSharedPreferences("PDFtools", Context.MODE_PRIVATE)
            .getString("sessionName", "hello")
        val all = File(app.filesDir, folder).listFiles()
        return if (all != null && all.size > 0) {
            all[0]
        } else null
    }

    @Throws(IOException::class)
    private fun copy(src: File?, dst: File) {
        FileInputStream(src).use { inputStream ->
            FileOutputStream(dst).use { outputStream ->
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
            }
        }
    }

    fun isAvailable(name: String): Boolean {
        val sb = StringBuilder()
        sb.append(Environment.getExternalStorageDirectory())
        sb.append("/PDFtools")
        val folder =  File(sb.toString())
        if (!folder.exists()) {
            println(folder.mkdir())
        }
        val files = folder.list() ?: return true

        for (file in files) {
            if (file.toLowerCase(Locale.ROOT) == name.toLowerCase(Locale.ROOT) + ".pdf") {
                return false
            }
        }
        return true
    }

    fun doSharpen(ctx: Context?, original: Bitmap): Bitmap {
        val matrix =
            floatArrayOf(-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f)
        val bitmap = Bitmap.createBitmap(
            original.width, original.height,
            Bitmap.Config.ARGB_8888
        )
        val rs = RenderScript.create(ctx)
        val allocIn = Allocation.createFromBitmap(rs, original)
        val allocOut = Allocation.createFromBitmap(rs, bitmap)
        val convolution = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs))
        convolution.setInput(allocIn)
        convolution.setCoefficients(matrix)
        convolution.forEach(allocOut)
        allocOut.copyTo(bitmap)
        rs.destroy()
        return bitmap
    }
}