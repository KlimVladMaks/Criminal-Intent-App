package com.example.criminal_intent

import android.app.Application

// Создаём класс, наследуя его от Application для получения доступа к жизненному циклу приложения
// Для доступа к жизненному циклу приложения, указываем данный класс в AndroidManifest,
// чтобы при запуске приложения ОС создала экземпляр CriminalIntentApplication
class CriminalIntentApplication: Application() {

    // Переопределяем функцию onCreate(), чтобы при создании приложения создавался экземпляр CrimeRepository
    override fun onCreate() {
        super.onCreate()

        // Инициализируем CrimeRepository, передавая ему экземпляр приложения
        CrimeRepository.initialize(this)
    }
}


