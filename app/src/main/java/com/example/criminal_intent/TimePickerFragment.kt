package com.example.criminal_intent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

// Создаём аргумент для передачи данных о выбранном времени в DatePickerFragment
private const val ARG_TIME = "time"

// Создаём класс для отображения диалогового окна с выбором времени совершения преступления
class TimePickerFragment: DialogFragment() {

    // Создаём интерфейс обратного вызова фрагмента
    interface Callbacks {

        // Функция, позволяющая получить выбранное в диалоговом окне время
        fun onTimeSelected(date: Date)
    }

    // Переопреедляем функцию, вызываемую при выводе DialogFragment на экран
    // (Функция создаёт и возвращает диалоговое окно - Dialog)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Записываем слушателя в переменную timeListener
        val timeListener = TimePickerDialog.OnTimeSetListener {

            // Слушатель принимает следующие данные:
            // TimePicker (помечен как "_" так как не используется), от которого исходит результат;
            // выбранные час и минуту
            _: TimePicker, hour: Int, minute: Int ->

            // Записываем в переменную resultTime объект типа Date, в котором год, месяц и день выбраны
            // по умолчанию, а час и минута считанны слушателем
            val resultTime: Date = GregorianCalendar(1970, 1, 1, hour, minute).time

            // Через обратный вызов возвращаем созданный выше объект типа Date
            targetFragment.let {
                (it as Callbacks).onTimeSelected(resultTime)
            }
        }

        // Получаем из аргументов фрагмента переданное время преступления
        val time = arguments?.getSerializable(ARG_TIME) as Date

        // Создаём экземпляр класса Calendar для работы с датами
        val calendar = Calendar.getInstance()

        // Загружаем в созданный календарь время, переданное в аргументах
        calendar.time = time

        // Получаем числовое значение часа
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Получаем числовое значение минуты
        val initialMinute = calendar.get(Calendar.MINUTE)

        // Возвращаем объект диалогового окна, передавая ему созданные выше аргументы
        return TimePickerDialog(

            // Контекстный объект, необходимый для доступа к необходимым ресурсам элемента
            requireContext(),

            // Слушатель взаимодействия с диалоговым окном
            timeListener,

            // Аргументы начального часа и минуты
            initialHour,
            initialMinute,

            // Указываем, что часы должны работать в 24-часовом формате
            true
        )
    }

    // Создаём общедоступный блок кода
    companion object {

        // Создаём функцию для получения экземпляра класса с добавленным в качестве аргумента
        // выбранным временем, представленным в формате Date
        fun newInstance(date: Date): TimePickerFragment {

            // Создаём пакет аргументов, добавляя в него переданное время
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }

            // Возвращаем экземпляр TimePickerFragment с добавленной информацие о переданном времени
            // в качестве аргумента
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}



