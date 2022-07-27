package com.document.pdfscanner.model

class GalleryFolder {
    var name: String = ""
    var firstImageContainedPath = ""
    var images = ArrayList<String>()
    var isSelected = false
    constructor()

    constructor(name: String, firstImageContainedPath: String) {
        this.name = name
        this.firstImageContainedPath = firstImageContainedPath
    }

    fun add(imageItem: String) {
        images.add(imageItem)
    }
}