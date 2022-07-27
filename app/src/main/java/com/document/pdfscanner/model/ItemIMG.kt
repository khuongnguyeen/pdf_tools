package com.document.pdfscanner.model

class ItemIMG {
    var name: String? = null
    var path: String? = null

    constructor() {}
    constructor(name: String?, path: String?) {
        this.path = path
        this.name = name
    }
}