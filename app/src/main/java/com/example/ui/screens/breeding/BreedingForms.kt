package com.example.ui.screens.breeding

import android.app.DatePickerDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.BreedingPairEntity
import com.example.data.local.entities.ChickEntity
import com.example.data.local.entities.EggEntity
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingPairFormScreen(
    pairId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePairs by viewModel.activeBreedingPairs.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()

    val existing = remember(pairId, activePairs) {
        if (pairId != null && pairId != 0L) activePairs.find { it.id == pairId } else null
    }

    var code by remember(existing) { mutableStateOf(existing?.code ?: "P-01") }
    var maleBirdId by remember(existing) { mutableStateOf(existing?.maleBirdId) }
    var femaleBirdId by remember(existing) { mutableStateOf(existing?.femaleBirdId) }
    var cageId by remember(existing) { mutableStateOf(existing?.cageId) }
    var pairedDate by remember(existing) { mutableStateOf(existing?.pairedDate ?: System.currentTimeMillis()) }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "Aktif") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }

    var maleDropdownExpanded by remember { mutableStateOf(false) }
    var femaleDropdownExpanded by remember { mutableStateOf(false) }
    var cageDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val males = birds.filter { it.gender == "Jantan" }
    val females = birds.filter { it.gender == "Betina" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pairId == null || pairId == 0L) "Buat Pasangan Indukan" else "Ubah Pasangan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSaving) return@IconButton
                            if (code.isBlank()) {
                                viewModel.showMessage("Kode pasangan tidak boleh kosong")
                                return@IconButton
                            }
                            if (maleBirdId == null || femaleBirdId == null) {
                                viewModel.showMessage("Silakan pilih burung jantan dan burung betina")
                                return@IconButton
                            }
                            if (maleBirdId == femaleBirdId) {
                                viewModel.showMessage("Burung jantan dan betina tidak boleh sama!")
                                return@IconButton
                            }

                            isSaving = true
                            val toSave = (existing ?: BreedingPairEntity(
                                code = code,
                                maleBirdId = maleBirdId!!,
                                femaleBirdId = femaleBirdId!!
                            )).copy(
                                code = code.trim(),
                                maleBirdId = maleBirdId!!,
                                femaleBirdId = femaleBirdId!!,
                                cageId = cageId,
                                pairedDate = pairedDate,
                                status = status,
                                notes = notes.trim()
                            )
                            viewModel.saveBreedingPair(toSave)
                            onNavigateBack()
                        }
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
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Kode Pasangan * (Misal: P-01, JODOH-MB01)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Select Male
            Text("Burung Jantan (♂)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentMale = birds.find { it.id == maleBirdId }?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Pilih Burung Jantan"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { maleDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentMale)
                        Text("Pilih", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = maleDropdownExpanded, onDismissRequest = { maleDropdownExpanded = false }) {
                    if (males.isEmpty()) {
                        DropdownMenuItem(text = { Text("Tidak ada burung jantan tersedia") }, onClick = { maleDropdownExpanded = false })
                    } else {
                        males.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name.ifBlank { m.ringNumber }} (${m.typeName})") },
                                onClick = { maleBirdId = m.id; maleDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Select Female
            Text("Burung Betina (♀)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentFemale = birds.find { it.id == femaleBirdId }?.let { "${it.name.ifBlank { it.ringNumber }} (${it.typeName})" } ?: "Pilih Burung Betina"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { femaleDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentFemale)
                        Text("Pilih", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = femaleDropdownExpanded, onDismissRequest = { femaleDropdownExpanded = false }) {
                    if (females.isEmpty()) {
                        DropdownMenuItem(text = { Text("Tidak ada burung betina tersedia") }, onClick = { femaleDropdownExpanded = false })
                    } else {
                        females.forEach { f ->
                            DropdownMenuItem(
                                text = { Text("${f.name.ifBlank { f.ringNumber }} (${f.typeName})") },
                                onClick = { femaleBirdId = f.id; femaleDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Cage
            Text("Kandang Ternak", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentCage = cages.find { it.id == cageId }?.let { "${it.code} - ${it.name}" } ?: "Tanpa Kandang"
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
                    DropdownMenuItem(text = { Text("Tanpa Kandang") }, onClick = { cageId = null; cageDropdownExpanded = false })
                    cages.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.code} - ${c.name}") },
                            onClick = { cageId = c.id; cageDropdownExpanded = false }
                        )
                    }
                }
            }

            // Status
            Text("Status Pasangan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Aktif", "Selesai", "Dipisah", "Gagal").forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Penjodohan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button
                    if (code.isBlank() || maleBirdId == null || femaleBirdId == null) {
                        viewModel.showMessage("Lengkapi kode dan pilih indukan jantan & betina")
                        return@Button
                    }
                    if (maleBirdId == femaleBirdId) {
                        viewModel.showMessage("Indukan jantan dan betina tidak boleh sama")
                        return@Button
                    }
                    isSaving = true
                    val toSave = (existing ?: BreedingPairEntity(code = code, maleBirdId = maleBirdId!!, femaleBirdId = femaleBirdId!!)).copy(
                        code = code.trim(),
                        maleBirdId = maleBirdId!!,
                        femaleBirdId = femaleBirdId!!,
                        cageId = cageId,
                        pairedDate = pairedDate,
                        status = status,
                        notes = notes.trim()
                    )
                    viewModel.saveBreedingPair(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Pasangan")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggFormScreen(
    eggId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val eggs by viewModel.activeEggs.collectAsStateWithLifecycle()
    val pairs by viewModel.activeBreedingPairs.collectAsStateWithLifecycle()

    val existing = remember(eggId, eggs) {
        if (eggId != null && eggId != 0L) eggs.find { it.id == eggId } else null
    }

    var selectedPairId by remember(existing) { mutableStateOf(existing?.pairId ?: pairs.firstOrNull()?.id ?: 0L) }
    var clutchDate by remember(existing) { mutableStateOf(existing?.clutchDate ?: System.currentTimeMillis()) }
    var totalEggsStr by remember(existing) { mutableStateOf(existing?.totalEggs?.toString() ?: "3") }
    var fertileStr by remember(existing) { mutableStateOf(existing?.fertileCount?.toString() ?: "0") }
    var infertileStr by remember(existing) { mutableStateOf(existing?.infertileCount?.toString() ?: "0") }
    var hatchedStr by remember(existing) { mutableStateOf(existing?.hatchedCount?.toString() ?: "0") }
    var brokenStr by remember(existing) { mutableStateOf(existing?.brokenCount?.toString() ?: "0") }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "Pengeraman") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }

    var pairDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eggId == null || eggId == 0L) "Catat Telur Baru" else "Ubah Catatan Telur") },
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
            // Pair selector
            Text("Pilih Pasangan Indukan *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentPair = pairs.find { it.id == selectedPairId }?.code ?: "Pilih Pasangan"
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { pairDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currentPair)
                        Text("Pilih", color = MaterialTheme.colorScheme.primary)
                    }
                }
                DropdownMenu(expanded = pairDropdownExpanded, onDismissRequest = { pairDropdownExpanded = false }) {
                    pairs.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.code) },
                            onClick = { selectedPairId = p.id; pairDropdownExpanded = false }
                        )
                    }
                }
            }

            // Date
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = clutchDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            val s = Calendar.getInstance().apply { set(y, m, d) }
                            clutchDate = s.timeInMillis
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal Bertelur: ${Formatters.formatDate(clutchDate)}")
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = totalEggsStr,
                    onValueChange = { totalEggsStr = it },
                    label = { Text("Total Telur") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = hatchedStr,
                    onValueChange = { hatchedStr = it },
                    label = { Text("Menetas") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fertileStr,
                    onValueChange = { fertileStr = it },
                    label = { Text("Fertil") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = infertileStr,
                    onValueChange = { infertileStr = it },
                    label = { Text("Infertil") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("Status Pengeraman", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pengeraman", "Sebagian Menetas", "Selesai Menetas", "Gagal").forEach { s ->
                    FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) })
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Telur") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (selectedPairId == 0L) {
                        viewModel.showMessage("Silakan buat atau pilih pasangan terlebih dahulu")
                        return@Button
                    }
                    val tot = totalEggsStr.toIntOrNull() ?: 0
                    val hatch = hatchedStr.toIntOrNull() ?: 0
                    val fer = fertileStr.toIntOrNull() ?: 0
                    val infer = infertileStr.toIntOrNull() ?: 0
                    val brk = brokenStr.toIntOrNull() ?: 0

                    val toSave = (existing ?: EggEntity(pairId = selectedPairId)).copy(
                        pairId = selectedPairId,
                        clutchDate = clutchDate,
                        totalEggs = tot,
                        hatchedCount = hatch,
                        fertileCount = fer,
                        infertileCount = infer,
                        brokenCount = brk,
                        status = status,
                        notes = notes.trim()
                    )
                    viewModel.saveEgg(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Catatan Telur")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChickFormScreen(
    chickId: Long?,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chicks by viewModel.activeChicks.collectAsStateWithLifecycle()
    val pairs by viewModel.activeBreedingPairs.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()

    val existing = remember(chickId, chicks) {
        if (chickId != null && chickId != 0L) chicks.find { it.id == chickId } else null
    }

    var ringNumber by remember(existing) { mutableStateOf(existing?.ringNumber ?: "") }
    var typeName by remember(existing) { mutableStateOf(existing?.typeName ?: "") }
    var gender by remember(existing) { mutableStateOf(existing?.gender ?: "Belum Diketahui") }
    var hatchDate by remember(existing) { mutableStateOf(existing?.hatchDate ?: System.currentTimeMillis()) }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "Dirawat") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var selectedSireId by remember(existing) { mutableStateOf(existing?.sireId) }
    var selectedDamId by remember(existing) { mutableStateOf(existing?.damId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (chickId == null || chickId == 0L) "Catat Anakan Baru" else "Ubah Data Anakan") },
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
                value = ringNumber,
                onValueChange = { ringNumber = it },
                label = { Text("Nomor Ring Anakan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = typeName,
                onValueChange = { typeName = it },
                label = { Text("Jenis Burung (Universal) *") },
                placeholder = { Text("Ketik jenis burung bebas...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = hatchDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            val s = Calendar.getInstance().apply { set(y, m, d) }
                            hatchDate = s.timeInMillis
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal Menetas: ${Formatters.formatDate(hatchDate)}")
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            Text("Status Anakan", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Dirawat", "Mandiri", "Dipindahkan", "Mati").forEach { s ->
                    FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) })
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Perkembangan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val toSave = (existing ?: ChickEntity()).copy(
                        ringNumber = ringNumber.trim(),
                        typeName = typeName.trim(),
                        gender = gender,
                        hatchDate = hatchDate,
                        status = status,
                        notes = notes.trim(),
                        sireId = selectedSireId,
                        damId = selectedDamId
                    )
                    viewModel.saveChick(toSave)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Anakan")
            }
        }
    }
}
