package com.example.criminal_intent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// Создаём тэг для данного класса
private const val TAG = "CrimeListFragmentTAG"

// Создаём класс фрагмента для работы со списком преступлений
class CrimeListFragment: Fragment() {

    // Создаём интерфейс для обратного вызова (передачи сообщения в) хост-активити
    interface Callbacks {

        // Функция, возвращающая хост-активити информацию о выбранном (нажатом) преступлении
        fun onCrimeSelected(crimeId: UUID)
    }

    // Инициализируем переменную, хранящую экземпляр интерфейса Callbacks обратного вызова хост-активити
    private var callbacks: Callbacks? = null

    // Создаём переменную для хранения списка преступлений
    private lateinit var crimeRecyclerView: RecyclerView

    // Создаём переменную для хранения надписи о том, что список пуст
    private lateinit var emptyListTextView: TextView

    // Создаём переменную для хранения кнопки создания нового преступления
    private lateinit var newCrimeButton: Button

    // Создаём переменную для хранения адаптера списка и по умолчанию добавляем в него пустой список
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    // Лениво инициализируем экземпляр CrimeListViewModel, привязывая его к данному фрагменту
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    // Переопреедляем функцию onAttach(), вызываемую, когда фрагмент прикрепляется к activity
    // В качестве context передаётся экземпляр activity
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Помещаем в callbacks экземпляр activity, к которой был прикреплён фрагмент
        callbacks = context as Callbacks?
    }

    // Переопределяем функцию, вызываемую при создании фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Указываем, что фрагмент должен получить обратные вызовы верхнего меню
        // (По-сути, подключаем верхнее меню, указывая, что фрагмент будет работать с ним)
        setHasOptionsMenu(true)
    }

    // Переопределяем функцию для заполнения представления фрагмента
    // (Функция возвращает представление в виде View)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Создаём объект представления View
        // (Указываем: XML-макет фрагмента; объект, в который нужно поместить фрагмент;
        // информацию, нужно ли включать заполненое представление в родителя)
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        // Инициализируем список преступлений
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView

        // Инициализируем текстовое поле с надписью о том, что список преступлений пуст
        emptyListTextView = view.findViewById(R.id.empty_list_text) as TextView

        // Инициализируем кнопку создания нового преступления
        newCrimeButton = view.findViewById(R.id.new_crime_button)

        // Подключаем к списку RecyclerView объект LinearLayoutManager, который отвечает за
        // размещение объектов списка по вертикали и их прокрутку
        // (Для подключения используем Context, с которым в данный момент связан этот фрагмент)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        // Загружаем адаптер для RecyclerView
        crimeRecyclerView.adapter = adapter

        // Возвращаем объект представления
        return view
    }

    // Переопределяем функцию, вызываемую сразу после onCreateView(), когда представление уже задано
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем наблюдателя за состоянием LiveData
        // Наблюдатель реагирует каждый раз, когда изменяются данные в LiveData
        crimeListViewModel.crimesListLiveData.observe(

            // viewLifecycleOwner следит за жизненным циклом фрагмента и
            // не позволяет обновить данные, когда фрагмент находится в нерабочем состоянии
            viewLifecycleOwner,

            // Если список не пуст, выводим в Logcat сообщение о количестве элементов
            // и обновляем список преступлений
            androidx.lifecycle.Observer {
                it?.let {
                    Log.i(TAG, "Got crimes ${it.size}")
                    updateUI(it)
                }
            }
        )
    }

    // Переопределяем функцию, вызываемую при запуске (старте) фрагмента
    override fun onStart() {
        super.onStart()

        // Добавляем слушателя к кнопке создания нового преступления
        newCrimeButton.setOnClickListener {

            // Создаём новый экземпляр преступления
            val crime = Crime()

            // Добавляем созданный экземпляр преступления в базу данных
            crimeListViewModel.addCrime(crime)

            // Отдаём хост-активити через интерфейс обратного вызова команду вывести карточку
            // вышесозданного нового преступления на экран
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    // Переопределяем функцию, вызываемую при откреплении фрагмента от activity
    override fun onDetach() {
        super.onDetach()

        // Устанавливаем callbacks равным null, так как он больше не должен обращаться к хост-активити
        callbacks = null
    }

    // Переопределяем функцию, вызываемую при создании верхнего меню фрагмента
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        // Заполняем верхнее меню XML-макетом fragment_crime_list
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    // Переопределяем функцию, вызываемую при выборе (нажатии) команды в верхнем меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Исследуем ID выбранной команды
        return when (item.itemId) {

            // Если ID соответсвует команде создания нового преступления
            R.id.new_crime -> {

                // Создаём новый экземпляр преступления
                val crime = Crime()

                // Добавляем созданный экземпляр преступления в базу данных
                crimeListViewModel.addCrime(crime)

                // Отдаём хост-активити через интерфейс обратного вызова команду вывести карточку
                // вышесозданного нового преступления на экран
                callbacks?.onCrimeSelected(crime.id)

                // Возвращаем true, чтобы показать, что дальнейшая обработка не требуется
                true
            }

            // Иначе возвращаем базовый вариант onOptionsItemSelected(), который не создаёт нового перступления
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Функция для обновляения интерфейса фрагмента (в частности списка преступлений)
    private fun updateUI(crimes: List<Crime>) {

        // Создаём адаптер, передавая ему список преступлений
        adapter = CrimeAdapter(crimes)

        // Если количество преступлений в списке не равно нулю
        if (adapter!!.itemCount != 0) {

            // Скрываем текстовое поле с нодписью о пустом списке
            emptyListTextView.visibility = View.GONE

            // Скрываем кнопку создания нового преступления
            newCrimeButton.visibility = View.GONE
        }

        // Иначе
        else {

            // Показываем текстовое поле с нодписью о пустом списке
            emptyListTextView.visibility = View.VISIBLE

            // Показываем кнопку создания нового преступления
            newCrimeButton.visibility = View.VISIBLE
        }

        // Подулючаем созданный адаптер к RecyclerView
        crimeRecyclerView.adapter = adapter
    }

    // Создаём абстрактный класс холдера, чтобы наследовать от него различные виды других холдеров
    private abstract class AbstractCrimeHolder(view: View): RecyclerView.ViewHolder(view) {

        // Создаём экземпляр класса Crime
        var crime = Crime()

        // Инициализируем из подключённого itemView заголовок преступления
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)

        // Инициализируем из подключённого itemView дату преступления
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)

        // Инициализируем изображение с наручниками для решённых преступлений
        val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
    }

    // Создаём внутренний класс CrimeHolder (наследуем его от AbstractCrimeHolder), предназначенный
    // для хранение представления View для одного преступления
    // (В дальнейшем CrimeHolder-ы используются для наполнения списка преступлений)
    // (Дополнительно реализуем в классе механизм нажатия на элемент списка с помощью View.OnClickListener)
    private inner class CrimeHolder(view: View)
        : AbstractCrimeHolder(view), View.OnClickListener {

        // При инициализации холдера подключаем к нему слушателя нажатий
        init {
            itemView.setOnClickListener(this)
        }

        // Создаём функцию для привязываение переданного преступления к данному холдеру
        fun bind(crime: Crime) {

            // Сохраняем переданное преступление в свойствах класса
            this.crime = crime

            // Обновляем заголовок и дату представления преступления согласно переданному экземпляру
            titleTextView.text = this.crime.title

            // Дополнительно форматируем дату в удобночитаемый вид
            dateTextView.text =
                SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                    .format(this.crime.date)

            // Если приступление решено, то делаем изображение с наручниками видимым
            // Иначе делаем его невидимым
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Функция, вызываемая при нажатии на элемент холдера
        override fun onClick(v: View) {

            // Возвращаем хост-активити информацию об ID нажатого преступления
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    // Создаём ещё один класс холдер для хранения серьёзных преступлений
    // (Также наследуем данный класс от AbstractCrimeHolder и View.OnClickListener)
    private inner class SeriousCrimeHolder(view: View)
        : AbstractCrimeHolder(view), View.OnClickListener {

        // Инициализируем кнопку для вызова полиции
        val callPoliceButton: Button = itemView.findViewById(R.id.call_police_button)

        // При инициализации холдера подключаем к нему слушателя нажатий
        init {
            itemView.setOnClickListener(this)
        }

        // Создаём функцию для привязываение переданного преступления к данному холдеру
        fun bind(crime: Crime) {

            // Сохраняем переданное преступление в свойствах класса
            this.crime = crime

            // Обновляем заголовок и дату представления преступления согласно переданному экземпляру
            titleTextView.text = this.crime.title

            // Дополнительно форматируем дату в удобночитаемый вид
            dateTextView.text =
                SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                    .format(this.crime.date)

            // Добавляем слушателя, который при нажатии на кнопку выводит всплывающее сообщение,
            // что полиция вызвана
            callPoliceButton.setOnClickListener {
                Toast.makeText(context, "The police are called!", Toast.LENGTH_SHORT).show()
            }

            // Если приступление решено, то делаем изображение с наручниками видимым
            // Иначе делаем его невидимым
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Функция, вызываемая при нажатии на элемент холдера
        override fun onClick(v: View) {

            // Возвращаем хост-активити информацию об ID нажатого преступления
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    // Создаём внутренний класс Adapter, который отвечает за создание CrimeHolder-ов и подключение
    // их к списку преступлений
    // (Adapter связывает RecyclerView и набор данных с преступлениями)
    // (Используем ListAdapter, чтобы иметь возможность обновить лишь одно преступление, а не весь
    // список сразу как в случае Adapter)
    // (Передаём ListAdapter объект DiffCallback, чтобы ListAdapter мог определить изменённые
    // элементы списка)
    private inner class CrimeAdapter(var crimes: List<Crime>)
        : androidx.recyclerview.widget.ListAdapter<Crime, AbstractCrimeHolder>(DiffCallback) {

        // Создаём функция onCreateViewHolder(), которая отвечает за создание представления
        // на дисплее, оборачивает его в холдер и возвращает результат
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractCrimeHolder {

            // Проверяем тип переданного представления
            when (viewType) {

                // Если тип равен 0 (обычное преступление), то создаём объект представления View
                // с макетом list_item_crime.xml и возвращаем, оборачивая в CrimeHolder
                0 -> {
                    val view = layoutInflater
                        .inflate(R.layout.list_item_crime, parent, false)
                    return CrimeHolder(view)
                }

                // Иначе (серьёзное преступление) создаём объект представления View
                // с макетом list_item_serious_crime.xml и возвращаем, оборачивая в SeriousCrimeHolder
                else -> {
                    val view = layoutInflater
                        .inflate(R.layout.list_item_serious_crime, parent, false)
                    return SeriousCrimeHolder(view)
                }
            }
        }

        // Создаём функцию getItemCount(), позволяющую получить размер списка преступлений
        override fun getItemCount() = crimes.size

        // Создаём функцию onBindViewHolder(), которая отвечает за заполнение данного холдера holder
        // преступлением из данной позиции position
        override fun onBindViewHolder(holder: AbstractCrimeHolder, position: Int) {

            // Получаем экземпляр преступления с заданной позиции
            val crime = crimes[position]

            // Добавляем в переданный холдер необходимые данные
            // путём вызова соответсвующей функции холдера
            when (holder) {
                is CrimeHolder -> holder.bind(crime)
                is SeriousCrimeHolder -> holder.bind(crime)
            }
        }

        // Переопределяем функцию, которая задаёт тип для элемента с переданной позицией
        override fun getItemViewType(position: Int): Int {

            // Достаём из списка конкретное преступление
            val crime = crimes[position]

            // Если вызов полиции требуется, то помечаем преступление как тяжёлое
            // Иначе помечаем преступление как обычное
            return when (crime.requiresPolice) {
                true -> 1
                else -> 0
            }
        }
    }

    // Создаём объект DiffCallback, который должен определять, какие именно элементы списка были изменены
    // (DiffCallback используется ListAdapter для точечного обновления лишь изменённых элементов списка,
    // а не всего списка, как в случае в Adapter)
    object DiffCallback: DiffUtil.ItemCallback<Crime>() {

        // Переопределяем функцию, вызываемую для сравнения двух элементов списка
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {

            // Сравниваем ID переданных преступлений и возвращаем результат сравнения
            return oldItem.id == newItem.id
        }

        // Переопределяем функцию, вызываемую для сравнения содержания двух элементов списка
        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {

            // Сравниваем переданные преступления между собой и возвращаем результат сравнения
            return oldItem == newItem
        }

    }

    // Объекты, доступные без создания экземпляра класса
    companion object {

        // Создаём функцию, которая возвращает экземпляр фрагмента
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}



