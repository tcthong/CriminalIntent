package com.example.criminalintent

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminalintent.database.CrimeDao
import com.example.criminalintent.database.CrimeDatabase
import com.example.criminalintent.database.migration_1_2
import java.io.File
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)
        .build()
    private val crimeDao: CrimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val fileDirs = context.applicationContext.filesDir

    companion object {
        private var instance: CrimeRepository? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = CrimeRepository(context)
            }
        }

        fun getInstance(): CrimeRepository {
            return instance ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrimeById(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(fileDirs, crime.photoFileName)
}