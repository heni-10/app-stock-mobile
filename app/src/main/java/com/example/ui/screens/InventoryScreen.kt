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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.ProductVariant
import com.example.data.model.ProductWithVariants
import com.example.ui.components.AdjustStockDialog
import com.example.ui.components.ColorDot
import com.example.ui.components.RestockDialog
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
fun InventoryScreen(
    products: List<ProductWithVariants>,
    categories: List<Category>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    stockFilter: String,
    onSelectStockFilter: (String) -> Unit,
    currency: String,
    onProductClick: (ProductWithVariants) -> Unit,
    onAddProductClick: () -> Unit,
    onRestockVariant: (variantId: Long, qty: Int, supplier: String, notes: String) -> Unit,
    onAdjustVariant: (variantId: Long, newQty: Int, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var restockTarget by remember { mutableStateOf<Pair<ProductVariant, String>?>(null) }
    var adjustTarget by remember { mutableStateOf<Pair<ProductVariant, String>?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search clothing style, SKU, color...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Text("✕", color = Slate400, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("inventory_search_bar")
                )
            }

            // Categories horizontal pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "All",
                            onClick = { onSelectCategory("All") },
                            label = { Text("All Categories", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { onSelectCategory(cat.name) },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Stock status filter row
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StockFilterPill(
                        label = "All Items",
                        count = products.size,
                        isSelected = stockFilter == "ALL",
                        onClick = { onSelectStockFilter("ALL") }
                    )
                    StockFilterPill(
                        label = "Low Stock",
                        count = products.count { it.isLowStock },
                        color = AmberWarning,
                        isSelected = stockFilter == "LOW",
                        onClick = { onSelectStockFilter("LOW") }
                    )
                    StockFilterPill(
                        label = "Out of Stock",
                        count = products.count { it.isOutOfStock },
                        color = RoseError,
                        isSelected = stockFilter == "OUT",
                        onClick = { onSelectStockFilter("OUT") }
                    )
                    StockFilterPill(
                        label = "In Stock",
                        count = products.count { !it.isOutOfStock },
                        color = EmeraldGreen,
                        isSelected = stockFilter == "IN",
                        onClick = { onSelectStockFilter("IN") }
                    )
                }
            }

            // Product Cards List
            if (products.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("No clothing products found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Try adjusting your search query or filters, or tap '+ Add Product' below.",
                                fontSize = 13.sp,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(products, key = { it.product.id }) { item ->
                    ProductInventoryCard(
                        productWithVariants = item,
                        currency = currency,
                        onProductClick = { onProductClick(item) },
                        onRestock = { variant -> restockTarget = Pair(variant, item.product.name) },
                        onAdjust = { variant -> adjustTarget = Pair(variant, item.product.name) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddProductClick,
            containerColor = BrandPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("add_product_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    // Restock Dialog
    restockTarget?.let { (variant, prodName) ->
        RestockDialog(
            variant = variant,
            productName = prodName,
            onDismiss = { restockTarget = null },
            onConfirm = { qty, supplier, notes ->
                onRestockVariant(variant.id, qty, supplier, notes)
                restockTarget = null
            }
        )
    }

    // Adjust Stock Dialog
    adjustTarget?.let { (variant, prodName) ->
        AdjustStockDialog(
            variant = variant,
            productName = prodName,
            onDismiss = { adjustTarget = null },
            onConfirm = { newQty, reason ->
                onAdjustVariant(variant.id, newQty, reason)
                adjustTarget = null
            }
        )
    }
}

@Composable
fun StockFilterPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color = BrandPrimary
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Slate100,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else Slate600
            )
            Surface(
                shape = CircleShape,
                color = if (isSelected) color else Slate200,
                modifier = Modifier.size(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = count.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Slate600
                    )
                }
            }
        }
    }
}

@Composable
fun ProductInventoryCard(
    productWithVariants: ProductWithVariants,
    currency: String,
    onProductClick: () -> Unit,
    onRestock: (ProductVariant) -> Unit,
    onAdjust: (ProductVariant) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val product = productWithVariants.product
    val variants = productWithVariants.variants

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = product.sku,
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }

                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Color dots preview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val distinctColors = variants.distinctBy { it.color }
                        distinctColors.take(4).forEach { v ->
                            ColorDot(hex = v.colorHex, size = 12)
                        }
                        if (distinctColors.size > 4) {
                            Text("+${distinctColors.size - 4}", fontSize = 10.sp, color = Slate400)
                        }
                        Text(
                            text = "• ${variants.size} variants (${variants.map { it.size }.distinct().joinToString("/")})",
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
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = BrandPrimary
                    )
                    StockBadge(quantity = productWithVariants.totalStock)
                }
            }

            // Expand / Collapse Variant Matrix
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Size & Color Breakdown" else "View All ${variants.size} Variants & Stock",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded Variants List
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    variants.forEach { v ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ColorDot(hex = v.colorHex)
                                    Text(
                                        text = "${v.color} / Size ${v.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StockBadge(quantity = v.currentQuantity, minThreshold = v.minimumStock)
                                    OutlinedButton(
                                        onClick = { onRestock(v) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("+ Stock", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onAdjust(v) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Adjust", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
