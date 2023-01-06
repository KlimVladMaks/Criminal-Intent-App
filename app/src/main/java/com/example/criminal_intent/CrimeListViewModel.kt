package com.example.criminal_intent

import androidx.lifecycle.ViewModel

// Создаём класс для хранения списка преступлений, наследуя его от ViewModel
class CrimeListViewModel: ViewModel() {

    // Создаём список для хранения преступлений
    val crimes = mutableListOf<Crime>()

    // Вызывается при инициализации экземпляра класса
    init {
        // Создаём несколько фиктивных преступлений, чтобы наполнить список
        for (i in 0 until 100) {
            val crime = Crime()
            crime.title = "Crime #${i}"
            crime.isSolved = (i % 2 == 0)
            crimes += crime
        }
    }
}