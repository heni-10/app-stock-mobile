package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryMovement
import com.example.ui.components.formatDate
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseErrorContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementsScreen(
    movements: List<InventoryMovement>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("ALL") }
    val types = listOf("ALL", "SALE", "STOCK_IN", "ADJUSTMENT", "RETURN", "CANCELLATION")

    val filteredMovements = remember(movements, selectedType) {
        if (selectedType == "ALL") movements else movements.filter { it.movementType == selectedType }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Stock Movement Audit Trail", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Traceable log of all inventory sales, additions, adjustments, and returns.",
                fontSize = 12.sp,
                color = Slate600
            )

            // Types filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                items(types) { t ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = t },
                        label = { Text(t, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredMovements.isEmpty()) {
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
                        Icon(Icons.Default.History, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                        Text("No movements recorded yet", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 76.dp)
                ) {
                    items(filteredMovements, key = { it.id }) { mov ->
                        MovementItemCard(mov = mov)
                    }
                }
            }
        }
    }
}

@Composable
fun MovementItemCard(mov: InventoryMovement) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MovementBadge(mov.movementType)
                    Text(formatDate(mov.createdAt), fontSize = 11.sp, color = Slate400)
                }
                Text(
                    text = "${mov.productName} (${mov.variantInfo})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )
                Text(
                    text = mov.reason,
                    fontSize = 11.sp,
                    color = Slate600
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val isPositive = mov.quantityChange > 0
                Text(
                    text = if (isPositive) "+${mov.quantityChange}" else "${mov.quantityChange}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isPositive) EmeraldGreen else if (mov.movementType == "SALE") RoseError else AmberWarning
                )
                Text(
                    text = "Stock: ${mov.quantityAfter}",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun MovementBadge(type: String) {
    val (bgColor, textColor) = when (type) {
        "STOCK_IN" -> Pair(EmeraldGreenContainer, EmeraldGreen)
        "SALE" -> Pair(RoseErrorContainer, RoseError)
        "RETURN" -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
        "CANCELLATION" -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
        else -> Pair(AmberWarningContainer, AmberWarning)
    }
    Surface(shape = RoundedCornerShape(4.dp), color = bgColor) {
        Text(type, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
