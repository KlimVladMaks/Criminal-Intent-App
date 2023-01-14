package com.example.criminal_intent

import androidx.lifecycle.ViewModel

// Создаём класс для хранения списка преступлений, наследуя его от ViewModel
class CrimeListViewModel: ViewModel() {

    // Запрашиваем экземпляр репозитория CrimeRepository
    private val crimeRepository = CrimeRepository.get()

    // Запрашиваем список всех преступлений
    val crimesListLiveData = crimeRepository.getCrimes()
}


