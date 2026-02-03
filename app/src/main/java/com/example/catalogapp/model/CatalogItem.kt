package com.example.catalogapp.model

data class CatalogItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    var isFavorite: Boolean = false
)
