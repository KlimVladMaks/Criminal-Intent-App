package com.example.criminal_intent

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

// Создаём тэг для обозначения URI изображения
const val PHOTO_URI = "PHOTO_URI"

// Создаём класс, который будет отображеть диалоговое окно с увеличенным изображением преступления
class ZoomDialogFragment: DialogFragment() {

    // Переопределяем функцию, вызываемую при создании представления окна
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Создаём представление, подключае к нему макет zoom_layout.xml
        val view = inflater.inflate(R.layout.zoom_layout, container, false)

        // Создаём и инициализируем переменную для хранения области для изображения
        val imageView = view.findViewById(R.id.zoom_image_view) as ImageView

        // Извлекаем имя изображения, которое нужно увеличить
        val photoFileName = arguments?.getString(PHOTO_URI) as String

        // Помещаем изображение с полученным именем в соответсвующую область
        imageView
            .setImageBitmap(BitmapFactory
                .decodeFile(requireContext().filesDir.path + "/" + photoFileName))

        // Возвращаем созданное представление
        return view
    }

    // Создаём блок кода, который доступен без создания экземпляра класса
    companion object {

        // Функция для создания нового экземпляра класса
        fun newInstance(photoFileName: String): ZoomDialogFragment {

            // Создаём экземпляр класса ZoomDialogFragment
            val frag = ZoomDialogFragment()

            // Создаём объект Bundle для хранения аргументов
            val args = Bundle()

            // Загружаем в объект Bundle имя переданного изображения
            args.putSerializable(PHOTO_URI, photoFileName)

            // Загружаем заданные выше аргументы в экземпляр класса
            frag.arguments = args

            // Возвращаем экземпляр класса ZoomDialogFragment
            return frag
        }
    }
}


