package com.example.ui.screens.birdtypes

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
import com.example.data.local.entities.BirdTypeEntity
import com.example.ui.components.ConfirmDialog
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdTypesScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val types by viewModel.activeBirdTypes.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var typeToEdit by remember { mutableStateOf<BirdTypeEntity?>(null) }
    var typeToDelete by remember { mutableStateOf<BirdTypeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Jenis Burung", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("add_bird_type_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Jenis")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jenis")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(types, key = { it.id }) { type ->
                val birdCount = birds.count { it.typeName.equals(type.name, ignoreCase = true) }
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
                            Text(type.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (type.description.isNotBlank()) {
                                Text(type.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Digunakan oleh: $birdCount ekor burung", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { typeToEdit = type }) {
                            Icon(Icons.Default.Edit, contentDescription = "Ubah")
                        }
                        IconButton(onClick = { typeToDelete = type }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || typeToEdit != null) {
        val editing = typeToEdit
        var nameInput by remember { mutableStateOf(editing?.name ?: "") }
        var descInput by remember { mutableStateOf(editing?.description ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                typeToEdit = null
            },
            title = { Text(if (editing == null) "Tambah Jenis Burung" else "Ubah Jenis Burung") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Jenis Burung *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Keterangan / Nama Latin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isBlank()) {
                            viewModel.showMessage("Nama jenis burung tidak boleh kosong")
                            return@Button
                        }
                        viewModel.saveBirdType(
                            (editing ?: BirdTypeEntity(name = nameInput)).copy(
                                name = nameInput.trim(),
                                description = descInput.trim()
                            )
                        )
                        showAddDialog = false
                        typeToEdit = null
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddDialog = false
                    typeToEdit = null
                }) {
                    Text("Batal")
                }
            }
        )
    }

    typeToDelete?.let { t ->
        val count = birds.count { it.typeName.equals(t.name, ignoreCase = true) }
        ConfirmDialog(
            title = "Hapus Jenis Burung",
            message = if (count > 0) "Jenis \"${t.name}\" masih digunakan oleh $count burung aktif. Menghapusnya akan memindahkan jenis ini ke arsip agar data burung tetap utuh." else "Hapus \"${t.name}\" secara permanen?",
            confirmText = "Hapus",
            isDestructive = true,
            onConfirm = {
                if (count > 0) {
                    viewModel.archiveBirdType(t.id)
                } else {
                    viewModel.deleteBirdTypePermanently(t.id)
                }
                typeToDelete = null
            },
            onDismiss = { typeToDelete = null }
        )
    }
}
