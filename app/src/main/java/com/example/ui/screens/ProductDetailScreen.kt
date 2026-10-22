package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.ProductVariant
import com.example.data.model.ProductWithVariants
import com.example.ui.components.AdjustStockDialog
import com.example.ui.components.ColorDot
import com.example.ui.components.RestockDialog
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productWithVariants: ProductWithVariants,
    currency: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Product) -> Unit,
    onRestockVariant: (variantId: Long, qty: Int, supplier: String, notes: String) -> Unit,
    onAdjustVariant: (variantId: Long, newQty: Int, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val product = productWithVariants.product
    val variants = productWithVariants.variants
    var restockTarget by remember { mutableStateOf<ProductVariant?>(null) }
    var adjustTarget by remember { mutableStateOf<ProductVariant?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val totalStock = variants.sumOf { it.currentQuantity }
    val totalCostValue = variants.sumOf { it.purchaseCost * it.currentQuantity }
    val totalRetailValue = variants.sumOf { it.sellingPrice * it.currentQuantity }
    val profitPerUnit = (product.defaultPrice - product.defaultCost).coerceAtLeast(0.0)
    val markupPercent = if (product.defaultCost > 0) ((profitPerUnit / product.defaultCost) * 100).toInt() else 0

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_product_top_button")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = BrandPrimary)
                }
                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = RoseError)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 76.dp)
        ) {
            // Style Info Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = product.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            StockBadge(quantity = totalStock)
                        }

                        Text(
                            text = product.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Slate900
                        )

                        Text(
                            text = "SKU: ${product.sku} • Supplier: ${product.supplierName.ifBlank { "Unassigned" }}",
                            fontSize = 12.sp,
                            color = Slate600
                        )

                        if (product.description.isNotBlank()) {
                            Text(
                                text = product.description,
                                fontSize = 13.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }

            // Financial Margins Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Financial Margins & Stock Valuation", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Default Selling Price", fontSize = 11.sp, color = Slate600)
                                Text(formatCurrency(product.defaultPrice, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandPrimary)
                            }
                            Column {
                                Text("Wholesale Cost", fontSize = 11.sp, color = Slate600)
                                Text(formatCurrency(product.defaultCost, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Est. Gross Margin", fontSize = 11.sp, color = Slate600)
                                Text("+$markupPercent% (${formatCurrency(profitPerUnit, currency)})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldGreen)
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Stock Cost Valuation:", fontSize = 12.sp, color = Slate600)
                            Text(formatCurrency(totalCostValue, currency), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Projected Retail Revenue:", fontSize = 12.sp, color = Slate600)
                            Text(formatCurrency(totalRetailValue, currency), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldGreen)
                        }
                    }
                }
            }

            // Variants Matrix
            item {
                Text("Size & Color Inventory Matrix (${variants.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(variants, key = { it.id }) { variant ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ColorDot(hex = variant.colorHex, size = 18)
                                Column {
                                    Text(
                                        text = "${variant.color} • Size ${variant.size}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = variant.sku,
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                            }
                            StockBadge(quantity = variant.currentQuantity, minThreshold = variant.minimumStock)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Price: ${formatCurrency(variant.sellingPrice, currency)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary
                                )
                                Text(
                                    text = "Cost: ${formatCurrency(variant.purchaseCost, currency)} (Min alert: ${variant.minimumStock})",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { restockTarget = variant },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("+ Stock In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { adjustTarget = variant },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Adjust", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Restock Dialog
    restockTarget?.let { v ->
        RestockDialog(
            variant = v,
            productName = product.name,
            onDismiss = { restockTarget = null },
            onConfirm = { qty, supplier, notes ->
                onRestockVariant(v.id, qty, supplier, notes)
                restockTarget = null
            }
        )
    }

    // Adjust Stock Dialog
    adjustTarget?.let { v ->
        AdjustStockDialog(
            variant = v,
            productName = product.name,
            onDismiss = { adjustTarget = null },
            onConfirm = { newQty, reason ->
                onAdjustVariant(v.id, newQty, reason)
                adjustTarget = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Product?") },
            text = {
                Text("Are you sure you want to delete '${product.name}' and all its variants? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete(product)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
