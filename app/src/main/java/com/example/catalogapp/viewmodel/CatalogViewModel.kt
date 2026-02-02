package com.example.catalogapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catalogapp.model.CatalogItem

/**
 * ViewModel для управления состоянием каталога товаров
 */
class CatalogViewModel : ViewModel() {

    // Приватная изменяемая LiveData для внутреннего использования
    private val _catalogItems = MutableLiveData<List<CatalogItem>>()
    
    // Публичная неизменяемая LiveData для наблюдения из Fragment
    val catalogItems: LiveData<List<CatalogItem>> = _catalogItems

    init {
        // Инициализация тестового каталога с 12 элементами
        loadCatalog()
    }

    /**
     * Загрузка тестовых данных каталога
     */
    private fun loadCatalog() {
        val items = listOf(
            CatalogItem(1, "Ноутбук ASUS", "Мощный ноутбук для работы и игр с процессором Intel Core i7", 85000.0, false),
            CatalogItem(2, "Смартфон Samsung", "Флагманский смартфон с отличной камерой 108 МП", 65000.0, false),
            CatalogItem(3, "Наушники Sony", "Беспроводные наушники с активным шумоподавлением", 25000.0, true),
            CatalogItem(4, "Планшет iPad", "Планшет для творчества и развлечений с Apple Pencil", 45000.0, false),
            CatalogItem(5, "Умные часы Apple", "Фитнес-трекер и умные часы в одном устройстве", 35000.0, false),
            CatalogItem(6, "Монитор LG", "4K монитор 27 дюймов для профессиональной работы", 42000.0, false),
            CatalogItem(7, "Клавиатура Logitech", "Механическая клавиатура для геймеров с RGB подсветкой", 8500.0, true),
            CatalogItem(8, "Мышь Razer", "Игровая мышь с высокой точностью 16000 DPI", 5500.0, false),
            CatalogItem(9, "Веб-камера Logitech", "Full HD веб-камера для видеозвонков и стримов", 7500.0, false),
            CatalogItem(10, "SSD диск Samsung", "Быстрый твердотельный накопитель на 1TB", 9500.0, false),
            CatalogItem(11, "Роутер TP-Link", "Wi-Fi 6 роутер для быстрого интернета до 3000 Мбит/с", 12000.0, false),
            CatalogItem(12, "Powerbank Xiaomi", "Портативное зарядное устройство 20000 mAh", 3500.0, true)
        )
        _catalogItems.value = items
    }

    /**
     * Переключение статуса "избранное" для товара
     * @param itemId идентификатор товара
     */
    fun toggleFavorite(itemId: Int) {
        _catalogItems.value = _catalogItems.value?.map { item ->
            if (item.id == itemId) {
                item.copy(isFavorite = !item.isFavorite)
            } else {
                item
            }
        }
    }

    /**
     * Получение товара по ID
     * @param itemId идентификатор товара
     * @return найденный товар или null
     */
    fun getItemById(itemId: Int): CatalogItem? {
        return _catalogItems.value?.find { it.id == itemId }
    }
}
