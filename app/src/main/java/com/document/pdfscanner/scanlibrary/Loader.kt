package com.document.pdfscanner.scanlibrary

import kotlin.jvm.Synchronized

/**
 * A class to load the required native Libraries
 * @author Nalin Angrish, Sekar.
 */
object Loader {
    /** Whether we have already loaded the native libraries or not  */
    private var done = false

    /**
     * Load the native libraries.
     */
    @JvmStatic
    @Synchronized
    fun load() {
        if (done) return
        System.loadLibrary("Scanner")
        //        System.loadLibrary("Scanner");
        System.loadLibrary("opencv_java3")
        done = true
    }
}