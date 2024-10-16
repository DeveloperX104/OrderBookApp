package com.example.orderbook.controller

import com.example.orderbook.service.OrderService
import com.example.orderbook.model.LimitOrderRequest
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import javax.validation.Validation
import com.fasterxml.jackson.databind.ObjectMapper

class OrderController(
    private val orderService: OrderService,
    private val apiKey: String,
    private val objectMapper: ObjectMapper 
) {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun checkAuthorization(ctx: RoutingContext): Boolean {
        val authHeader = ctx.request().getHeader("Authorization")
        return authHeader == "$apiKey"
    }

    private fun withAuth(ctx: RoutingContext, handler: (RoutingContext) -> Unit) {
        if (!checkAuthorization(ctx)) {
            ctx.response().setStatusCode(401).end("Unauthorized")
        } else {
            handler(ctx)
        }
    }

    fun getOrderBook(ctx: RoutingContext) = withAuth(ctx) {
        val orderBook = orderService.getOrderBook()
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(Json.encodePrettily(orderBook))
    }

    fun submitLimitOrder(ctx: RoutingContext) = withAuth(ctx) {
        try {
            val orderRequest = objectMapper.readValue(ctx.body().asString(), LimitOrderRequest::class.java)

            // Validate the order request
            val violations = validator.validate(orderRequest)
            if (violations.isNotEmpty()) {
                val errors = violations.map { violation ->
                    violation.propertyPath.toString() to violation.message
                }.toMap()
                ctx.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(errors))
            } else {
                orderService.submitLimitOrder(orderRequest)
                ctx.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(mapOf("message" to "Order placed successfully")))
            }
        } catch (e: IllegalArgumentException) {
            ctx.response()
                .setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(mapOf("error" to e.message)))
        } catch (e: Exception) {
            ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(mapOf("error" to "Internal server error")))
        }
    }

    fun getRecentTrades(ctx: RoutingContext) = withAuth(ctx) {
        val trades = orderService.getRecentTrades()
        ctx.response()
            .putHeader("content-type", "application/json")
            .end(Json.encodePrettily(trades))
    }
}

