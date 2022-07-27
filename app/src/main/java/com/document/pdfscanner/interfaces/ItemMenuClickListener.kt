package com.document.pdfscanner.interfaces

import com.document.pdfscanner.model.ItemPDFModel

interface ItemMenuClickListener {
    fun onItemMenuClick(itemPDFModel: ItemPDFModel)
}