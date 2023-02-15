package com.example.criminal_intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*

// Данный файл является частью котроллера

// Создаём тэг для отладки MainActivity
private const val TAG = "MainActivityTAG"

// Создаём главный Activity-класс, с которого начинается работа приложения
// Добавляем в MainActivity интерфейс CrimeListFragment.Callbacks для реализации функции обратного вызова
// Добавляем в MainActivity интерфейс CrimeFragment.Callbacks для реализации функции обратного вызова
class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

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

    // Переопределяем функцию, вызываемую при нажатии на пресступление
    // Функция принимает на вход ID преступления
    override fun onCrimeSelected(crimeId: UUID) {

        // Создаём экземпляр фрагмента CrimeFragment, передавая ему ID переданного преступления
        val fragment = CrimeFragment.newInstance(crimeId)

        // Устанавливаем экземпляр CrimeFragment в качестве нового фрагмента, подключая его к макету
        // (Заменяем старый фрагмент на новый фрагмент)
        // Функция addToBackStack() позволяет поместить старый фрагмент в обратный стек и вернуться
        // к нему при нажатии кнопки "Назад" (в качестве аргуемнта функция принимает название состояния стека)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Переопределяем функцию, вызываемую при удалении преступления
    override fun onCrimeDelete() {

        // Удаляем фрагмент, который находится сверху стэка
        // (В данном случае закрываем окно с карточкой преступления, так как данное преступление уже удалено)
        supportFragmentManager.popBackStack()
    }
}


