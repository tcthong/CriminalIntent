package com.example.criminalintent.database

import androidx.room.TypeConverter
import java.util.*


class CrimeTypeConverters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millis: Long?): Date? {
        return millis?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun fromUuid(uuid: UUID?): String? {
        return uuid.toString()
    }

    @TypeConverter
    fun toUuid(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
}