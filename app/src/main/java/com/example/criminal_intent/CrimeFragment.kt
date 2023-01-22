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
import androidx.lifecycle.ViewModelProvider
import java.util.*

// Данный файл является частью контроллера

// Создаём тэг для вывода отладочных сообщений
private const val TAG = "CrimeFragmentTAG"

// Создаём тэг для обрещения к ID преступления в пакете Bundle
private const val ARG_CRIME_ID = "crime_id"

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

    // Лениво инициализируем экземпляр CrimeDetailViewModel, привязывая его к данному фрагменту
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    // Переопределяем функцию создания фрагмента
    // (Данная функция должна быть открытой, чтобы вызываться произвольной Activity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём экземпляр класса Crime
        crime = Crime()

        // Получаем ID выбранного преступления из пакета аргументов фрагмента
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID

        // Загружаем в crimeDetailViewModel информацию об ID выбранного преступления
        crimeDetailViewModel.loadCrime(crimeId)
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

    // Переопределяем функцию, вызываемую сразу после onCreateView(), когда представление уже задано
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем наблюдателя за состоянием LiveData
        // Наблюдатель реагирует каждый раз, когда изменяются данные в LiveData
        crimeDetailViewModel.crimeLiveData.observe(

            // viewLifecycleOwner следит за жизненным циклом фрагмента и
            // не позволяет обновить данные, когда фрагмент находится в нерабочем состоянии
            viewLifecycleOwner,

            // Загружаем информацию о выбранном преступлении в собственное представление crime
            // данного фрагмента и обновляем его интерфейс
            androidx.lifecycle.Observer {
                it?.let {
                    this.crime = it
                    updateUI()
                }
            }
        )
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

    // Переопределяем функцию, вызываемую перед остановкой фрагмента
    override fun onStop() {
        super.onStop()

        // Сохраняем изменения, внесённые в карточку перступления
        crimeDetailViewModel.saveCrime(crime)
    }

    // Обновляем интерфейс страницы конкретного преступления в соответсвии с данными текущего преступления
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()

        // Для окошек с галочкой отключаем анимацию при добавлении загруженных данных, чтобы избежать
        // анимированного выставления галочки при запуске фрагмента и повороте устройства
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        requiresPoliceCheckBox.apply {
            isChecked = crime.requiresPolice
            jumpDrawablesToCurrentState()
        }
    }

    // Добавляем поле свойств, доступных без создания экземпляра класса
    companion object {

        // Создаём функцию для получения экземпляра фрагмента с возможностью передать ему ID преступления
        fun newInstance(crimeId: UUID): CrimeFragment {

            // Создаём пакет Bundle, в который помещаем информацию о переданном ID преступления
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }

            // Возвращаем экземпляр CrimeFragment с добавленным к нему пакетом аргументов
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}


