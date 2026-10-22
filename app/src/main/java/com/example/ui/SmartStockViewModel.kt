package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.SmartStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    DASHBOARD,
    INVENTORY,
    NEW_SALE,
    SALES_HISTORY,
    ONLINE_ORDERS,
    REPORTS,
    CUSTOMERS_SUPPLIERS,
    STOCK_MOVEMENTS,
    SETTINGS,
    PRODUCT_DETAIL,
    ADD_EDIT_PRODUCT
}

data class DashboardMetrics(
    val totalProducts: Int = 0,
    val totalStockItems: Int = 0,
    val todaySalesCount: Int = 0,
    val todayRevenue: Double = 0.0,
    val monthRevenue: Double = 0.0,
    val estimatedGrossProfitMonth: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val inventoryWholesaleValue: Double = 0.0,
    val inventoryRetailValue: Double = 0.0
)

data class TopSeller(
    val productName: String,
    val unitsSold: Int,
    val revenue: Double
)

class SmartStockViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SmartStockDatabase.getDatabase(application)
    val repository = SmartStockRepository(db)

    // Current Screen Navigation Stack
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<AppScreen>()

    // Selected product for detail/edit
    private val _selectedProduct = MutableStateFlow<ProductWithVariants?>(null)
    val selectedProduct: StateFlow<ProductWithVariants?> = _selectedProduct.asStateFlow()

    // Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedStockFilter = MutableStateFlow("ALL") // "ALL", "LOW", "OUT", "IN"
    val dateFilter = MutableStateFlow("THIS_MONTH") // "TODAY", "LAST_7_DAYS", "THIS_MONTH", "ALL"

    // Cart for POS
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    val cartCustomerName = MutableStateFlow("")
    val cartCustomerPhone = MutableStateFlow("")
    val cartDiscount = MutableStateFlow(0.0)
    val cartPaymentMethod = MutableStateFlow("CASH")
    val cartSalesChannel = MutableStateFlow("Physical Store")

    private val _checkoutSuccessSale = MutableStateFlow<Sale?>(null)
    val checkoutSuccessSale: StateFlow<Sale?> = _checkoutSuccessSale.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Repositories streams
    val productsWithVariants: StateFlow<List<ProductWithVariants>> = repository.productsWithVariants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saleItems: StateFlow<List<SaleItem>> = repository.allSaleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val onlineOrders: StateFlow<List<OnlineOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movements: StateFlow<List<InventoryMovement>> = repository.allMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile: StateFlow<BusinessProfile?> = repository.businessProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered Products
    val filteredProducts = combine(
        productsWithVariants,
        searchQuery,
        selectedCategory,
        selectedStockFilter
    ) { list, query, category, stockFilter ->
        list.filter { item ->
            val matchQuery = query.isBlank() ||
                item.product.name.contains(query, ignoreCase = true) ||
                item.product.sku.contains(query, ignoreCase = true) ||
                item.variants.any { it.sku.contains(query, ignoreCase = true) || it.color.contains(query, ignoreCase = true) }

            val matchCategory = category == "All" || item.product.category.equals(category, ignoreCase = true)

            val matchStock = when (stockFilter) {
                "LOW" -> item.variants.any { it.currentQuantity in 1..it.minimumStock }
                "OUT" -> item.variants.all { it.currentQuantity <= 0 }
                "IN" -> item.variants.any { it.currentQuantity > 0 }
                else -> true
            }

            matchQuery && matchCategory && matchStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Metrics
    val dashboardMetrics = combine(
        productsWithVariants,
        sales,
        saleItems
    ) { prods, salesList, allItems ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis

        val validSales = salesList.filter { it.status == "COMPLETED" }
        val todaySales = validSales.filter { it.createdAt >= startOfToday }
        val monthSales = validSales.filter { it.createdAt >= startOfMonth }

        var totalStock = 0
        var lowCount = 0
        var outCount = 0
        var wholesaleVal = 0.0
        var retailVal = 0.0

        for (p in prods) {
            for (v in p.variants) {
                totalStock += v.currentQuantity
                wholesaleVal += v.purchaseCost * v.currentQuantity
                retailVal += v.sellingPrice * v.currentQuantity
                if (v.currentQuantity <= 0) {
                    outCount++
                } else if (v.currentQuantity <= v.minimumStock) {
                    lowCount++
                }
            }
        }

        val todayRev = todaySales.sumOf { it.totalAmount }
        val monthRev = monthSales.sumOf { it.totalAmount }

        // Month COGS from sale items
        val monthSaleIds = monthSales.map { it.id }.toSet()
        val monthItems = allItems.filter { it.saleId in monthSaleIds }
        val monthCost = monthItems.sumOf { it.unitCost * it.quantity }
        val grossProfit = (monthRev - monthCost).coerceAtLeast(0.0)

        DashboardMetrics(
            totalProducts = prods.size,
            totalStockItems = totalStock,
            todaySalesCount = todaySales.size,
            todayRevenue = todayRev,
            monthRevenue = monthRev,
            estimatedGrossProfitMonth = grossProfit,
            lowStockCount = lowCount,
            outOfStockCount = outCount,
            inventoryWholesaleValue = wholesaleVal,
            inventoryRetailValue = retailVal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Top Selling Products
    val topSellers = combine(sales, saleItems) { salesList, items ->
        val completedIds = salesList.filter { it.status == "COMPLETED" }.map { it.id }.toSet()
        items.filter { it.saleId in completedIds }
            .groupBy { it.productName }
            .map { (name, list) ->
                TopSeller(
                    productName = name,
                    unitsSold = list.sumOf { it.quantity },
                    revenue = list.sumOf { it.lineTotal }
                )
            }
            .sortedByDescending { it.unitsSold }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Navigation methods
    fun navigateTo(screen: AppScreen, product: ProductWithVariants? = null) {
        if (product != null) {
            _selectedProduct.value = product
        }
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.lastIndex)
            return true
        }
        if (_currentScreen.value != AppScreen.DASHBOARD) {
            _currentScreen.value = AppScreen.DASHBOARD
            return true
        }
        return false
    }

    // Cart Management
    fun addToCart(product: Product, variant: ProductVariant, quantity: Int = 1) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.variant.id == variant.id }
        val existingQty = if (index >= 0) currentList[index].quantity else 0
        val targetQty = existingQty + quantity

        if (targetQty > variant.currentQuantity) {
            _errorMessage.value = "Cannot add $quantity item(s). Only ${variant.currentQuantity - existingQty} available in stock for ${variant.color}/${variant.size}."
            return
        }

        if (index >= 0) {
            currentList[index] = currentList[index].copy(quantity = targetQty)
        } else {
            currentList.add(CartItem(product, variant, quantity))
        }
        _cart.value = currentList
        _successMessage.value = "Added ${product.name} (${variant.color}/${variant.size}) to cart"
    }

    fun updateCartItemQuantity(variantId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(variantId)
            return
        }
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.variant.id == variantId }
        if (index >= 0) {
            val item = currentList[index]
            if (newQuantity > item.variant.currentQuantity) {
                _errorMessage.value = "Only ${item.variant.currentQuantity} items available in stock."
                return
            }
            currentList[index] = item.copy(quantity = newQuantity)
            _cart.value = currentList
        }
    }

    fun removeFromCart(variantId: Long) {
        _cart.value = _cart.value.filter { it.variant.id != variantId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        cartCustomerName.value = ""
        cartCustomerPhone.value = ""
        cartDiscount.value = 0.0
    }

    fun confirmSale() {
        if (_cart.value.isEmpty()) {
            _errorMessage.value = "Please add at least one product to the sale."
            return
        }

        viewModelScope.launch {
            val result = repository.recordSale(
                cartItems = _cart.value,
                customerName = cartCustomerName.value,
                customerPhone = cartCustomerPhone.value,
                discountAmount = cartDiscount.value,
                paymentMethod = cartPaymentMethod.value,
                salesChannel = cartSalesChannel.value
            )

            result.onSuccess { sale ->
                _checkoutSuccessSale.value = sale
                clearCart()
                _successMessage.value = "Sale ${sale.saleNumber} recorded successfully!"
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to record sale"
            }
        }
    }

    fun dismissCheckoutSuccess() {
        _checkoutSuccessSale.value = null
    }

    // Restock variant
    fun restockVariant(variantId: Long, quantity: Int, supplier: String, notes: String) {
        viewModelScope.launch {
            val result = repository.addStockIn(variantId, quantity, supplier, notes)
            result.onSuccess {
                _successMessage.value = "Successfully added +$quantity units to stock!"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    // Adjust variant stock
    fun adjustVariantStock(variantId: Long, newQuantity: Int, reason: String) {
        viewModelScope.launch {
            val result = repository.adjustStock(variantId, newQuantity, reason)
            result.onSuccess {
                _successMessage.value = "Stock adjusted to $newQuantity units."
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    // Cancel or return sale
    fun cancelOrReturnSale(saleId: Long, isReturn: Boolean, reason: String) {
        viewModelScope.launch {
            val result = repository.cancelOrReturnSale(saleId, isReturn, reason)
            result.onSuccess {
                _successMessage.value = if (isReturn) "Sale returned & inventory restored." else "Sale cancelled & inventory restored."
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    // Product actions
    fun saveProduct(product: Product, variants: List<ProductVariant>) {
        viewModelScope.launch {
            repository.saveProductWithVariants(product, variants)
            _successMessage.value = "Product saved successfully!"
            navigateBack()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _successMessage.value = "Product deleted."
            navigateBack()
        }
    }

    // Online order actions
    fun createOnlineOrder(order: OnlineOrder) {
        viewModelScope.launch {
            repository.saveOrder(order)
            _successMessage.value = "Order ${order.orderNumber} created!"
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            _successMessage.value = "Order updated to $newStatus"
        }
    }

    // Customer & Supplier actions
    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _successMessage.value = "Customer ${customer.name} saved!"
        }
    }

    fun addSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.saveSupplier(supplier)
            _successMessage.value = "Supplier ${supplier.name} saved!"
        }
    }

    // Business Profile
    fun updateBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateBusinessProfile(profile)
            _successMessage.value = "Store settings updated!"
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
