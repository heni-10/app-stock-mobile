package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sale
import com.example.data.model.SaleItem
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
import kotlinx.coroutines.launch

@Composable
fun SalesHistoryScreen(
    sales: List<Sale>,
    currency: String,
    onFetchSaleItems: suspend (Long) -> List<SaleItem>,
    onCancelOrReturnSale: (saleId: Long, isReturn: Boolean, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var activeDetailSale by remember { mutableStateOf<Sale?>(null) }
    var activeSaleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var showReturnDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredSales = remember(sales, searchQuery, selectedChannel, selectedStatus) {
        sales.filter { sale ->
            val matchQuery = searchQuery.isBlank() ||
                sale.saleNumber.contains(searchQuery, ignoreCase = true) ||
                sale.customerName.contains(searchQuery, ignoreCase = true) ||
                sale.customerPhone.contains(searchQuery, ignoreCase = true)

            val matchChannel = selectedChannel == "All" || sale.salesChannel.equals(selectedChannel, ignoreCase = true)
            val matchStatus = selectedStatus == "All" || sale.status.equals(selectedStatus, ignoreCase = true)

            matchQuery && matchChannel && matchStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Sale #, customer name, phone...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("sales_history_search")
        )

        // Channels Filter
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            item {
                FilterChip(
                    selected = selectedChannel == "All",
                    onClick = { selectedChannel = "All" },
                    label = { Text("All Channels", fontSize = 11.sp) }
                )
            }
            items(listOf("Physical Store", "Instagram", "WhatsApp", "Facebook")) { ch ->
                FilterChip(
                    selected = selectedChannel == ch,
                    onClick = { selectedChannel = ch },
                    label = { Text(ch, fontSize = 11.sp) }
                )
            }
        }

        // Status Filter
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("All", "COMPLETED", "CANCELLED", "RETURNED").forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    label = { Text(status, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Sales List
        if (filteredSales.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                    Text("No sales matching criteria", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 76.dp)
            ) {
                items(filteredSales, key = { it.id }) { sale ->
                    SaleHistoryCard(
                        sale = sale,
                        currency = currency,
                        onClick = {
                            activeDetailSale = sale
                            scope.launch {
                                activeSaleItems = onFetchSaleItems(sale.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Sale Detail Dialog
    activeDetailSale?.let { sale ->
        AlertDialog(
            onDismissRequest = {
                activeDetailSale = null
                activeSaleItems = emptyList()
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sale.saleNumber, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandPrimary)
                        Text(formatDate(sale.createdAt), fontSize = 11.sp, color = Slate400)
                    }
                    SaleStatusBadge(status = sale.status)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer:", fontSize = 12.sp, color = Slate600)
                        Text(
                            "${sale.customerName} ${if (sale.customerPhone.isNotBlank()) "(${sale.customerPhone})" else ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sales Channel:", fontSize = 12.sp, color = Slate600)
                        Text(sale.salesChannel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment:", fontSize = 12.sp, color = Slate600)
                        Text("${sale.paymentMethod} (${sale.paymentStatus})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Slate200)
                    )

                    Text("Sold Items (${activeSaleItems.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        modifier = Modifier.height(140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeSaleItems) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.variantInfo} • Qty: ${item.quantity} x ${formatCurrency(item.unitPrice, currency)}",
                                            fontSize = 10.sp,
                                            color = Slate600
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(item.lineTotal, currency),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 12.sp)
                        Text(formatCurrency(sale.subtotal, currency), fontSize = 12.sp)
                    }

                    if (sale.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", fontSize = 12.sp, color = AmberWarning)
                            Text("-${formatCurrency(sale.discountAmount, currency)}", fontSize = 12.sp, color = AmberWarning)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            formatCurrency(sale.totalAmount, currency),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (sale.status == "COMPLETED") EmeraldGreen else RoseError
                        )
                    }
                }
            },
            confirmButton = {
                if (sale.status == "COMPLETED") {
                    OutlinedButton(
                        onClick = { showReturnDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                        modifier = Modifier.testTag("initiate_return_button")
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel / Return")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDetailSale = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Cancel / Return Confirmation Dialog
    if (showReturnDialog && activeDetailSale != null) {
        var returnReason by remember { mutableStateOf("Customer size exchange / return") }
        var isReturn by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showReturnDialog = false },
            title = {
                Text("Process Return / Cancellation", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sale ${activeDetailSale?.saleNumber} will be marked as ${if (isReturn) "RETURNED" else "CANCELLED"}. The sold items will be automatically restocked into inventory with full audit tracking.",
                        fontSize = 13.sp,
                        color = Slate600
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isReturn,
                            onClick = { isReturn = true },
                            label = { Text("Customer Return") }
                        )
                        FilterChip(
                            selected = !isReturn,
                            onClick = { isReturn = false },
                            label = { Text("Void / Cancel") }
                        )
                    }

                    OutlinedTextField(
                        value = returnReason,
                        onValueChange = { returnReason = it },
                        label = { Text("Reason for Return") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeDetailSale?.let { s ->
                            onCancelOrReturnSale(s.id, isReturn, returnReason)
                        }
                        showReturnDialog = false
                        activeDetailSale = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                    modifier = Modifier.testTag("confirm_return_button")
                ) {
                    Text("Confirm & Restock Items")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReturnDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SaleHistoryCard(
    sale: Sale,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("sale_card_${sale.saleNumber}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = sale.saleNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrandPrimary
                    )
                    SaleStatusBadge(status = sale.status)
                }
                Text(
                    text = "${sale.customerName} • ${sale.salesChannel}",
                    fontSize = 12.sp,
                    color = Slate600
                )
                Text(
                    text = formatDate(sale.createdAt),
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = formatCurrency(sale.totalAmount, currency),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (sale.status == "COMPLETED") EmeraldGreen else RoseError
                )
                Text(
                    text = sale.paymentMethod,
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun SaleStatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "COMPLETED" -> Pair(EmeraldGreenContainer, EmeraldGreen)
        "RETURNED" -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9)) // Purple
        else -> Pair(RoseErrorContainer, RoseError)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
