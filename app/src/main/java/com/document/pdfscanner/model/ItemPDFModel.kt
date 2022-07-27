package com.document.pdfscanner.model

import java.io.Serializable

class ItemPDFModel : Serializable {
    var size: String? = null
    var name: String? = null
    var path: String? = null

    constructor(size: String?, name: String?, path: String?) {
        this.size = size
        this.name = name
        this.path = path
    }

    constructor() {}
}