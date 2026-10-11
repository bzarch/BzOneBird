package com.example.ui.screens.competitions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.CompetitionRecordEntity
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompetitionFormScreen(
    recordId: Long,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBirds by viewModel.activeBirds.collectAsStateWithLifecycle()

    var eventName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedBirdId by remember { mutableStateOf<Long?>(null) }
    var birdName by remember { mutableStateOf("") }
    var ticketClass by remember { mutableStateOf("") }
    var gantanganNumber by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("Juara 1") }
    var reward by remember { mutableStateOf("") }
    var judgeNotes by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val rankOptions = listOf("Juara 1", "Juara 2", "Juara 3", "10 Besar", "Nominasi", "Peserta")

    LaunchedEffect(recordId) {
        if (recordId > 0 && !isInitialized) {
            val existing = viewModel.getCompetitionById(recordId)
            existing?.let {
                eventName = it.eventName
                location = it.location
                eventDate = it.date
                selectedBirdId = it.birdId
                birdName = it.birdName
                ticketClass = it.ticketClass
                gantanganNumber = it.gantanganNumber
                rank = it.rank
                reward = it.reward
                judgeNotes = it.judgeNotes
            }
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId > 0) "Edit Prestasi Lomba" else "Catat Prestasi Lomba", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_form_comp")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (recordId > 0) {
                        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("btn_delete_form_comp")) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (eventName.isBlank()) {
                                viewModel.showMessage("Harap isi nama event lomba")
                                return@Button
                            }

                            val entity = CompetitionRecordEntity(
                                id = recordId,
                                eventName = eventName.trim(),
                                date = eventDate,
                                location = location.trim(),
                                birdId = selectedBirdId,
                                birdName = birdName.trim().ifBlank {
                                    activeBirds.find { it.id == selectedBirdId }?.let { b ->
                                        if (b.name.isNotBlank()) b.name else "Ring ${b.ringNumber}"
                                    } ?: "Burung Lomba"
                                },
                                ticketClass = ticketClass.trim(),
                                gantanganNumber = gantanganNumber.trim(),
                                rank = rank,
                                reward = reward.trim(),
                                judgeNotes = judgeNotes.trim()
                            )
                            viewModel.saveCompetition(entity) {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_competition")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan")
                    }
                }
            }
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
            // Event Name
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text("Nama Lomba / Event Gantangan *") },
                placeholder = { Text("Misal: Latpres Batavia Enterprise 2026") },
                leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_event_name")
            )

            // Date & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = Formatters.formatDate(eventDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal Lomba") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true }
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi Gantangan") },
                    placeholder = { Text("Misal: Arena Batavia") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bird Selection Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih Burung Yang Diturunkan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    if (activeBirds.isNotEmpty()) {
                        var expandedBirdDropdown by remember { mutableStateOf(false) }
                        val currentBird = activeBirds.find { it.id == selectedBirdId }

                        Box {
                            OutlinedButton(
                                onClick = { expandedBirdDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentBird?.let {
                                        "${it.name.ifBlank { "Ring " + it.ringNumber }} (${it.typeName})"
                                    } ?: "Pilih dari Data Burung Anda"
                                )
                            }
                            DropdownMenu(
                                expanded = expandedBirdDropdown,
                                onDismissRequest = { expandedBirdDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("-- Input Manual / Bebas --") },
                                    onClick = {
                                        selectedBirdId = null
                                        expandedBirdDropdown = false
                                    }
                                )
                                activeBirds.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text("${b.name.ifBlank { "Ring: " + b.ringNumber }} - ${b.typeName}") },
                                        onClick = {
                                            selectedBirdId = b.id
                                            birdName = if (b.name.isNotBlank()) b.name else "Ring ${b.ringNumber}"
                                            expandedBirdDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = birdName,
                        onValueChange = { birdName = it },
                        label = { Text("Nama Burung / No. Ring") },
                        placeholder = { Text("Misal: Petir / BZ-001") },
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Rank Selection Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Peringkat / Juara *", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rankOptions.forEach { r ->
                        FilterChip(
                            selected = rank == r,
                            onClick = { rank = r },
                            label = { Text(r) }
                        )
                    }
                }
            }

            // Ticket Class & Gantangan Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ticketClass,
                    onValueChange = { ticketClass = it },
                    label = { Text("Kelas / Tiket") },
                    placeholder = { Text("Misal: Utama 24G") },
                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = gantanganNumber,
                    onValueChange = { gantanganNumber = it },
                    label = { Text("No. Gantangan") },
                    placeholder = { Text("Misal: No. 28") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Reward / Trophy
            OutlinedTextField(
                value = reward,
                onValueChange = { reward = it },
                label = { Text("Hadiah / Trofi / Piagam") },
                placeholder = { Text("Misal: Trofi Emas + Piagam + Rp 1.500.000") },
                leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Judge Evaluation & Performance Notes
            OutlinedTextField(
                value = judgeNotes,
                onValueChange = { judgeNotes = it },
                label = { Text("Catatan Juri & Evaluasi Penampilan Burung") },
                placeholder = { Text("Misal: Gaya ngedur satu titik, durasi 95%, volume tembus, materi cililin dan kenari rapat.") },
                leadingIcon = { Icon(Icons.Default.RateReview, contentDescription = null) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Catatan Lomba?") },
            text = { Text("Data prestasi lomba ini akan dihapus permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCompetition(recordId)
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = eventDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { eventDate = it }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
