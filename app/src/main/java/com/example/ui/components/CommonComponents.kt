package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductVariant
import com.example.data.model.Sale
import com.example.ui.AppScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseErrorContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double, currency: String = "TND"): String {
    return String.format(Locale.US, "%.2f %s", amount, currency)
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun ColorDot(hex: String, modifier: Modifier = Modifier, size: Int = 16) {
    val color = runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(Color.Gray)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Slate200, CircleShape)
    )
}

@Composable
fun StockBadge(quantity: Int, minThreshold: Int = 5, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when {
        quantity <= 0 -> Triple(RoseErrorContainer, RoseError, "Out of Stock ($quantity)")
        quantity <= minThreshold -> Triple(AmberWarningContainer, AmberWarning, "Low Stock ($quantity)")
        else -> Triple(EmeraldGreenContainer, EmeraldGreen, "$quantity in stock")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartStockTopAppBar(
    title: String,
    lowStockCount: Int,
    onNavigateToSettings: () -> Unit,
    onNavigateToLowStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Clothing Inventory & Sales",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        },
        actions = {
            BadgedBox(
                badge = {
                    if (lowStockCount > 0) {
                        Badge(
                            containerColor = AmberWarning,
                            contentColor = Slate900
                        ) {
                            Text(lowStockCount.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                IconButton(
                    onClick = onNavigateToLowStock,
                    modifier = Modifier.testTag("low_stock_bell_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Low Stock Alerts",
                        tint = if (lowStockCount > 0) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@Composable
fun SmartStockBottomBar(
    currentScreen: AppScreen,
    cartItemCount: Int,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onSelectScreen(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.INVENTORY || currentScreen == AppScreen.PRODUCT_DETAIL,
            onClick = { onSelectScreen(AppScreen.INVENTORY) },
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
            label = { Text("Inventory", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_inventory")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.NEW_SALE,
            onClick = { onSelectScreen(AppScreen.NEW_SALE) },
            icon = {
                BadgedBox(badge = {
                    if (cartItemCount > 0) {
                        Badge(containerColor = EmeraldGreen, contentColor = Color.White) {
                            Text(cartItemCount.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }) {
                    Icon(Icons.Default.PointOfSale, contentDescription = "New Sale")
                }
            },
            label = { Text("New Sale", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldGreen,
                indicatorColor = EmeraldGreenContainer
            ),
            modifier = Modifier.testTag("nav_new_sale")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.SALES_HISTORY,
            onClick = { onSelectScreen(AppScreen.SALES_HISTORY) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Sales") },
            label = { Text("Sales", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_sales")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.ONLINE_ORDERS,
            onClick = { onSelectScreen(AppScreen.ONLINE_ORDERS) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Orders") },
            label = { Text("Orders", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_orders")
        )
    }
}

@Composable
fun RestockDialog(
    variant: ProductVariant,
    productName: String,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, supplier: String, notes: String) -> Unit
) {
    var qtyString by remember { mutableStateOf("10") }
    var supplier by remember { mutableStateOf("Atelier Mediterranean Garments") }
    var notes by remember { mutableStateOf("Received regular shipment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Stock In / Restock", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$productName (${variant.color} / ${variant.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandPrimary
                )
                Text(
                    text = "Current stock: ${variant.currentQuantity} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantity to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("restock_quantity_input")
                )
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reference") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qtyString.toIntOrNull() ?: 0
                    if (q > 0) {
                        onConfirm(q, supplier, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("restock_confirm_button")
            ) {
                Text("Confirm Stock In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdjustStockDialog(
    variant: ProductVariant,
    productName: String,
    onDismiss: () -> Unit,
    onConfirm: (newQty: Int, reason: String) -> Unit
) {
    var qtyString by remember { mutableStateOf(variant.currentQuantity.toString()) }
    var reason by remember { mutableStateOf("Physical audit recount") }

    val commonReasons = listOf(
        "Physical audit recount",
        "Damaged item / defect write-off",
        "Sample giveaway / marketing display",
        "Lost / stolen inventory",
        "Return from client / unrecorded"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Adjust Inventory", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$productName (${variant.color} / ${variant.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandPrimary
                )
                Text(
                    text = "Current recorded stock: ${variant.currentQuantity} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it.filter { char -> char.isDigit() } },
                    label = { Text("Actual New Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("adjust_new_quantity_input")
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Adjustment") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Quick Reasons:", fontSize = 12.sp, color = Slate400)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { reason = "Physical audit recount" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Audit", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { reason = "Damaged / Defect" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Damaged", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qtyString.toIntOrNull() ?: 0
                    onConfirm(q, reason)
                },
                modifier = Modifier.testTag("adjust_confirm_button")
            ) {
                Text("Save Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReceiptDialog(
    sale: Sale,
    currency: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                Text("Sale Confirmed!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
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
                        Text("Receipt #", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(sale.saleNumber, fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer", fontSize = 13.sp)
                        Text(sale.customerName, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Channel", fontSize = 13.sp)
                        Text(sale.salesChannel, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment", fontSize = 13.sp)
                        Text(sale.paymentMethod, fontSize = 13.sp)
                    }
                    if (sale.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", fontSize = 13.sp, color = AmberWarning)
                            Text("-${formatCurrency(sale.discountAmount, currency)}", fontSize = 13.sp, color = AmberWarning)
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
                        Text("Total Paid", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            formatCurrency(sale.totalAmount, currency),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = EmeraldGreen
                        )
                    }
                    Text(
                        text = "Stock quantities automatically updated in database.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                modifier = Modifier.fillMaxWidth().testTag("receipt_done_button")
            ) {
                Text("Start Next Sale")
            }
        }
    )
}
