package com.example.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.FeedItemEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.activeFeedItems.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    var itemForMovement by remember { mutableStateOf<FeedItemEntity?>(null) }
    var itemToArchive by remember { mutableStateOf<FeedItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<FeedItemEntity?>(null) }

    val categories = listOf("Pakan Biji", "Voer", "Buah & Sayur", "Vitamin & Suplemen", "Perlengkapan")

    val displayedItems = remember(items, selectedCategory) {
        if (selectedCategory == null) items else items.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stok Pakan & Perlengkapan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onAddItemClick, modifier = Modifier.testTag("add_inventory_top_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Barang")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItemClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_inventory_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Barang")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Semua Kategori") }
                    )
                }
                categories.forEach { cat ->
                    item {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.Inventory2,
                        title = "Belum Ada Stok Barang",
                        description = "Catat stok pakan, vitamin, atau perlengkapan kandang Anda di sini.",
                        actionButtonText = "+ Tambah Barang",
                        onActionClick = onAddItemClick
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        val isLowStock = item.currentStock <= item.minStockThreshold
                        InventoryCardItem(
                            item = item,
                            isLowStock = isLowStock,
                            onMutasi = { itemForMovement = item },
                            onEdit = { onEditItemClick(item.id) },
                            onArchive = { itemToArchive = item },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Movement Dialog
    itemForMovement?.let { item ->
        StockMovementDialog(
            item = item,
            onDismiss = { itemForMovement = null },
            onConfirm = { type, qty, reason, notes ->
                viewModel.recordStockMovement(item.id, type, qty, reason, notes)
                itemForMovement = null
            }
        )
    }

    itemToArchive?.let { i ->
        ConfirmDialog(
            title = "Arsipkan Barang",
            message = "Pindahkan \"${i.name}\" ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveFeedItem(i.id)
                itemToArchive = null
            },
            onDismiss = { itemToArchive = null }
        )
    }

    itemToDelete?.let { i ->
        ConfirmDialog(
            title = "Hapus Barang Permanen",
            message = "Hapus \"${i.name}\" secara permanen dari database?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteFeedItemPermanently(i.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
fun InventoryCardItem(
    item: FeedItemEntity,
    isLowStock: Boolean,
    onMutasi: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Stok Menipis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Ubah") }, onClick = { menuExpanded = false; onEdit() })
                            DropdownMenuItem(text = { Text("Arsipkan") }, onClick = { menuExpanded = false; onArchive() })
                            DropdownMenuItem(text = { Text("Hapus Permanen", color = MaterialTheme.colorScheme.error) }, onClick = { menuExpanded = false; onDelete() })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stok: ${item.currentStock} ${item.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Batas Minimum: ${item.minStockThreshold} ${item.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = onMutasi,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catat Mutasi")
                }
            }
        }
    }
}

@Composable
fun StockMovementDialog(
    item: FeedItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Double, reason: String, notes: String) -> Unit
) {
    var type by remember { mutableStateOf("Pemakaian") }
    var qtyStr by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("Pemberian pakan harian") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mutasi Stok: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Stok Saat Ini: ${item.currentStock} ${item.unit}", style = MaterialTheme.typography.bodyMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Masuk", "Pemakaian", "Keluar", "Penyesuaian").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t, style = MaterialTheme.typography.labelSmall) })
                    }
                }

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text("Jumlah (${item.unit})") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan / Keperluan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyStr.toDoubleOrNull() ?: 0.0
                    if (qty > 0) {
                        onConfirm(type, qty, reason, notes)
                    }
                }
            ) {
                Text("Simpan Mutasi")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    itemId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.activeFeedItems.collectAsStateWithLifecycle()
    val existing = remember(itemId, items) {
        if (itemId != null && itemId != 0L) items.find { it.id == itemId } else null
    }

    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Pakan Biji") }
    var unit by remember(existing) { mutableStateOf(existing?.unit ?: "kg") }
    var stockStr by remember(existing) { mutableStateOf(existing?.currentStock?.toString() ?: "1.0") }
    var minStockStr by remember(existing) { mutableStateOf(existing?.minStockThreshold?.toString() ?: "1.0") }
    var priceStr by remember(existing) { mutableStateOf(existing?.purchasePrice?.toLong()?.toString() ?: "0") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null || itemId == 0L) "Tambah Barang" else "Ubah Barang") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Barang * (Misal: Milet Putih, Voer)") },
                modifier = Modifier.fillMaxWidth().testTag("item_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Kategori", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Pakan Biji", "Voer", "Vitamin & Suplemen", "Perlengkapan").forEach { c ->
                    FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c, style = MaterialTheme.typography.labelSmall) })
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stok Awal") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Satuan (kg, bks, botol)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = minStockStr,
                onValueChange = { minStockStr = it },
                label = { Text("Batas Peringatan Stok Minimum") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Harga Pembelian (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan / Supplier") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (name.isBlank()) {
                        viewModel.showMessage("Nama barang tidak boleh kosong")
                        return@Button
                    }
                    val st = stockStr.toDoubleOrNull() ?: 0.0
                    val mst = minStockStr.toDoubleOrNull() ?: 1.0
                    val pr = priceStr.toDoubleOrNull() ?: 0.0

                    val toSave = (existing ?: FeedItemEntity(name = name)).copy(
                        name = name.trim(),
                        category = category,
                        unit = unit.trim(),
                        currentStock = st.coerceAtLeast(0.0),
                        minStockThreshold = mst.coerceAtLeast(0.0),
                        purchasePrice = pr.coerceAtLeast(0.0),
                        notes = notes.trim()
                    )
                    viewModel.saveFeedItem(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_item_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Barang")
            }
        }
    }
}
