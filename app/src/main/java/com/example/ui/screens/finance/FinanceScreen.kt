package com.example.ui.screens.finance

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.TransactionEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.activeTransactions.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    var filterType by remember { mutableStateOf<String?>(null) } // null: Semua, "Pemasukan", "Pengeluaran"
    var txToArchive by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val balance = totalIncome - totalExpense

    val displayedTxs = remember(transactions, filterType) {
        if (filterType == null) transactions else transactions.filter { it.type == filterType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keuangan & Pembukuan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onAddTransactionClick, modifier = Modifier.testTag("add_finance_top_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Catat Transaksi")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_finance_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Transaksi")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Saldo Kas Burung", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatRupiah(balance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Pemasukan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    Formatters.formatRupiah(totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Pengeluaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    Formatters.formatRupiah(totalExpense),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Filter row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("Semua (${transactions.size})") }
                    )
                    FilterChip(
                        selected = filterType == "Pemasukan",
                        onClick = { filterType = if (filterType == "Pemasukan") null else "Pemasukan" },
                        label = { Text("Pemasukan") }
                    )
                    FilterChip(
                        selected = filterType == "Pengeluaran",
                        onClick = { filterType = if (filterType == "Pengeluaran") null else "Pengeluaran" },
                        label = { Text("Pengeluaran") }
                    )
                }
            }

            if (displayedTxs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        EmptyStateView(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Belum Ada Transaksi",
                            description = "Catat penjualan anakan, burung, pembelian pakan, atau biaya perawatan.",
                            actionButtonText = "+ Catat Transaksi",
                            onActionClick = onAddTransactionClick
                        )
                    }
                }
            } else {
                items(displayedTxs, key = { it.id }) { tx ->
                    val relatedBird = birds.find { it.id == tx.relatedBirdId }
                    TransactionCardItem(
                        tx = tx,
                        relatedBirdName = relatedBird?.name?.ifBlank { relatedBird.ringNumber },
                        onEdit = { onEditTransactionClick(tx.id) },
                        onArchive = { txToArchive = tx },
                        onDelete = { txToDelete = tx }
                    )
                }
            }
        }
    }

    txToArchive?.let { t ->
        ConfirmDialog(
            title = "Arsipkan Transaksi",
            message = "Pindahkan transaksi ${Formatters.formatRupiah(t.amount)} ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveTransaction(t.id)
                txToArchive = null
            },
            onDismiss = { txToArchive = null }
        )
    }

    txToDelete?.let { t ->
        ConfirmDialog(
            title = "Hapus Transaksi Permanen",
            message = "Hapus transaksi ${Formatters.formatRupiah(t.amount)} secara permanen? Perhitungan saldo akan diperbarui.",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTransactionPermanently(t.id)
                txToDelete = null
            },
            onDismiss = { txToDelete = null }
        )
    }
}

@Composable
fun TransactionCardItem(
    tx: TransactionEntity,
    relatedBirdName: String?,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isIncome = tx.type == "Pemasukan"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tx.description.isNotBlank()) {
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (relatedBirdName != null) {
                    Text(
                        text = "Burung: $relatedBirdName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = Formatters.formatDate(tx.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ${Formatters.formatRupiah(tx.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    txId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.activeTransactions.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    val existing = remember(txId, transactions) {
        if (txId != null && txId != 0L) transactions.find { it.id == txId } else null
    }

    var type by remember(existing) { mutableStateOf(existing?.type ?: "Pengeluaran") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Pakan") }
    var amountStr by remember(existing) { mutableStateOf(existing?.amount?.toLong()?.toString() ?: "") }
    var date by remember(existing) { mutableStateOf(existing?.date ?: System.currentTimeMillis()) }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var selectedBirdId by remember(existing) { mutableStateOf(existing?.relatedBirdId) }

    var birdDropdownExpanded by remember { mutableStateOf(false) }

    val incomeCategories = listOf("Penjualan Burung", "Penjualan Anakan", "Penjualan Telur", "Hadiah / Lomba", "Lainnya")
    val expenseCategories = listOf("Pakan", "Pembelian Burung", "Perlengkapan", "Perawatan", "Kesehatan & Obat", "Kandang", "Penangkaran", "Lainnya")

    val currentCategories = if (type == "Pemasukan") incomeCategories else expenseCategories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (txId == null || txId == 0L) "Catat Transaksi" else "Ubah Transaksi") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Jenis Transaksi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = type == "Pengeluaran",
                    onClick = {
                        type = "Pengeluaran"
                        category = expenseCategories.first()
                    },
                    label = { Text("Pengeluaran (Biaya)") }
                )
                FilterChip(
                    selected = type == "Pemasukan",
                    onClick = {
                        type = "Pemasukan"
                        category = incomeCategories.first()
                    },
                    label = { Text("Pemasukan (Pendapatan)") }
                )
            }

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Jumlah Nominal (Rp) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Kategori Transaksi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                currentCategories.chunked(3).forEach { rowCats ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowCats.forEach { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            // Date
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = date }
                        DatePickerDialog(context, { _, y, m, d ->
                            val s = Calendar.getInstance().apply { set(y, m, d) }
                            date = s.timeInMillis
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal: ${Formatters.formatDate(date)}")
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            // Related bird
            Text("Burung Terkait (Opsional)", style = MaterialTheme.typography.labelMedium)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentBird = birds.find { it.id == selectedBirdId }?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Tidak Terkait Burung Khusus"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { birdDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentBird)
                        Text("Ubah", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = birdDropdownExpanded, onDismissRequest = { birdDropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text("Tidak Terkait") }, onClick = { selectedBirdId = null; birdDropdownExpanded = false })
                    birds.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.name.ifBlank { b.ringNumber }} (${b.typeName})") },
                            onClick = { selectedBirdId = b.id; birdDropdownExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Keterangan Transaksi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        viewModel.showMessage("Nominal harus berupa angka lebih besar dari 0")
                        return@Button
                    }
                    val toSave = (existing ?: TransactionEntity(type = type, category = category, amount = amt)).copy(
                        type = type,
                        category = category,
                        amount = amt,
                        date = date,
                        description = description.trim(),
                        relatedBirdId = selectedBirdId
                    )
                    viewModel.saveTransaction(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_transaction_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Transaksi")
            }
        }
    }
}
