package com.example.ui.screens.genetics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class GeneticInheritanceMode(val label: String, val description: String) {
    SEX_LINKED("Sex-Linked (Terikat Kelamin)", "Gen menempel pada kromosom Z/X (Lutino, Cinnamon, Opaline, Pastel dll)"),
    RECESSIVE("Resesif Autosomal", "Perlu sepasang gen untuk muncul visual (Albino, Fallow, Blue, dll)"),
    DOMINANT("Dominan Autosomal", "Cukup satu gen sudah tampak visual (Dominant Pied, Spangle, Crest, dll)"),
    CO_DOMINANT("Co-Dominan / Multi-Gen", "Perpaduan kedua warna/sifat muncul bersamaan")
}

data class GeneticsResult(
    val maleOffspringText: List<String>,
    val femaleOffspringText: List<String>,
    val summaryRatio: String,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneticsCalculatorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(GeneticInheritanceMode.SEX_LINKED) }
    var mutationName by remember { mutableStateOf("Lutino") }
    var maleGenotype by remember { mutableStateOf("Visual Mutasi") } // "Visual Mutasi", "Split (Carrier)", "Normal"
    var femaleGenotype by remember { mutableStateOf("Normal") } // "Visual Mutasi", "Split (Carrier)", "Normal"

    val calculationResult = remember(selectedMode, maleGenotype, femaleGenotype, mutationName) {
        calculateGenetics(selectedMode, maleGenotype, femaleGenotype, mutationName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalkulator Genetika", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_genetics")) {
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
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Biotech,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Simulasi Persilangan Genetik",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prediksi probabilitas warna & sifat anakan jantan vs betina (Hukum Mendel universal).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Mode Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pola Pewarisan Genetik", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                GeneticInheritanceMode.values().forEach { mode ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedMode == mode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (selectedMode == mode) 2.dp else 0.dp,
                        border = if (selectedMode == mode) null else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = {
                                    selectedMode = mode
                                    if (mode == GeneticInheritanceMode.SEX_LINKED && mutationName == "Albino") {
                                        mutationName = "Lutino"
                                    } else if (mode == GeneticInheritanceMode.RECESSIVE && mutationName == "Lutino") {
                                        mutationName = "Albino"
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(mode.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            // Mutation Name Field
            OutlinedTextField(
                value = mutationName,
                onValueChange = { mutationName = it },
                label = { Text("Nama Mutasi / Warna yang Dianalisis") },
                placeholder = { Text("Misal: Lutino, Albino, Cinnamon, Opaline") },
                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Sire (Pejantan) Setup Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Male, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Induk Jantan (Sire)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    val maleOptions = if (selectedMode == GeneticInheritanceMode.DOMINANT) {
                        listOf("Visual Mutasi (Double Factor)", "Visual Mutasi (Single Factor)", "Normal")
                    } else {
                        listOf("Visual Mutasi", "Split / Carrier (Pembawa Gen)", "Normal")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        maleOptions.forEach { opt ->
                            FilterChip(
                                selected = maleGenotype == opt,
                                onClick = { maleGenotype = opt },
                                label = { Text(opt.substringBefore(" ("), maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Dam (Betina) Setup Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Female, contentDescription = null, tint = Color(0xFFD81B60))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Induk Betina (Dam)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    val femaleOptions = when (selectedMode) {
                        GeneticInheritanceMode.SEX_LINKED -> listOf("Visual Mutasi", "Normal") // Betina burung tidak bisa split sex-linked
                        GeneticInheritanceMode.DOMINANT -> listOf("Visual Mutasi", "Normal")
                        else -> listOf("Visual Mutasi", "Split / Carrier", "Normal")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        femaleOptions.forEach { opt ->
                            FilterChip(
                                selected = femaleGenotype == opt,
                                onClick = { femaleGenotype = opt },
                                label = { Text(opt.substringBefore(" ("), maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (selectedMode == GeneticInheritanceMode.SEX_LINKED) {
                        Text(
                            text = "💡 Catatan ilmiah: Burung betina (ZW) hanya memiliki 1 kromosom Z, sehingga betina TIDAK PERNAH bisa menjadi split/carrier untuk gen sex-linked. Betina hanya bisa Visual atau Normal murni.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Results Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hasil Prediksi Anakan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Rasio Global: ${calculationResult.summaryRatio}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    // Male Offspring Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Male, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Anakan Jantan (1.0):", fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            calculationResult.maleOffspringText.forEach { item ->
                                Text("• $item", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1565C0))
                            }
                        }
                    }

                    // Female Offspring Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFCE4EC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Female, contentDescription = null, tint = Color(0xFFD81B60), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Anakan Betina (0.1):", fontWeight = FontWeight.Bold, color = Color(0xFF880E4F))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            calculationResult.femaleOffspringText.forEach { item ->
                                Text("• $item", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFAD1457))
                            }
                        }
                    }

                    // Scientific explanation & breeder advice
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("💡 Panduan Pemuliaan (Breeder Advice):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(calculationResult.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun calculateGenetics(
    mode: GeneticInheritanceMode,
    male: String,
    female: String,
    mutation: String
): GeneticsResult {
    val m = if (mutation.isBlank()) "Mutasi" else mutation

    return when (mode) {
        GeneticInheritanceMode.SEX_LINKED -> {
            when {
                // Jantan Visual x Betina Visual -> 100% Visual Jantan & Betina
                male.startsWith("Visual") && female.startsWith("Visual") -> GeneticsResult(
                    maleOffspringText = listOf("100% Visual $m"),
                    femaleOffspringText = listOf("100% Visual $m"),
                    summaryRatio = "100% Visual $m (Semua Anakan)",
                    explanation = "Persilangan ideal jika ingin mencetak 100% anakan visual $m baik jantan maupun betina."
                )
                // Jantan Visual x Betina Normal -> 100% Betina Visual, 100% Jantan Split
                male.startsWith("Visual") && female.startsWith("Normal") -> GeneticsResult(
                    maleOffspringText = listOf("100% Normal Split (Carrier) $m"),
                    femaleOffspringText = listOf("100% Visual $m"),
                    summaryRatio = "50% Visual $m (Hanya Betina), 50% Split $m (Jantan)",
                    explanation = "Sifat 'Auto-Sexing': Semua anakan yang tampak visual $m DIPASTIKAN berjenis kelamin betina! Sedangkan anakan jantan membawa gen split."
                )
                // Jantan Split x Betina Visual -> 50% Visual, 50% Split
                male.startsWith("Split") && female.startsWith("Visual") -> GeneticsResult(
                    maleOffspringText = listOf("50% Visual $m", "50% Normal Split $m"),
                    femaleOffspringText = listOf("50% Visual $m", "50% Normal Murni"),
                    summaryRatio = "50% Visual $m, 25% Split $m, 25% Normal",
                    explanation = "Mencetak visual baik jantan maupun betina. Anakan betina yang tidak visual adalah normal murni (bukan split)."
                )
                // Jantan Split x Betina Normal -> 25% Betina Visual, 25% Jantan Split
                male.startsWith("Split") && female.startsWith("Normal") -> GeneticsResult(
                    maleOffspringText = listOf("50% Normal Split $m", "50% Normal Murni"),
                    femaleOffspringText = listOf("50% Visual $m", "50% Normal Murni"),
                    summaryRatio = "25% Visual (Betina), 25% Split (Jantan), 50% Normal",
                    explanation = "Satu-satunya anakan visual yang keluar dari persilangan ini adalah BETINA. Anakan jantan tampak normal tetapi 50% di antaranya membawa split."
                )
                // Jantan Normal x Betina Visual -> 100% Jantan Split, 100% Betina Normal
                male.startsWith("Normal") && female.startsWith("Visual") -> GeneticsResult(
                    maleOffspringText = listOf("100% Normal Split $m"),
                    femaleOffspringText = listOf("100% Normal Murni"),
                    summaryRatio = "0% Visual, 50% Split $m (Semua Jantan), 50% Normal (Semua Betina)",
                    explanation = "Tidak menghasilkan visual sama sekali. Namun seluruh anakan jantan dijamin 100% split $m dan siap dipakai untuk generasi berikutnya."
                )
                else -> GeneticsResult(
                    maleOffspringText = listOf("100% Normal"),
                    femaleOffspringText = listOf("100% Normal"),
                    summaryRatio = "100% Normal",
                    explanation = "Kedua indukan normal, anakan 100% normal tanpa mutasi."
                )
            }
        }
        GeneticInheritanceMode.RECESSIVE -> {
            when {
                // Visual x Visual -> 100% Visual
                male.startsWith("Visual") && female.startsWith("Visual") -> GeneticsResult(
                    maleOffspringText = listOf("100% Visual $m"),
                    femaleOffspringText = listOf("100% Visual $m"),
                    summaryRatio = "100% Visual $m",
                    explanation = "Kedua indukan homozigot resesif. Seluruh keturunan pasti visual $m."
                )
                // Visual x Split -> 50% Visual, 50% Split
                (male.startsWith("Visual") && female.startsWith("Split")) || (male.startsWith("Split") && female.startsWith("Visual")) -> GeneticsResult(
                    maleOffspringText = listOf("50% Visual $m", "50% Normal Split $m"),
                    femaleOffspringText = listOf("50% Visual $m", "50% Normal Split $m"),
                    summaryRatio = "50% Visual $m, 50% Split $m",
                    explanation = "Peluang 50% anakan visual, dan 50% anakan split. Tidak ada yang normal murni."
                )
                // Visual x Normal -> 100% Split
                (male.startsWith("Visual") && female.startsWith("Normal")) || (male.startsWith("Normal") && female.startsWith("Visual")) -> GeneticsResult(
                    maleOffspringText = listOf("100% Normal Split $m"),
                    femaleOffspringText = listOf("100% Normal Split $m"),
                    summaryRatio = "100% Normal Split $m",
                    explanation = "Seluruh anakan (100%) berpenampilan normal tetapi merupakan split/carrier $m."
                )
                // Split x Split -> 25% Visual, 50% Split, 25% Normal
                male.startsWith("Split") && female.startsWith("Split") -> GeneticsResult(
                    maleOffspringText = listOf("25% Visual $m", "50% Normal Split $m", "25% Normal"),
                    femaleOffspringText = listOf("25% Visual $m", "50% Normal Split $m", "25% Normal"),
                    summaryRatio = "25% Visual $m, 50% Split $m, 25% Normal Murni",
                    explanation = "Peluang klasik Mendel (1:2:1). Burung normal memiliki peluang 66% sebagai kemungkinan pembawa gen (possible split)."
                )
                // Split x Normal -> 50% Split, 50% Normal
                else -> GeneticsResult(
                    maleOffspringText = listOf("50% Possible Split $m", "50% Normal"),
                    femaleOffspringText = listOf("50% Possible Split $m", "50% Normal"),
                    summaryRatio = "0% Visual, 50% Possible Split $m, 50% Normal",
                    explanation = "Tidak menghasilkan visual. Separuh anakan membawa gen split (possible split), namun sulit dibedakan secara kasat mata."
                )
            }
        }
        GeneticInheritanceMode.DOMINANT -> {
            when {
                male.startsWith("Visual (Double") || female.startsWith("Visual (Double") -> GeneticsResult(
                    maleOffspringText = listOf("100% Visual $m (Single Factor)"),
                    femaleOffspringText = listOf("100% Visual $m (Single Factor)"),
                    summaryRatio = "100% Visual $m",
                    explanation = "Double Factor (homozigot dominan) menjamin 100% anakan tampak visual."
                )
                male.startsWith("Visual") && female.startsWith("Visual") -> GeneticsResult(
                    maleOffspringText = listOf("25% Double Factor $m", "50% Single Factor $m", "25% Normal"),
                    femaleOffspringText = listOf("25% Double Factor $m", "50% Single Factor $m", "25% Normal"),
                    summaryRatio = "75% Visual $m, 25% Normal",
                    explanation = "75% anakan akan tampak visual $m, dan 25% normal murni."
                )
                male.startsWith("Visual") || female.startsWith("Visual") -> GeneticsResult(
                    maleOffspringText = listOf("50% Visual $m (Single Factor)", "50% Normal"),
                    femaleOffspringText = listOf("50% Visual $m (Single Factor)", "50% Normal"),
                    summaryRatio = "50% Visual $m, 50% Normal",
                    explanation = "Gen dominan langsung muncul visual pada 50% anakan saat disilangkan dengan normal."
                )
                else -> GeneticsResult(
                    maleOffspringText = listOf("100% Normal"),
                    femaleOffspringText = listOf("100% Normal"),
                    summaryRatio = "100% Normal",
                    explanation = "Kedua indukan normal, anakan 100% normal."
                )
            }
        }
        GeneticInheritanceMode.CO_DOMINANT -> {
            GeneticsResult(
                maleOffspringText = listOf("50% Kombinasi Kedua Sifat", "25% Sifat Induk Jantan", "25% Sifat Induk Betina"),
                femaleOffspringText = listOf("50% Kombinasi Kedua Sifat", "25% Sifat Induk Jantan", "25% Sifat Induk Betina"),
                summaryRatio = "50% Corak Campuran / Intermediate",
                explanation = "Pada co-dominan, kedua gen diekspresikan secara bersamaan menciptakan corak baru (misal: Pied dominan bercampur warna dasar)."
            )
        }
    }
}
