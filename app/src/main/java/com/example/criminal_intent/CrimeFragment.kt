package com.example.criminal_intent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

// Данный файл является частью контроллера

// Создаём тэг для вывода отладочных сообщений
private const val TAG = "CrimeFragmentTAG"

// Создаём тэг для обрещения к ID преступления в пакете Bundle
private const val ARG_CRIME_ID = "crime_id"

// Создаём метку для идентификации диалогового фрагмента с календарём
private const val DIALOG_DATE = "DialogDate"

// Создаём код запроса для обращения к DatePickerFragment
private const val REQUEST_DATE = 0

// Создаём код запроса контакта подозреваемого
private const val REQUEST_CONTACT = 1

// Создаём шаблон формата, в котором следует отображать дату
private const val DATE_FORMAT = "EEE, MMM, dd"

// Создаём класс фрагмента
// (Наследуем его от DatePickerFragment.Callbacks, чтобы можно было получить выбранную на календаре дату)
class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {

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

    // Создаём переменную для кнопки отправки отчёта о преступлении
    private lateinit var reportButton: Button

    // Создаём переменную для хранения кнопки выбора контакта подозреваемого
    private lateinit var suspectButton: Button

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

        // Инициализируем кнопку отправки отчёта о преступлении
        reportButton = view.findViewById(R.id.crime_report) as Button

        // Инициализируем кнопку выбора контакта подозреваемого
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

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

        // Добавляем слушателя на кнопку выбора даты
        dateButton.setOnClickListener {

            // Создаём диалоговое окно с календарём, добавляя к нему дополнительные свойства
            // (В качестве аргументов передаём дату выбранного преступления)
            DatePickerFragment.newInstance(crime.date).apply {

                // Назначаем CrimeFragment целевым фрагментом для DatePickerFragment
                // (Теперь именно в CrimeFragment будут передаваться данные из DatePickerFragment)
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)

                // Выводим созданное диалоговое окно поверх фрагмента
                // (this@CrimeFragment используется для вызова функции requireFragmentManager()
                // именно из CrimeFragment, this нужно для работы в области видимости блока apply)
                // (DIALOG_DATE отвечает за идентификацию выведенного диалогового окна)
                // Общий вид данной функции - show(FragmentManager, String)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        // Добавляем слушаетля к кнопке отправки отчёта о преступлении
        reportButton.setOnClickListener {

            // Создаём внешний интент для отправки отчёта о преступлении
            // С помощью созданного интента делаем вызов требуемой Activity
            Intent(Intent.ACTION_SEND).apply {

                // Тип интента - простой текст
                type = "text/plain"

                // Загружаем строку с отчётом о преступлении
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())

                // Добовляем строку с темой интента
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))

            }.also {

                // Помещаем созданный интент в новый интент, который создаёт списка выбора доступных приложений
                val chooserIntent = Intent.createChooser(it, getString(R.string.send_report))

                // Делаем вызов требуемой Activity
                startActivity(chooserIntent)
            }
        }

        // Добавляем к кнопке выбора контакта подозреваемого несколько компонентов
        suspectButton.apply {

            // Создаём интент, направленный на выбор контакта
            // (Определяем данный интент вне слушателя, так как данный интент ещё понадобиться)
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            // Создаём слушатель нажатия на кнопку
            setOnClickListener {

                // Запускаем требуемую активити по интенту с ожиданием получения результата
                // (Передаём ей созданный выше интент и код запроса)
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            // Фрагмент кода для проверки наличия списка контактов на устройстве
            // (При отсутсвии списка, блокирует кнопку выбора контакта подозреваемого)
            // На данный момент, фрагмент кода не работает, блокируя соотвествующую кнопку даже при
            // наличии списка контактов
            /*
            // Записываем в переменную packageManager информацию о компанентах Android устройства
            val packageManager: PackageManager = requireActivity().packageManager

            // Ищем активити, которые соответсвуют pickContactIntent
            val resolvedActivity: ResolveInfo? = packageManager
                .resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            // Если на устройстве нет подходящих активити, то делаем кнопку добавления контакта
            // подозреваемого недоступной
            if (resolvedActivity == null) {
                isEnabled = false
            }
            */
        }
    }

    // Переопределяем функцию, вызываемую перед остановкой фрагмента
    override fun onStop() {
        super.onStop()

        // Сохраняем изменения, внесённые в карточку перступления
        crimeDetailViewModel.saveCrime(crime)
    }

    // Переопределяем функцию для получения выбранной на календаре даты
    override fun onDateSelected(date: Date) {

        // Загружаем выбранную дату в текущий экземпляр преступления
        crime.date = date

        // Обновляем интерфейс в соотвествии с новыми данными
        updateUI()
    }

    // Обновляем интерфейс страницы конкретного преступления в соответсвии с данными текущего преступления
    private fun updateUI() {

        // Устанавливаем заголовок преступления
        titleField.setText(crime.title)

        // Дополнительно форматируем дату в удобночитаемый вид
        dateButton.text = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
            .format(this.crime.date)

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

        // Если имя подозреваемого добавлено, то помещаем его в качестве текста на соответствующей кнопке
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
    }

    // Переопределяем функцию, вызываемую при получении ответа от другой вызванной активити
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Перебираем различные варианты ответов и реагируем на них
        when {

            // Если код результата сигнализирует об ошибке, то прекращаем выполенение функции
            resultCode != Activity.RESULT_OK -> return

            // Если код запроса соответствует коду запроса контакта и возвращённые данные не являются пустыми
            requestCode == REQUEST_CONTACT && data != null -> {

                // Считываем полученные данный в формате URI
                val contactUri: Uri? = data.data

                // Указываем, какие поля нам нужно извлечь из полученных данных
                // (В данном случае указываем, что нам нужно извлечь имя контакта)
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                // Если считанные данные равны null, то прекращаем выполнение функции
                if (contactUri == null) return

                // Создаём объект (курсор) для работы с полученными данными, из которых выделяем
                // поля, записанные в переменную queryFields
                val cursor = requireActivity().contentResolver
                        .query(contactUri, queryFields, null, null, null)

                // С помощью курсора извлекаем имя полученного контакта
                cursor?.use {

                    // Если курсор не содержит никаких элементов, то прекращаем выполнение функции
                    if (it.count == 0) {
                        return
                    }

                    // Перемещаем курсор в первый столбец
                    it.moveToFirst()

                    // Извлекаем имя подозреваемого с соответствующей позиции
                    val suspect = it.getString(0)

                    // Добавляем имя подозреваемого в экземпляр класса Crime данного фрагмента
                    crime.suspect = suspect

                    // Сохраняем обновлённое преступление
                    crimeDetailViewModel.saveCrime(crime)

                    // Устанавливаем имя подозреваемого в качестве текста для соответсвующей кнопки
                    suspectButton.text = suspect
                }
            }
        }
    }

    // Создаём функцию для получения отчёта о преступлении в строковом формате
    private fun getCrimeReport(): String {

        // Получаем строку, было ли решено преступление
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        // Создаём строку с датой совершения преступления
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        // Получаем строку с именем подозреваемого
        // (isBlank() проверяет, является ли строка пустой)
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        // Формируем и возвращаем строку с итоговым отчётом
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
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


