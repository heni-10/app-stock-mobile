package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.ProductVariant
import com.example.data.model.ProductWithVariants
import com.example.data.model.Sale
import com.example.ui.components.ColorDot
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@Composable
fun POSScreen(
    products: List<ProductWithVariants>,
    categories: List<Category>,
    cart: List<CartItem>,
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    customerPhone: String,
    onCustomerPhoneChange: (String) -> Unit,
    discountAmount: Double,
    onDiscountChange: (Double) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    salesChannel: String,
    onSalesChannelChange: (String) -> Unit,
    currency: String,
    onAddToCart: (product: Product, variant: ProductVariant, quantity: Int) -> Unit,
    onUpdateQuantity: (variantId: Long, newQty: Int) -> Unit,
    onRemoveItem: (variantId: Long) -> Unit,
    onClearCart: () -> Unit,
    onConfirmSale: () -> Unit,
    confirmedSale: Sale?,
    onDismissReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Catalog, 1: Cart / Checkout
    var catalogSearch by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var variantPickerProduct by remember { mutableStateOf<ProductWithVariants?>(null) }

    val filteredList = remember(products, catalogSearch, selectedCat) {
        products.filter { p ->
            val matchQuery = catalogSearch.isBlank() ||
                p.product.name.contains(catalogSearch, ignoreCase = true) ||
                p.product.sku.contains(catalogSearch, ignoreCase = true)
            val matchCat = selectedCat == "All" || p.product.category.equals(selectedCat, ignoreCase = true)
            matchQuery && matchCat
        }
    }

    val subtotal = cart.sumOf { it.lineTotal }
    val total = (subtotal - discountAmount).coerceAtLeast(0.0)
    val totalUnits = cart.sumOf { it.quantity }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Selector between Catalog and Cart
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("1. Select Products", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("pos_tab_catalog")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (totalUnits > 0) "2. Cart ($totalUnits)" else "2. Cart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("pos_tab_cart")
            )
        }

        if (selectedTab == 0) {
            // Catalog View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                // Search bar
                OutlinedTextField(
                    value = catalogSearch,
                    onValueChange = { catalogSearch = it },
                    placeholder = { Text("Search catalog to add items...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("pos_search_input")
                )

                // Category chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCat == "All",
                            onClick = { selectedCat = "All" },
                            label = { Text("All", fontSize = 12.sp) }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCat == cat.name,
                            onClick = { selectedCat = cat.name },
                            label = { Text(cat.name, fontSize = 12.sp) }
                        )
                    }
                }

                // Products List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 76.dp)
                ) {
                    items(filteredList, key = { it.product.id }) { item ->
                        POSProductCard(
                            productWithVariants = item,
                            currency = currency,
                            onSelect = { variantPickerProduct = item }
                        )
                    }
                }
            }
        } else {
            // Cart & Checkout View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Cart Items ($totalUnits)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (cart.isNotEmpty()) {
                            TextButton(onClick = onClearCart) {
                                Text("Clear Cart", color = RoseError, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (cart.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Slate400, modifier = Modifier.size(40.dp))
                                Text("Your sale cart is empty", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Switch to 'Select Products' to add clothing items to this sale.", fontSize = 12.sp, color = Slate400)
                                Button(
                                    onClick = { selectedTab = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                ) {
                                    Text("Browse Products")
                                }
                            }
                        }
                    }
                } else {
                    items(cart, key = { it.variant.id }) { item ->
                        CartItemRow(
                            item = item,
                            currency = currency,
                            onIncrease = { onUpdateQuantity(item.variant.id, item.quantity + 1) },
                            onDecrease = { onUpdateQuantity(item.variant.id, item.quantity - 1) },
                            onRemove = { onRemoveItem(item.variant.id) }
                        )
                    }

                    // Order & Customer Details
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Customer & Transaction Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = onCustomerNameChange,
                                    label = { Text("Customer Name (Optional)") },
                                    placeholder = { Text("Walk-in Customer") },
                                    modifier = Modifier.fillMaxWidth().testTag("pos_customer_name")
                                )

                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = onCustomerPhoneChange,
                                    label = { Text("Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    placeholder = { Text("+216 ...") },
                                    modifier = Modifier.fillMaxWidth().testTag("pos_customer_phone")
                                )

                                Text("Sales Channel:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate600)
                                val channels = listOf("Physical Store", "Instagram", "WhatsApp", "Facebook")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(channels) { ch ->
                                        FilterChip(
                                            selected = salesChannel == ch,
                                            onClick = { onSalesChannelChange(ch) },
                                            label = { Text(ch, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate600)
                                val methods = listOf("CASH", "CARD", "BANK_TRANSFER", "COD")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(methods) { method ->
                                        FilterChip(
                                            selected = paymentMethod == method,
                                            onClick = { onPaymentMethodChange(method) },
                                            label = { Text(method, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                var discountText by remember(discountAmount) {
                                    mutableStateOf(if (discountAmount > 0) discountAmount.toString() else "")
                                }
                                OutlinedTextField(
                                    value = discountText,
                                    onValueChange = {
                                        discountText = it
                                        onDiscountChange(it.toDoubleOrNull() ?: 0.0)
                                    },
                                    label = { Text("Discount ($currency)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth().testTag("pos_discount_input")
                                )
                            }
                        }
                    }

                    // Order Summary & Checkout Button
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal ($totalUnits items)", fontSize = 13.sp)
                                    Text(formatCurrency(subtotal, currency), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }

                                if (discountAmount > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Discount Applied", fontSize = 13.sp, color = AmberWarning)
                                        Text("-${formatCurrency(discountAmount, currency)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AmberWarning)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Slate200)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total to Charge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        formatCurrency(total, currency),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = EmeraldGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = onConfirmSale,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("pos_confirm_sale_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confirm Sale & Deduct Stock", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Variant Picker Dialog
    variantPickerProduct?.let { p ->
        VariantSelectionDialog(
            productWithVariants = p,
            currency = currency,
            onDismiss = { variantPickerProduct = null },
            onSelectVariant = { variant, quantity ->
                onAddToCart(p.product, variant, quantity)
                variantPickerProduct = null
                selectedTab = 1 // Switch to cart view so seller can review
            }
        )
    }

    // Receipt Confirmation Dialog
    confirmedSale?.let { sale ->
        ReceiptDialog(
            sale = sale,
            currency = currency,
            onDismiss = onDismissReceipt
        )
    }
}

@Composable
fun POSProductCard(
    productWithVariants: ProductWithVariants,
    currency: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("pos_prod_${productWithVariants.product.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = productWithVariants.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = productWithVariants.product.category,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Text("•", fontSize = 11.sp, color = Slate400)
                    Text(
                        text = "${productWithVariants.variants.size} variants",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatCurrency(productWithVariants.minPrice, currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandPrimary
                )
                StockBadge(quantity = productWithVariants.totalStock)
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    currency: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorDot(hex = item.variant.colorHex, size = 12)
                    Text(
                        text = "${item.variant.color} • Size ${item.variant.size}",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                    Text(
                        text = "(${item.variant.currentQuantity} in stock)",
                        fontSize = 10.sp,
                        color = if (item.variant.currentQuantity <= 3) AmberWarning else Slate400
                    )
                }
                Text(
                    text = "${formatCurrency(item.variant.sellingPrice, currency)} each",
                    fontSize = 11.sp,
                    color = BrandPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Stepper and Line Total
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        IconButton(
                            onClick = onDecrease,
                            modifier = Modifier.size(28.dp).testTag("cart_decrease_${item.variant.id}")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = item.quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = onIncrease,
                            enabled = item.quantity < item.variant.currentQuantity,
                            modifier = Modifier.size(28.dp).testTag("cart_increase_${item.variant.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Text(
                    text = formatCurrency(item.lineTotal, currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Slate400, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun VariantSelectionDialog(
    productWithVariants: ProductWithVariants,
    currency: String,
    onDismiss: () -> Unit,
    onSelectVariant: (ProductVariant, Int) -> Unit
) {
    var selectedVariant by remember {
        mutableStateOf(productWithVariants.variants.firstOrNull { it.currentQuantity > 0 } ?: productWithVariants.variants.firstOrNull())
    }
    var quantity by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Size & Color", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(productWithVariants.product.name, fontSize = 12.sp, color = Slate400)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose Variant:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate600)

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(productWithVariants.variants) { v ->
                        val isSelected = selectedVariant?.id == v.id
                        val isOutOfStock = v.currentQuantity <= 0

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BrandPrimary.copy(alpha = 0.12f) else if (isOutOfStock) Slate100 else Color.White,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandPrimary) else androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isOutOfStock) {
                                    selectedVariant = v
                                    quantity = 1
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ColorDot(hex = v.colorHex)
                                    Column {
                                        Text(
                                            text = "${v.color} - Size ${v.size}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isOutOfStock) Slate400 else Slate900
                                        )
                                        Text(
                                            text = formatCurrency(v.sellingPrice, currency),
                                            fontSize = 11.sp,
                                            color = BrandPrimary
                                        )
                                    }
                                }

                                StockBadge(quantity = v.currentQuantity, minThreshold = v.minimumStock)
                            }
                        }
                    }
                }

                // Quantity selector
                selectedVariant?.let { v ->
                    if (v.currentQuantity > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quantity:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    enabled = quantity > 1,
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("-", fontSize = 18.sp)
                                }
                                Text(
                                    text = quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                OutlinedButton(
                                    onClick = { if (quantity < v.currentQuantity) quantity++ },
                                    enabled = quantity < v.currentQuantity,
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedVariant?.let { v ->
                        if (v.currentQuantity > 0) {
                            onSelectVariant(v, quantity)
                        }
                    }
                },
                enabled = selectedVariant != null && (selectedVariant?.currentQuantity ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("pos_add_to_cart_confirm")
            ) {
                Text("Add to Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
