package com.example.criminal_intent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminal_intent.database.CrimeDatabase
import java.util.UUID

// Задаём имя базы данных
private const val DATABASE_NAME = "crime-database"

// Создаём шаблон репозитория для доступа к базе данных
// Класс CrimeRepository является синглтоном, то есть существует в единтсвенном экземпляре и
// удаляется после завершения работы приложения
class CrimeRepository private constructor(context: Context) {

    // Создаём саму базу данных, передавая ей контекст приложения (для даступа к файловой системе),
    // класс CrimeDatabase и выбранное имя для базы данных
    // TODO: createFromAsset("crime.db") используется временно
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).createFromAsset("crime.db").build()

    // Создаём объект доступа к данным
    private val crimeDao = database.crimeDao()

    // Функция для получения всего списка преступлений
    // (Используем LiveData для запуска запроса в фоновом потоке)
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    // Функция для получения одного преступления по его UUID
    // (Используем LiveData для запуска запроса в фоновом потоке)
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    // Создаём объект, содержимое которого доступно и за пределами класса
    companion object {

        // Создаём переемнную для хранения экземпляра CrimeRepository и по-умолчанию присваиваем ей null
        private var INSTANCE: CrimeRepository? = null

        // Функция для инициализации экземпляра CrimeRepository
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        // Функция для получения экземпляра CrimeRepository
        // Если экземпляра не существует (переменная равна null), то выбрасывается ошибка
        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}

