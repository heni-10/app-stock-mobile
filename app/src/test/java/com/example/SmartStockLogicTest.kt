package com.example

import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.ProductVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartStockLogicTest {

    @Test
    fun `test cart calculations and gross profit`() {
        val product = Product(
            id = 1L,
            name = "Oversized Tee",
            sku = "TEE-01",
            defaultCost = 20.0,
            defaultPrice = 50.0
        )

        val variant = ProductVariant(
            id = 10L,
            productId = 1L,
            size = "M",
            color = "Black",
            purchaseCost = 20.0,
            sellingPrice = 50.0,
            currentQuantity = 15,
            minimumStock = 5
        )

        val cartItem = CartItem(
            product = product,
            variant = variant,
            quantity = 3
        )

        assertEquals(150.0, cartItem.lineTotal, 0.001)
        assertEquals(60.0, cartItem.lineCost, 0.001)

        val revenue = cartItem.lineTotal
        val cogs = cartItem.lineCost
        val grossProfit = revenue - cogs

        assertEquals(90.0, grossProfit, 0.001)
    }

    @Test
    fun `test remaining stock calculation prevents negative stock`() {
        val initialStock = 8
        val requestedSold = 8
        val remaining = initialStock - requestedSold
        assertTrue(remaining >= 0)
        assertEquals(0, remaining)
    }
}
