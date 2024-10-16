package com.example.orderbook.app

import com.example.orderbook.controller.OrderController
import com.example.orderbook.repository.InMemoryOrderRepository
import com.example.orderbook.service.OrderService
import com.typesafe.config.ConfigFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class MainVerticle : AbstractVerticle() {

    override fun start() {
        val config = ConfigFactory.load() 
        val port = config.getInt("server.port")
        val apiKey = config.getString("security.apiKey")
        val orderRepository = InMemoryOrderRepository()
        val orderService = OrderService(orderRepository)
        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val orderController = OrderController(orderService, apiKey, objectMapper)
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.get("/orderbook").handler(orderController::getOrderBook)
        router.post("/orders/limit").handler(orderController::submitLimitOrder)
        router.get("/trades/recent").handler(orderController::getRecentTrades)

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port) { http ->
                if (http.succeeded()) {
                    println("HTTP server started on port $port")
                } else {
                    println("HTTP server failed to start: ${http.cause()}")
                }
            }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val vertx = Vertx.vertx()
            vertx.deployVerticle(MainVerticle()) { res ->
                if (res.succeeded()) {
                    println("Deployment id is: ${res.result()}")
                } else {
                    println("Deployment failed: ${res.cause()}")
                }
            }
        }
    }
}
