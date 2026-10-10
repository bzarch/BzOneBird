package com.example.ui.screens.care

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.CareRecordEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareScreen(
    viewModel: BzOneViewModel,
    onAddCareClick: () -> Unit,
    onEditCareClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val careRecords by viewModel.activeCareRecords.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hari Ini, 1: Semua
    var selectedActivityType by remember { mutableStateOf<String?>(null) }
    var recordToArchive by remember { mutableStateOf<CareRecordEntity?>(null) }
    var recordToDelete by remember { mutableStateOf<CareRecordEntity?>(null) }

    val startOfDay = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val endOfDay = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.timeInMillis
    }

    val displayedRecords = remember(careRecords, selectedTab, selectedActivityType) {
        var list = careRecords
        if (selectedTab == 0) {
            list = list.filter { it.dateTime in startOfDay..endOfDay }
        }
        if (!selectedActivityType.isNullOrBlank()) {
            list = list.filter { it.activityType == selectedActivityType }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perawatan Harian", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onAddCareClick, modifier = Modifier.testTag("add_care_top_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Catatan Perawatan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCareClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_care_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Agenda Hari Ini") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Semua Riwayat") }
                )
            }

            // Activity Type Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedActivityType == null,
                        onClick = { selectedActivityType = null },
                        label = { Text("Semua Jenis") }
                    )
                }
                listOf(
                    "Pemberian Pakan",
                    "Air Minum",
                    "Pembersihan Kandang",
                    "Penjemuran",
                    "Mandi",
                    "Latihan/Pemasteran",
                    "Pemeriksaan Kondisi"
                ).forEach { act ->
                    item {
                        FilterChip(
                            selected = selectedActivityType == act,
                            onClick = { selectedActivityType = if (selectedActivityType == act) null else act },
                            label = { Text(act) }
                        )
                    }
                }
            }

            if (displayedRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.EventAvailable,
                        title = if (selectedTab == 0) "Tidak Ada Jadwal Hari Ini" else "Belum Ada Catatan Perawatan",
                        description = "Catat pemberian pakan, pembersihan, jemur, atau jadwal perawatan harian burung.",
                        actionButtonText = "+ Catat Perawatan",
                        onActionClick = onAddCareClick
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedRecords, key = { it.id }) { item ->
                        val targetBird = birds.find { it.id == item.birdId }
                        val targetCage = cages.find { it.id == item.cageId }

                        CareCardItem(
                            record = item,
                            targetBirdName = targetBird?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" },
                            targetCageName = targetCage?.let { "${it.code} - ${it.name}" },
                            onToggleStatus = {
                                val next = if (item.status == "Selesai") "Belum Selesai" else "Selesai"
                                viewModel.updateCareStatus(item.id, next)
                            },
                            onEdit = { onEditCareClick(item.id) },
                            onArchive = { recordToArchive = item },
                            onDelete = { recordToDelete = item }
                        )
                    }
                }
            }
        }
    }

    recordToArchive?.let { r ->
        ConfirmDialog(
            title = "Arsipkan Perawatan",
            message = "Pindahkan catatan \"${r.activityType}\" ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveCareRecord(r.id)
                recordToArchive = null
            },
            onDismiss = { recordToArchive = null }
        )
    }

    recordToDelete?.let { r ->
        ConfirmDialog(
            title = "Hapus Perawatan Permanen",
            message = "Hapus catatan \"${r.activityType}\" secara permanen dari database?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteCareRecordPermanently(r.id)
                recordToDelete = null
            },
            onDismiss = { recordToDelete = null }
        )
    }
}

@Composable
fun CareCardItem(
    record: CareRecordEntity,
    targetBirdName: String?,
    targetCageName: String?,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isDone = record.status == "Selesai"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Status Selesai",
                    tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.activityType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (targetBirdName != null) {
                    Text(
                        text = "Burung: $targetBirdName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (targetCageName != null) {
                    Text(
                        text = "Kandang: $targetCageName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (record.notes.isNotBlank()) {
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Formatters.formatDateTime(record.dateTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Ubah") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Arsipkan") },
                        onClick = {
                            menuExpanded = false
                            onArchive()
                        },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Permanen", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}
