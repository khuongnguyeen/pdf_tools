package com.document.pdfscanner.ulti


import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.document.pdfscanner.R
import com.document.pdfscanner.model.GalleryFolder
import com.document.pdfscanner.model.ItemIMG
import com.document.pdfscanner.model.ItemPDFModel

import java.io.File
import java.text.DecimalFormat
import java.util.ArrayList

class FileStorage {
    var parentList = ArrayList<ItemPDFModel>()
    fun getListFilePDF(dir: File): ArrayList<ItemPDFModel> {
        val tempPDFList = ArrayList<ItemPDFModel>()
        val pdfPattern = ".pdf"
        var str: String
        val fileList = dir.listFiles()
        if (fileList != null && fileList.isNotEmpty()) {
            for (i in fileList.indices) {

                if (fileList[i].isDirectory) {
                    tempPDFList.addAll(getListFilePDF(fileList[i]))

                } else {
                    if (fileList[i].name.toLowerCase().endsWith(pdfPattern)) {
                        var itemPDFModel = ItemPDFModel()
                        str = try {
                            fileList[i].name.substring(0, fileList[i].name.length - 4)
                        } catch (e: Exception) {
                            "null"
                        }
                        itemPDFModel = ItemPDFModel(
                            convertUnits(fileList[i].length().toDouble()),
                            str,
                            fileList[i].absolutePath
                        )
                        tempPDFList.add(itemPDFModel)
                    }
                }
            }
        }
        return tempPDFList
    }

    fun getListFileByFolder(dir: File): ArrayList<ItemPDFModel> {
        val tempPDFList = ArrayList<ItemPDFModel>()
        val pdfPattern = ".pdf"
        var str: String
        val fileList = dir.listFiles()
        if (fileList != null && fileList.isNotEmpty()) {
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    tempPDFList.addAll(getListFileByFolder(fileList[i]))
                } else {
                    if (fileList[i].name.toLowerCase().endsWith(pdfPattern)) {
                        var itemPDFModel = ItemPDFModel()
                        str = try {
                            fileList[i].name.substring(0, fileList[i].name.length - 4)
                        } catch (e: Exception) {
                            "null"
                        }
                        itemPDFModel = ItemPDFModel(
                            convertUnits(fileList[i].length().toDouble()), str,
                            fileList[i].absolutePath)
                        tempPDFList.add(itemPDFModel)
                    }
                }
            }
        }
        return tempPDFList
    }

    fun getListFileIMG(dir: File): ArrayList<ItemIMG> {
        val tempPDFList = ArrayList<ItemIMG>()
        val jpgPattern = ".jpg"
        var str: String
        val fileList = dir.listFiles()
        if (fileList != null && fileList.isNotEmpty()) {
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    tempPDFList.addAll(getListFileIMG(fileList[i]))
                } else {
                    if (fileList[i].name.endsWith(jpgPattern)) {
                        var itemimg = ItemIMG()
                        str = try {
                            fileList[i].name.substring(0, fileList[i].name.length - 4)
                        } catch (e: Exception) {
                            "null"
                        }
                        itemimg = ItemIMG(str, fileList[i].absolutePath)
                        tempPDFList.add(itemimg)
                    }
                }
            }
        }

        return tempPDFList
    }

    fun getList(file: File): java.util.ArrayList<ItemPDFModel> {
        Log.d("eaaaa", "rrrr")
        var start = System.currentTimeMillis()
        var str: String
        val listFiles = file.listFiles()
        if (listFiles != null && listFiles.isNotEmpty()) {
            for (i in listFiles.indices) {
                if (listFiles[i].isDirectory) {
                    if (!listFiles[i].name.endsWith("Samsung") && !listFiles[i].name.endsWith("Android") && !listFiles[i].name.endsWith(
                            "Huawei"
                        ) && !listFiles[i].name.endsWith("DCIM") && !listFiles[i].name.endsWith("Pictures") && !listFiles[i].name.endsWith(
                            "Music"
                        ) && !listFiles[i].name.endsWith("Zing MP3") && !listFiles[i].name.endsWith(
                            "NCT"
                        ) && !listFiles[i].name.endsWith("Zing TV")
                    ) {
                        getList(listFiles[i])
                    }
                } else {
                    if (listFiles[i].name.toLowerCase().endsWith(".pdf")) {
                        try {
                            str = listFiles[i].name.substring(0, listFiles[i].name.length - 4)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            str = "file"
                        }
                        parentList.add(
                            ItemPDFModel(
                                convertUnits(listFiles[i].length().toDouble()),
                                str,
                                listFiles[i].path
                            )
                        )
                    }
                }
            }
            Log.d("eaaee", "${System.currentTimeMillis() - start}")
        }
        return parentList
    }

    fun allPdfs(context: Context): ArrayList<ItemPDFModel> {
        val ss = System.currentTimeMillis()
        val str: String

        val arrayList = ArrayList<ItemPDFModel>()
        val contentResolver = context!!.contentResolver
        val contentUri = MediaStore.Files.getContentUri("external")
        try {
            val query = contentResolver.query(contentUri, arrayOf("_data"), "mime_type=?",
                arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!), null)
            if (query != null && query!!.moveToFirst()) {
                do {
                    val file = File(query!!.getString(query!!.getColumnIndex("_data")))
                    if (file.length() != 0L) {

                        val pdf = ItemPDFModel(
                            convertUnits(file.length().toDouble()),
                            file.name,
                            file.path
                        )
                        arrayList.add(pdf)
                    }
                } while (query!!.moveToNext())
                query!!.close()
                Log.d("eaaee", "${System.currentTimeMillis() - ss}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return arrayList
    }

    private fun convertUnits(nb: Double): String {
        val numberFormat = DecimalFormat("#.0")
        val numb = nb!! / 1024.0
        return if (numb >= 1000.0) {

            numberFormat.format(numb / 1024.0) + " Mb"
        } else {
            numberFormat.format(numb) + " Kb"
        }
    }

}