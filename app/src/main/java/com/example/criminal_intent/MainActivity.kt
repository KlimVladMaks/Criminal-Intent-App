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

        // Записываем в переменную фрагмент по ID его макета
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Если макет фрагмента пуст (т.е. фрагмент к нему не подключён)
        if (currentFragment == null) {
            // То создаём экземпляр фрагмента CrimeListFragment
            val fragment = CrimeListFragment.newInstance()
            // И подключаем его к данному макету (проводим транзакцию фрагмента)
            // (Создать новую транзакцию фрагмента, включить в нее одну операцию add, а затем закрепить)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
}


