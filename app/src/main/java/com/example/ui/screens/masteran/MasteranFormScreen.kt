package com.example.ui.screens.masteran

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.MasteranAudioEntity
import com.example.viewmodel.BzOneViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MasteranFormScreen(
    masteranId: Long,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedSourceType by remember { mutableStateOf("Lokal") } // "Lokal" or "YouTube"
    var title by remember { mutableStateOf("") }
    var sourceUri by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tembakan") }
    var targetSpecies by remember { mutableStateOf("Universal") }
    var notes by remember { mutableStateOf("") }
    var localFileName by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    val categories = listOf("Tembakan", "Ngeroll", "Isian Mewah", "Terapi Suara Air", "Pancingan Bunyi", "Campuran")

    // File audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            sourceUri = it.toString()
            // Persist read permission if supported
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            // Try getting file display name
            var name = ""
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}

            if (name.isBlank()) {
                name = it.lastPathSegment?.substringAfterLast("/") ?: "audio_masteran.mp3"
            }
            localFileName = name
            if (title.isBlank()) {
                title = name.substringBeforeLast(".")
            }
        }
    }

    LaunchedEffect(masteranId) {
        if (masteranId > 0 && !isInitialized) {
            val existing = viewModel.getMasteranById(masteranId)
            existing?.let {
                title = it.title
                selectedSourceType = it.sourceType
                sourceUri = it.sourceUri
                category = it.category
                targetSpecies = it.targetSpecies
                notes = it.notes
                if (it.sourceType == "Lokal") {
                    localFileName = it.sourceUri.substringAfterLast("/").ifBlank { "File Audio Tersimpan" }
                }
            }
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (masteranId > 0) "Edit Masteran" else "Tambah Masteran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_form_masteran")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (masteranId > 0) {
                        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("btn_delete_form_masteran")) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Masteran", tint = MaterialTheme.colorScheme.error)
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
                            if (title.isBlank()) {
                                viewModel.showMessage("Harap isi judul masteran")
                                return@Button
                            }
                            if (sourceUri.isBlank()) {
                                viewModel.showMessage(
                                    if (selectedSourceType == "Lokal") "Harap pilih file audio dari HP"
                                    else "Harap masukkan tautan video/audio YouTube"
                                )
                                return@Button
                            }

                            val entity = MasteranAudioEntity(
                                id = masteranId,
                                title = title.trim(),
                                sourceType = selectedSourceType,
                                sourceUri = sourceUri.trim(),
                                category = category,
                                targetSpecies = targetSpecies.trim().ifBlank { "Universal" },
                                notes = notes.trim()
                            )
                            viewModel.saveMasteran(entity) {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_masteran")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Source Selector Tabs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pilih Sumber Audio Masteran",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSourceType == "Lokal",
                            onClick = { selectedSourceType = "Lokal" },
                            label = { Text("Penyimpanan Lokal (HP)") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedSourceType == "YouTube",
                            onClick = { selectedSourceType = "YouTube" },
                            label = { Text("Tautan YouTube") },
                            leadingIcon = { Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color(0xFFD32F2F)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Input based on selectedSourceType
            if (selectedSourceType == "Lokal") {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "File Audio di HP (MP3, WAV, M4A, OGG)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (localFileName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("File Terpilih:", style = MaterialTheme.typography.labelSmall)
                                        Text(localFileName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_pick_audio")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (localFileName.isBlank()) "Pilih File Audio dari HP" else "Ganti File Audio")
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tautan Video / Audio YouTube",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = sourceUri,
                            onValueChange = { sourceUri = it },
                            label = { Text("URL YouTube atau Web Audio") },
                            placeholder = { Text("https://youtu.be/... atau https://www.youtube.com/watch?v=...") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_youtube_url")
                        )
                        Text(
                            text = "Contoh: https://youtu.be/abcxyz atau URL file audio online langsung (.mp3)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Masteran *") },
                placeholder = { Text("Misal: Masteran Cililin Speed Rapat") },
                leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_masteran_title")
            )

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kategori Masteran", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            // Target Species (Universal by default)
            OutlinedTextField(
                value = targetSpecies,
                onValueChange = { targetSpecies = it },
                label = { Text("Target Burung (Universal / Spesifik)") },
                placeholder = { Text("Universal, Kenari, Murai Batu, Kacer, dll") },
                leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan / Waktu Pemasteran Ideal") },
                placeholder = { Text("Misal: Putar pagi jam 06.00 dan malam jam 19.00 saat dikerodong") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
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
            title = { Text("Hapus Audio Masteran?") },
            text = { Text("Data masteran ini akan dihapus secara permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMasteran(masteranId)
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
}
