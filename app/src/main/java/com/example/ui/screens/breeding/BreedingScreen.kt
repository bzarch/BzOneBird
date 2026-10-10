package com.example.ui.screens.breeding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entities.BreedingPairEntity
import com.example.data.local.entities.ChickEntity
import com.example.data.local.entities.EggEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingScreen(
    viewModel: BzOneViewModel,
    onAddPairClick: () -> Unit,
    onEditPairClick: (Long) -> Unit,
    onAddEggClick: () -> Unit,
    onEditEggClick: (Long) -> Unit,
    onAddChickClick: () -> Unit,
    onEditChickClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pairs by viewModel.activeBreedingPairs.collectAsStateWithLifecycle()
    val eggs by viewModel.activeEggs.collectAsStateWithLifecycle()
    val chicks by viewModel.activeChicks.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pasangan, 1: Telur, 2: Anakan

    var pairToArchive by remember { mutableStateOf<BreedingPairEntity?>(null) }
    var pairToDelete by remember { mutableStateOf<BreedingPairEntity?>(null) }
    var eggToArchive by remember { mutableStateOf<EggEntity?>(null) }
    var eggToDelete by remember { mutableStateOf<EggEntity?>(null) }
    var chickToArchive by remember { mutableStateOf<ChickEntity?>(null) }
    var chickToDelete by remember { mutableStateOf<ChickEntity?>(null) }
    var chickToConvert by remember { mutableStateOf<ChickEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Penangkaran & Ternak", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            when (selectedTab) {
                                0 -> onAddPairClick()
                                1 -> onAddEggClick()
                                2 -> onAddChickClick()
                            }
                        },
                        modifier = Modifier.testTag("add_breeding_top_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> onAddPairClick()
                        1 -> onAddEggClick()
                        2 -> onAddChickClick()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("breeding_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
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
                    text = { Text("Pasangan (${pairs.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Telur (${eggs.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Anakan (${chicks.size})") }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: PASANGAN INDUKAN
                    if (pairs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(
                                icon = Icons.Default.Favorite,
                                title = "Belum Ada Pasangan Indukan",
                                description = "Jodohkan burung jantan dan betina untuk memulai siklus penangkaran.",
                                actionButtonText = "Buat Pasangan Baru",
                                onActionClick = onAddPairClick
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pairs, key = { it.id }) { pair ->
                                val male = birds.find { it.id == pair.maleBirdId }
                                val female = birds.find { it.id == pair.femaleBirdId }
                                val cage = cages.find { it.id == pair.cageId }

                                BreedingPairCard(
                                    pair = pair,
                                    maleName = male?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Jantan #${pair.maleBirdId}",
                                    femaleName = female?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Betina #${pair.femaleBirdId}",
                                    cageName = cage?.let { "${it.code} - ${it.name}" },
                                    onEdit = { onEditPairClick(pair.id) },
                                    onArchive = { pairToArchive = pair },
                                    onDelete = { pairToDelete = pair }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: TELUR
                    if (eggs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(
                                icon = Icons.Default.Egg,
                                title = "Belum Ada Catatan Telur",
                                description = "Catat jumlah telur bertelur, fertilitas, dan perkiraan tanggal menetas.",
                                actionButtonText = "Catat Telur",
                                onActionClick = onAddEggClick
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(eggs, key = { it.id }) { egg ->
                                val pair = pairs.find { it.id == egg.pairId }
                                EggCard(
                                    egg = egg,
                                    pairCode = pair?.code ?: "Pasangan #${egg.pairId}",
                                    onEdit = { onEditEggClick(egg.id) },
                                    onArchive = { eggToArchive = egg },
                                    onDelete = { eggToDelete = egg }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: ANAKAN
                    if (chicks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(
                                icon = Icons.Default.ChildCare,
                                title = "Belum Ada Catatan Anakan",
                                description = "Catat anakan hasil tetasan dan pantau perkembangannya hingga mandiri.",
                                actionButtonText = "Catat Anakan Baru",
                                onActionClick = onAddChickClick
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(chicks, key = { it.id }) { chick ->
                                ChickCard(
                                    chick = chick,
                                    onEdit = { onEditChickClick(chick.id) },
                                    onArchive = { chickToArchive = chick },
                                    onDelete = { chickToDelete = chick },
                                    onConvertToBird = { chickToConvert = chick }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    pairToArchive?.let { p ->
        ConfirmDialog(
            title = "Arsipkan Pasangan",
            message = "Pindahkan pasangan \"${p.code}\" ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveBreedingPair(p.id)
                pairToArchive = null
            },
            onDismiss = { pairToArchive = null }
        )
    }

    pairToDelete?.let { p ->
        ConfirmDialog(
            title = "Hapus Pasangan Permanen",
            message = "Hapus pasangan \"${p.code}\" secara permanen dari database?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBreedingPairPermanently(p.id)
                pairToDelete = null
            },
            onDismiss = { pairToDelete = null }
        )
    }

    eggToArchive?.let { e ->
        ConfirmDialog(
            title = "Arsipkan Catatan Telur",
            message = "Arsipkan catatan telur tanggal ${Formatters.formatDate(e.clutchDate)}?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveEgg(e.id)
                eggToArchive = null
            },
            onDismiss = { eggToArchive = null }
        )
    }

    eggToDelete?.let { e ->
        ConfirmDialog(
            title = "Hapus Telur Permanen",
            message = "Hapus catatan telur ini secara permanen?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteEggPermanently(e.id)
                eggToDelete = null
            },
            onDismiss = { eggToDelete = null }
        )
    }

    chickToArchive?.let { c ->
        ConfirmDialog(
            title = "Arsipkan Anakan",
            message = "Arsipkan anakan ${c.ringNumber.ifBlank { "#${c.id}" }}?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveChick(c.id)
                chickToArchive = null
            },
            onDismiss = { chickToArchive = null }
        )
    }

    chickToDelete?.let { c ->
        ConfirmDialog(
            title = "Hapus Anakan Permanen",
            message = "Hapus anakan ini secara permanen?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteChickPermanently(c.id)
                chickToDelete = null
            },
            onDismiss = { chickToDelete = null }
        )
    }

    chickToConvert?.let { c ->
        ConfirmDialog(
            title = "Jadikan Burung Mandiri",
            message = "Buat catatan Burung baru dari anakan ini (${c.ringNumber.ifBlank { "Anakan #${c.id}" }})? Silsilah induk jantan dan betina akan otomatis terhubung.",
            confirmText = "Jadikan Burung",
            isDestructive = false,
            onConfirm = {
                viewModel.convertChickToBird(c, "Anakan ${c.ringNumber}".trim(), null)
                chickToConvert = null
            },
            onDismiss = { chickToConvert = null }
        )
    }
}

@Composable
fun BreedingPairCard(
    pair: BreedingPairEntity,
    maleName: String,
    femaleName: String,
    cageName: String?,
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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pair.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(pair.status) }
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "♂ Jantan: $maleName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = "♀ Betina: $femaleName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(4.dp))
            if (cageName != null) {
                Text(text = "Kandang: $cageName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "Mulai Jodoh: ${Formatters.formatDate(pair.pairedDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun EggCard(
    egg: EggEntity,
    pairCode: String,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Egg, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Telur $pairCode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Telur: ${egg.totalEggs} butir", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Menetas: ${egg.hatchedCount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text("Fertil: ${egg.fertileCount} • Infertil: ${egg.infertileCount} • Pecah: ${egg.brokenCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tanggal Bertelur: ${Formatters.formatDate(egg.clutchDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            if (egg.estimatedHatchDate != null) {
                Text("Perkiraan Menetas: ${Formatters.formatDate(egg.estimatedHatchDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun ChickCard(
    chick: ChickEntity,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onConvertToBird: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (chick.ringNumber.isNotBlank()) "Ring: ${chick.ringNumber}" else "Anakan #${chick.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(chick.status) })
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

            Text("Jenis: ${chick.typeName.ifBlank { "Burung" }} • Kelamin: ${chick.gender}", style = MaterialTheme.typography.bodyMedium)
            Text("Tanggal Menetas: ${Formatters.formatDate(chick.hatchDate)} (${Formatters.calculateAge(chick.hatchDate)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

            if (chick.birdId == null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onConvertToBird,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Jadikan Burung Mandiri")
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text("✓ Sudah ditambahkan ke Data Burung (#${chick.birdId})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
