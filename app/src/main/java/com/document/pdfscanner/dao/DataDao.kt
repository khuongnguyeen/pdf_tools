package com.document.pdfscanner.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.document.pdfscanner.model.Data


@Dao
interface DataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: Data)

    @Query("SELECT * FROM table_info ")
    fun selectDataAll(): LiveData<List<Data>>

    @Query("DELETE FROM table_info")
    fun deleteAllData()

    @Query("DELETE FROM table_info WHERE path = :path")
    fun deleteDataById(path: String)

    @Query("SELECT * FROM table_info where path = :path")
    fun selectDataByPath(path : String):LiveData<List<Data>>


}