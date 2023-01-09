package com.example.criminal_intent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

// Данный файл является частью контроллера

// Создаём класс фрагмента
class CrimeFragment: Fragment() {

    // Создаём переменную для хранения экземпляра класса Crime
    private lateinit var crime: Crime

    // Создаём переменную для хранения поля для ввода текста
    private lateinit var titleField: EditText

    // Создаём переменную для хранения кнопки с датой
    private lateinit var dateButton: Button

    // Создаём переменную для хранения окошка с галочкой
    private lateinit var solvedCheckBox: CheckBox

    // Создаём переменную для окошка с галочкой - "Requires Police"
    private lateinit var requiresPoliceCheckBox: CheckBox

    // Переопределяем функцию создания фрагмента
    // (Данная функция должна быть открытой, чтобы вызываться произвольной Activity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём экземпляр класса Crime
        crime = Crime()
    }

    // Переопределяем функцию для заполнения представления фрагмента
    // (Функция возвращает представление в виде View)
    override fun onCreateView(
        inflater: LayoutInflater, // Объект для создания экземпляра View
        container: ViewGroup?, // Объект, в который нужно поместить представление фрагмента
        savedInstanceState: Bundle? // Сохранённая информация о предыдущем состоянии фрагмента
    ): View? {

        // Создаём объект представления View
        // (Указываем: XML-макет фрагмента; объект, в который нужно поместить фрагмент;
        // информацию, нужно ли включать заполненое представление в родителя)
        // (В данном случае, включать заполненное представление в родителя не нужно, так как
        // представление будет в дальнейшем добалено в контейнер Activity, которая сама обработает
        // этот момент позже)
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        // Инициализируем текстовове поле, используя ID и XML-макета, подключённого во view
        titleField = view.findViewById(R.id.crime_title) as EditText

        // Инициализируем кнопку с датой
        dateButton = view.findViewById(R.id.crime_date) as Button

        // Инициализируем окошко с галочкой "Преступление решено"
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        // Инициализируем окошко с галочкой "Требуется полиция"
        requiresPoliceCheckBox = view.findViewById(R.id.requires_police_checkbox) as CheckBox

        // Добавляем в кнопку текущую дату и выключаем кнопку (делаем нечувствительной к нажатиям)
        dateButton.apply {
            text = crime.date.toString()
            isEnabled = false
        }

        // Возвращаем созданный объект View
        return view
    }

    // Переопределяем функцию, вызываемую при запуске фрагмента
    override fun onStart() {
        super.onStart()

        // Создаём класс-одиночку (создаётся в единственном экземпляре), наследуя его от
        // класса-наблюдателя за текстовым полем
        // (Использование данного класса в onStart() позволяет сохранить текст при повороте устройства)
        val titleWatcher = object: TextWatcher {

            // Функция, работающая до изменения текста
            // (Параметры функции несут информацию об изменениях текста)
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int, //
                count: Int,
                after: Int
            ) {
                // Это пространство оставленно пустым специально
            }

            // Функция, работающая вовремя изменения текста
            // (Параметры функции несут информацию об изменениях текста)
            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Записываем в заголовок класса преступлния тот текст, который ввёл пользователь
                crime.title = sequence.toString()
            }

            // Функция, работающая после измненеия текста
            // (Параметры функции несут информацию об изменениях текста)
            override fun afterTextChanged(sequence: Editable?) {
                // Это пространство оставленно пустым специально
            }
        }

        // Добавляем к инициализированному текстовому полю созданного слешателя
        titleField.addTextChangedListener(titleWatcher)

        // Добовляем к окошку с галочкой слушателя, проверяющего, была ли установлена галочка
        // и записывающего информацию об этом (true или false) в экземпляр класса Crime
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        // Добовляем к окошку с галочкой слушателя, проверяющего, была ли установлена галочка
        // и записывающего информацию об этом (true или false) в экземпляр класса Crime
        requiresPoliceCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.requiresPolice = isChecked
            }
        }
    }
}


