package com.example.criminal_intent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.criminal_intent.Crime

// Создаём абстрактный класс (методы всегда открыт, нельзя создать экземпляры) для базы данных
// Указываем, что в качестве сущностей для базы данных будет использоваться класс Crime
// Указываем первую версию, так как в дальнейшем база дынных может быть расширена для приёма других сущностей
@Database(entities = [Crime::class], version=1)
abstract class CrimeDatabase: RoomDatabase() {
}


