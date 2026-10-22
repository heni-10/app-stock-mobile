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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OnlineOrder
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseErrorContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@Composable
fun OnlineOrdersScreen(
    orders: List<OnlineOrder>,
    currency: String,
    onCreateOrder: (OnlineOrder) -> Unit,
    onUpdateOrderStatus: (orderId: Long, newStatus: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val statusList = listOf("ALL", "NEW", "CONFIRMED", "PREPARING", "SHIPPED", "DELIVERED", "CANCELLED")

    val filteredOrders = remember(orders, selectedStatus) {
        if (selectedStatus == "ALL") orders else orders.filter { it.orderStatus == selectedStatus }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Social Media & Online Orders",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Slate900
            )
            Text(
                text = "Track orders received via Instagram, WhatsApp, and Facebook with delivery status updates.",
                fontSize = 12.sp,
                color = Slate600
            )

            // Status filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                items(statusList) { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = st },
                        label = { Text(st, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                        Text("No orders in '$selectedStatus' status", fontWeight = FontWeight.Bold)
                        Text("Tap '+' to register a new order received online.", fontSize = 12.sp, color = Slate400)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        OnlineOrderCard(
                            order = order,
                            currency = currency,
                            onUpdateStatus = { nextStatus -> onUpdateOrderStatus(order.id, nextStatus) }
                        )
                    }
                }
            }
        }

        // FAB to register new online order
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = BrandPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("add_online_order_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Online Order")
        }
    }

    if (showCreateDialog) {
        CreateOnlineOrderDialog(
            currency = currency,
            onDismiss = { showCreateDialog = false },
            onConfirm = { newOrder ->
                onCreateOrder(newOrder)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun OnlineOrderCard(
    order: OnlineOrder,
    currency: String,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandPrimary)
                    Surface(shape = RoundedCornerShape(4.dp), color = Slate100) {
                        Text(order.salesChannel, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                OrderStatusBadge(status = order.orderStatus)
            }

            Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                Text(order.customerPhone, fontSize = 12.sp, color = Slate600)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                Text(order.deliveryAddress, fontSize = 12.sp, color = Slate600, maxLines = 1)
            }

            Surface(shape = RoundedCornerShape(6.dp), color = Slate100, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = order.itemsSummary,
                    fontSize = 12.sp,
                    color = Slate900,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total with Delivery", fontSize = 11.sp, color = Slate400)
                    Text(formatCurrency(order.totalAmount, currency), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldGreen)
                }

                // Status advancement buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (order.orderStatus) {
                        "NEW" -> {
                            Button(
                                onClick = { onUpdateStatus("CONFIRMED") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Confirm", fontSize = 11.sp)
                            }
                        }
                        "CONFIRMED" -> {
                            Button(
                                onClick = { onUpdateStatus("SHIPPED") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Mark Shipped", fontSize = 11.sp)
                            }
                        }
                        "SHIPPED" -> {
                            Button(
                                onClick = { onUpdateStatus("DELIVERED") },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Mark Delivered", fontSize = 11.sp)
                            }
                        }
                    }
                    if (order.orderStatus != "CANCELLED" && order.orderStatus != "DELIVERED") {
                        OutlinedButton(
                            onClick = { onUpdateStatus("CANCELLED") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp, color = RoseError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "NEW" -> Pair(Color(0xFFDBEAFE), BrandPrimary)
        "CONFIRMED" -> Pair(Color(0xFFFEF3C7), AmberWarning)
        "SHIPPED" -> Pair(Color(0xFFE0E7FF), Color(0xFF4338CA))
        "DELIVERED" -> Pair(EmeraldGreenContainer, EmeraldGreen)
        else -> Pair(RoseErrorContainer, RoseError)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Text(status, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
fun CreateOnlineOrderDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (OnlineOrder) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var itemsSummary by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var deliveryFeeText by remember { mutableStateOf("7.0") }
    var channel by remember { mutableStateOf("Instagram") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Online Order", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = itemsSummary,
                    onValueChange = { itemsSummary = it },
                    label = { Text("Items Description (e.g. 1x Tee Black/L)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { totalAmountText = it },
                        label = { Text("Total ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deliveryFeeText,
                        onValueChange = { deliveryFeeText = it },
                        label = { Text("Delivery Fee") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Channel:", fontSize = 11.sp, color = Slate600)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Instagram", "WhatsApp", "Facebook", "TikTok")) { ch ->
                        FilterChip(
                            selected = channel == ch,
                            onClick = { channel = ch },
                            label = { Text(ch, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = totalAmountText.toDoubleOrNull() ?: 0.0
                    val fee = deliveryFeeText.toDoubleOrNull() ?: 0.0
                    val order = OnlineOrder(
                        orderNumber = "ORD-${(100..999).random()}",
                        customerName = customerName.ifBlank { "Social Media Customer" },
                        customerPhone = customerPhone,
                        deliveryAddress = address.ifBlank { "Pickup / Undisclosed" },
                        deliveryFee = fee,
                        itemsSummary = itemsSummary.ifBlank { "Clothing items" },
                        totalAmount = tot,
                        orderStatus = "NEW",
                        salesChannel = channel
                    )
                    onConfirm(order)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Save Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
