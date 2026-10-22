package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Long = 1L,
    val businessName: String = "SmartStock Boutique",
    val currency: String = "TND",
    val phone: String = "+216 98 123 456",
    val address: String = "Avenue Habib Bourguiba, Tunis",
    val ownerName: String = "Heni Mechria",
    val lowStockDefaultThreshold: Int = 5
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val sku: String,
    val description: String = "",
    val category: String = "T-Shirts",
    val defaultCost: Double = 0.0,
    val defaultPrice: Double = 0.0,
    val supplierName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class ProductVariant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val size: String, // XS, S, M, L, XL, XXL, 38, 40, etc.
    val color: String, // Black, White, Navy, Olive, Coral
    val colorHex: String = "#334155",
    val purchaseCost: Double,
    val sellingPrice: Double,
    val currentQuantity: Int,
    val minimumStock: Int = 5,
    val sku: String = ""
)

@Entity(tableName = "inventory_movements")
data class InventoryMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val variantId: Long,
    val productId: Long,
    val productName: String,
    val variantInfo: String, // e.g. "Black / M"
    val movementType: String, // "SALE", "STOCK_IN", "ADJUSTMENT", "RETURN", "CANCELLATION", "ORDER_RESERVE"
    val quantityChange: Int, // e.g. -2 for sale, +10 for stock in
    val quantityAfter: Int,
    val reason: String,
    val referenceId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val saleNumber: String, // "SL-1001"
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = "CASH", // "CASH", "CARD", "BANK_TRANSFER", "COD"
    val paymentStatus: String = "PAID",
    val salesChannel: String = "Physical Store", // "Physical Store", "Instagram", "WhatsApp", "Facebook"
    val status: String = "COMPLETED", // "COMPLETED", "CANCELLED", "RETURNED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val variantId: Long,
    val variantInfo: String,
    val quantity: Int,
    val unitPrice: Double,
    val unitCost: Double,
    val lineTotal: Double
)

@Entity(tableName = "online_orders")
data class OnlineOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryFee: Double = 0.0,
    val itemsSummary: String, // e.g. "2x Oversized Tee (Black/L), 1x Denim Jeans"
    val totalAmount: Double,
    val orderStatus: String = "NEW", // "NEW", "CONFIRMED", "PREPARING", "SHIPPED", "DELIVERED", "CANCELLED"
    val salesChannel: String = "Instagram", // "Instagram", "WhatsApp", "Facebook"
    val paymentMethod: String = "Cash on Delivery",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val totalSpent: Double = 0.0,
    val totalOrders: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val category: String = "Apparel Manufacturer",
    val notes: String = ""
)

// UI Aggregate Data Models
data class ProductWithVariants(
    val product: Product,
    val variants: List<ProductVariant>
) {
    val totalStock: Int get() = variants.sumOf { it.currentQuantity }
    val isLowStock: Boolean get() = variants.any { it.currentQuantity in 1..it.minimumStock }
    val isOutOfStock: Boolean get() = variants.all { it.currentQuantity <= 0 }
    val minPrice: Double get() = variants.minOfOrNull { it.sellingPrice } ?: product.defaultPrice
    val maxPrice: Double get() = variants.maxOfOrNull { it.sellingPrice } ?: product.defaultPrice
}

data class CartItem(
    val product: Product,
    val variant: ProductVariant,
    var quantity: Int
) {
    val lineTotal: Double get() = variant.sellingPrice * quantity
    val lineCost: Double get() = variant.purchaseCost * quantity
}
