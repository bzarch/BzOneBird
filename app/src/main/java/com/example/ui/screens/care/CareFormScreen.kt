package com.example.ui.screens.care

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.CareRecordEntity
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareFormScreen(
    recordId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val careRecords by viewModel.activeCareRecords.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    val existing = remember(recordId, careRecords) {
        if (recordId != null && recordId != 0L) careRecords.find { it.id == recordId } else null
    }

    var activityType by remember(existing) { mutableStateOf(existing?.activityType ?: "Pemberian Pakan") }
    var dateTime by remember(existing) { mutableStateOf(existing?.dateTime ?: System.currentTimeMillis()) }
    var selectedBirdId by remember(existing) { mutableStateOf(existing?.birdId) }
    var selectedCageId by remember(existing) { mutableStateOf(existing?.cageId) }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "Selesai") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }

    var birdDropdownExpanded by remember { mutableStateOf(false) }
    var cageDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val activityPresets = listOf(
        "Pemberian Pakan",
        "Air Minum",
        "Pembersihan Kandang",
        "Penjemuran",
        "Mandi",
        "Latihan/Pemasteran",
        "Pemeriksaan Kondisi"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null || recordId == 0L) "Catat Perawatan" else "Ubah Perawatan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSaving) return@IconButton
                            if (activityType.isBlank()) {
                                viewModel.showMessage("Jenis aktivitas tidak boleh kosong")
                                return@IconButton
                            }
                            isSaving = true
                            val toSave = (existing ?: CareRecordEntity(activityType = activityType)).copy(
                                activityType = activityType.trim(),
                                dateTime = dateTime,
                                birdId = selectedBirdId,
                                cageId = selectedCageId,
                                status = status,
                                notes = notes.trim()
                            )
                            viewModel.saveCareRecord(toSave)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("save_care_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan")
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
            Text("Aktivitas Perawatan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Preset activity chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pilih Jenis Cepat:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    activityPresets.take(3).forEach { p ->
                        FilterChip(
                            selected = activityType == p,
                            onClick = { activityType = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    activityPresets.drop(3).take(3).forEach { p ->
                        FilterChip(
                            selected = activityType == p,
                            onClick = { activityType = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = activityType,
                onValueChange = { activityType = it },
                label = { Text("Nama Aktivitas *") },
                modifier = Modifier.fillMaxWidth().testTag("care_activity_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Date & Time Picker
            Text("Waktu Pelaksanaan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateTime }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                cal.set(Calendar.YEAR, y)
                                cal.set(Calendar.MONTH, m)
                                cal.set(Calendar.DAY_OF_MONTH, d)
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        cal.set(Calendar.MINUTE, minute)
                                        dateTime = cal.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(Formatters.formatDateTime(dateTime), style = MaterialTheme.typography.bodyLarge)
                        Text("Ketuk untuk ubah tanggal & jam", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            // Target Bird
            Text("Burung Terkait (Opsional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentBirdName = birds.find { it.id == selectedBirdId }?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Tidak Ditentukan / Semua"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { birdDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentBirdName)
                        Text("Ubah", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = birdDropdownExpanded, onDismissRequest = { birdDropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text("Tidak Ada / Umum") }, onClick = { selectedBirdId = null; birdDropdownExpanded = false })
                    birds.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.name.ifBlank { b.ringNumber }} (${b.typeName})") },
                            onClick = { selectedBirdId = b.id; birdDropdownExpanded = false }
                        )
                    }
                }
            }

            // Target Cage
            Text("Kandang Terkait (Opsional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentCage = cages.find { it.id == selectedCageId }?.let { "${it.code} - ${it.name}" } ?: "Tidak Ditentukan"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { cageDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentCage)
                        Text("Ubah", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = cageDropdownExpanded, onDismissRequest = { cageDropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text("Tidak Ada") }, onClick = { selectedCageId = null; cageDropdownExpanded = false })
                    cages.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.code} - ${c.name}") },
                            onClick = { selectedCageId = c.id; cageDropdownExpanded = false }
                        )
                    }
                }
            }

            // Status
            Text("Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Selesai", "Belum Selesai", "Dibatalkan").forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s) }
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan / Keterangan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button
                    if (activityType.isBlank()) {
                        viewModel.showMessage("Jenis aktivitas tidak boleh kosong")
                        return@Button
                    }
                    isSaving = true
                    val toSave = (existing ?: CareRecordEntity(activityType = activityType)).copy(
                        activityType = activityType.trim(),
                        dateTime = dateTime,
                        birdId = selectedBirdId,
                        cageId = selectedCageId,
                        status = status,
                        notes = notes.trim()
                    )
                    viewModel.saveCareRecord(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_care_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Catatan Perawatan")
            }
        }
    }
}
