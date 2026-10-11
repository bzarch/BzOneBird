package com.example.ui.screens.masteran

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.MasteranAudioEntity
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasteranScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val masteranList by viewModel.filteredMasteranList.collectAsStateWithLifecycle()
    val allMasteranList by viewModel.masteranList.collectAsStateWithLifecycle()
    val favoriteList by viewModel.favoriteMasteranList.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedMasteranCategory.collectAsStateWithLifecycle()

    val currentlyPlaying by viewModel.currentlyPlayingAudio.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val progressMs by viewModel.audioProgressMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioDurationMs.collectAsStateWithLifecycle()
    val isLooping by viewModel.isAudioLooping.collectAsStateWithLifecycle()
    val sleepTimer by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<MasteranAudioEntity?>(null) }

    val categories = listOf("Semua", "Tembakan", "Ngeroll", "Isian Mewah", "Terapi Suara Air", "Pancingan Bunyi", "Campuran")

    val displayedList = remember(masteranList, favoriteList, showOnlyFavorites) {
        if (showOnlyFavorites) favoriteList else masteranList
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masteran Burung", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_masteran")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        modifier = Modifier.testTag("btn_toggle_fav_filter")
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Filter Favorit",
                            tint = if (showOnlyFavorites) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showTimerDialog = true },
                        modifier = Modifier.testTag("btn_sleep_timer")
                    ) {
                        BadgedBox(
                            badge = {
                                if (sleepTimer > 0) {
                                    Badge { Text("${sleepTimer}m") }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Timer Otomatis Mati",
                                tint = if (sleepTimer > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToForm(0) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Masteran") },
                modifier = Modifier.testTag("fab_add_masteran")
            )
        },
        bottomBar = {
            // Player Bar if an audio track is active
            AnimatedVisibility(visible = currentlyPlaying != null) {
                currentlyPlaying?.let { track ->
                    Surface(
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (track.sourceType == "YouTube") Color(0xFFFF0000).copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (track.sourceType == "YouTube") Icons.Default.SmartDisplay else Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = if (track.sourceType == "YouTube") Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.category} • ${track.sourceType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Loop button
                                IconButton(onClick = { viewModel.toggleAudioLoop() }) {
                                    Icon(
                                        Icons.Default.Repeat,
                                        contentDescription = "Loop Ulang",
                                        tint = if (isLooping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                // Play / Pause button
                                FilledIconButton(
                                    onClick = {
                                        if (track.sourceType == "YouTube" && !track.sourceUri.endsWith(".mp3", true)) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(track.sourceUri))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                viewModel.showMessage("Gagal membuka tautan YouTube")
                                            }
                                        } else {
                                            viewModel.playMasteran(track, context)
                                        }
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play"
                                    )
                                }

                                // Stop button
                                IconButton(onClick = { viewModel.stopAudio() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Tutup Player")
                                }
                            }

                            if (track.sourceType == "Lokal" || track.sourceUri.endsWith(".mp3", true)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatTimeMs(progressMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Slider(
                                        value = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                                        onValueChange = { ratio ->
                                            if (durationMs > 0) {
                                                viewModel.seekAudio((ratio * durationMs).toInt())
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                    )
                                    Text(
                                        text = formatTimeMs(durationMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tautan YouTube siap diputar di aplikasi/web YouTube",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(track.sourceUri))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                viewModel.showMessage("Gagal membuka YouTube")
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("Buka YouTube")
                                    }
                                }
                            }
                        }
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
        ) {
            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = if (cat == "Semua") selectedCategory.isNullOrBlank() else selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.selectedMasteranCategory.value = if (cat == "Semua") null else cat
                        },
                        label = { Text(cat) }
                    )
                }
            }

            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = if (showOnlyFavorites) "Belum ada masteran favorit" else "Belum Ada Masteran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (showOnlyFavorites) "Tandai ikon hati pada audio masteran untuk memasukkannya ke favorit."
                            else "Tambahkan audio masteran dari penyimpanan lokal HP atau tautan YouTube untuk melatih kicauan burung Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Button(
                            onClick = { onNavigateToForm(0) },
                            modifier = Modifier.testTag("btn_empty_add_masteran")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tambah Masteran Pertama")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Total ${allMasteranList.size} masteran tersedia. Mendukung file audio lokal (MP3/WAV) dan link YouTube.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    items(displayedList, key = { it.id }) { track ->
                        val isCurrentPlaying = currentlyPlaying?.id == track.id
                        MasteranTrackCard(
                            track = track,
                            isPlaying = isCurrentPlaying && isPlaying,
                            isCurrentTrack = isCurrentPlaying,
                            onPlayClick = {
                                if (track.sourceType == "YouTube" && !track.sourceUri.endsWith(".mp3", true)) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(track.sourceUri))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Gagal membuka YouTube")
                                    }
                                } else {
                                    viewModel.playMasteran(track, context)
                                }
                            },
                            onFavoriteClick = { viewModel.toggleMasteranFavorite(track) },
                            onEditClick = { onNavigateToForm(track.id) },
                            onDeleteClick = { itemToDelete = track }
                        )
                    }
                }
            }
        }
    }

    // Timer Dialog
    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Timer Pemasteran Cerdas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih waktu pemasteran otomatis berhenti:")
                    val options = listOf(
                        0 to "Matikan Timer",
                        15 to "15 Menit",
                        30 to "30 Menit",
                        45 to "45 Menit",
                        60 to "60 Menit (1 Jam)",
                        120 to "120 Menit (2 Jam)"
                    )
                    options.forEach { (mins, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setSleepTimer(mins)
                                    showTimerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sleepTimer == mins,
                                onClick = {
                                    viewModel.setSleepTimer(mins)
                                    showTimerDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = if (sleepTimer == mins) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Audio Masteran?") },
            text = { Text("Audio masteran \"${track.title}\" akan dihapus secara permanen dari daftar.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMasteran(track.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun MasteranTrackCard(
    track: MasteranAudioEntity,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentTrack) 3.dp else 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause Circle Button
            FilledIconButton(
                onClick = onPlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge Source
                    val isYT = track.sourceType == "YouTube"
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isYT) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = if (isYT) "YouTube" else "Lokal",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isYT) Color(0xFFC62828) else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = track.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (track.playCount > 0) {
                        Text(
                            text = "• ${track.playCount}x putar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (track.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Favorite Icon
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = if (track.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }

            // 3-dots Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Masteran") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Masteran", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

private fun formatTimeMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
