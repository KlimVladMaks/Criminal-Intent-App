package com.example.criminal_intent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import kotlin.math.roundToInt

// Функция для маштабирования растрового изображения
// Функция принимает на вход файловый путь до изображени и требуемые ширину и высоту
// Функция возвращает масштабированное изображение в формате Bitmap
fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {

    // Загружаем в переменную options параметры BitmapFactory
    var options = BitmapFactory.Options()

    // Указываем, что нужно просто декодировать границы
    options.inJustDecodeBounds = true

    // Декодируем файл по задонному пути path и помещаем декодированную информацию в options
    BitmapFactory.decodeFile(path, options)

    // Получаем ширину изображения
    val srcWidth = options.outWidth.toFloat()

    // Получаем высоту изображения
    val srcHeight = options.outHeight.toFloat()

    // Оригинальный масштаб равен 1
    var inSimpleSize = 1

    // Если оригинальные размеры изображения больше требуемых
    if (srcHeight > destHeight || srcWidth > destWidth) {

        // Рассчитываем коэффициент требуемой высоты
        val heightScale = srcHeight / destHeight

        // Рассчитываем коэффициент требуемой ширины
        val widthScale = srcWidth / destWidth

        // Берём больший из двух рассчитанных коэффициентов
        val sampleScale = if (heightScale > widthScale) {
            heightScale
        } else {
            widthScale
        }

        // Округляем больший коэфициент до целого числа
        inSimpleSize = sampleScale.roundToInt()
    }

    // Загружаем в переменную options параметры BitmapFactory
    options = BitmapFactory.Options()

    // Загружаем в options расчитанный выше коэффициент сжатия
    options.inSampleSize = inSimpleSize

    // Возвращаем масштабированное (сжатое) изображение в формате Bitmap, взятое из пути path
    return BitmapFactory.decodeFile(path, options)
}

// Функция для масштабирования изображения, расположенного по пути path, для размеров заданной активити
fun getScaledBitmap(path: String, activity: Activity): Bitmap {

    // За единицу измерения берём точку
    val size = Point()

    // Записываем в точку size координаты дальнего угла активити (т.е., по-сути, размеры активити)
    activity.windowManager.defaultDisplay.getSize(size)

    // Возвращаем изображение, масштабированное согласно полученным размерам
    return getScaledBitmap(path, size.x, size.y)
}

