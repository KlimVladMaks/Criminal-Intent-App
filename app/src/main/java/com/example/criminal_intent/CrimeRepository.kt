package com.example.criminal_intent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminal_intent.database.CrimeDatabase
import com.example.criminal_intent.database.migration_1_2
import java.util.UUID
import java.util.concurrent.Executors

// Задаём имя базы данных
private const val DATABASE_NAME = "crime-database"

// Создаём шаблон репозитория для доступа к базе данных
// Класс CrimeRepository является синглтоном, то есть существует в единтсвенном экземпляре и
// удаляется после завершения работы приложения
class CrimeRepository private constructor(context: Context) {

    // Создаём саму базу данных, передавая ей контекст приложения (для даступа к файловой системе),
    // класс CrimeDatabase и выбранное имя для базы данных
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2) // Реализуем миграцию от 1-й ко 2-й версии базы данных
     .build()

    // Создаём объект доступа к данным
    private val crimeDao = database.crimeDao()

    // Создаём экземпляр исполнителя - объекта, который ссылкается на какой-либо поток
    // В данном случае исполнитель ссылается на новый фоновый поток, в котором можно безапасно работать с БД
    private val executor = Executors.newSingleThreadExecutor()

    // Функция для получения всего списка преступлений
    // (Используем LiveData для запуска запроса в фоновом потоке)
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    // Функция для получения одного преступления по его UUID
    // (Используем LiveData для запуска запроса в фоновом потоке)
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    // Создаём функцию для обновления преступления в базе данных
    fun updateCrime(crime: Crime) {

        // Запускаем блок кода в том потоке, на который ссылается исполнитель
        executor.execute {
            // Обновляем переданное преступление в базе данных
            crimeDao.updateCrime(crime)
        }
    }

    // Функция для добавления нового пресупления в базу данных
    fun addCrime(crime: Crime) {

        // Запускаем блок кода в том потоке, на который ссылается исполнитель
        executor.execute {
            // Добавляем переданное преступление в базу данных
            crimeDao.addCrime(crime)
        }
    }

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


