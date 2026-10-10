package com.example.ui.screens.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.data.local.entities.ReminderEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    onAddReminderClick: () -> Unit,
    onEditReminderClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.activeReminders.collectAsStateWithLifecycle()
    var reminderToArchive by remember { mutableStateOf<ReminderEntity?>(null) }
    var reminderToDelete by remember { mutableStateOf<ReminderEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengingat & Jadwal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onAddReminderClick, modifier = Modifier.testTag("add_reminder_top_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Pengingat")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminderClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengingat")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Default.Alarm,
                    title = "Belum Ada Pengingat",
                    description = "Pasang jadwal pengingat untuk pemberian pakan, jemur, periksa telur, atau kontrol kesehatan.",
                    actionButtonText = "+ Buat Pengingat",
                    onActionClick = onAddReminderClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { rem ->
                    ReminderCardItem(
                        reminder = rem,
                        onToggle = { viewModel.toggleReminderCompleted(rem.id, !rem.isCompleted) },
                        onEdit = { onEditReminderClick(rem.id) },
                        onArchive = { reminderToArchive = rem },
                        onDelete = { reminderToDelete = rem }
                    )
                }
            }
        }
    }

    reminderToArchive?.let { r ->
        ConfirmDialog(
            title = "Arsipkan Pengingat",
            message = "Pindahkan pengingat \"${r.title}\" ke Arsip?",
            confirmText = "Arsipkan",
            isDestructive = false,
            onConfirm = {
                viewModel.archiveReminder(r.id)
                reminderToArchive = null
            },
            onDismiss = { reminderToArchive = null }
        )
    }

    reminderToDelete?.let { r ->
        ConfirmDialog(
            title = "Hapus Pengingat Permanen",
            message = "Hapus pengingat ini secara permanen?",
            confirmText = "Hapus Permanen",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteReminderPermanently(r.id)
                reminderToDelete = null
            },
            onDismiss = { reminderToDelete = null }
        )
    }
}

@Composable
fun ReminderCardItem(
    reminder: ReminderEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (reminder.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${reminder.category} • Pengulangan: ${reminder.repeatInterval}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatDateTime(reminder.dateTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormScreen(
    reminderId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reminders by viewModel.activeReminders.collectAsStateWithLifecycle()
    val existing = remember(reminderId, reminders) {
        if (reminderId != null && reminderId != 0L) reminders.find { it.id == reminderId } else null
    }

    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var dateTime by remember(existing) { mutableStateOf(existing?.dateTime ?: System.currentTimeMillis()) }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Umum") }
    var repeatInterval by remember(existing) { mutableStateOf(existing?.repeatInterval ?: "Sekali") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reminderId == null || reminderId == 0L) "Buat Pengingat" else "Ubah Pengingat") },
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Pengingat * (Misal: Beri Kroto, Jemur Pagi)") },
                modifier = Modifier.fillMaxWidth().testTag("reminder_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Date & Time
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateTime }
                        DatePickerDialog(context, { _, y, m, d ->
                            cal.set(Calendar.YEAR, y)
                            cal.set(Calendar.MONTH, m)
                            cal.set(Calendar.DAY_OF_MONTH, d)
                            TimePickerDialog(context, { _, h, min ->
                                cal.set(Calendar.HOUR_OF_DAY, h)
                                cal.set(Calendar.MINUTE, min)
                                dateTime = cal.timeInMillis
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(Formatters.formatDateTime(dateTime), style = MaterialTheme.typography.bodyLarge)
                        Text("Ketuk untuk atur tanggal dan waktu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            Text("Kategori", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Pakan", "Kandang", "Jemur/Mandi", "Kesehatan", "Penetasan", "Umum").forEach { c ->
                    FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c, style = MaterialTheme.typography.labelSmall) })
                }
            }

            Text("Pengulangan", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Sekali", "Harian", "Mingguan", "Bulanan").forEach { r ->
                    FilterChip(selected = repeatInterval == r, onClick = { repeatInterval = r }, label = { Text(r) })
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Keterangan Tambahan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (title.isBlank()) {
                        viewModel.showMessage("Judul pengingat tidak boleh kosong")
                        return@Button
                    }
                    val toSave = (existing ?: ReminderEntity(title = title)).copy(
                        title = title.trim(),
                        dateTime = dateTime,
                        category = category,
                        repeatInterval = repeatInterval,
                        notes = notes.trim()
                    )
                    viewModel.saveReminder(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_reminder_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Pengingat")
            }
        }
    }
}
