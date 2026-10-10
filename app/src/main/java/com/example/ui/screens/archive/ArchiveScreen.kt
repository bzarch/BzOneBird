package com.example.ui.screens.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val archivedBirds by viewModel.archivedBirds.collectAsStateWithLifecycle()
    val archivedCages by viewModel.archivedCages.collectAsStateWithLifecycle()
    val archivedPairs by viewModel.archivedBreedingPairs.collectAsStateWithLifecycle()
    val archivedCare by viewModel.archivedCareRecords.collectAsStateWithLifecycle()
    val archivedTxs by viewModel.archivedTransactions.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Burung, 1: Kandang, 2: Pasangan, 3: Lainnya

    var confirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmDeleteMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tempat Sampah / Arsip", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Burung (${archivedBirds.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Kandang (${archivedCages.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Pasangan (${archivedPairs.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Lainnya (${archivedCare.size + archivedTxs.size})") })
            }

            when (selectedTab) {
                0 -> {
                    if (archivedBirds.isEmpty()) {
                        EmptyStateView(icon = Icons.Default.DeleteOutline, title = "Arsip Burung Kosong", description = "Tidak ada burung yang sedang diarsipkan.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(archivedBirds, key = { it.id }) { b ->
                                ArchivedItemCard(
                                    title = b.name.ifBlank { b.ringNumber.ifBlank { b.typeName } },
                                    subtitle = "${b.typeName} • ${b.gender} • No Ring: ${b.ringNumber.ifBlank { "-" }}",
                                    onRestore = { viewModel.restoreBird(b.id) },
                                    onDeletePermanently = {
                                        confirmDeleteMessage = "Hapus burung \"${b.name.ifBlank { b.ringNumber }}\" secara permanen dari database?"
                                        confirmDeleteAction = { viewModel.deleteBirdPermanently(b.id) }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (archivedCages.isEmpty()) {
                        EmptyStateView(icon = Icons.Default.DeleteOutline, title = "Arsip Kandang Kosong", description = "Tidak ada kandang yang sedang diarsipkan.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(archivedCages, key = { it.id }) { c ->
                                ArchivedItemCard(
                                    title = "${c.code} - ${c.name}",
                                    subtitle = "${c.type} • Kapasitas: ${c.plannedCapacity}",
                                    onRestore = { viewModel.restoreCage(c.id) },
                                    onDeletePermanently = {
                                        confirmDeleteMessage = "Hapus kandang \"${c.name}\" secara permanen?"
                                        confirmDeleteAction = { viewModel.deleteCagePermanently(c.id) }
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    if (archivedPairs.isEmpty()) {
                        EmptyStateView(icon = Icons.Default.DeleteOutline, title = "Arsip Pasangan Kosong", description = "Tidak ada pasangan yang sedang diarsipkan.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(archivedPairs, key = { it.id }) { p ->
                                ArchivedItemCard(
                                    title = p.code,
                                    subtitle = "Jantan #${p.maleBirdId} x Betina #${p.femaleBirdId} • Tanggal: ${Formatters.formatDate(p.pairedDate)}",
                                    onRestore = { viewModel.restoreBreedingPair(p.id) },
                                    onDeletePermanently = {
                                        confirmDeleteMessage = "Hapus pasangan \"${p.code}\" secara permanen?"
                                        confirmDeleteAction = { viewModel.deleteBreedingPairPermanently(p.id) }
                                    }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    val combinedOther = archivedCare.map { "Perawatan: ${it.activityType}" to it.id } + archivedTxs.map { "${it.type}: ${it.category} (${Formatters.formatRupiah(it.amount)})" to it.id }
                    if (combinedOther.isEmpty()) {
                        EmptyStateView(icon = Icons.Default.DeleteOutline, title = "Arsip Lainnya Kosong", description = "Tidak ada catatan lain yang diarsipkan.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(archivedCare, key = { "care_${it.id}" }) { cr ->
                                ArchivedItemCard(
                                    title = "Perawatan: ${cr.activityType}",
                                    subtitle = "Status: ${cr.status} • ${Formatters.formatDateTime(cr.dateTime)}",
                                    onRestore = { viewModel.restoreCareRecord(cr.id) },
                                    onDeletePermanently = {
                                        confirmDeleteMessage = "Hapus catatan perawatan ini secara permanen?"
                                        confirmDeleteAction = { viewModel.deleteCareRecordPermanently(cr.id) }
                                    }
                                )
                            }
                            items(archivedTxs, key = { "tx_${it.id}" }) { tx ->
                                ArchivedItemCard(
                                    title = "Transaksi: ${tx.type} (${tx.category})",
                                    subtitle = "${Formatters.formatRupiah(tx.amount)} • ${Formatters.formatDate(tx.date)}",
                                    onRestore = { viewModel.restoreTransaction(tx.id) },
                                    onDeletePermanently = {
                                        confirmDeleteMessage = "Hapus transaksi ini secara permanen?"
                                        confirmDeleteAction = { viewModel.deleteTransactionPermanently(tx.id) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDeleteAction != null) {
        ConfirmDialog(
            title = "Hapus Permanen",
            message = confirmDeleteMessage,
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                confirmDeleteAction?.invoke()
                confirmDeleteAction = null
            },
            onDismiss = { confirmDeleteAction = null }
        )
    }
}

@Composable
fun ArchivedItemCard(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onRestore, modifier = Modifier.testTag("restore_button")) {
                    Icon(Icons.Default.Restore, contentDescription = "Pulihkan", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeletePermanently, modifier = Modifier.testTag("permanent_delete_button")) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Hapus Permanen", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
