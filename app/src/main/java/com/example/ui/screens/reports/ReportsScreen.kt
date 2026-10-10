package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DetailSectionCard
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val birds by viewModel.activeBirds.collectAsStateWithLifecycle()
    val cages by viewModel.activeCages.collectAsStateWithLifecycle()
    val eggs by viewModel.activeEggs.collectAsStateWithLifecycle()
    val chicks by viewModel.activeChicks.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val feedItems by viewModel.activeFeedItems.collectAsStateWithLifecycle()

    val totalBirds = birds.size
    val totalCapacity = cages.sumOf { it.plannedCapacity }
    val occupiedBirds = birds.count { it.cageId != null }

    val totalEggsClutched = eggs.sumOf { it.totalEggs }
    val totalHatched = eggs.sumOf { it.hatchedCount }
    val hatchRate = if (totalEggsClutched > 0) ((totalHatched.toDouble() / totalEggsClutched) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan & Statistik", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sebaran Jenis Burung
            item {
                DetailSectionCard(title = "Distribusi Jenis Burung ($totalBirds Ekor)") {
                    if (stats.speciesCounts.isEmpty()) {
                        Text("Belum ada data burung.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        stats.speciesCounts.forEach { sp ->
                            val pct = if (totalBirds > 0) (sp.count.toFloat() / totalBirds) else 0f
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(sp.typeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${sp.count} ekor (${(pct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Jenis Kelamin
            item {
                DetailSectionCard(title = "Demografi Kelamin") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        DemographicBadge("Jantan", stats.maleBirds, Color(0xFF1565C0))
                        DemographicBadge("Betina", stats.femaleBirds, Color(0xFFC2185B))
                        DemographicBadge("Belum Tahu", stats.unknownGenderBirds, Color(0xFF616161))
                    }
                }
            }

            // Pemanfaatan Kandang
            item {
                DetailSectionCard(title = "Pemanfaatan Kapasitas Kandang") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Kapasitas Terencana", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalCapacity Ekor", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Burung Berpenempatan", style = MaterialTheme.typography.bodyMedium)
                        Text("$occupiedBirds Ekor", fontWeight = FontWeight.Bold)
                    }
                    val occupancyPct = if (totalCapacity > 0) (occupiedBirds.toFloat() / totalCapacity).coerceAtMost(1f) else 0f
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { occupancyPct },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Hasil Penangkaran
            item {
                DetailSectionCard(title = "Performa Penangkaran & Penetasan") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pasangan Aktif", style = MaterialTheme.typography.bodyMedium)
                        Text("${stats.activePairs} Pasang", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Telur Dicatat", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalEggsClutched Butir", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Telur Berhasil Menetas", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalHatched Butir", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tingkat Keberhasilan Tetas", style = MaterialTheme.typography.bodyMedium)
                        Text("$hatchRate%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Anakan Dicatat", style = MaterialTheme.typography.bodyMedium)
                        Text("${chicks.size} Ekor", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Rekap Keuangan
            item {
                DetailSectionCard(title = "Rekap Finansial Keseluruhan") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Pemasukan", style = MaterialTheme.typography.bodyMedium)
                        Text(Formatters.formatRupiah(totalIncome), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Pengeluaran", style = MaterialTheme.typography.bodyMedium)
                        Text(Formatters.formatRupiah(totalExpense), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Selisih Kas / Saldo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val sal = totalIncome - totalExpense
                        Text(
                            Formatters.formatRupiah(sal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (sal >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DemographicBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
