package com.example.orderbook.model

data class OrderBook(
    val asks: List<OrderBookEntry>,
    val bids: List<OrderBookEntry>
) {
    data class OrderBookEntry(
        val price: Double,
        val quantity: Double,
        val orderCount: Int,
        val currencyPair: String
    )
}
