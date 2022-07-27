package com.document.pdfscanner.interfaces

import com.document.pdfscanner.model.ItemPDFModel

interface ItemClickListener {
    fun onItemClickListener(itemPDFModel: ItemPDFModel)
}