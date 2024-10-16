package com.example.orderbook.controller

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OrderControllerTest {

    private lateinit var vertx: Vertx
    private lateinit var client: WebClient

    @BeforeEach
    fun setUp() {
        vertx = Vertx.vertx()
        client = WebClient.create(vertx)
    }

    @Test
    fun testBasicConnectivity() {
        val latch = CountDownLatch(1)
        client.get(8080, "localhost", "/orderbook").send { response ->
            if (response.succeeded()) {
                val result = response.result()
                println("GET /orderbook succeeded with status code: ${result.statusCode()}")
                assertEquals(200, result.statusCode())
                assertNotNull(result.bodyAsJsonObject())
            } else {
                println("GET /orderbook failed: ${response.cause()}")
                throw AssertionError("Request failed: ${response.cause()}")
            }
            latch.countDown()
        }
        
        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun testSingleOrderSubmission() {
        val latch = CountDownLatch(1)
        val buyOrderJson = JsonObject().put("price", 1000.0).put("quantity", 0.5)
            .put("side", "buy").put("currencyPair", "BTCZAR")
        
        client.post(8080, "localhost", "/orders/limit").sendJsonObject(buyOrderJson) { response ->
            if (response.succeeded()) {
                val result = response.result()
                println("POST /orders/limit succeeded with status code: ${result.statusCode()}")
                assertEquals(201, result.statusCode())
                assertNotNull(result.bodyAsJsonObject())
            } else {
                println("POST /orders/limit failed: ${response.cause()}")
                throw AssertionError("Request failed: ${response.cause()}")
            }
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun testOrderBookAfterOrderSubmission() {
        val latch = CountDownLatch(1)
        val buyOrderJson = JsonObject().put("price", 1000.0).put("quantity", 0.5)
            .put("side", "buy").put("currencyPair", "BTCZAR")
        val sellOrderJson = JsonObject().put("price", 1200.0).put("quantity", 1.0)
            .put("side", "sell").put("currencyPair", "BTCZAR")

        client.post(8080, "localhost", "/orders/limit").sendJsonObject(buyOrderJson) { buyResponse ->
            if (buyResponse.failed()) throw AssertionError("Buy order failed: ${buyResponse.cause()}")
            
            client.post(8080, "localhost", "/orders/limit").sendJsonObject(sellOrderJson) { sellResponse ->
                if (sellResponse.failed()) throw AssertionError("Sell order failed: ${sellResponse.cause()}")
                
                client.get(8080, "localhost", "/orderbook").send { orderBookResponse ->
                    if (orderBookResponse.succeeded()) {
                        val orderBook = orderBookResponse.result().bodyAsJsonObject()
                        val asks = orderBook.getJsonArray("asks")
                        val bids = orderBook.getJsonArray("bids")

                        assertNotNull(asks)
                        assertNotNull(bids)

                        val firstAsk = asks.getJsonObject(0)
                        assertEquals(1200.0, firstAsk.getDouble("price"))
                        assertEquals(1.0, firstAsk.getDouble("quantity"))
                        assertEquals(1, firstAsk.getInteger("orderCount"))

                        val firstBid = bids.getJsonObject(0)
                        assertEquals(1000.0, firstBid.getDouble("price"))
                        assertEquals(0.5, firstBid.getDouble("quantity"))
                        assertEquals(1, firstBid.getInteger("orderCount"))
                        
                        println("GET /orderbook validated successfully")
                    } else {
                        println("GET /orderbook failed: ${orderBookResponse.cause()}")
                        throw AssertionError("Order book retrieval failed: ${orderBookResponse.cause()}")
                    }
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun testInvalidOrderNegativePrice() {
        val latch = CountDownLatch(1)
        val invalidOrderJson = JsonObject().put("price", -1000.0).put("quantity", 0.5)
            .put("side", "buy").put("currencyPair", "BTCZAR")

        client.post(8080, "localhost", "/orders/limit").sendJsonObject(invalidOrderJson) { response ->
            if (response.succeeded()) {
                val result = response.result()
                println("POST with negative price status code: ${result.statusCode()}")
                assertEquals(400, result.statusCode())
                assertNotNull(result.bodyAsJsonObject().getString("error"))
            } else {
                throw AssertionError("Request failed: ${response.cause()}")
            }
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun testMissingRequiredFields() {
        val latch = CountDownLatch(1)
        val missingFieldOrderJson = JsonObject().put("quantity", 1.0)
            .put("side", "sell").put("currencyPair", "BTCZAR")

        client.post(8080, "localhost", "/orders/limit").sendJsonObject(missingFieldOrderJson) { response ->
            if (response.succeeded()) {
                val result = response.result()
                println("POST with missing field status code: ${result.statusCode()}")
                assertEquals(400, result.statusCode())
                assertNotNull(result.bodyAsJsonObject().getString("error"))
            } else {
                throw AssertionError("Request failed: ${response.cause()}")
            }
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun testUnauthorizedRequest() {
        val latch = CountDownLatch(1)
        val validOrderJson = JsonObject().put("price", 1000.0).put("quantity", 0.5)
            .put("side", "buy").put("currencyPair", "BTCZAR")

        client.post(8080, "localhost", "/orders/limit")
            .putHeader("Authorization", "invalid-api-key")
            .sendJsonObject(validOrderJson) { response ->
                if (response.succeeded()) {
                    val result = response.result()
                    println("POST unauthorized status code: ${result.statusCode()}")
                    assertEquals(401, result.statusCode())
                    assertNotNull(result.bodyAsJsonObject().getString("error"))
                } else {
                    throw AssertionError("Request failed: ${response.cause()}")
                }
                latch.countDown()
            }

        latch.await(10, TimeUnit.SECONDS)
    }

    @Test
    fun testLargeOrderQuantity() {
        val latch = CountDownLatch(1)
        val largeOrderJson = JsonObject().put("price", 1000.0).put("quantity", 1_000_000.0)
            .put("side", "buy").put("currencyPair", "BTCZAR")

        client.post(8080, "localhost", "/orders/limit").sendJsonObject(largeOrderJson) { response ->
            if (response.succeeded()) {
                val result = response.result()
                println("POST with large quantity status code: ${result.statusCode()}")
                assertEquals(201, result.statusCode())
                assertNotNull(result.bodyAsJsonObject())
            } else {
                throw AssertionError("Request failed: ${response.cause()}")
            }
            latch.countDown()
        }

        latch.await(10, TimeUnit.SECONDS)
    }
}
