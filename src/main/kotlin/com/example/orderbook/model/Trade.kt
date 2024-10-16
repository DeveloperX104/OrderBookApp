package com.example.orderbook.model

import java.util.UUID

data class Trade(
    val price: Double,
    val quantity: Double,
    val takerSide: String,
    val currencyPair: String,
    val tradedAt: String,
    val sequenceId: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString(),
    val quoteVolume: Double
)
