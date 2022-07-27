package com.document.pdfscanner.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

import android.os.AsyncTask
import com.document.pdfscanner.model.Data


@Database(entities = [Data::class], version = 1,exportSchema = false)
abstract class UserRoomDatabase : RoomDatabase() {

abstract fun dataDao(): DataDao
    private class UserDatabaseCallback : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            INSTANCE?.let { PopulateDbAsync(it).execute() }

        }
    }

    private class PopulateDbAsync internal constructor(db: UserRoomDatabase) :
        AsyncTask<Void, Void, Void>() {

        private val mKeyDao : DataDao
        init {
            mKeyDao = db.dataDao()
        }

        override fun doInBackground(vararg params: Void): Void? {

            return null
        }
    }
    companion object {
        @Volatile
        private var INSTANCE: UserRoomDatabase? = null
        fun getDatabase(context: Context): UserRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserRoomDatabase::class.java,
                    "user_database"
                ).fallbackToDestructiveMigration()
                    .addCallback(UserDatabaseCallback())
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }

}