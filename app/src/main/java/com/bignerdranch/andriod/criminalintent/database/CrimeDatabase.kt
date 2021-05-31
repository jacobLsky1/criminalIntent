package com.bignerdranch.andriod.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.andriod.criminalintent.*

@Database(entities = [ Crime::class ], version = 3)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao

}
val migration_1_2 = object :Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
    }
}
val migration_2_3 = object :Migration(2,3){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspectNumber TEXT NOT NULL DEFAULT ''")
    }
}

// IMPORTENT!!!! migration are importent for the database because if the the app does not have
//migrtions and updates the database all the exsiting data WILL BE LOST!!!!
// because the database is destroyed and built again from scratch