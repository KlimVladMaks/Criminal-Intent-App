package com.example.criminal_intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// Данный файл является частью котроллера

// Создаём главный Activity-класс, с которого начинается работа приложения
class MainActivity : AppCompatActivity() {

    // Переопределяем функцию для инициализации приложения
    override fun onCreate(savedInstanceState: Bundle?) {
        // Вызываем основной функционал функции onCreate()
        super.onCreate(savedInstanceState)
        // Подключаем к Activity XML-макет экрана
        setContentView(R.layout.activity_main)
    }
}


