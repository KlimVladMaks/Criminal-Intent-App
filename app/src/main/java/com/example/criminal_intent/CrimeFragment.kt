package com.example.criminal_intent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Данный файл является частью контроллера

// Создаём тэг для вывода отладочных сообщений
private const val TAG = "CrimeFragmentTAG"

// Создаём тэг для обрещения к ID преступления в пакете Bundle
private const val ARG_CRIME_ID = "crime_id"

// Создаём метку для идентификации диалогового фрагмента с календарём
private const val DIALOG_DATE = "DialogDate"

// Создаём метку для идентификации диалогового фрагмента выбора времени
private const val DIALOG_TIME = "DialogTime"

// Создаём код запроса для обращения к DatePickerFragment
private const val REQUEST_DATE = 0

// Создаём код запроса контакта подозреваемого
private const val REQUEST_CONTACT = 1

// Создаём код запроса фото преступления
private const val REQUEST_PHOTO = 2

// Создаём код запроса для обращения к TimePickerFragment
private const val REQUEST_TIME = 3

// Создаём код запроса для обращения к списку контактов
private const val REQUEST_PHONE = 4

// Создаём шаблон формата, в котором следует отображать дату
private const val DATE_FORMAT = "EEE, MMM, dd"

// Создаём класс фрагмента
// (Наследуем его от DatePickerFragment.Callbacks, чтобы можно было получить выбранную на календаре дату)
// (Наследуем его от TimePickerFragment.Callbacks, чтобы можно было получить выбранное время)
class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    // Создаём интерфейс для обратного вызова (передачи сообщения в) хост-активити
    interface Callbacks {

        // Функция, возвращающая хост-активити информацию о том, что текущее преступление было удалено
        fun onCrimeDelete()
    }

    // Инициализируем переменную, хранящую экземпляр интерфейса Callbacks обратного вызова хост-активити
    private var callbacks: Callbacks ?= null

    // Создаём переменную для хранения экземпляра класса Crime
    private lateinit var crime: Crime

    // Создаём переменную для хранения ссылки на фото перступления
    private lateinit var photoFile: File

    // Создаём переменную для хранения Uri-ссылки на фото преступления
    private lateinit var photoUri: Uri

    // Создаём переменную для хранения поля для ввода текста
    private lateinit var titleField: EditText

    // Создаём переменную для хранения кнопки с датой
    private lateinit var dateButton: Button

    // Создаём переменную для хранения кнопки с временем
    private lateinit var timeButton: Button

    // Создаём переменную для хранения окошка с галочкой
    private lateinit var solvedCheckBox: CheckBox

    // Создаём переменную для окошка с галочкой - "Requires Police"
    private lateinit var requiresPoliceCheckBox: CheckBox

    // Создаём переменную для кнопки отправки отчёта о преступлении
    private lateinit var reportButton: Button

    // Создаём переменную для хранения кнопки выбора контакта подозреваемого
    private lateinit var suspectButton: Button

    // Создаём переменную для хранения фото преступления
    private lateinit var photoView: ImageView

    // Создаём переменную для хранения кнопки подготовки фото
    private lateinit var photoButton: ImageButton

    // Создаём переменную для хранения кнопки звонка подозреваемому
    private lateinit var callSuspectButton: Button

    // Лениво инициализируем экземпляр CrimeDetailViewModel, привязывая его к данному фрагменту
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    // Переопреедляем функцию onAttach(), вызываемую, когда фрагмент прикрепляется к activity
    // В качестве context передаётся экземпляр activity
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Помещаем в callbacks экземпляр activity, к которой был прикреплён фрагмент
        callbacks = context as CrimeFragment.Callbacks?
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

        // Указываем, что фрагмент должен получить обратные вызовы верхнего меню
        // (По-сути, подключаем верхнее меню, указывая, что фрагмент будет работать с ним)
        setHasOptionsMenu(true)
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

        // Инициализируем кнопку с временем
        timeButton = view.findViewById(R.id.crime_time) as Button

        // Инициализируем окошко с галочкой "Преступление решено"
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        // Инициализируем окошко с галочкой "Требуется полиция"
        requiresPoliceCheckBox = view.findViewById(R.id.requires_police_checkbox) as CheckBox

        // Инициализируем кнопку отправки отчёта о преступлении
        reportButton = view.findViewById(R.id.crime_report) as Button

        // Инициализируем кнопку выбора контакта подозреваемого
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

        // Инициализируем блок с фото преступления
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        // Инициализируем кнопку для установки фото преступления
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton

        // Инициализируем кнопку звонка подозреваемому
        callSuspectButton = view.findViewById(R.id.call_suspect_button)

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

            // Обрабатываем выбранное преступление, извлекая из него необходимую информацию
            androidx.lifecycle.Observer {
                it?.let {

                    // Загружаем информацию о выбранном преступлении в собственное представление crime
                    // данного фрагмента
                    this.crime = it

                    // Получаем ссылку на файл с фото выбранного преступления
                    photoFile = crimeDetailViewModel.getPhotoFile(it)

                    // Используем созданный выше файловый путь для создания URI-ссылки, указывающей
                    // на фото преступления.
                    // FileProvider.getUriForFile() передаём экземпляр activity, ссылку на файловый провайдер
                    // и файловый путь к фото
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.example.criminal_intent.fileprovider",
                        photoFile)

                    // Обновляем интерфейс фрагмента
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

        // Добавляем слушателя на кнопку выбора времени
        timeButton.setOnClickListener {

            // Создаём диалоговое окно выбора времени, добавляя к нему дополнительные свойства
            // (В качестве аргументов передаём время выбранного преступления)
            TimePickerFragment.newInstance(crime.date).apply {

                // Назначаем CrimeFragment целевым фрагментом для TimePickerFragment
                // (Теперь именно в CrimeFragment будут передаваться данные из TimePickerFragment)
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)

                // Выводим созданное диалоговое окно поверх фрагмента
                // (this@CrimeFragment используется для вызова функции requireFragmentManager()
                // именно из CrimeFragment, this нужно для работы в области видимости блока apply)
                // (DIALOG_DATE отвечает за идентификацию выведенного диалогового окна)
                // Общий вид данной функции - show(FragmentManager, String)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
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
            // (Определяем данный интент вне слушателя, так как данный интент ещё понадобится)
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
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            // Если на устройстве нет подходящих активити, то делаем кнопку добавления контакта
            // подозреваемого недоступной
            if (resolvedActivity == null) {
                isEnabled = false
            }
            */
        }

        // Добавляем к кнопке установки фото преступления несколько компонентов
        photoButton.apply {

            // Записываем в переменную packageManager информацию о компанентах Android устройства
            val packageManager: PackageManager = requireActivity().packageManager

            // Создаём интент, вызывающий приложения, способные сделать фотографию
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Ищем активити, которые соответсвуют интенту captureImage
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

            // Если подходящих активити нет, то блокируем кнопку фото
            if (resolvedActivity == null) {
                isEnabled = false
            }

            // Устанавливаем слушателя нажатия на кнопку
            setOnClickListener {

                // Добавляем в интент URI-ссылку по которой нужно разместить фотографию
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                // Получаем список всех активити, которые могут выполнить созданный выше интент
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                // Перебераем все найденные выше подходящие активити
                for (cameraActivity in cameraActivities) {

                    // Каждой подходящей активити даём разрещение на запись фото по URI-ссылке
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                // Запускаем функцию вызова активити по созданному выше интенту, передавая ей
                // код, по которому она должна вернуть результат
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        // Добавляем к кнопке звонка подозреваемому несколько компонентов
        callSuspectButton.apply {

            // Создаём интент для обращения к списку контактов и получения номера подозреваемого
            val pickPhoneIntent = Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

            // Добавляем слушателя кнопке
            setOnClickListener {

                // Запускаем интент с ожиданием результата (ответа)
                startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
            }
        }
    }

    // Переопределяем функцию, вызываемую перед остановкой фрагмента
    override fun onStop() {
        super.onStop()

        // Сохраняем изменения, внесённые в карточку перступления
        crimeDetailViewModel.saveCrime(crime)
    }

    // Переопределяем функцию, которая вызывается, когда фрагмент отвязывается от активити
    override fun onDetach() {
        super.onDetach()

        // Отзываем разрешение у других приложений на доступ к файлу текущего фото преступления
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        // Устанавливаем callbacks равным null, так как он больше не должен обращаться к хост-активити
        callbacks = null
    }

    // Переопределяем функцию, вызываемую при создании верхнего меню фрагмента
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        // Заполняем верхнее меню XML-макетом fragment_crime
        inflater.inflate(R.menu.fragment_crime, menu)
    }

    // Переопределяем функцию, вызываемую при выборе (нажатии) команды в верхнем меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Исследуем ID выбранной команды
        return when (item.itemId) {

            // Если ID соответсвует команде удаления текущего преступления
            R.id.delete_crime -> {

                // Удаляем текущее преступление через ViewModel
                crimeDetailViewModel.deleteCrime(crime)

                // Сообщаем хост-активити, что текущее преступление было удалено
                callbacks?.onCrimeDelete()

                // Возвращаем true, чтобы показать, что дальнейшая обработка не требуется
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Переопределяем функцию для получения выбранной на календаре даты
    override fun onDateSelected(date: Date) {

        // Загружаем выбранную дату в текущий экземпляр преступления
        crime.date = date

        // Обновляем интерфейс в соотвествии с новыми данными
        updateUI()
    }

    // Переопределяем функцию для получения выбранного времени
    override fun onTimeSelected(date: Date) {

        // Загружаем полученные час и минуту в текущий экземпляр преступления
        crime.date.hours = date.hours
        crime.date.minutes = date.minutes

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

        // Дополнительно форматируем время в удобночитаемый вид
        timeButton.text = SimpleDateFormat("kk:mm", Locale.getDefault())
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

        // Обновляем (устанавливаем) фото преступления
        updatePhotoView()
    }

    // Функция для обновления фотографии преступления
    private fun updatePhotoView() {

        // Если файл с фото существует (существует указатель на него)
        if (photoFile.exists()) {

            // Получаем по заданному пути масштабированное изображение под данную активити
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())

            // Размещаем полученное фото в соответсвующую область ImageView
            photoView.setImageBitmap(bitmap)

        } else {

            // Иначе устанавливаем область ImageView пустой
            photoView.setImageBitmap(null)
        }
    }

    // Переопределяем функцию, вызываемую при получении ответа от другой вызванной активити
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Перебираем различные варианты ответов и реагируем на них
        when {

            // Если код ответа сигнализирует об ошибке, то прекращаем выполенение функции
            resultCode != Activity.RESULT_OK -> return

            // Если код ответа соответствует коду запроса контакта и возвращённые данные не являются пустыми
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

            // Если код ответа соответсвует коду подготовки фото преступления
            requestCode == REQUEST_PHOTO -> {

                // Отзываем разрешение у других приложений на доступ к файлу текущего фото преступления
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                // Обновляем фото преступления
                updatePhotoView()
            }

            // Если код ответа соответсвует запросу номера подозреваемого
            requestCode == REQUEST_PHONE && data != null -> {

                // Извлекаем URI контакта из полученных данных
                val contactURI : Uri? = data.data

                // Получаем ID для обращение к номеру телефона
                val queryFields = ContactsContract.CommonDataKinds.Phone._ID

                // Создаём курсор для работы с переданными данными
                val cursor = requireActivity()
                    .contentResolver
                    .query(contactURI!!, null, queryFields, null, null)

                // Обрабатываем переданные данные при помощи курсора
                cursor.use {

                    // Если количество переданных полей равно нулю, то прекращаем обработку
                    if (it?.count == 0) {
                        return
                    }

                    // Перемещаем курсор на первую позицию
                    it?.moveToFirst()

                    // Извлекаем номер телефона
                    val number = it
                        ?.getString(it
                            .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                    // Создаём интент для нобора номера телефона
                    val dialNumber = Intent(Intent.ACTION_DIAL)

                    // Добовляем в созданый интент полученный номер подозреваемого
                    dialNumber.data = Uri.parse("tel: $number")

                    // Запускаем интент
                    startActivity(dialNumber)
                }

                // Закрываем курсор
                cursor?.close()
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


