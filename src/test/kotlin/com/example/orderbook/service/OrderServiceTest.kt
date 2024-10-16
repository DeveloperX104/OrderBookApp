package com.example.orderbook.service

import com.example.orderbook.model.LimitOrderRequest
import com.example.orderbook.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderServiceTest {

    private val repository = InMemoryOrderRepository()
    private val service = OrderService(repository)

    @Test
    fun `getOrderBook includes order count and aggregated quantity for asks and bids`() {
        service.submitLimitOrder(LimitOrderRequest(1000.0, 0.5, "buy", "BTCZAR"))
        service.submitLimitOrder(LimitOrderRequest(1000.0, 1.0, "buy", "BTCZAR"))
        service.submitLimitOrder(LimitOrderRequest(1200.0, 2.0, "sell", "BTCZAR"))
        service.submitLimitOrder(LimitOrderRequest(1200.0, 0.8, "sell", "BTCZAR"))

        val orderBook = service.getOrderBook()

        val firstBid = orderBook.bids.find { it.price == 1000.0 }
        assertNotNull(firstBid)
        assertEquals(1.5, firstBid!!.quantity)
        assertEquals(2, firstBid.orderCount)

        val firstAsk = orderBook.asks.find { it.price == 1200.0 }
        assertNotNull(firstAsk)
        assertEquals(2.8, firstAsk!!.quantity)
        assertEquals(2, firstAsk.orderCount)
    }

    @Test
    fun `submit order with invalid side throws exception`() {
        val request = LimitOrderRequest(100.0, 1.0, "hold", "BTCZAR")
        assertThrows<IllegalArgumentException> {
            service.submitLimitOrder(request)
        }
    }

    @Test
    fun `submit order with zero or negative quantity throws exception`() {
        val zeroQuantityOrder = LimitOrderRequest(1000.0, 0.0, "buy", "BTCZAR")
        val negativeQuantityOrder = LimitOrderRequest(1000.0, -1.0, "buy", "BTCZAR")

        assertThrows<IllegalArgumentException> {
            service.submitLimitOrder(zeroQuantityOrder)
        }
        assertThrows<IllegalArgumentException> {
            service.submitLimitOrder(negativeQuantityOrder)
        }
    }

    @Test
    fun `submit order with zero or negative price throws exception`() {
        val zeroPriceOrder = LimitOrderRequest(0.0, 1.0, "buy", "BTCZAR")
        val negativePriceOrder = LimitOrderRequest(-1000.0, 1.0, "buy", "BTCZAR")

        assertThrows<IllegalArgumentException> {
            service.submitLimitOrder(zeroPriceOrder)
        }
        assertThrows<IllegalArgumentException> {
            service.submitLimitOrder(negativePriceOrder)
        }
    }

    @Test
    fun `large quantity and price order is accepted`() {
        val largeOrder = LimitOrderRequest(1_000_000.0, 1_000_000.0, "buy", "BTCZAR")
        service.submitLimitOrder(largeOrder)

        val orderBook = service.getOrderBook()
        val firstBid = orderBook.bids.find { it.price == 1_000_000.0 }

        assertNotNull(firstBid)
        assertEquals(1_000_000.0, firstBid!!.quantity)
        assertEquals(1, firstBid.orderCount)
    }

    @Test
    fun `unsupported currency pair throws exception`() {
        val unsupportedPairOrder = LimitOrderRequest(1000.0, 1.0, "buy", "ETHUSDC")
        assertThrows<IllegalArgumentException> {
            service.submitLimitOrder(unsupportedPairOrder)
        }
    }

    @Test
    fun `order book matches correctly and removes matched orders`() {
        service.submitLimitOrder(LimitOrderRequest(1000.0, 1.0, "buy", "BTCZAR"))
        service.submitLimitOrder(LimitOrderRequest(1000.0, 1.0, "sell", "BTCZAR"))

        val orderBook = service.getOrderBook()
        assertTrue(orderBook.bids.none { it.price == 1000.0 })
        assertTrue(orderBook.asks.none { it.price == 1000.0 })
    }

    @Test
    fun `order book retains unmatched orders`() {
        service.submitLimitOrder(LimitOrderRequest(1000.0, 1.0, "buy", "BTCZAR"))
        service.submitLimitOrder(LimitOrderRequest(1100.0, 1.0, "sell", "BTCZAR"))

        val orderBook = service.getOrderBook()

        val firstBid = orderBook.bids.find { it.price == 1000.0 }
        val firstAsk = orderBook.asks.find { it.price == 1100.0 }

        assertNotNull(firstBid)
        assertEquals(1.0, firstBid!!.quantity)

        assertNotNull(firstAsk)
        assertEquals(1.0, firstAsk!!.quantity)
    }
}
