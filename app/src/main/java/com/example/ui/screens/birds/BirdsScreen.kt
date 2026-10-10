package com.example.ui.screens.birds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.BirdEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BirdSortOrder
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdsScreen(
    viewModel: BzOneViewModel,
    onBirdClick: (Long) -> Unit,
    onAddBirdClick: () -> Unit,
    onEditBirdClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val birds by viewModel.filteredBirds.collectAsStateWithLifecycle()
    val allActiveBirds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.birdSearchQuery.collectAsStateWithLifecycle()
    val selectedSpecies by viewModel.selectedSpeciesFilter.collectAsStateWithLifecycle()
    val selectedGender by viewModel.selectedGenderFilter.collectAsStateWithLifecycle()
    val selectedCage by viewModel.selectedCageFilter.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.birdSortOrder.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedBirdIds.collectAsStateWithLifecycle()

    val birdTypes by viewModel.activeBirdTypes.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showArchiveConfirmDialog by remember { mutableStateOf(false) }
    var singleBirdToDelete by remember { mutableStateOf<BirdEntity?>(null) }
    var singleBirdToArchive by remember { mutableStateOf<BirdEntity?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val isSelectionMode = selectedIds.isNotEmpty()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} dipilih") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearBirdSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Batal Pilihan")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAllFilteredBirds() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Pilih Semua")
                        }
                        IconButton(onClick = { showArchiveConfirmDialog = true }) {
                            Icon(Icons.Default.Archive, contentDescription = "Arsipkan")
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Permanen")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Data Burung",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${birds.size} dari ${allActiveBirds.size} burung aktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier.testTag("filter_birds_button")
                        ) {
                            val hasActiveFilter = selectedSpecies != null || selectedGender != null || selectedCage != null || selectedStatus != null
                            BadgedBox(
                                badge = {
                                    if (hasActiveFilter) {
                                        Badge { Text("!") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onAddBirdClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_bird_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Burung")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.birdSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("bird_search_input"),
                placeholder = { Text("Cari nama, ring, jenis, catatan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.birdSearchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Horizontal Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear filter chip if any active
                if (selectedSpecies != null || selectedGender != null || selectedCage != null || selectedStatus != null || searchQuery.isNotBlank()) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearBirdFilters() },
                            label = { Text("Reset Filter") },
                            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                // Gender chips
                item {
                    FilterChip(
                        selected = selectedGender == "Jantan",
                        onClick = { viewModel.selectedGenderFilter.value = if (selectedGender == "Jantan") null else "Jantan" },
                        label = { Text("Jantan") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedGender == "Betina",
                        onClick = { viewModel.selectedGenderFilter.value = if (selectedGender == "Betina") null else "Betina" },
                        label = { Text("Betina") }
                    )
                }

                // Dynamic species filter chips based on birds actually owned
                val existingSpecies = allActiveBirds.map { it.typeName }.filter { it.isNotBlank() }.distinct()
                items(existingSpecies) { spName ->
                    FilterChip(
                        selected = selectedSpecies == spName,
                        onClick = { viewModel.selectedSpeciesFilter.value = if (selectedSpecies == spName) null else spName },
                        label = { Text(spName) }
                    )
                }
            }

            // Bird list
            if (birds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (searchQuery.isNotBlank() || selectedSpecies != null || selectedGender != null) {
                        EmptyStateView(
                            icon = Icons.Default.SearchOff,
                            title = "Tidak Ditemukan",
                            description = "Tidak ada burung yang sesuai dengan filter pencarian Anda.",
                            actionButtonText = "Reset Filter",
                            onActionClick = { viewModel.clearBirdFilters() }
                        )
                    } else {
                        EmptyStateView(
                            icon = Icons.Default.Pets,
                            title = "Belum Ada Burung",
                            description = "Koleksi burung Anda masih kosong. Mulai tambahkan burung pertama Anda sekarang.",
                            actionButtonText = "Tambah Burung",
                            onActionClick = onAddBirdClick
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(birds, key = { it.id }) { bird ->
                        val isSelected = selectedIds.contains(bird.id)
                        BirdCardItem(
                            bird = bird,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleBirdSelection(bird.id)
                                } else {
                                    onBirdClick(bird.id)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleBirdSelection(bird.id)
                            },
                            onEdit = { onEditBirdClick(bird.id) },
                            onArchive = { singleBirdToArchive = bird },
                            onDelete = { singleBirdToDelete = bird }
                        )
                    }
                }
            }
        }
    }

    // Single item archive confirmation dialog
    singleBirdToArchive?.let { bird ->
        ConfirmDialog(
            title = "Arsipkan Burung",
            message = "Pindahkan \"${bird.name.ifBlank { bird.ringNumber.ifBlank { bird.typeName } }}\" ke Arsip? Burung tidak akan tampil di daftar aktif tetapi dapat dipulihkan.",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveBird(bird.id)
                singleBirdToArchive = null
            },
            onDismiss = { singleBirdToArchive = null }
        )
    }

    // Single item delete confirmation dialog
    singleBirdToDelete?.let { bird ->
        ConfirmDialog(
            title = "Hapus Burung Permanen",
            message = "PERINGATAN: Apakah Anda yakin ingin menghapus \"${bird.name.ifBlank { bird.ringNumber.ifBlank { bird.typeName } }}\" secara permanen? Tindakan ini tidak dapat dibatalkan.",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBirdPermanently(bird.id)
                singleBirdToDelete = null
            },
            onDismiss = { singleBirdToDelete = null }
        )
    }

    // Batch Archive Dialog
    if (showArchiveConfirmDialog) {
        ConfirmDialog(
            title = "Arsipkan ${selectedIds.size} Burung",
            message = "Pindahkan seluruh burung yang dipilih ke Arsip? Data tetap tersimpan dan dapat dipulihkan kapan saja.",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveSelectedBirds()
                showArchiveConfirmDialog = false
            },
            onDismiss = { showArchiveConfirmDialog = false }
        )
    }

    // Batch Delete Dialog
    if (showDeleteConfirmDialog) {
        ConfirmDialog(
            title = "Hapus Permanen ${selectedIds.size} Burung",
            message = "PERINGATAN: Menghapus ${selectedIds.size} burung secara permanen akan menghapus data tersebut dari database. Yakin melanjutkan?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSelectedBirdsPermanently()
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter & Urutkan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {
                        viewModel.clearBirdFilters()
                        showFilterSheet = false
                    }) {
                        Text("Reset")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Sort Order
                Text("Urutan:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sortOrder == BirdSortOrder.TERBARU,
                        onClick = { viewModel.birdSortOrder.value = BirdSortOrder.TERBARU },
                        label = { Text("Terbaru") }
                    )
                    FilterChip(
                        selected = sortOrder == BirdSortOrder.NAMA_ASC,
                        onClick = { viewModel.birdSortOrder.value = BirdSortOrder.NAMA_ASC },
                        label = { Text("Nama (A-Z)") }
                    )
                    FilterChip(
                        selected = sortOrder == BirdSortOrder.NO_RING_ASC,
                        onClick = { viewModel.birdSortOrder.value = BirdSortOrder.NO_RING_ASC },
                        label = { Text("No Ring") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Status Filter
                Text("Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Aktif", "Dikembangbiakkan", "Dijual", "Mati").forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { viewModel.selectedStatusFilter.value = if (selectedStatus == st) null else st },
                            label = { Text(st) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terapkan")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BirdCardItem(
    bird: BirdEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("bird_card_${bird.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Bird Icon / Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (bird.gender) {
                            "Jantan" -> MaterialTheme.colorScheme.primaryContainer
                            "Betina" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = when (bird.gender) {
                        "Jantan" -> MaterialTheme.colorScheme.primary
                        "Betina" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = bird.name.ifBlank { bird.ringNumber.ifBlank { bird.typeName } },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Gender badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (bird.gender) {
                            "Jantan" -> Color(0xFFE3F2FD)
                            "Betina" -> Color(0xFFFCE4EC)
                            else -> Color(0xFFEEEEEE)
                        }
                    ) {
                        Text(
                            text = bird.gender,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (bird.gender) {
                                "Jantan" -> Color(0xFF1565C0)
                                "Betina" -> Color(0xFFC2185B)
                                else -> Color(0xFF616161)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${bird.typeName}${if (bird.variety.isNotBlank()) " • ${bird.variety}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (bird.ringNumber.isNotBlank()) {
                        Text(
                            text = "Ring: ${bird.ringNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (bird.cageId != null) {
                        Text(
                            text = "Kandang #${bird.cageId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = "• Status: ${bird.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bird.status == "Aktif") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Action 3-dots menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu Tindakan")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Lihat Detail") },
                        onClick = {
                            menuExpanded = false
                            onClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Ubah Data") },
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
                    HorizontalDivider()
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
