package com.example.criminal_intent

import java.util.Date
import java.util.UUID

// Данный файл является частью модели

// Создаём класс данных для хранения информации о конкретном преступлении
data class Crime(
    val id: UUID = UUID.randomUUID(), // По умолчанию присваиваем приступлению уникальный ID
    var title: String = "",
    var date: Date = Date(), // По умолчанию присваиваем приступлению текущую дату
    var isSolved: Boolean = false,
    var requiresPolice: Boolean = false
)


