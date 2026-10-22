package com.example.data.repository

import com.example.data.local.SmartStockDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.InventoryMovement
import com.example.data.model.OnlineOrder
import com.example.data.model.Product
import com.example.data.model.ProductVariant
import com.example.data.model.ProductWithVariants
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.Supplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SmartStockRepository(private val db: SmartStockDatabase) {
    private val productDao = db.productDao()
    private val categoryDao = db.categoryDao()
    private val inventoryDao = db.inventoryDao()
    private val saleDao = db.saleDao()
    private val orderDao = db.orderDao()
    private val customerSupplierDao = db.customerSupplierDao()
    private val businessProfileDao = db.businessProfileDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allVariants: Flow<List<ProductVariant>> = productDao.getAllVariants()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allMovements: Flow<List<InventoryMovement>> = inventoryDao.getAllMovements()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allSaleItems: Flow<List<SaleItem>> = saleDao.getAllSaleItems()
    val allOrders: Flow<List<OnlineOrder>> = orderDao.getAllOrders()
    val allCustomers: Flow<List<Customer>> = customerSupplierDao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> = customerSupplierDao.getAllSuppliers()
    val businessProfile: Flow<BusinessProfile?> = businessProfileDao.getProfile()

    // Combined Flow: Products with their Variants
    val productsWithVariants: Flow<List<ProductWithVariants>> =
        combine(allProducts, allVariants) { products, variants ->
            val variantMap = variants.groupBy { it.productId }
            products.map { product ->
                ProductWithVariants(
                    product = product,
                    variants = variantMap[product.id] ?: emptyList()
                )
            }
        }

    suspend fun getSaleItems(saleId: Long): List<SaleItem> = withContext(Dispatchers.IO) {
        saleDao.getSaleItemsSync(saleId)
    }

    /**
     * Executes a sale atomically:
     * 1. Validates stock for all variants
     * 2. Inserts sale record
     * 3. Inserts sale items
     * 4. Decrements variant stock
     * 5. Logs inventory movements
     */
    suspend fun recordSale(
        cartItems: List<CartItem>,
        customerName: String,
        customerPhone: String,
        discountAmount: Double,
        paymentMethod: String,
        salesChannel: String
    ): Result<Sale> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Cart cannot be empty"))
        }

        // 1. Validate Stock: No Negative Stock
        for (item in cartItems) {
            val currentVariant = productDao.getVariantById(item.variant.id)
                ?: return@withContext Result.failure(IllegalStateException("Variant ${item.variant.sku} not found"))
            if (currentVariant.currentQuantity < item.quantity) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Insufficient stock for ${item.product.name} (${currentVariant.color}/${currentVariant.size}). Available: ${currentVariant.currentQuantity}, Requested: ${item.quantity}"
                    )
                )
            }
        }

        val subtotal = cartItems.sumOf { it.lineTotal }
        val finalTotal = (subtotal - discountAmount).coerceAtLeast(0.0)
        val saleNumber = "SL-${(1000..9999).random()}"

        val sale = Sale(
            saleNumber = saleNumber,
            customerName = customerName.ifBlank { "Walk-in Customer" },
            customerPhone = customerPhone,
            subtotal = subtotal,
            discountAmount = discountAmount,
            totalAmount = finalTotal,
            paymentMethod = paymentMethod,
            paymentStatus = "PAID",
            salesChannel = salesChannel,
            status = "COMPLETED"
        )

        val saleId = saleDao.insertSale(sale)
        val createdSale = sale.copy(id = saleId)

        val saleItems = cartItems.map { item ->
            SaleItem(
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                variantId = item.variant.id,
                variantInfo = "${item.variant.color} / ${item.variant.size}",
                quantity = item.quantity,
                unitPrice = item.variant.sellingPrice,
                unitCost = item.variant.purchaseCost,
                lineTotal = item.lineTotal
            )
        }
        saleDao.insertSaleItems(saleItems)

        // Decrement variant stock & log movement
        for (item in cartItems) {
            val variant = productDao.getVariantById(item.variant.id) ?: continue
            val newQty = variant.currentQuantity - item.quantity
            productDao.updateVariantQuantity(variant.id, newQty)

            inventoryDao.insertMovement(
                InventoryMovement(
                    variantId = variant.id,
                    productId = item.product.id,
                    productName = item.product.name,
                    variantInfo = "${variant.color} / ${variant.size}",
                    movementType = "SALE",
                    quantityChange = -item.quantity,
                    quantityAfter = newQty,
                    reason = "Sale $saleNumber ($salesChannel)",
                    referenceId = saleNumber
                )
            )
        }

        // If customer exists by name or phone, update purchases
        if (customerPhone.isNotBlank()) {
            val customers = customerSupplierDao.getAllCustomers().first()
            val match = customers.find { it.phone == customerPhone }
            if (match != null) {
                customerSupplierDao.updateCustomerStats(match.id, finalTotal)
            } else if (customerName.isNotBlank() && customerName != "Walk-in Customer") {
                customerSupplierDao.insertCustomer(
                    Customer(
                        name = customerName,
                        phone = customerPhone,
                        totalSpent = finalTotal,
                        totalOrders = 1
                    )
                )
            }
        }

        Result.success(createdSale)
    }

    /**
     * Cancel or Return sale: Restores inventory and marks status
     */
    suspend fun cancelOrReturnSale(saleId: Long, isReturn: Boolean, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val sale = saleDao.getSaleById(saleId)
            ?: return@withContext Result.failure(IllegalArgumentException("Sale not found"))

        if (sale.status == "CANCELLED" || sale.status == "RETURNED") {
            return@withContext Result.failure(IllegalStateException("Sale is already marked as ${sale.status}"))
        }

        val saleItems = saleDao.getSaleItemsSync(saleId)
        val newStatus = if (isReturn) "RETURNED" else "CANCELLED"
        val movementType = if (isReturn) "RETURN" else "CANCELLATION"

        // Restore stock
        for (item in saleItems) {
            val variant = productDao.getVariantById(item.variantId)
            if (variant != null) {
                val newQty = variant.currentQuantity + item.quantity
                productDao.updateVariantQuantity(variant.id, newQty)

                inventoryDao.insertMovement(
                    InventoryMovement(
                        variantId = variant.id,
                        productId = item.productId,
                        productName = item.productName,
                        variantInfo = item.variantInfo,
                        movementType = movementType,
                        quantityChange = item.quantity,
                        quantityAfter = newQty,
                        reason = "$newStatus: $reason (${sale.saleNumber})",
                        referenceId = sale.saleNumber
                    )
                )
            }
        }

        saleDao.updateSaleStatus(saleId, newStatus)
        Result.success(Unit)
    }

    /**
     * Stock In / Restock variant
     */
    suspend fun addStockIn(
        variantId: Long,
        quantityToAdd: Int,
        supplierName: String,
        notes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (quantityToAdd <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        }
        val variant = productDao.getVariantById(variantId)
            ?: return@withContext Result.failure(IllegalArgumentException("Variant not found"))
        val product = productDao.getProductById(variant.productId)

        val newQty = variant.currentQuantity + quantityToAdd
        productDao.updateVariantQuantity(variantId, newQty)

        inventoryDao.insertMovement(
            InventoryMovement(
                variantId = variant.id,
                productId = variant.productId,
                productName = product?.name ?: "Product",
                variantInfo = "${variant.color} / ${variant.size}",
                movementType = "STOCK_IN",
                quantityChange = quantityToAdd,
                quantityAfter = newQty,
                reason = "Received from $supplierName: ${notes.ifBlank { "Regular restock" }}",
                referenceId = "RESTOCK"
            )
        )
        Result.success(Unit)
    }

    /**
     * Adjust inventory quantity (damages, audit count, corrections)
     */
    suspend fun adjustStock(
        variantId: Long,
        newQuantity: Int,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (newQuantity < 0) {
            return@withContext Result.failure(IllegalArgumentException("Stock quantity cannot be negative"))
        }
        val variant = productDao.getVariantById(variantId)
            ?: return@withContext Result.failure(IllegalArgumentException("Variant not found"))
        val product = productDao.getProductById(variant.productId)

        val diff = newQuantity - variant.currentQuantity
        if (diff == 0) return@withContext Result.success(Unit)

        productDao.updateVariantQuantity(variantId, newQuantity)

        inventoryDao.insertMovement(
            InventoryMovement(
                variantId = variant.id,
                productId = variant.productId,
                productName = product?.name ?: "Product",
                variantInfo = "${variant.color} / ${variant.size}",
                movementType = "ADJUSTMENT",
                quantityChange = diff,
                quantityAfter = newQuantity,
                reason = "Adjustment: $reason",
                referenceId = "ADJUST"
            )
        )
        Result.success(Unit)
    }

    /**
     * Save or update product with its size & color variants
     */
    suspend fun saveProductWithVariants(
        product: Product,
        variants: List<ProductVariant>
    ): Long = withContext(Dispatchers.IO) {
        val productId = if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }

        // Process variants
        for (v in variants) {
            val variantWithProdId = v.copy(productId = productId)
            if (variantWithProdId.id == 0L) {
                val varId = productDao.insertVariant(variantWithProdId)
                if (variantWithProdId.currentQuantity > 0) {
                    inventoryDao.insertMovement(
                        InventoryMovement(
                            variantId = varId,
                            productId = productId,
                            productName = product.name,
                            variantInfo = "${v.color} / ${v.size}",
                            movementType = "STOCK_IN",
                            quantityChange = v.currentQuantity,
                            quantityAfter = v.currentQuantity,
                            reason = "Initial inventory setup",
                            referenceId = product.sku
                        )
                    )
                }
            } else {
                productDao.updateVariant(variantWithProdId)
            }
        }
        productId
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun saveOrder(order: OnlineOrder): Long = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }

    suspend fun saveCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        customerSupplierDao.insertCustomer(customer)
    }

    suspend fun saveSupplier(supplier: Supplier): Long = withContext(Dispatchers.IO) {
        customerSupplierDao.insertSupplier(supplier)
    }

    suspend fun updateBusinessProfile(profile: BusinessProfile) = withContext(Dispatchers.IO) {
        businessProfileDao.insertOrUpdateProfile(profile)
    }

    /**
     * Seed initial demo data for clothing boutique
     */
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingProds = productDao.getAllProducts().first()
        if (existingProds.isNotEmpty()) return@withContext

        // Categories
        val categories = listOf(
            Category(name = "T-Shirts"),
            Category(name = "Dresses"),
            Category(name = "Denim & Pants"),
            Category(name = "Hoodies & Sweats"),
            Category(name = "Outerwear"),
            Category(name = "Activewear"),
            Category(name = "Accessories")
        )
        categoryDao.insertCategories(categories)

        // Business Profile
        businessProfileDao.insertOrUpdateProfile(
            BusinessProfile(
                id = 1L,
                businessName = "SmartStock Boutique",
                currency = "TND",
                phone = "+216 98 450 120",
                address = "12 Rue de Marseille, Tunis",
                ownerName = "Heni M.",
                lowStockDefaultThreshold = 5
            )
        )

        // Suppliers
        val sup1Id = customerSupplierDao.insertSupplier(
            Supplier(
                name = "Atelier Mediterranean Garments",
                phone = "+216 71 890 123",
                address = "Monastir Industrial Zone",
                category = "Cotton & Knitwear Specialist",
                notes = "Lead time: 3 days. High quality 240gsm cotton."
            )
        )
        val sup2Id = customerSupplierDao.insertSupplier(
            Supplier(
                name = "Nordic Denim Works",
                phone = "+216 73 456 789",
                address = "Sousse Port Area",
                category = "Denim & Heavy Cotton",
                notes = "Premium Japanese & Turkish selvedge denim blends."
            )
        )

        // Customers
        customerSupplierDao.insertCustomer(
            Customer(
                name = "Sarra Ben Amor",
                phone = "+216 22 110 998",
                address = "La Marsa, Tunis",
                totalSpent = 380.0,
                totalOrders = 3
            )
        )
        customerSupplierDao.insertCustomer(
            Customer(
                name = "Yassine Dridi",
                phone = "+216 55 443 221",
                address = "Ennasr 2, Ariana",
                totalSpent = 245.0,
                totalOrders = 2
            )
        )
        customerSupplierDao.insertCustomer(
            Customer(
                name = "Nour Khemir",
                phone = "+216 99 887 766",
                address = "Sousse Ville",
                totalSpent = 520.0,
                totalOrders = 4
            )
        )

        // Products & Variants
        // Product 1: Oversized Heavyweight Tee
        val p1 = Product(
            name = "Oversized Heavyweight Tee",
            sku = "TEE-OVS-101",
            description = "100% premium combed cotton, 260 GSM drop-shoulder fit.",
            category = "T-Shirts",
            defaultCost = 22.0,
            defaultPrice = 49.0,
            supplierName = "Atelier Mediterranean Garments"
        )
        val p1Id = productDao.insertProduct(p1)
        val p1Variants = listOf(
            ProductVariant(productId = p1Id, size = "S", color = "Black", colorHex = "#18181B", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 14, minimumStock = 5, sku = "TEE-OVS-101-BLK-S"),
            ProductVariant(productId = p1Id, size = "M", color = "Black", colorHex = "#18181B", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 18, minimumStock = 5, sku = "TEE-OVS-101-BLK-M"),
            ProductVariant(productId = p1Id, size = "L", color = "Black", colorHex = "#18181B", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 8, minimumStock = 5, sku = "TEE-OVS-101-BLK-L"),
            ProductVariant(productId = p1Id, size = "XL", color = "Black", colorHex = "#18181B", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 3, minimumStock = 5, sku = "TEE-OVS-101-BLK-XL"), // Low stock!
            ProductVariant(productId = p1Id, size = "S", color = "Vintage White", colorHex = "#F4F4F5", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 12, minimumStock = 5, sku = "TEE-OVS-101-WHT-S"),
            ProductVariant(productId = p1Id, size = "M", color = "Vintage White", colorHex = "#F4F4F5", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 15, minimumStock = 5, sku = "TEE-OVS-101-WHT-M"),
            ProductVariant(productId = p1Id, size = "L", color = "Vintage White", colorHex = "#F4F4F5", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 2, minimumStock = 5, sku = "TEE-OVS-101-WHT-L"), // Low stock!
            ProductVariant(productId = p1Id, size = "M", color = "Sage Green", colorHex = "#84A98C", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 9, minimumStock = 5, sku = "TEE-OVS-101-SGE-M"),
            ProductVariant(productId = p1Id, size = "L", color = "Sage Green", colorHex = "#84A98C", purchaseCost = 22.0, sellingPrice = 49.0, currentQuantity = 0, minimumStock = 5, sku = "TEE-OVS-101-SGE-L")  // Out of stock!
        )
        productDao.insertVariants(p1Variants)

        // Product 2: High-Rise Straight Denim Jeans
        val p2 = Product(
            name = "High-Rise Straight Denim",
            sku = "JNS-STR-202",
            description = "Classic 5-pocket denim with comfort stretch and clean vintage wash.",
            category = "Denim & Pants",
            defaultCost = 45.0,
            defaultPrice = 98.0,
            supplierName = "Nordic Denim Works"
        )
        val p2Id = productDao.insertProduct(p2)
        val p2Variants = listOf(
            ProductVariant(productId = p2Id, size = "26", color = "Vintage Indigo", colorHex = "#2B4C7E", purchaseCost = 45.0, sellingPrice = 98.0, currentQuantity = 6, minimumStock = 4, sku = "JNS-202-IND-26"),
            ProductVariant(productId = p2Id, size = "28", color = "Vintage Indigo", colorHex = "#2B4C7E", purchaseCost = 45.0, sellingPrice = 98.0, currentQuantity = 11, minimumStock = 4, sku = "JNS-202-IND-28"),
            ProductVariant(productId = p2Id, size = "30", color = "Vintage Indigo", colorHex = "#2B4C7E", purchaseCost = 45.0, sellingPrice = 98.0, currentQuantity = 4, minimumStock = 4, sku = "JNS-202-IND-30"), // Low stock
            ProductVariant(productId = p2Id, size = "32", color = "Vintage Indigo", colorHex = "#2B4C7E", purchaseCost = 45.0, sellingPrice = 98.0, currentQuantity = 7, minimumStock = 4, sku = "JNS-202-IND-32"),
            ProductVariant(productId = p2Id, size = "28", color = "Washed Black", colorHex = "#27272A", purchaseCost = 45.0, sellingPrice = 98.0, currentQuantity = 9, minimumStock = 4, sku = "JNS-202-BLK-28"),
            ProductVariant(productId = p2Id, size = "30", color = "Washed Black", colorHex = "#27272A", purchaseCost = 45.0, sellingPrice = 98.0, currentQuantity = 5, minimumStock = 4, sku = "JNS-202-BLK-30")
        )
        productDao.insertVariants(p2Variants)

        // Product 3: Linen Blend Wrap Midi Dress
        val p3 = Product(
            name = "Linen Wrap Midi Dress",
            sku = "DRS-LIN-303",
            description = "Breathable lightweight linen blend with side-tie belt and subtle V-neck.",
            category = "Dresses",
            defaultCost = 55.0,
            defaultPrice = 129.0,
            supplierName = "Atelier Mediterranean Garments"
        )
        val p3Id = productDao.insertProduct(p3)
        val p3Variants = listOf(
            ProductVariant(productId = p3Id, size = "S", color = "Terracotta", colorHex = "#C86446", purchaseCost = 55.0, sellingPrice = 129.0, currentQuantity = 8, minimumStock = 4, sku = "DRS-303-TER-S"),
            ProductVariant(productId = p3Id, size = "M", color = "Terracotta", colorHex = "#C86446", purchaseCost = 55.0, sellingPrice = 129.0, currentQuantity = 10, minimumStock = 4, sku = "DRS-303-TER-M"),
            ProductVariant(productId = p3Id, size = "L", color = "Terracotta", colorHex = "#C86446", purchaseCost = 55.0, sellingPrice = 129.0, currentQuantity = 2, minimumStock = 4, sku = "DRS-303-TER-L"), // Low stock
            ProductVariant(productId = p3Id, size = "S", color = "Natural Beige", colorHex = "#D7C9AA", purchaseCost = 55.0, sellingPrice = 129.0, currentQuantity = 7, minimumStock = 4, sku = "DRS-303-BEI-S"),
            ProductVariant(productId = p3Id, size = "M", color = "Natural Beige", colorHex = "#D7C9AA", purchaseCost = 55.0, sellingPrice = 129.0, currentQuantity = 9, minimumStock = 4, sku = "DRS-303-BEI-M")
        )
        productDao.insertVariants(p3Variants)

        // Product 4: Relaxed Fleece Hoodie
        val p4 = Product(
            name = "Relaxed Fleece Hoodie",
            sku = "HOD-FLC-404",
            description = "Super-soft brushed interior with double-layered hood and front pouch.",
            category = "Hoodies & Sweats",
            defaultCost = 38.0,
            defaultPrice = 85.0,
            supplierName = "Atelier Mediterranean Garments"
        )
        val p4Id = productDao.insertProduct(p4)
        val p4Variants = listOf(
            ProductVariant(productId = p4Id, size = "M", color = "Heather Grey", colorHex = "#9CA3AF", purchaseCost = 38.0, sellingPrice = 85.0, currentQuantity = 12, minimumStock = 5, sku = "HOD-404-GRY-M"),
            ProductVariant(productId = p4Id, size = "L", color = "Heather Grey", colorHex = "#9CA3AF", purchaseCost = 38.0, sellingPrice = 85.0, currentQuantity = 15, minimumStock = 5, sku = "HOD-404-GRY-L"),
            ProductVariant(productId = p4Id, size = "XL", color = "Heather Grey", colorHex = "#9CA3AF", purchaseCost = 38.0, sellingPrice = 85.0, currentQuantity = 6, minimumStock = 5, sku = "HOD-404-GRY-XL"),
            ProductVariant(productId = p4Id, size = "M", color = "Midnight Navy", colorHex = "#1E3A8A", purchaseCost = 38.0, sellingPrice = 85.0, currentQuantity = 8, minimumStock = 5, sku = "HOD-404-NVY-M"),
            ProductVariant(productId = p4Id, size = "L", color = "Midnight Navy", colorHex = "#1E3A8A", purchaseCost = 38.0, sellingPrice = 85.0, currentQuantity = 1, minimumStock = 5, sku = "HOD-404-NVY-L") // Low stock!
        )
        productDao.insertVariants(p4Variants)

        // Seed Sales History
        val s1 = Sale(
            saleNumber = "SL-1048",
            customerName = "Sarra Ben Amor",
            customerPhone = "+216 22 110 998",
            subtotal = 178.0,
            discountAmount = 10.0,
            totalAmount = 168.0,
            paymentMethod = "CARD",
            salesChannel = "Physical Store",
            status = "COMPLETED",
            createdAt = System.currentTimeMillis() - 3600_000 * 3
        )
        val s1Id = saleDao.insertSale(s1)
        saleDao.insertSaleItems(
            listOf(
                SaleItem(saleId = s1Id, productId = p1Id, productName = "Oversized Heavyweight Tee", variantId = 1, variantInfo = "Black / M", quantity = 1, unitPrice = 49.0, unitCost = 22.0, lineTotal = 49.0),
                SaleItem(saleId = s1Id, productId = p3Id, productName = "Linen Wrap Midi Dress", variantId = 15, variantInfo = "Terracotta / M", quantity = 1, unitPrice = 129.0, unitCost = 55.0, lineTotal = 129.0)
            )
        )

        val s2 = Sale(
            saleNumber = "SL-1047",
            customerName = "Walk-in Customer",
            customerPhone = "",
            subtotal = 98.0,
            discountAmount = 0.0,
            totalAmount = 98.0,
            paymentMethod = "CASH",
            salesChannel = "Physical Store",
            status = "COMPLETED",
            createdAt = System.currentTimeMillis() - 3600_000 * 5
        )
        val s2Id = saleDao.insertSale(s2)
        saleDao.insertSaleItems(
            listOf(
                SaleItem(saleId = s2Id, productId = p2Id, productName = "High-Rise Straight Denim", variantId = 10, variantInfo = "Vintage Indigo / 28", quantity = 1, unitPrice = 98.0, unitCost = 45.0, lineTotal = 98.0)
            )
        )

        val s3 = Sale(
            saleNumber = "SL-1046",
            customerName = "Nour Khemir",
            customerPhone = "+216 99 887 766",
            subtotal = 183.0,
            discountAmount = 15.0,
            totalAmount = 168.0,
            paymentMethod = "COD",
            salesChannel = "Instagram",
            status = "COMPLETED",
            createdAt = System.currentTimeMillis() - 3600_000 * 26
        )
        val s3Id = saleDao.insertSale(s3)
        saleDao.insertSaleItems(
            listOf(
                SaleItem(saleId = s3Id, productId = p1Id, productName = "Oversized Heavyweight Tee", variantId = 2, variantInfo = "Vintage White / S", quantity = 2, unitPrice = 49.0, unitCost = 22.0, lineTotal = 98.0),
                SaleItem(saleId = s3Id, productId = p4Id, productName = "Relaxed Fleece Hoodie", variantId = 18, variantInfo = "Heather Grey / L", quantity = 1, unitPrice = 85.0, unitCost = 38.0, lineTotal = 85.0)
            )
        )

        // Seed Online Orders
        orderDao.insertOrder(
            OnlineOrder(
                orderNumber = "ORD-702",
                customerName = "Mariem Saidi",
                customerPhone = "+216 28 334 556",
                deliveryAddress = "Appartement B4, Résidence Jasmin, Bardo, Tunis",
                deliveryFee = 7.0,
                itemsSummary = "1x Linen Wrap Midi Dress (Beige/M), 1x Heavyweight Tee (White/M)",
                totalAmount = 185.0,
                orderStatus = "CONFIRMED",
                salesChannel = "Instagram",
                paymentMethod = "Cash on Delivery",
                notes = "Deliver between 2 PM and 6 PM"
            )
        )
        orderDao.insertOrder(
            OnlineOrder(
                orderNumber = "ORD-701",
                customerName = "Karim Trabelsi",
                customerPhone = "+216 97 123 789",
                deliveryAddress = "Villa 14, Rue des Orangers, Nabeul",
                deliveryFee = 8.0,
                itemsSummary = "2x Relaxed Fleece Hoodie (Navy/L, Grey/XL)",
                totalAmount = 178.0,
                orderStatus = "SHIPPED",
                salesChannel = "WhatsApp",
                paymentMethod = "Bank Transfer",
                notes = "Aramex Tracking: TN902183"
            )
        )

        // Seed Inventory Movements
        inventoryDao.insertMovements(
            listOf(
                InventoryMovement(variantId = 1, productId = p1Id, productName = "Oversized Heavyweight Tee", variantInfo = "Black / M", movementType = "SALE", quantityChange = -1, quantityAfter = 18, reason = "Sale SL-1048 (Physical Store)", referenceId = "SL-1048"),
                InventoryMovement(variantId = 15, productId = p3Id, productName = "Linen Wrap Midi Dress", variantInfo = "Terracotta / M", movementType = "SALE", quantityChange = -1, quantityAfter = 10, reason = "Sale SL-1048 (Physical Store)", referenceId = "SL-1048"),
                InventoryMovement(variantId = 10, productId = p2Id, productName = "High-Rise Straight Denim", variantInfo = "Vintage Indigo / 28", movementType = "SALE", quantityChange = -1, quantityAfter = 11, reason = "Sale SL-1047 (Physical Store)", referenceId = "SL-1047"),
                InventoryMovement(variantId = 1, productId = p1Id, productName = "Oversized Heavyweight Tee", variantInfo = "Black / M", movementType = "STOCK_IN", quantityChange = 15, quantityAfter = 19, reason = "Received from Atelier Mediterranean Garments", referenceId = "RESTOCK"),
                InventoryMovement(variantId = 9, productId = p1Id, productName = "Oversized Heavyweight Tee", variantInfo = "Sage Green / L", movementType = "ADJUSTMENT", quantityChange = -1, quantityAfter = 0, reason = "Adjustment: Fabric pull / damaged sample", referenceId = "ADJUST")
            )
        )
    }
}
