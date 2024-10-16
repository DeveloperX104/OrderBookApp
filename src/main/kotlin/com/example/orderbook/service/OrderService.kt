package com.example.orderbook.service

import com.example.orderbook.model.LimitOrderRequest
import com.example.orderbook.model.Order
import com.example.orderbook.model.OrderBook
import com.example.orderbook.model.Trade
import com.example.orderbook.repository.OrderRepository
import java.time.Instant

class OrderService(private val orderRepository: OrderRepository) {

    private val validSides = setOf("buy", "sell")
    private val supportedCurrencyPairs = setOf("BTCZAR") 

    fun getOrderBook(): OrderBook {
        return orderRepository.getOrderBook()
    }

    fun submitLimitOrder(orderRequest: LimitOrderRequest) {
        if (orderRequest.side.lowercase() !in validSides) {
            throw IllegalArgumentException("Invalid side. Expected 'buy' or 'sell'.")
        }
        if (orderRequest.quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than zero.")
        }
        if (orderRequest.price <= 0) {
            throw IllegalArgumentException("Price must be greater than zero.")
        }
        if (orderRequest.currencyPair.uppercase() !in supportedCurrencyPairs) {
            throw IllegalArgumentException("Unsupported currency pair: ${orderRequest.currencyPair}")
        }

        val order = Order(
            price = orderRequest.price,
            quantity = orderRequest.quantity,
            side = orderRequest.side.lowercase(),
            currencyPair = orderRequest.currencyPair.uppercase()
        )
        orderRepository.saveOrder(order)
        matchOrders(order)
    }

    private fun matchOrders(order: Order) {
        val oppositeOrders = if (order.side == "buy") orderRepository.getSellOrders() else orderRepository.getBuyOrders()
        val matchedTrades = mutableListOf<Trade>()

        for ((price, ordersAtPrice) in oppositeOrders) {
            if (order.quantity <= 0) break

            if ((order.side == "buy" && order.price >= price) || (order.side == "sell" && order.price <= price)) {
                val iterator = ordersAtPrice.iterator()
                while (iterator.hasNext() && order.quantity > 0) {
                    val oppositeOrder = iterator.next() 
                    val tradeQuantity = minOf(order.quantity, oppositeOrder.quantity)

                    matchedTrades.add(
                        Trade(
                            price = price,
                            quantity = tradeQuantity,
                            takerSide = order.side,
                            currencyPair = order.currencyPair,
                            tradedAt = Instant.now().toString(),
                            quoteVolume = price * tradeQuantity
                        )
                    )
                    order.quantity -= tradeQuantity
                    oppositeOrder.quantity -= tradeQuantity

                    if (oppositeOrder.quantity <= 0) iterator.remove()
                }
            }
        }

        // Record trades and clean up empty orders
        matchedTrades.forEach(orderRepository::recordTrade)
        orderRepository.removeEmptyOrders() 
    }

    fun getRecentTrades(): List<Trade> {
        return orderRepository.getRecentTrades()
    }
}
