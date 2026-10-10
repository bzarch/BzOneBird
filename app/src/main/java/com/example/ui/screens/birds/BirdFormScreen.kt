package com.example.ui.screens.birds

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.BirdEntity
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdFormScreen(
    birdId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeBirds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val birdTypes by viewModel.activeBirdTypes.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    val existingBird = remember(birdId, activeBirds) {
        if (birdId != null && birdId != 0L) activeBirds.find { it.id == birdId } else null
    }

    var name by remember(existingBird) { mutableStateOf(existingBird?.name ?: "") }
    var ringNumber by remember(existingBird) { mutableStateOf(existingBird?.ringNumber ?: "") }
    var typeName by remember(existingBird) { mutableStateOf(existingBird?.typeName ?: "") }
    var variety by remember(existingBird) { mutableStateOf(existingBird?.variety ?: "") }
    var colorMutation by remember(existingBird) { mutableStateOf(existingBird?.colorMutation ?: "") }
    var gender by remember(existingBird) { mutableStateOf(existingBird?.gender ?: "Belum Diketahui") }
    var birthDate by remember(existingBird) { mutableStateOf(existingBird?.birthDate) }
    var isBirthDateEstimated by remember(existingBird) { mutableStateOf(existingBird?.isBirthDateEstimated ?: false) }
    var origin by remember(existingBird) { mutableStateOf(existingBird?.origin ?: "Ternak Sendiri") }
    var acquisitionDate by remember(existingBird) { mutableStateOf(existingBird?.acquisitionDate ?: System.currentTimeMillis()) }
    var acquisitionCostStr by remember(existingBird) { mutableStateOf(existingBird?.acquisitionCost?.toLong()?.toString() ?: "") }
    var conditionOnAcquisition by remember(existingBird) { mutableStateOf(existingBird?.conditionOnAcquisition ?: "Sehat") }
    var selectedCageId by remember(existingBird) { mutableStateOf(existingBird?.cageId) }
    var selectedSireId by remember(existingBird) { mutableStateOf(existingBird?.sireId) }
    var selectedDamId by remember(existingBird) { mutableStateOf(existingBird?.damId) }
    var status by remember(existingBird) { mutableStateOf(existingBird?.status ?: "Aktif") }
    var notes by remember(existingBird) { mutableStateOf(existingBird?.notes ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var cageDropdownExpanded by remember { mutableStateOf(false) }

    val potentialSires = activeBirds.filter { it.gender == "Jantan" && it.id != birdId }
    val potentialDams = activeBirds.filter { it.gender == "Betina" && it.id != birdId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (birdId == null || birdId == 0L) "Tambah Burung Baru" else "Ubah Data Burung") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSaving) return@IconButton
                            if (typeName.isBlank()) {
                                viewModel.showMessage("Jenis burung tidak boleh kosong")
                                return@IconButton
                            }
                            val cost = acquisitionCostStr.toDoubleOrNull()
                            if (cost != null && cost < 0) {
                                viewModel.showMessage("Harga perolehan tidak boleh negatif")
                                return@IconButton
                            }

                            isSaving = true
                            val birdToSave = (existingBird ?: BirdEntity(typeName = typeName)).copy(
                                name = name.trim(),
                                ringNumber = ringNumber.trim(),
                                typeName = typeName.trim(),
                                variety = variety.trim(),
                                colorMutation = colorMutation.trim(),
                                gender = gender,
                                birthDate = birthDate,
                                isBirthDateEstimated = isBirthDateEstimated,
                                origin = origin,
                                acquisitionDate = acquisitionDate,
                                acquisitionCost = cost,
                                conditionOnAcquisition = conditionOnAcquisition.trim(),
                                cageId = selectedCageId,
                                sireId = selectedSireId,
                                damId = selectedDamId,
                                status = status,
                                notes = notes.trim()
                            )

                            val prevCage = existingBird?.cageId
                            val prevCageName = cages.find { it.id == prevCage }?.let { "${it.code} - ${it.name}" } ?: "-"
                            val newCageName = cages.find { it.id == selectedCageId }?.let { "${it.code} - ${it.name}" } ?: "Tanpa Kandang"

                            viewModel.saveBird(birdToSave, prevCage, prevCageName, newCageName)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("save_bird_button")
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
            // Identity
            Text("Identitas Burung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Burung (Opsional)") },
                modifier = Modifier.fillMaxWidth().testTag("bird_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = ringNumber,
                onValueChange = { ringNumber = it },
                label = { Text("Nomor / Kode Ring (Opsional)") },
                modifier = Modifier.fillMaxWidth().testTag("bird_ring_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = typeName,
                onValueChange = { typeName = it },
                label = { Text("Jenis Burung (Universal) *") },
                placeholder = { Text("Ketik jenis burung bebas (misal: Kenari, Murai, Finch, dll)") },
                modifier = Modifier.fillMaxWidth().testTag("bird_type_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Ras / Varietas") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = colorMutation,
                    onValueChange = { colorMutation = it },
                    label = { Text("Warna / Mutasi") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Gender
            Text("Jenis Kelamin", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Jantan", "Betina", "Belum Diketahui").forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g) }
                    )
                }
            }

            // Birth Date Picker
            Text("Tanggal Lahir / Umur", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance()
                        birthDate?.let { cal.timeInMillis = it }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance()
                                selectedCal.set(year, month, dayOfMonth)
                                birthDate = selectedCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (birthDate != null) Formatters.formatDate(birthDate) else "Pilih Tanggal Lahir",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (birthDate != null) {
                            Text(
                                text = "Umur: ${Formatters.calculateAge(birthDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isBirthDateEstimated,
                    onCheckedChange = { isBirthDateEstimated = it }
                )
                Text("Tanggal lahir merupakan perkiraan", style = MaterialTheme.typography.bodyMedium)
            }

            // Cage Placement
            Text("Penempatan Kandang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentCageName = cages.find { it.id == selectedCageId }?.let { "${it.code} - ${it.name}" } ?: "Belum Ditentukan"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { cageDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kandang: $currentCageName", style = MaterialTheme.typography.bodyLarge)
                        Text("Ubah", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(
                    expanded = cageDropdownExpanded,
                    onDismissRequest = { cageDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tanpa Kandang") },
                        onClick = {
                            selectedCageId = null
                            cageDropdownExpanded = false
                        }
                    )
                    cages.forEach { cg ->
                        DropdownMenuItem(
                            text = { Text("${cg.code} - ${cg.name} (${cg.type})") },
                            onClick = {
                                selectedCageId = cg.id
                                cageDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Acquisition & Origin
            Text("Asal & Perolehan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Text("Asal Burung", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Ternak Sendiri", "Beli", "Hadiah", "Adopsi").forEach { o ->
                    FilterChip(
                        selected = origin == o,
                        onClick = { origin = o },
                        label = { Text(o) }
                    )
                }
            }

            OutlinedTextField(
                value = acquisitionCostStr,
                onValueChange = { acquisitionCostStr = it },
                label = { Text("Harga Perolehan (Rp, Opsional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = conditionOnAcquisition,
                onValueChange = { conditionOnAcquisition = it },
                label = { Text("Kondisi Saat Diperoleh") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Status
            Text("Status Burung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Aktif", "Dikembangbiakkan", "Dijual", "Dipindahkan", "Mati").forEach { s ->
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
                label = { Text("Catatan Bebas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button
                    if (typeName.isBlank()) {
                        viewModel.showMessage("Jenis burung tidak boleh kosong")
                        return@Button
                    }
                    val cost = acquisitionCostStr.toDoubleOrNull()
                    if (cost != null && cost < 0) {
                        viewModel.showMessage("Harga perolehan tidak boleh negatif")
                        return@Button
                    }

                    isSaving = true
                    val birdToSave = (existingBird ?: BirdEntity(typeName = typeName)).copy(
                        name = name.trim(),
                        ringNumber = ringNumber.trim(),
                        typeName = typeName.trim(),
                        variety = variety.trim(),
                        colorMutation = colorMutation.trim(),
                        gender = gender,
                        birthDate = birthDate,
                        isBirthDateEstimated = isBirthDateEstimated,
                        origin = origin,
                        acquisitionDate = acquisitionDate,
                        acquisitionCost = cost,
                        conditionOnAcquisition = conditionOnAcquisition.trim(),
                        cageId = selectedCageId,
                        sireId = selectedSireId,
                        damId = selectedDamId,
                        status = status,
                        notes = notes.trim()
                    )

                    val prevCage = existingBird?.cageId
                    val prevCageName = cages.find { it.id == prevCage }?.let { "${it.code} - ${it.name}" } ?: "-"
                    val newCageName = cages.find { it.id == selectedCageId }?.let { "${it.code} - ${it.name}" } ?: "Tanpa Kandang"

                    viewModel.saveBird(birdToSave, prevCage, prevCageName, newCageName)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_bird_form_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (birdId == null || birdId == 0L) "Simpan Burung" else "Simpan Perubahan")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
