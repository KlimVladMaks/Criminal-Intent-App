package com.example.criminal_intent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Создаём тэг для данного класса
private const val TAG = "CrimeListFragmentTAG"

// Создаём класс фрагмента для работы со списком преступлений
class CrimeListFragment: Fragment() {

    // Создаём переменную для хранения списка преступлений
    private lateinit var crimeRecyclerView: RecyclerView

    // Создаём переменную для хранения адаптера списка
    private var adapter: CrimeAdapter? = null

    // Лениво инициализируем экземпляр CrimeListViewModel, привязывая его к данному фрагменту
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    // Переопреедляем функцию создания фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Выводим в консоль количество преступлений в списке
        Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
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

        // Подключаем к списку RecyclerView объект LinearLayoutManager, который отвечает за
        // размещение объектов списка по вертикали и их прокрутку
        // (Для подключения используем Context, с которым в данный момент связан этот фрагмент)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        // Обновляем список преступлений
        updateUI()

        // Возвращаем объект представления
        return view
    }

    // Функция для обновляения интерфейса фрагмента (в частности списка преступлений)
    private fun updateUI() {

        // Достаём из ViewModel список преступлений
        val crimes = crimeListViewModel.crimes

        // Создаём адаптер, передавая ему список преступлений
        adapter = CrimeAdapter(crimes)

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
            dateTextView.text = this.crime.date.toString()
        }

        // Функция, вызываемая при нажатии на элемент холдера
        override fun onClick(v: View) {

            // Выводим всплывающее сообщение с заголовком нажатого преступления
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
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
            dateTextView.text = this.crime.date.toString()

            // Добавляем слушателя, который при нажатии на кнопку выводит всплывающее сообщение,
            // что полиция вызвана
            callPoliceButton.setOnClickListener {
                Toast.makeText(context, "The police are called!", Toast.LENGTH_SHORT).show()
            }
        }

        // Функция, вызываемая при нажатии на элемент холдера
        override fun onClick(v: View) {

            // Выводим всплывающее сообщение с заголовком нажатого преступления
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }
    }

    // Создаём внутренний класс Adapter, который отвечает за создание CrimeHolder-ов и подключение
    // их к списку преступлений
    // (Adapter связывает RecyclerView и набор данных с преступлениями)
    private inner class CrimeAdapter(var crimes: List<Crime>)
        : RecyclerView.Adapter<AbstractCrimeHolder>() {

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

    // Объекты, доступные без создания экземпляра класса
    companion object {

        // Создаём функцию, которая возвращает экземпляр фрагмента
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}