package com.example.orderbook.repository

import com.example.orderbook.model.Order
import com.example.orderbook.model.OrderBook
import com.example.orderbook.model.Trade
import java.util.*

interface OrderRepository {

    fun getOrderBook(): OrderBook
    fun saveOrder(order: Order)
    fun getRecentTrades(): List<Trade>
    fun getBuyOrders(): SortedMap<Double, MutableList<Order>>
    fun getSellOrders(): SortedMap<Double, MutableList<Order>>
    fun recordTrade(trade: Trade)
    fun removeEmptyOrders()
}
