package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.SmartStockViewModel
import com.example.ui.components.SmartStockBottomBar
import com.example.ui.components.SmartStockTopAppBar
import com.example.ui.screens.AddEditProductScreen
import com.example.ui.screens.CustomersSuppliersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialReportsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OnlineOrdersScreen
import com.example.ui.screens.POSScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SalesHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockMovementsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SmartStockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartStockApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartStockApp(viewModel: SmartStockViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProductsWithVariants by viewModel.productsWithVariants.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleItems by viewModel.saleItems.collectAsStateWithLifecycle()
    val onlineOrders by viewModel.onlineOrders.collectAsStateWithLifecycle()
    val movements by viewModel.movements.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val topSellers by viewModel.topSellers.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val stockFilter by viewModel.selectedStockFilter.collectAsStateWithLifecycle()

    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartCustomerName by viewModel.cartCustomerName.collectAsStateWithLifecycle()
    val cartCustomerPhone by viewModel.cartCustomerPhone.collectAsStateWithLifecycle()
    val cartDiscount by viewModel.cartDiscount.collectAsStateWithLifecycle()
    val cartPaymentMethod by viewModel.cartPaymentMethod.collectAsStateWithLifecycle()
    val cartSalesChannel by viewModel.cartSalesChannel.collectAsStateWithLifecycle()
    val checkoutSuccessSale by viewModel.checkoutSuccessSale.collectAsStateWithLifecycle()

    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()

    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val currency = profile?.currency ?: "TND"

    // Toast & Snackbar feedback
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }

    // Predictive back handler for navigation
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            if (currentScreen in listOf(AppScreen.DASHBOARD, AppScreen.INVENTORY, AppScreen.NEW_SALE, AppScreen.SALES_HISTORY, AppScreen.ONLINE_ORDERS)) {
                SmartStockTopAppBar(
                    title = profile?.businessName ?: "SmartStock",
                    lowStockCount = metrics.lowStockCount,
                    onNavigateToSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onNavigateToLowStock = {
                        viewModel.selectedStockFilter.value = "LOW"
                        viewModel.navigateTo(AppScreen.INVENTORY)
                    }
                )
            }
        },
        bottomBar = {
            if (currentScreen in listOf(AppScreen.DASHBOARD, AppScreen.INVENTORY, AppScreen.NEW_SALE, AppScreen.SALES_HISTORY, AppScreen.ONLINE_ORDERS)) {
                SmartStockBottomBar(
                    currentScreen = currentScreen,
                    cartItemCount = cart.sumOf { it.quantity },
                    onSelectScreen = { screen -> viewModel.navigateTo(screen) }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        profile = profile,
                        metrics = metrics,
                        products = allProductsWithVariants,
                        recentSales = sales,
                        topSellers = topSellers,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onRestockVariant = { vId, qty, supplier, notes ->
                            viewModel.restockVariant(vId, qty, supplier, notes)
                        }
                    )
                }

                AppScreen.INVENTORY -> {
                    InventoryScreen(
                        products = products,
                        categories = categories,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedCategory = selectedCategory,
                        onSelectCategory = { viewModel.selectedCategory.value = it },
                        stockFilter = stockFilter,
                        onSelectStockFilter = { viewModel.selectedStockFilter.value = it },
                        currency = currency,
                        onProductClick = { p -> viewModel.navigateTo(AppScreen.PRODUCT_DETAIL, p) },
                        onAddProductClick = { viewModel.navigateTo(AppScreen.ADD_EDIT_PRODUCT, null) },
                        onRestockVariant = { vId, qty, supplier, notes ->
                            viewModel.restockVariant(vId, qty, supplier, notes)
                        },
                        onAdjustVariant = { vId, newQty, reason ->
                            viewModel.adjustVariantStock(vId, newQty, reason)
                        }
                    )
                }

                AppScreen.NEW_SALE -> {
                    POSScreen(
                        products = allProductsWithVariants,
                        categories = categories,
                        cart = cart,
                        customerName = cartCustomerName,
                        onCustomerNameChange = { viewModel.cartCustomerName.value = it },
                        customerPhone = cartCustomerPhone,
                        onCustomerPhoneChange = { viewModel.cartCustomerPhone.value = it },
                        discountAmount = cartDiscount,
                        onDiscountChange = { viewModel.cartDiscount.value = it },
                        paymentMethod = cartPaymentMethod,
                        onPaymentMethodChange = { viewModel.cartPaymentMethod.value = it },
                        salesChannel = cartSalesChannel,
                        onSalesChannelChange = { viewModel.cartSalesChannel.value = it },
                        currency = currency,
                        onAddToCart = { prod, variant, qty -> viewModel.addToCart(prod, variant, qty) },
                        onUpdateQuantity = { vId, qty -> viewModel.updateCartItemQuantity(vId, qty) },
                        onRemoveItem = { vId -> viewModel.removeFromCart(vId) },
                        onClearCart = { viewModel.clearCart() },
                        onConfirmSale = { viewModel.confirmSale() },
                        confirmedSale = checkoutSuccessSale,
                        onDismissReceipt = { viewModel.dismissCheckoutSuccess() }
                    )
                }

                AppScreen.SALES_HISTORY -> {
                    SalesHistoryScreen(
                        sales = sales,
                        currency = currency,
                        onFetchSaleItems = { saleId -> viewModel.repository.getSaleItems(saleId) },
                        onCancelOrReturnSale = { saleId, isReturn, reason ->
                            viewModel.cancelOrReturnSale(saleId, isReturn, reason)
                        }
                    )
                }

                AppScreen.ONLINE_ORDERS -> {
                    OnlineOrdersScreen(
                        orders = onlineOrders,
                        currency = currency,
                        onCreateOrder = { newOrder -> viewModel.createOnlineOrder(newOrder) },
                        onUpdateOrderStatus = { orderId, newStatus ->
                            viewModel.updateOrderStatus(orderId, newStatus)
                        }
                    )
                }

                AppScreen.PRODUCT_DETAIL -> {
                    selectedProduct?.let { p ->
                        ProductDetailScreen(
                            productWithVariants = p,
                            currency = currency,
                            onBack = { viewModel.navigateBack() },
                            onEdit = { viewModel.navigateTo(AppScreen.ADD_EDIT_PRODUCT, p) },
                            onDelete = { prod -> viewModel.deleteProduct(prod) },
                            onRestockVariant = { vId, qty, supplier, notes ->
                                viewModel.restockVariant(vId, qty, supplier, notes)
                            },
                            onAdjustVariant = { vId, newQty, reason ->
                                viewModel.adjustVariantStock(vId, newQty, reason)
                            }
                        )
                    } ?: run {
                        viewModel.navigateBack()
                    }
                }

                AppScreen.ADD_EDIT_PRODUCT -> {
                    AddEditProductScreen(
                        editingProduct = selectedProduct,
                        categories = categories,
                        currency = currency,
                        onBack = { viewModel.navigateBack() },
                        onSaveProduct = { prod, variantsList ->
                            viewModel.saveProduct(prod, variantsList)
                        }
                    )
                }

                AppScreen.STOCK_MOVEMENTS -> {
                    StockMovementsScreen(
                        movements = movements,
                        onBack = { viewModel.navigateBack() }
                    )
                }

                AppScreen.REPORTS -> {
                    FinancialReportsScreen(
                        sales = sales,
                        saleItems = saleItems,
                        products = allProductsWithVariants,
                        topSellers = topSellers,
                        currency = currency,
                        onBack = { viewModel.navigateBack() }
                    )
                }

                AppScreen.CUSTOMERS_SUPPLIERS -> {
                    CustomersSuppliersScreen(
                        customers = customers,
                        suppliers = suppliers,
                        currency = currency,
                        onBack = { viewModel.navigateBack() },
                        onAddCustomer = { viewModel.addCustomer(it) },
                        onAddSupplier = { viewModel.addSupplier(it) }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        profile = profile,
                        onBack = { viewModel.navigateBack() },
                        onSaveProfile = { updated -> viewModel.updateBusinessProfile(updated) }
                    )
                }
            }
        }
    }
}
