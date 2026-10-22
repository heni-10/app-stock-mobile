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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductWithVariants
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.ui.TopSeller
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsScreen(
    sales: List<Sale>,
    saleItems: List<SaleItem>,
    products: List<ProductWithVariants>,
    topSellers: List<TopSeller>,
    currency: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dateRange by remember { mutableStateOf("THIS_MONTH") } // "TODAY", "LAST_7_DAYS", "THIS_MONTH", "ALL"

    val filteredSales = remember(sales, dateRange) {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val startTime = when (dateRange) {
            "TODAY" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "LAST_7_DAYS" -> now - 7L * 24 * 3600 * 1000
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> 0L
        }
        sales.filter { it.createdAt >= startTime && it.status == "COMPLETED" }
    }

    val saleIds = remember(filteredSales) { filteredSales.map { it.id }.toSet() }
    val relevantItems = remember(saleItems, saleIds) { saleItems.filter { it.saleId in saleIds } }

    val totalRevenue = filteredSales.sumOf { it.totalAmount }
    val totalDiscounts = filteredSales.sumOf { it.discountAmount }
    val totalCOGS = relevantItems.sumOf { it.unitCost * it.quantity }
    val grossProfit = (totalRevenue - totalCOGS).coerceAtLeast(0.0)
    val marginPercent = if (totalRevenue > 0) ((grossProfit / totalRevenue) * 100).toInt() else 0
    val averageOrderValue = if (filteredSales.isNotEmpty()) totalRevenue / filteredSales.size else 0.0

    // Channel breakdown
    val channelSales = remember(filteredSales) {
        filteredSales.groupBy { it.salesChannel }
            .mapValues { it.value.sumOf { s -> s.totalAmount } }
    }

    // Inventory Valuation
    val totalStockUnits = products.sumOf { it.totalStock }
    val totalStockWholesale = products.sumOf { it.variants.sumOf { v -> v.purchaseCost * v.currentQuantity } }
    val totalStockRetail = products.sumOf { it.variants.sumOf { v -> v.sellingPrice * v.currentQuantity } }
    val potentialGrossProfit = (totalStockRetail - totalStockWholesale).coerceAtLeast(0.0)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Financial Overview & Reports", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Date Filter Pills
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("TODAY" to "Today", "LAST_7_DAYS" to "Last 7 Days", "THIS_MONTH" to "This Month", "ALL" to "All Time").forEach { (key, label) ->
                        FilterChip(
                            selected = dateRange == key,
                            onClick = { dateRange = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Profit & Revenue Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Revenue vs. Profit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreen.copy(alpha = 0.15f)) {
                                Text("$marginPercent% Gross Margin", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }

                        Text(
                            text = "Revenue represents total sales earned before expenses. Gross profit equals revenue minus product manufacturing/purchase cost (COGS).",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Revenue", fontSize = 11.sp, color = Slate400)
                                Text(formatCurrency(totalRevenue, currency), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandPrimary)
                            }
                            Column {
                                Text("Cost of Goods (COGS)", fontSize = 11.sp, color = Slate400)
                                Text(formatCurrency(totalCOGS, currency), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Gross Profit", fontSize = 11.sp, color = Slate400)
                                Text(formatCurrency(grossProfit, currency), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = EmeraldGreen)
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate100))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Completed Sales: ${filteredSales.size}", fontSize = 12.sp, color = Slate600)
                            Text("Avg Order Value: ${formatCurrency(averageOrderValue, currency)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Sales By Channel Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sales by Channel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = BrandSecondary)
                        }

                        if (channelSales.isEmpty()) {
                            Text("No sales data in this date range.", fontSize = 12.sp, color = Slate400)
                        } else {
                            channelSales.forEach { (channel, amount) ->
                                val pct = if (totalRevenue > 0) (amount / totalRevenue).toFloat() else 0f
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(channel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${formatCurrency(amount, currency)} (${(pct * 100).toInt()}%)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        color = if (channel == "Physical Store") BrandPrimary else BrandSecondary,
                                        trackColor = Slate100,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Current Inventory Valuation Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Current Inventory Asset Valuation", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Estimated market and cost value of all unsold clothing on boutique shelves.",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Units in Stock", fontSize = 11.sp, color = Slate400)
                                Text("$totalStockUnits items", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column {
                                Text("Wholesale Cost Value", fontSize = 11.sp, color = Slate400)
                                Text(formatCurrency(totalStockWholesale, currency), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandAccent)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Projected Retail Value", fontSize = 11.sp, color = Slate400)
                                Text(formatCurrency(totalStockRetail, currency), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
