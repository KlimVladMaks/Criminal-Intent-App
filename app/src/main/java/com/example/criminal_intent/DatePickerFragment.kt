package com.example.criminal_intent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

// Создаём аргумент для передачи данных о выбранной дате в DatePickerFragment
private const val ARG_DATE = "date"

// Создаём класс для отображения диалогового окна с календарём для выбора даты преступления
class DatePickerFragment: DialogFragment() {

    // Создаём интерфейс обратного вызова фрагмента
    interface Callbacks {

        // Функция, позволяющая получить выбранную в диалоговом окне (календаре) дату
        fun onDateSelected(date: Date)
    }

    // Переопреедляем функцию, вызываемую при выводе DialogFragment на экран
    // (Функция создаёт и возвращает диалоговое окно - Dialog)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Добавляем слушателя для календаря
        val dateListener = DatePickerDialog.OnDateSetListener {

            // Слушатель принимает следующие данные:
            // DatePicker (помечен как "_" так как не используется), от которого исходит результат;
            // выбранные год, месяц и день
            _: DatePicker, year: Int, month: Int, day: Int ->

            // Собираем выбранные год, месяц и день в один объект Date
            val resultDate: Date = GregorianCalendar(year, month, day).time

            // (В targetFragment хранится экземпляр фрагмента, запустившего DatePickerFragment)
            // (Так как в нём значение null, его нужно обернуть в безопасный вызов let)
            //  (Затем экземпляр фрагмента передается в интерфейс Callbacks и вызывается функция
            //  onDateSelected(), передающая новую дату)
            targetFragment.let {
                (it as Callbacks).onDateSelected(resultDate)
            }
        }

        // Получаем из аргументов фрагмента переданную дату преступления
        val date = arguments?.getSerializable(ARG_DATE) as Date

        // Создаём экземпляр класса Calendar для работы с датами
        val calendar = Calendar.getInstance()

        // Загружаем в созданный календарь дату, переданную в аргументах
        calendar.time = date

        // Получаем числовое значение года
        val initialYear = calendar.get(Calendar.YEAR)

        // Получаем числовое значение месяца
        val initialMonth = calendar.get(Calendar.MONTH)

        // Получаем числовое значение дня
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Возвращаем объект диалогового окна, передавая ему созданные выше аргументы
        return DatePickerDialog(

            // Контекстный объект, необходимый для доступа к необходимым ресурсам элемента
            requireContext(),

            // Слушатель взаимодействия с диалоговым окном
            dateListener,

            // Аргументы начального года, месяца и дня
            initialYear,
            initialMonth,
            initialDay
        )
    }

    // Создаём общедоступный блок кода
    companion object {

        // Создаём функцию для получения экземпляра класса с добавленной в качестве аргумента выбранной датой
        fun newInstance(date: Date): DatePickerFragment {

            // Создаём пакет аргументов, добавляя в него переданную дату
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            // Возвращаем экземпляр DatePickerFragment с добавленной информацие о переданной дате
            // в качестве аргумента
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}