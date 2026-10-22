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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profile: BusinessProfile?,
    onBack: () -> Unit,
    onSaveProfile: (BusinessProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var businessName by remember { mutableStateOf(profile?.businessName ?: "SmartStock Boutique") }
    var currency by remember { mutableStateOf(profile?.currency ?: "TND") }
    var phone by remember { mutableStateOf(profile?.phone ?: "+216 98 450 120") }
    var address by remember { mutableStateOf(profile?.address ?: "12 Rue de Marseille, Tunis") }
    var ownerName by remember { mutableStateOf(profile?.ownerName ?: "Heni M.") }
    var lowStockThresholdText by remember { mutableStateOf((profile?.lowStockDefaultThreshold ?: 5).toString()) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Business Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                Button(
                    onClick = {
                        val thresh = lowStockThresholdText.toIntOrNull() ?: 5
                        onSaveProfile(
                            BusinessProfile(
                                id = 1L,
                                businessName = businessName,
                                currency = currency,
                                phone = phone,
                                address = address,
                                ownerName = ownerName,
                                lowStockDefaultThreshold = thresh
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.padding(end = 8.dp).testTag("save_settings_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.height(2.dp))
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
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = BrandPrimary)
                            Text("Store Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business / Boutique Name") },
                            modifier = Modifier.fillMaxWidth().testTag("settings_store_name")
                        )

                        Text("Store Currency:", fontSize = 12.sp, color = Slate600)
                        val currencies = listOf("TND", "USD", "EUR", "GBP", "CAD", "DZD", "MAD")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            currencies.forEach { cur ->
                                FilterChip(
                                    selected = currency == cur,
                                    onClick = { currency = cur },
                                    label = { Text(cur, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner / Manager Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Contact Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Boutique / Store Address") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lowStockThresholdText,
                            onValueChange = { lowStockThresholdText = it },
                            label = { Text("Default Low-Stock Alert Threshold") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // System Information Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate200.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Slate600)
                            Text("About SmartStock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "SmartStock v1.0 provides offline-first transactional inventory tracking with automatic stock deduction, size & color variant matrix management, and traceable audit trails.",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                        Text(
                            text = "Engineered for boutiques, physical shops, and social media sellers.",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}
