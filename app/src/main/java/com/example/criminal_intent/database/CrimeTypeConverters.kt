package com.example.criminal_intent.database

import android.util.Log
import androidx.room.TypeConverter
import java.util.*

private const val TAG = "CrimeTypeConvertersTAG"

// Создаём класс, содержащий функции для конвертирования сложных значений в примитивные и обратно
// для корректной работы базы данных Room
class CrimeTypeConverters {

    // Функция, конвертирующуая дату в длинное целое число (примитив)
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    // Функция, конвертирующая число секунд с начала эпохи в объект типа Date
    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it) }
    }

    // Функция, конвертирующая UUID в строку
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    // Функция, конвертирующая строку с ID в объект типа UUID
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
}