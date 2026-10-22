package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.ProductVariant
import com.example.data.model.ProductWithVariants
import com.example.data.model.Sale
import com.example.ui.AppScreen
import com.example.ui.DashboardMetrics
import com.example.ui.TopSeller
import com.example.ui.components.ColorDot
import com.example.ui.components.RestockDialog
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryLight
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenContainer
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseErrorContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@Composable
fun DashboardScreen(
    profile: BusinessProfile?,
    metrics: DashboardMetrics,
    products: List<ProductWithVariants>,
    recentSales: List<Sale>,
    topSellers: List<TopSeller>,
    onNavigate: (AppScreen) -> Unit,
    onRestockVariant: (variantId: Long, qty: Int, supplier: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = profile?.currency ?: "TND"
    var restockTarget by remember { mutableStateOf<Pair<ProductVariant, String>?>(null) }

    // Find all low & out of stock variants for direct alerts
    val alertVariants = remember(products) {
        val list = mutableListOf<Triple<String, ProductVariant, Long>>()
        for (p in products) {
            for (v in p.variants) {
                if (v.currentQuantity <= v.minimumStock) {
                    list.add(Triple(p.product.name, v, p.product.id))
                }
            }
        }
        list.sortedBy { it.second.currentQuantity }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Store Hero Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                modifier = Modifier.fillMaxWidth().testTag("store_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Navy900, BrandPrimary, Navy800)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = profile?.businessName ?: "SmartStock Boutique",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${profile?.address ?: "Boutique"} • Currency: $currency",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = EmeraldGreen.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Today's summary inside Hero
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S REVENUE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = formatCurrency(metrics.todayRevenue, currency),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TODAY'S SALES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${metrics.todaySalesCount} orders",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(AppScreen.NEW_SALE) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.weight(1f).testTag("quick_new_sale_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Sale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { onNavigate(AppScreen.ADD_EDIT_PRODUCT) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.weight(1f).testTag("quick_add_product_button")
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Product", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Secondary Quick Action Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onNavigate(AppScreen.STOCK_MOVEMENTS) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock Log", fontSize = 11.sp)
                }
                FilledTonalButton(
                    onClick = { onNavigate(AppScreen.REPORTS) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reports", fontSize = 11.sp)
                }
                FilledTonalButton(
                    onClick = { onNavigate(AppScreen.CUSTOMERS_SUPPLIERS) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocalMall, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contacts", fontSize = 11.sp)
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Monthly Revenue",
                        value = formatCurrency(metrics.monthRevenue, currency),
                        subtitle = "Gross Profit: ${formatCurrency(metrics.estimatedGrossProfitMonth, currency)}",
                        icon = Icons.Default.AttachMoney,
                        iconTint = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Stock on Hand",
                        value = "${metrics.totalStockItems} items",
                        subtitle = "${metrics.totalProducts} clothing styles",
                        icon = Icons.Default.Inventory2,
                        iconTint = BrandSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Low Stock Alerts",
                        value = "${metrics.lowStockCount} items",
                        subtitle = "${metrics.outOfStockCount} out of stock",
                        icon = Icons.Default.Warning,
                        iconTint = if (metrics.lowStockCount > 0) AmberWarning else EmeraldGreen,
                        onClick = { onNavigate(AppScreen.INVENTORY) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Inventory Valuation",
                        value = formatCurrency(metrics.inventoryWholesaleValue, currency),
                        subtitle = "Wholesale cost on shelves",
                        icon = Icons.Default.Receipt,
                        iconTint = BrandAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Low Stock Urgent Alert Banner
        if (alertVariants.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberWarningContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberWarning)
                            Text(
                                text = "Action Required: Low Stock Items (${alertVariants.size})",
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Products that have reached or fallen below your minimum inventory threshold.",
                            fontSize = 12.sp,
                            color = Slate600
                        )

                        // Top 3 urgent variants
                        alertVariants.take(3).forEach { (prodName, variant, _) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        ColorDot(hex = variant.colorHex)
                                        Column {
                                            Text(
                                                text = prodName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${variant.color} • Size ${variant.size}",
                                                fontSize = 11.sp,
                                                color = Slate600
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StockBadge(quantity = variant.currentQuantity, minThreshold = variant.minimumStock)
                                        OutlinedButton(
                                            onClick = { restockTarget = Pair(variant, prodName) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("restock_alert_btn_${variant.id}")
                                        ) {
                                            Text("Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Selling Products Section
        if (topSellers.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Best-Selling Products",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldGreen)
                        }

                        topSellers.forEachIndexed { index, seller ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = BrandPrimary
                                            )
                                        }
                                    }
                                    Text(
                                        text = seller.productName,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${seller.unitsSold} units",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = formatCurrency(seller.revenue, currency),
                                        fontSize = 11.sp,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                            if (index < topSellers.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Slate100)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Recorded Sales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "View All",
                            color = BrandPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigate(AppScreen.SALES_HISTORY) }
                        )
                    }

                    if (recentSales.isEmpty()) {
                        Text(
                            text = "No sales recorded yet. Tap '+ New Sale' to start recording transactions.",
                            fontSize = 13.sp,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        recentSales.take(4).forEach { sale ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = sale.saleNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BrandPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Slate100
                                        ) {
                                            Text(
                                                text = sale.salesChannel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Slate600,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${sale.customerName} • ${formatDate(sale.createdAt)}",
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                                Text(
                                    text = formatCurrency(sale.totalAmount, currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (sale.status == "COMPLETED") EmeraldGreen else RoseError
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Restock Dialog Trigger
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
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate600
                )
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
