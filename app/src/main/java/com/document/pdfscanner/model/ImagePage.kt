package com.document.pdfscanner.model

import java.io.Serializable

class ImagePage : Serializable {
    var imageUri: String? = null
    var pageNumber = 0

    constructor() {}
    constructor(i: Int, str: String?) {
        pageNumber = i
        imageUri = str
    }
}