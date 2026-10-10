package com.example.ui.screens.cages

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
import com.example.data.local.entities.CageEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CagesScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onAddCageClick: () -> Unit,
    onEditCageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    var cageToArchive by remember { mutableStateOf<CageEntity?>(null) }
    var cageToDelete by remember { mutableStateOf<CageEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kandang Burung", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onAddCageClick, modifier = Modifier.testTag("add_cage_top_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Kandang")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCageClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_cage_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kandang")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.HomeWork,
                    title = "Belum Ada Kandang",
                    description = "Kelola kandang soliter, koloni, atau kandang ternak burung Anda.",
                    actionButtonText = "+ Tambah Kandang",
                    onActionClick = onAddCageClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cages, key = { it.id }) { cage ->
                    val occupantBirds = birds.filter { it.cageId == cage.id }
                    val occupantCount = occupantBirds.size
                    val isOverCapacity = occupantCount > cage.plannedCapacity

                    CageCardItem(
                        cage = cage,
                        occupantCount = occupantCount,
                        isOverCapacity = isOverCapacity,
                        occupants = occupantBirds.map { it.name.ifBlank { it.ringNumber.ifBlank { it.typeName } } },
                        onEdit = { onEditCageClick(cage.id) },
                        onArchive = { cageToArchive = cage },
                        onDelete = { cageToDelete = cage }
                    )
                }
            }
        }
    }

    cageToArchive?.let { c ->
        ConfirmDialog(
            title = "Arsipkan Kandang",
            message = "Pindahkan \"${c.name}\" ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveCage(c.id)
                cageToArchive = null
            },
            onDismiss = { cageToArchive = null }
        )
    }

    cageToDelete?.let { c ->
        ConfirmDialog(
            title = "Hapus Kandang Permanen",
            message = "Hapus \"${c.name}\" secara permanen? Burung di dalam kandang ini akan menjadi tanpa kandang.",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteCagePermanently(c.id)
                cageToDelete = null
            },
            onDismiss = { cageToDelete = null }
        )
    }
}

@Composable
fun CageCardItem(
    cage: CageEntity,
    occupantCount: Int,
    isOverCapacity: Boolean,
    occupants: List<String>,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${cage.code} - ${cage.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(cage.type) }
                    )
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Ubah") }, onClick = { menuExpanded = false; onEdit() })
                            DropdownMenuItem(text = { Text("Arsipkan") }, onClick = { menuExpanded = false; onArchive() })
                            DropdownMenuItem(text = { Text("Hapus Permanen", color = MaterialTheme.colorScheme.error) }, onClick = { menuExpanded = false; onDelete() })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            if (cage.location.isNotBlank()) {
                Text(
                    text = "Lokasi: ${cage.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terisi: $occupantCount / ${cage.plannedCapacity} Ekor",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverCapacity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (isOverCapacity) {
                    Text(
                        text = "⚠ Melebihi Kapasitas!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (occupants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Penghuni: ${occupants.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CageFormScreen(
    cageId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()
    val existing = remember(cageId, cages) {
        if (cageId != null && cageId != 0L) cages.find { it.id == cageId } else null
    }

    var code by remember(existing) { mutableStateOf(existing?.code ?: "K-01") }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var type by remember(existing) { mutableStateOf(existing?.type ?: "Kandang Soliter") }
    var location by remember(existing) { mutableStateOf(existing?.location ?: "") }
    var capacityStr by remember(existing) { mutableStateOf(existing?.plannedCapacity?.toString() ?: "1") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "Aktif") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cageId == null || cageId == 0L) "Tambah Kandang" else "Ubah Kandang") },
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
                value = code,
                onValueChange = { code = it },
                label = { Text("Kode Kandang * (Misal: K-01)") },
                modifier = Modifier.fillMaxWidth().testTag("cage_code_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Kandang *") },
                modifier = Modifier.fillMaxWidth().testTag("cage_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Jenis Kandang", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Kandang Soliter", "Kandang Ternak", "Kandang Koloni", "Kandang Umbaran").forEach { t ->
                    FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lokasi (Misal: Ruang Depan, Aviary Belakang)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = capacityStr,
                onValueChange = { capacityStr = it },
                label = { Text("Kapasitas Terencana (Ekor)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Kandang") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (code.isBlank() || name.isBlank()) {
                        viewModel.showMessage("Kode dan nama kandang tidak boleh kosong")
                        return@Button
                    }
                    val cap = capacityStr.toIntOrNull() ?: 1
                    val toSave = (existing ?: CageEntity(code = code, name = name)).copy(
                        code = code.trim(),
                        name = name.trim(),
                        type = type,
                        location = location.trim(),
                        plannedCapacity = cap.coerceAtLeast(1),
                        notes = notes.trim(),
                        status = status
                    )
                    viewModel.saveCage(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_cage_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Kandang")
            }
        }
    }
}
