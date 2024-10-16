package com.example.orderbook.repository

import com.example.orderbook.model.Order
import com.example.orderbook.model.OrderBook
import com.example.orderbook.model.Trade
import java.util.*

class InMemoryOrderRepository : OrderRepository {

    private val buyOrders = TreeMap<Double, MutableList<Order>>(Collections.reverseOrder())
    private val sellOrders = TreeMap<Double, MutableList<Order>>()
    private val recentTrades = mutableListOf<Trade>()

    override fun getOrderBook(): OrderBook {
        val asks = sellOrders.map { (price, orders) ->
            OrderBook.OrderBookEntry(
                price = price,
                quantity = orders.sumOf { it.quantity },
                orderCount = orders.size,
                currencyPair = orders.first().currencyPair
            )
        }

        val bids = buyOrders.map { (price, orders) ->
            OrderBook.OrderBookEntry(
                price = price,
                quantity = orders.sumOf { it.quantity },
                orderCount = orders.size,
                currencyPair = orders.first().currencyPair
            )
        }

        return OrderBook(
            asks = asks,
            bids = bids
        )
    }

    override fun saveOrder(order: Order) {
        val orders = if (order.side == "buy") buyOrders else sellOrders
        orders.computeIfAbsent(order.price) { mutableListOf() }.add(order)
    }

    override fun getRecentTrades(): List<Trade> = recentTrades

    override fun getBuyOrders(): SortedMap<Double, MutableList<Order>> = buyOrders
    override fun getSellOrders(): SortedMap<Double, MutableList<Order>> = sellOrders

    override fun recordTrade(trade: Trade) {
        if (recentTrades.size >= 50) recentTrades.removeAt(0) // Limit to last 50 trades
        recentTrades.add(trade)
    }


    override fun removeEmptyOrders() {
        buyOrders.entries.removeIf { (_, orders) ->
            orders.removeIf { it.quantity <= 0 }
            orders.isEmpty()
        }

        sellOrders.entries.removeIf { (_, orders) ->
            orders.removeIf { it.quantity <= 0 }
            orders.isEmpty()
        }
    }
}
