package com.document.pdfscanner.model

import java.util.*

class BackList private constructor() {
    var list = ArrayList<DataListMerge>()
    val list2 = ArrayList<DataListMerge>()
    var number = 0
        get() = if (!firstOne) {
            field
        } else {
            3
        }
        set(i) {
            firstOne = false
            field = i
        }
    private var firstOne = true
    var fO = true
    fun setList(itemPDFModel: DataListMerge) {
        list.add(itemPDFModel)
    }

    companion object {
        var instance: BackList? = null
            get() {
                if (field == null) {
                    field = BackList()
                }
                return field
            }
            private set
    }
}