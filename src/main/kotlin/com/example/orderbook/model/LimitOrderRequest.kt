package com.example.orderbook.model

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

data class LimitOrderRequest(
    @field:Positive(message = "Price must be positive")
    val price: Double,

    @field:Positive(message = "Quantity must be positive")
    val quantity: Double,

    @field:NotBlank(message = "Side is required")
    val side: String,

    @field:NotBlank(message = "Currency pair is required")
    val currencyPair: String
)
