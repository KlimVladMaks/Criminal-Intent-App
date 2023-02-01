package com.example.criminal_intent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// Данный файл является частью модели

// Создаём класс данных для хранения информации о конкретном преступлении
// (ID, заголовок, дата, решено ли преступление, требуется ли вызов полиции, имя подозреваемого)
// Превращаем класс Crime в сущность для хранения таблицы данных о преступлениях в долгосрочной памяти
// (Каждый элемент класса Crime будет занимать одну строку в таблице, где столбцами будут его свойства)
// Свойство с аннотацией @PrimaryKey станет первичным ключём для таблицы
// (уникальным значением, по которому можно обратиться к конкретному элементу таблицы)
@Entity
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var requiresPolice: Boolean = false,
    var suspect: String = ""
) {

    // Создаём переменную, при запросе значения которой возвращается уникальное имя для фото
    val photoFileName
        get() = "IMG_$id.jpg"
}


