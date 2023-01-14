package com.example.criminal_intent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.criminal_intent.Crime

// Создаём абстрактный класс (методы всегда открыт, нельзя создать экземпляры) для базы данных
// Указываем, что в качестве сущностей для базы данных будет использоваться класс Crime
// Указываем первую версию, так как в дальнейшем база дынных может быть расширена для приёма других сущностей
// Добавляем класс с конвертерами CrimeTypeConverters для преобразования сложных объектов в примитивы и обратно
@Database(entities = [Crime::class], version=1)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase: RoomDatabase() {

    // Генерируем конкретную реализацию CrimeDao, чтобы обращаться к ней при работе с базой данных
    abstract fun crimeDao(): CrimeDao
}


