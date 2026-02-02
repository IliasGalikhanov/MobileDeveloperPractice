package com.example.catalogapp.model

/**
 * Модель данных для элемента каталога
 * @param id уникальный идентификатор товара
 * @param title название товара
 * @param description описание товара
 * @param price цена товара
 * @param isFavorite флаг "в избранном"
 */
data class CatalogItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    var isFavorite: Boolean = false
)
