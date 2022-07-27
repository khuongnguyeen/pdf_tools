package com.document.pdfscanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.document.pdfscanner.model.Data
import com.document.pdfscanner.reposistory.DataRepository


class DataViewModel constructor(application: Application) : AndroidViewModel(application) {

    private var mRepository: DataRepository

    init {
        mRepository = DataRepository(application)
    }

    fun insertData(data: Data){
      return  mRepository.insertData(data)
    }
     fun getAllData():LiveData<List<Data>>{
       return mRepository.getAllDatas()
    }
   fun deleteDataByPath(path: String){
       return mRepository.deleteDataByPath(path)
    }
    fun deleteAllData(){
        return mRepository.deleteAllData()
    }
fun selectDataByPath(path : String):LiveData<List<Data>>{
    return mRepository.selectDataByPath(path)
}

}