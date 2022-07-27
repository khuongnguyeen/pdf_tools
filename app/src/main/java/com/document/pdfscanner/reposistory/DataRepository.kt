package com.document.pdfscanner.reposistory

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.document.pdfscanner.dao.DataDao
import com.document.pdfscanner.dao.UserRoomDatabase
import com.document.pdfscanner.model.Data


class DataRepository(application: Application) {
    private var mDao: DataDao

    private var mAllData: LiveData<List<Data>>
    private var mSelectData: LiveData<List<Data>>

    init {
        val db = UserRoomDatabase.getDatabase(application)
        mDao = db.dataDao()
        mAllData = mDao.selectDataAll()
        mSelectData = mDao.selectDataByPath("")
    }
    fun selectDataByPath(path :String):LiveData<List<Data>>{
        mSelectData = mDao.selectDataByPath(path)
        return mSelectData

    }

    fun getAllDatas(): LiveData<List<Data>> {
        return mAllData
    }

    fun insertData(data: Data) {
        insertDataAsyncTask(mDao).execute(data)
    }

    fun deleteDataByPath(path: String) {
        deleteDataAsyncTask(mDao).execute(path)
    }

    fun deleteAllData() {
        deleteAllAsyncTask(mDao).execute()
    }

    private class insertDataAsyncTask internal constructor(private val mAsyncTaskDao: DataDao) :
        AsyncTask<Data, Void, Void>() {

        override fun doInBackground(vararg params: Data): Void? {

            mAsyncTaskDao.deleteDataById(params[0].path!!)
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    private class deleteDataAsyncTask  internal constructor(private val mAsyncTaskDao: DataDao) :
        AsyncTask<String, Void, Void>() {

        override fun doInBackground(vararg params: String?): Void? {
            params[0]?.let { mAsyncTaskDao.deleteDataById(it)
                Log.d("qqxxxx","Delete " +" path.OK!!")
            }
            return null
        }
    }

    private class deleteAllAsyncTask internal constructor(private val mAsyncTaskDao: DataDao) :
        AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            mAsyncTaskDao.deleteAllData()
            return null
        }
    }
    private class deleteDataByPathAsyncTask internal constructor(private val mAsyncTaskDao: DataDao) :
        AsyncTask<String, Void, Void>() {

        override fun doInBackground(vararg params: String): Void? {
             mAsyncTaskDao.selectDataByPath(params[0])
            return null
        }
    }

}