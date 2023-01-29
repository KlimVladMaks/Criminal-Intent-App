package com.example.criminal_intent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminal_intent.Crime

// Создаём абстрактный класс (методы всегда открыт, нельзя создать экземпляры) для базы данных
// Указываем, что в качестве сущностей для базы данных будет использоваться класс Crime
// (Указываем первую версию, так как в дальнейшем база дынных может быть расширена для приёма других сущностей)
// Добавляем класс с конвертерами CrimeTypeConverters для преобразования сложных объектов в примитивы и обратно
// Указываем вторую версию, так как был добавлен дополнительный пункт - имя подозреваемого
@Database(entities = [Crime::class], version=2)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase: RoomDatabase() {

    // Генерируем конкретную реализацию CrimeDao, чтобы обращаться к ней при работе с базой данных
    abstract fun crimeDao(): CrimeDao
}

// Создаём объект Migration для обновления базы данных от 1-й ко 2-й версии
// (Данный объект используется, чтобы сохранить старые базы данных на устройствах пользователя.
// Без использования данного объекта на устройствах пользователей будут созданны новые пустые базы данных)
val migration_1_2 = object: Migration(1, 2) {

    // Переопреедляем функцию migrate, которая получает старую базу данных в качестве экземпляра
    override fun migrate(database: SupportSQLiteDatabase) {

        // Добовляем в базу данных дополнительный столбец "suspect", содержащий имя подозреваемого
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}


