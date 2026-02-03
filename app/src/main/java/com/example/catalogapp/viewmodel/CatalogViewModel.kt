package com.example.catalogapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catalogapp.model.CatalogItem

class CatalogViewModel : ViewModel() {
    private val _catalogItems = MutableLiveData<List<CatalogItem>>()

    val catalogItems: LiveData<List<CatalogItem>> = _catalogItems

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        val items = listOf(
            CatalogItem(1, "Ноутбук ASUS", "Мощный ноутбук для работы и игр с процессором Intel Core i7", 500000.0, false),
            CatalogItem(2, "Смартфон Samsung", "Флагманский смартфон с отличной камерой 108 МП", 400000.0, false),
            CatalogItem(3, "Наушники Sony", "Беспроводные наушники с активным шумоподавлением", 150000.0, true),
            CatalogItem(4, "Планшет iPad", "Планшет для творчества и развлечений с Apple Pencil", 300000.0, false),
            CatalogItem(5, "Умные часы Apple", "Фитнес-трекер и умные часы в одном устройстве", 210000.0, false),
            CatalogItem(6, "Монитор LG", "4K монитор 27 дюймов для профессиональной работы", 252000.0, false),
            CatalogItem(7, "Клавиатура Logitech", "Механическая клавиатура для геймеров с RGB подсветкой", 51000.0, true),
            CatalogItem(8, "Мышь Razer", "Игровая мышь с высокой точностью 16000 DPI", 33000.0, false),
            CatalogItem(9, "Веб-камера Logitech", "Full HD веб-камера для видеозвонков и стримов", 45000.0, false),
            CatalogItem(10, "SSD диск Samsung", "Быстрый твердотельный накопитель на 1TB", 57000.0, false),
            CatalogItem(11, "Роутер TP-Link", "Wi-Fi 6 роутер для быстрого интернета до 3000 Мбит/с", 72000.0, false),
            CatalogItem(12, "Powerbank Xiaomi", "Портативное зарядное устройство 20000 mAh", 21000.0, true)
        )
        _catalogItems.value = items
    }

    fun toggleFavorite(itemId: Int) {
        _catalogItems.value = _catalogItems.value?.map { item ->
            if (item.id == itemId) {
                item.copy(isFavorite = !item.isFavorite)
            } else {
                item
            }
        }
    }

    fun getItemById(itemId: Int): CatalogItem? {
        return _catalogItems.value?.find { it.id == itemId }
    }
}
