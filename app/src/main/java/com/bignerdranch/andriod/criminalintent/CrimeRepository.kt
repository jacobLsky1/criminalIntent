package com.bignerdranch.andriod.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.andriod.criminalintent.database.CrimeDatabase
import com.bignerdranch.andriod.criminalintent.database.migration_1_2
import com.bignerdranch.andriod.criminalintent.database.migration_2_3
import java.io.File
import java.util.*
import java.util.concurrent.Executors


private const val DATABASE_NAME = "crime-database"
class CrimeRepository private constructor(context: Context) {

    private val database : CrimeDatabase = Room.databaseBuilder(context.applicationContext, CrimeDatabase::class.java, DATABASE_NAME).addMigrations(migration_2_3).build()
    private val crimeDao = database.crimeDao()
    private  val extractor = Executors.newSingleThreadExecutor()
    private  val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)


    fun updatCrime(crime:Crime){
        extractor.execute{
            crimeDao.updateCrime(crime)
        }
    }
    fun addCrime(crime:Crime)
    {
        extractor.execute{
            crimeDao.addCrime(crime)
        }
    }
    fun getPhotoFile(crime: Crime):File = File(filesDir,crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }
        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}