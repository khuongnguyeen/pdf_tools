package com.document.pdfscanner.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.document.pdfscanner.ulti.ConstantSPKey

@Entity(tableName = ConstantSPKey.TABLE_INFO)
class Data {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ConstantSPKey.COLUMN_ID)
    var id: Int? = null
    var size: String? = null
    var name: String? = null
    var path: String? = null

    @Ignore
    constructor(size: String?, name: String?, path: String?) {
        this.size = size
        this.name = name
        this.path = path
    }

    constructor() {}
}