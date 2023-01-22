package com.example.criminal_intent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.UUID

// Создаём класс CrimeDetailViewModel для запроса инофрмации о преступлении и сохранении её при
// повороте устройства
class CrimeDetailViewModel(): ViewModel() {

    // Устанавливаем связь с CrimeRepository для работы с базой данных
    private val crimeRepository = CrimeRepository.get()

    // Переменная для хранения ID отображаемого преступления
    // (Используем MutableLiveData, чтобы можно было изменять ID преступления)
    // (Изночально не содержит ID, так как данная информация загружается позже)
    private val crimeIdLiveData = MutableLiveData<UUID>()

    // Получаем информацию о выбранном преступлении из crimeRepository
    // (Используется именно LiveData, так как ViewModel-и никогда не должны выставлять публично MutableLiveData)
    // Transformations используется для преобразования данных в реальном времени (отношение "триггрер-ответ")
    // (Это позволяет лишь один раз считать значения из crimeLiveData, а потом они будут
    // автоматически обновляться в уже существующем потоке данных)
    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(crimeIdLiveData) {
        crimeRepository.getCrime(it)
    }

    // Функция для загрузки информации об ID преступления в crimeIdLiveData
    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    // Функция для сохранения изменений на карточке преступления путём обновления базы данных
    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }
}


