package com.example.fap.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    @TypeConverter
    fun toTimestamp(value: Date?): Long? {
        return value?.let { value.time }
    }
}