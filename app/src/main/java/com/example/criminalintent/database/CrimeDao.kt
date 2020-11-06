package com.example.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.criminalintent.Crime
import java.util.*

@Dao
interface CrimeDao {
    @Query("select * from Crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("select * from crime where id = (:id)")
    fun getCrimeById(id: UUID): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)
}