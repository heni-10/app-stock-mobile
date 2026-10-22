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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.ProductVariant
import com.example.data.model.ProductWithVariants
import com.example.ui.components.ColorDot
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

data class ColorOption(val name: String, val hex: String)

val StandardColors = listOf(
    ColorOption("Black", "#18181B"),
    ColorOption("White", "#FFFFFF"),
    ColorOption("Vintage Navy", "#1E3A8A"),
    ColorOption("Natural Beige", "#D7C9AA"),
    ColorOption("Sage Green", "#84A98C"),
    ColorOption("Terracotta", "#C86446"),
    ColorOption("Heather Grey", "#9CA3AF"),
    ColorOption("Burgundy", "#7F1D1D"),
    ColorOption("Denim Blue", "#2B4C7E")
)

val StandardSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
val NumericSizes = listOf("26", "28", "30", "32", "34", "36", "38", "40")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    editingProduct: ProductWithVariants?,
    categories: List<Category>,
    currency: String,
    onBack: () -> Unit,
    onSaveProduct: (Product, List<ProductVariant>) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(editingProduct?.product?.name ?: "") }
    var sku by remember { mutableStateOf(editingProduct?.product?.sku ?: "STYLE-${(100..999).random()}") }
    var category by remember { mutableStateOf(editingProduct?.product?.category ?: "T-Shirts") }
    var description by remember { mutableStateOf(editingProduct?.product?.description ?: "") }
    var supplier by remember { mutableStateOf(editingProduct?.product?.supplierName ?: "Atelier Mediterranean Garments") }
    var defaultCostText by remember { mutableStateOf((editingProduct?.product?.defaultCost ?: 25.0).toString()) }
    var defaultPriceText by remember { mutableStateOf((editingProduct?.product?.defaultPrice ?: 55.0).toString()) }

    // Variant generator states
    val selectedSizes = remember { mutableStateListOf<String>() }
    val selectedColors = remember { mutableStateListOf<ColorOption>() }

    // Current variants list
    val variants = remember {
        mutableStateListOf<ProductVariant>().apply {
            if (editingProduct != null) {
                addAll(editingProduct.variants)
            }
        }
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = if (editingProduct == null) "Add Clothing Product" else "Edit Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                Button(
                    onClick = {
                        val cost = defaultCostText.toDoubleOrNull() ?: 0.0
                        val price = defaultPriceText.toDoubleOrNull() ?: 0.0
                        if (name.isBlank()) {
                            validationError = "Please enter product name"
                            return@Button
                        }
                        if (variants.isEmpty()) {
                            validationError = "Please generate or add at least one size & color variant"
                            return@Button
                        }
                        val product = Product(
                            id = editingProduct?.product?.id ?: 0L,
                            name = name,
                            sku = sku,
                            description = description,
                            category = category,
                            defaultCost = cost,
                            defaultPrice = price,
                            supplierName = supplier
                        )
                        onSaveProduct(product, variants.toList())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.padding(end = 8.dp).testTag("save_product_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
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
            validationError?.let { err ->
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RoseError.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseError)
                    ) {
                        Text(
                            text = err,
                            color = RoseError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Basic Info Section
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Basic Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Style Name") },
                            placeholder = { Text("e.g. Classic Oversized Heavyweight Tee") },
                            modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("Product Reference / SKU") },
                                modifier = Modifier.weight(1f).testTag("product_sku_input")
                            )

                            OutlinedTextField(
                                value = supplier,
                                onValueChange = { supplier = it },
                                label = { Text("Supplier") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Category:", fontSize = 12.sp, color = Slate600)
                        val catList = categories.map { it.name }.ifEmpty { listOf("T-Shirts", "Dresses", "Denim & Pants", "Hoodies & Sweats", "Outerwear") }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(catList) { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = defaultCostText,
                                onValueChange = { defaultCostText = it },
                                label = { Text("Purchase Cost ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("product_cost_input")
                            )
                            OutlinedTextField(
                                value = defaultPriceText,
                                onValueChange = { defaultPriceText = it },
                                label = { Text("Selling Price ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("product_price_input")
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Fabric Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Variant Generator Section
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPrimary)
                            Text("Automatic Variant Matrix Generator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "Pick sizes and colors to auto-generate all inventory variants at once.",
                            fontSize = 12.sp,
                            color = Slate600
                        )

                        // Sizes Selection
                        Text("Select Sizes:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(StandardSizes + NumericSizes) { sz ->
                                val isSelected = selectedSizes.contains(sz)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedSizes.remove(sz) else selectedSizes.add(sz)
                                    },
                                    label = { Text(sz, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Colors Selection
                        Text("Select Colors:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(StandardColors) { col ->
                                val isSelected = selectedColors.contains(col)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) BrandPrimary.copy(alpha = 0.15f) else Slate100,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary) else null,
                                    modifier = Modifier.clickable {
                                        if (isSelected) selectedColors.remove(col) else selectedColors.add(col)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        ColorDot(hex = col.hex, size = 12)
                                        Text(col.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val cost = defaultCostText.toDoubleOrNull() ?: 20.0
                                val price = defaultPriceText.toDoubleOrNull() ?: 45.0
                                for (col in selectedColors) {
                                    for (sz in selectedSizes) {
                                        // Avoid duplicate size+color
                                        if (variants.none { it.size == sz && it.color == col.name }) {
                                            variants.add(
                                                ProductVariant(
                                                    productId = editingProduct?.product?.id ?: 0L,
                                                    size = sz,
                                                    color = col.name,
                                                    colorHex = col.hex,
                                                    purchaseCost = cost,
                                                    sellingPrice = price,
                                                    currentQuantity = 10,
                                                    minimumStock = 4,
                                                    sku = "$sku-${col.name.take(3).uppercase()}-$sz"
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = selectedSizes.isNotEmpty() && selectedColors.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("generate_variants_button")
                        ) {
                            Text("Generate (${selectedSizes.size * selectedColors.size} Variants)")
                        }
                    }
                }
            }

            // Configured Variants List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Configured Variants (${variants.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    TextButton(onClick = {
                        val cost = defaultCostText.toDoubleOrNull() ?: 20.0
                        val price = defaultPriceText.toDoubleOrNull() ?: 45.0
                        variants.add(
                            ProductVariant(
                                productId = editingProduct?.product?.id ?: 0L,
                                size = "M",
                                color = "Black",
                                colorHex = "#18181B",
                                purchaseCost = cost,
                                sellingPrice = price,
                                currentQuantity = 10,
                                minimumStock = 4,
                                sku = "$sku-CUSTOM"
                            )
                        )
                    }) {
                        Text("+ Add Single Variant", fontSize = 12.sp)
                    }
                }
            }

            itemsIndexed(variants) { index, v ->
                VariantEditableRow(
                    variant = v,
                    currency = currency,
                    onUpdate = { updated -> variants[index] = updated },
                    onDelete = { variants.removeAt(index) }
                )
            }
        }
    }
}

@Composable
fun VariantEditableRow(
    variant: ProductVariant,
    currency: String,
    onUpdate: (ProductVariant) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorDot(hex = variant.colorHex)
                    Text(
                        text = "${variant.color} - Size ${variant.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseError, modifier = Modifier.size(16.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                var stockText by remember(variant.currentQuantity) { mutableStateOf(variant.currentQuantity.toString()) }
                OutlinedTextField(
                    value = stockText,
                    onValueChange = {
                        stockText = it
                        val q = it.toIntOrNull() ?: 0
                        onUpdate(variant.copy(currentQuantity = q))
                    },
                    label = { Text("Initial Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                var minText by remember(variant.minimumStock) { mutableStateOf(variant.minimumStock.toString()) }
                OutlinedTextField(
                    value = minText,
                    onValueChange = {
                        minText = it
                        val m = it.toIntOrNull() ?: 4
                        onUpdate(variant.copy(minimumStock = m))
                    },
                    label = { Text("Min Alert") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                var priceText by remember(variant.sellingPrice) { mutableStateOf(variant.sellingPrice.toString()) }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        val p = it.toDoubleOrNull() ?: 0.0
                        onUpdate(variant.copy(sellingPrice = p))
                    },
                    label = { Text("Price ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
