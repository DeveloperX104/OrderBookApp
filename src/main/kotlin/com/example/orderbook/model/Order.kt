package com.example.orderbook.model

data class Order(
    var price: Double,
    var quantity: Double,
    val side: String,
    val currencyPair: String
)
