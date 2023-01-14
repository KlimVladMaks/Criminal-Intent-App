package com.example.criminal_intent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.criminal_intent.Crime
import java.util.UUID

// Создаём интерфейс, реализующий объект доступа к данным (DAO)
@Dao
interface CrimeDao {

    // Функция для получения всего списка преступлений
    // (Используем LiveData для запуска запроса в фоновом потоке)
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    // Функция для получения одного преступления по его UUID
    // (Используем LiveData для запуска запроса в фоновом потоке)
    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>
}


