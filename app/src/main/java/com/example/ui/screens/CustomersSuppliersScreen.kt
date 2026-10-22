package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Customer
import com.example.data.model.Supplier
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersSuppliersScreen(
    customers: List<Customer>,
    suppliers: List<Supplier>,
    currency: String,
    onBack: () -> Unit,
    onAddCustomer: (Customer) -> Unit,
    onAddSupplier: (Supplier) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Customers & Suppliers", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Customers (${customers.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Suppliers (${suppliers.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                // Customers Tab
                if (customers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No customers saved yet.", color = Slate400)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(customers, key = { it.id }) { c ->
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
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(c.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (c.phone.isNotBlank()) {
                                            Text(c.phone, fontSize = 12.sp, color = Slate600)
                                        }
                                        if (c.address.isNotBlank()) {
                                            Text(c.address, fontSize = 11.sp, color = Slate400)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatCurrency(c.totalSpent, currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldGreen
                                        )
                                        Text("${c.totalOrders} orders", fontSize = 11.sp, color = Slate400)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Suppliers Tab
                if (suppliers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No suppliers saved yet.", color = Slate400)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(suppliers, key = { it.id }) { s ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Slate200, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(shape = RoundedCornerShape(4.dp), color = Slate100) {
                                            Text(s.category, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    if (s.phone.isNotBlank()) {
                                        Text(s.phone, fontSize = 12.sp, color = Slate600)
                                    }
                                    if (s.address.isNotBlank()) {
                                        Text(s.address, fontSize = 11.sp, color = Slate400)
                                    }
                                    if (s.notes.isNotBlank()) {
                                        Text(s.notes, fontSize = 11.sp, color = BrandPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FAB
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 20.dp)
                    .testTag("add_contact_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    }

    if (showAddDialog) {
        if (selectedTab == 0) {
            AddCustomerDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    onAddCustomer(it)
                    showAddDialog = false
                }
            )
        } else {
            AddSupplierDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    onAddSupplier(it)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / City") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(Customer(name = name, phone = phone, address = address))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (Supplier) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cotton & Knitwear") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Supplier", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Supplier / Factory Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Specialty") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes / Lead Time") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(Supplier(name = name, phone = phone, address = address, category = category, notes = notes))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Save Supplier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
