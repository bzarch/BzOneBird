package com.example.ui.screens.health

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.HealthRecordEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onAddRecordClick: () -> Unit,
    onEditRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val records by viewModel.activeHealthRecords.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    var recordToArchive by remember { mutableStateOf<HealthRecordEntity?>(null) }
    var recordToDelete by remember { mutableStateOf<HealthRecordEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kesehatan Burung", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onAddRecordClick, modifier = Modifier.testTag("add_health_top_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Catat Kesehatan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecordClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_health_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Kesehatan")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Veterinary Medical Disclaimer Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Catatan ini adalah dokumentasi riwayat pribadi dari pengguna. Untuk kondisi burung sakit serius atau darurat, segera konsultasikan dengan dokter hewan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Default.Healing,
                        title = "Belum Ada Catatan Kesehatan",
                        description = "Pantau kondisi kesehatan, gejala, pemberian vitamin, dan jadwal kontrol burung.",
                        actionButtonText = "+ Catat Kondisi Kesehatan",
                        onActionClick = onAddRecordClick
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.id }) { rec ->
                        val bird = birds.find { it.id == rec.birdId }
                        HealthCardItem(
                            record = rec,
                            birdName = bird?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Burung #${rec.birdId}",
                            onEdit = { onEditRecordClick(rec.id) },
                            onArchive = { recordToArchive = rec },
                            onDelete = { recordToDelete = rec }
                        )
                    }
                }
            }
        }
    }

    recordToArchive?.let { r ->
        ConfirmDialog(
            title = "Arsipkan Catatan Kesehatan",
            message = "Arsipkan catatan pemeriksaan ini?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveHealthRecord(r.id)
                recordToArchive = null
            },
            onDismiss = { recordToArchive = null }
        )
    }

    recordToDelete?.let { r ->
        ConfirmDialog(
            title = "Hapus Riwayat Kesehatan",
            message = "Hapus catatan kesehatan ini secara permanen?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteHealthRecordPermanently(r.id)
                recordToDelete = null
            },
            onDismiss = { recordToDelete = null }
        )
    }
}

@Composable
fun HealthCardItem(
    record: HealthRecordEntity,
    birdName: String,
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
                    Text(text = birdName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = Formatters.formatDate(record.checkDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
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
            Spacer(modifier = Modifier.height(4.dp))
            if (record.conditionDesc.isNotBlank() || record.symptoms.isNotBlank()) {
                Text(text = "Kondisi/Gejala: ${record.conditionDesc.ifBlank { record.symptoms }}", style = MaterialTheme.typography.bodyMedium)
            }
            if (record.treatment.isNotBlank() || record.medication.isNotBlank()) {
                Text(text = "Tindakan/Obat: ${record.treatment} ${record.medication}".trim(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (record.vetName.isNotBlank()) {
                Text(text = "Dokter/Klinik: ${record.vetName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthFormScreen(
    recordId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.activeHealthRecords.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    val existing = remember(recordId, records) {
        if (recordId != null && recordId != 0L) records.find { it.id == recordId } else null
    }

    var selectedBirdId by remember(existing) { mutableStateOf(existing?.birdId ?: birds.firstOrNull()?.id ?: 0L) }
    var checkDate by remember(existing) { mutableStateOf(existing?.checkDate ?: System.currentTimeMillis()) }
    var conditionDesc by remember(existing) { mutableStateOf(existing?.conditionDesc ?: "") }
    var symptoms by remember(existing) { mutableStateOf(existing?.symptoms ?: "") }
    var treatment by remember(existing) { mutableStateOf(existing?.treatment ?: "") }
    var medication by remember(existing) { mutableStateOf(existing?.medication ?: "") }
    var vetName by remember(existing) { mutableStateOf(existing?.vetName ?: "") }
    var followupResult by remember(existing) { mutableStateOf(existing?.followupResult ?: "") }

    var birdDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null || recordId == 0L) "Catat Kesehatan Burung" else "Ubah Catatan Kesehatan") },
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
            Text("Pilih Burung *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentBird = birds.find { it.id == selectedBirdId }?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Pilih Burung"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { birdDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentBird)
                        Text("Pilih", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = birdDropdownExpanded, onDismissRequest = { birdDropdownExpanded = false }) {
                    birds.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.name.ifBlank { b.ringNumber }} (${b.typeName})") },
                            onClick = { selectedBirdId = b.id; birdDropdownExpanded = false }
                        )
                    }
                }
            }

            // Date
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = checkDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            val s = Calendar.getInstance().apply { set(y, m, d) }
                            checkDate = s.timeInMillis
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal Pemeriksaan: ${Formatters.formatDate(checkDate)}")
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = conditionDesc,
                onValueChange = { conditionDesc = it },
                label = { Text("Kondisi Umum (Misal: Lesu, Kurang Nafsu Makan)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                label = { Text("Gejala yang Terlihat") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = treatment,
                onValueChange = { treatment = it },
                label = { Text("Tindakan / Perawatan yang Diberikan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                label = { Text("Obat / Vitamin yang Digunakan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = vetName,
                onValueChange = { vetName = it },
                label = { Text("Nama Dokter Hewan / Klinik (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = followupResult,
                onValueChange = { followupResult = it },
                label = { Text("Hasil Tindak Lanjut") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (selectedBirdId == 0L) {
                        viewModel.showMessage("Silakan pilih burung terlebih dahulu")
                        return@Button
                    }
                    val toSave = (existing ?: HealthRecordEntity(birdId = selectedBirdId)).copy(
                        birdId = selectedBirdId,
                        checkDate = checkDate,
                        conditionDesc = conditionDesc.trim(),
                        symptoms = symptoms.trim(),
                        treatment = treatment.trim(),
                        medication = medication.trim(),
                        vetName = vetName.trim(),
                        followupResult = followupResult.trim()
                    )
                    viewModel.saveHealthRecord(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_health_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Catatan Kesehatan")
            }
        }
    }
}
