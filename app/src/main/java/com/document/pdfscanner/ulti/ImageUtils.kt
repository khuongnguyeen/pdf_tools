package com.document.pdfscanner.ulti

import android.content.res.Resources
import android.graphics.*
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class ImageUtils {
    fun getCompressedBitmap(str: String?): Bitmap {
        val bitmap: Bitmap
        val bitmap2: Bitmap?
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val decodeFile = BitmapFactory.decodeFile(str, options)
        var i = options.outHeight
        var i2 = options.outWidth
        val f = i2.toFloat()
        val f2 = i.toFloat()
        val f3 = f / f2
        if (f2 > 1920.0f || f > 1080.0f) {
            when {
                f3 < 0.5625f -> {
                    i2 = (1920.0f / f2 * f).toInt()
                    i = 1920.0f.toInt()
                }
                f3 > 0.5625f -> {
                    i = (1080.0f / f * f2).toInt()
                    i2 = 1080.0f.toInt()
                }
                else -> {
                    i = 1920.0f.toInt()
                    i2 = 1080.0f.toInt()
                }
            }
        }
        with(options){
            inSampleSize = calculateInSampleSize(options, i2, i)
            inJustDecodeBounds = false
            inDither = false
            inPurgeable = true
            inInputShareable = true
            inTempStorage = ByteArray(16384)
        }
        bitmap = try {
            BitmapFactory.decodeFile(str, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            decodeFile
        }
        bitmap2 = try {
            Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888)
        } catch (e2: OutOfMemoryError) {
            e2.printStackTrace()
            null
        }
        val f4 = i2.toFloat()
        val f5 = f4 / options.outWidth.toFloat()
        val f6 = i.toFloat()
        val f7 = f6 / options.outHeight.toFloat()
        val f8 = f4 / 2.0f
        val f9 = f6 / 2.0f
        val matrix = Matrix()
        matrix.setScale(f5, f7, f8, f9)
        val canvas = Canvas(bitmap2!!)
        canvas.setMatrix(matrix)
        canvas.drawBitmap(
            bitmap,
            f8 - (bitmap.width / 2).toFloat(),
            f9 - (bitmap.height / 2).toFloat(),
            Paint(2)
        )
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun getCompressedBitmap(bArr: ByteArray): Bitmap? {
        val bitmap: Bitmap
        val bitmap2: Bitmap?
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val decodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.size, options)
        var i = options.outHeight
        var i2 = options.outWidth
        val f = i2.toFloat()
        val f2 = i.toFloat()
        val f3 = f / f2
        if (f2 > 1920.0f || f > 1080.0f) {
            when {
                f3 < 0.5625f -> {
                    i2 = (1920.0f / f2 * f).toInt()
                    i = 1920.0f.toInt()
                }
                f3 > 0.5625f -> {
                    i = (1080.0f / f * f2).toInt()
                    i2 = 1080.0f.toInt()
                }
                else -> {
                    i = 1920.0f.toInt()
                    i2 = 1080.0f.toInt()
                }
            }
        }
        with(options){
            inSampleSize = calculateInSampleSize(options, i2, i)
            inJustDecodeBounds = false
            inDither = false
            inPurgeable = true
            inInputShareable = true
            inTempStorage = ByteArray(16384)
        }
        bitmap = try {
            BitmapFactory.decodeByteArray(bArr, 0, bArr.size, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            decodeByteArray
        }
        bitmap2 = try {
            Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888)
        } catch (e2: OutOfMemoryError) {
            e2.printStackTrace()
            null
        }
        val f4 = i2.toFloat()
        val f5 = f4 / options.outWidth.toFloat()
        val f6 = i.toFloat()
        val f7 = f6 / options.outHeight.toFloat()
        val f8 = f4 / 2.0f
        val f9 = f6 / 2.0f
        val matrix = Matrix()
        matrix.setScale(f5, f7, f8, f9)
        val canvas = Canvas(bitmap2!!)
        canvas.setMatrix(matrix)
        canvas.drawBitmap(
            bitmap,
            f8 - (bitmap.width / 2).toFloat(),
            f9 - (bitmap.height / 2).toFloat(),
            Paint(2)
        )
        return bitmap2
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, i: Int, i2: Int): Int {
        var i3: Int
        val i4 = options.outHeight
        val i5 = options.outWidth
        if (i4 > i2 || i5 > i) {
            i3 = (i4.toFloat() / i2.toFloat()).roundToInt()
            val round = (i5.toFloat() / i.toFloat()).roundToInt()
            if (i3 >= round) {
                i3 = round
            }
        } else {
            i3 = 1
        }
        while ((i5 * i4).toFloat() / (i3 * i3).toFloat() > (i * i2 * 2).toFloat()) {
            i3++
        }
        return i3
    }

    companion object {
        var mInstant: ImageUtils? = null
        val instant: ImageUtils?
            get() {
                if (mInstant == null) {
                    mInstant = ImageUtils()
                }
                return mInstant
            }
    }
}