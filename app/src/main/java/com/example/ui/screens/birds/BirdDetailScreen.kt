package com.example.ui.screens.birds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.DetailSectionCard
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdDetailScreen(
    birdId: Long,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onEditBird: (Long) -> Unit,
    onNavigateToPedigree: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val archivedBirds by viewModel.archivedBirds.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    val bird = birds.find { it.id == birdId } ?: archivedBirds.find { it.id == birdId }

    val movements by viewModel.getBirdMovements(birdId).collectAsStateWithLifecycle(initialValue = emptyList())
    val careRecords by viewModel.getRecordsForBird(birdId).collectAsStateWithLifecycle(initialValue = emptyList())
    val healthRecords by viewModel.getHealthRecordsForBird(birdId).collectAsStateWithLifecycle(initialValue = emptyList())

    var showArchiveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (bird == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Burung") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Burung tidak ditemukan atau telah dihapus.")
            }
        }
        return
    }

    val cageName = cages.find { it.id == bird.cageId }?.let { "${it.code} - ${it.name}" } ?: if (bird.cageId != null) "Kandang #${bird.cageId}" else "Belum Ditempatkan"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bird.name.ifBlank { bird.ringNumber.ifBlank { bird.typeName } }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditBird(bird.id) }, modifier = Modifier.testTag("edit_bird_button")) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah Data")
                    }
                    IconButton(onClick = { showArchiveDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = "Arsipkan")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Permanen", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = bird.name.ifBlank { "Burung #${bird.id}" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${bird.typeName} ${if (bird.variety.isNotBlank()) "• ${bird.variety}" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(bird.gender) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (bird.gender == "Jantan") Icons.Default.Male else if (bird.gender == "Betina") Icons.Default.Female else Icons.Default.QuestionMark,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Status: ${bird.status}") }
                            )
                        }
                        if (onNavigateToPedigree != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = { onNavigateToPedigree(bird.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cetak Sertifikat Silsilah (Pedigree)")
                            }
                        }
                    }
                }
            }

            // Identitas Fisik & Perolehan
            item {
                DetailSectionCard(title = "Informasi Lengkap") {
                    DetailRow(label = "Nomor Ring", value = bird.ringNumber.ifBlank { "-" })
                    DetailRow(label = "Jenis Burung", value = bird.typeName)
                    DetailRow(label = "Ras / Varietas", value = bird.variety.ifBlank { "-" })
                    DetailRow(label = "Warna / Mutasi", value = bird.colorMutation.ifBlank { "-" })
                    DetailRow(
                        label = "Tanggal Lahir / Umur",
                        value = if (bird.birthDate != null) "${Formatters.formatDate(bird.birthDate)} (${Formatters.calculateAge(bird.birthDate)})${if (bird.isBirthDateEstimated) " [Perkiraan]" else ""}" else "-"
                    )
                    DetailRow(label = "Kandang Saat Ini", value = cageName)
                    DetailRow(label = "Asal Burung", value = bird.origin)
                    DetailRow(label = "Tanggal Diperoleh", value = Formatters.formatDate(bird.acquisitionDate))
                    DetailRow(
                        label = "Harga Perolehan",
                        value = if (bird.acquisitionCost != null) Formatters.formatRupiah(bird.acquisitionCost) else "-"
                    )
                    DetailRow(label = "Kondisi Awal", value = bird.conditionOnAcquisition)
                }
            }

            // Silsilah / Induk
            item {
                DetailSectionCard(title = "Silsilah Indukan") {
                    val sireBird = birds.find { it.id == bird.sireId }
                    val damBird = birds.find { it.id == bird.damId }
                    DetailRow(
                        label = "Induk Jantan (Ayah)",
                        value = sireBird?.let { "${it.name.ifBlank { it.ringNumber }} (#${it.id})" } ?: if (bird.sireId != null) "Burung #${bird.sireId}" else "Tidak Diketahui"
                    )
                    DetailRow(
                        label = "Induk Betina (Ibu)",
                        value = damBird?.let { "${it.name.ifBlank { it.ringNumber }} (#${it.id})" } ?: if (bird.damId != null) "Burung #${bird.damId}" else "Tidak Diketahui"
                    )
                }
            }

            // Riwayat Perpindahan Kandang
            item {
                DetailSectionCard(title = "Riwayat Perpindahan Kandang (${movements.size})") {
                    if (movements.isEmpty()) {
                        Text(
                            text = "Belum ada catatan mutasi kandang.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        movements.forEach { m ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "${Formatters.formatDate(m.movementDate)}: ${m.previousCageName} → ${m.newCageName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (m.reason.isNotBlank()) {
                                    Text(
                                        text = "Alasan: ${m.reason}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Riwayat Perawatan Terakhir
            item {
                DetailSectionCard(title = "Riwayat Perawatan Terakhir (${careRecords.size})") {
                    if (careRecords.isEmpty()) {
                        Text(
                            text = "Belum ada catatan perawatan khusus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        careRecords.take(5).forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = c.activityType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(text = Formatters.formatDateTime(c.dateTime), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Text(text = c.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Riwayat Kesehatan
            item {
                DetailSectionCard(title = "Riwayat Kesehatan (${healthRecords.size})") {
                    if (healthRecords.isEmpty()) {
                        Text(
                            text = "Burung belum pernah mencatat riwayat sakit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        healthRecords.take(5).forEach { h ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(text = "${Formatters.formatDate(h.checkDate)} - ${h.conditionDesc.ifBlank { h.symptoms }}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                if (h.treatment.isNotBlank() || h.medication.isNotBlank()) {
                                    Text(text = "Tindakan/Obat: ${h.treatment} ${h.medication}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Catatan
            if (bird.notes.isNotBlank()) {
                item {
                    DetailSectionCard(title = "Catatan Khusus") {
                        Text(
                            text = bird.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (showArchiveDialog) {
        ConfirmDialog(
            title = "Arsipkan Burung",
            message = "Pindahkan \"${bird.name.ifBlank { bird.ringNumber }}\" ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveBird(bird.id)
                showArchiveDialog = false
                onNavigateBack()
            },
            onDismiss = { showArchiveDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus Burung Permanen",
            message = "PERINGATAN: Menghapus burung ini secara permanen akan menghapus seluruh data dan riwayat terkait. Yakin?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBirdPermanently(bird.id)
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
