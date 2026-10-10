package com.example.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val route: String,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        MoreMenuItem(
            title = "Kandang Burung",
            subtitle = "Manajemen kandang, kapasitas, dan lokasi",
            icon = Icons.Default.HomeWork,
            iconColor = MaterialTheme.colorScheme.primary,
            route = "cages",
            testTag = "menu_cages"
        ),
        MoreMenuItem(
            title = "Stok Pakan & Perlengkapan",
            subtitle = "Inventaris pakan, suplemen, dan peringatan batas minimum",
            icon = Icons.Default.Inventory2,
            iconColor = MaterialTheme.colorScheme.secondary,
            route = "inventory",
            testTag = "menu_inventory"
        ),
        MoreMenuItem(
            title = "Keuangan & Pembukuan",
            subtitle = "Pemasukan, pengeluaran, saldo, dan rekap transaksi",
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = MaterialTheme.colorScheme.tertiary,
            route = "finance",
            testTag = "menu_finance"
        ),
        MoreMenuItem(
            title = "Kesehatan Burung",
            subtitle = "Pencatatan gejala, riwayat perawatan, obat, dan dokter",
            icon = Icons.Default.Healing,
            iconColor = Color(0xFFD32F2F),
            route = "health",
            testTag = "menu_health"
        ),
        MoreMenuItem(
            title = "Pengingat & Jadwal",
            subtitle = "Jadwal pakan, jemur, kontrol kesehatan, dan penetasan",
            icon = Icons.Default.Alarm,
            iconColor = Color(0xFFF57C00),
            route = "reminders",
            testTag = "menu_reminders"
        ),
        MoreMenuItem(
            title = "Catatan Tambahan",
            subtitle = "Buku catatan harian dan catatan umum peternakan",
            icon = Icons.Default.NoteAlt,
            iconColor = Color(0xFF1976D2),
            route = "notes",
            testTag = "menu_notes"
        ),
        MoreMenuItem(
            title = "Kelola Jenis Burung",
            subtitle = "Daftar jenis burung dan kategori kustom",
            icon = Icons.Default.FilterVintage,
            iconColor = Color(0xFF388E3C),
            route = "bird_types",
            testTag = "menu_bird_types"
        ),
        MoreMenuItem(
            title = "Laporan & Statistik",
            subtitle = "Grafik dan sebaran data koleksi, jenis, & keuangan",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF7B1FA2),
            route = "reports",
            testTag = "menu_reports"
        ),
        MoreMenuItem(
            title = "Tempat Sampah / Arsip",
            subtitle = "Data yang diarsipkan dan opsi pemulihan data",
            icon = Icons.Default.DeleteOutline,
            iconColor = Color(0xFFC2185B),
            route = "archive",
            testTag = "menu_archive"
        ),
        MoreMenuItem(
            title = "Pengaturan & Cadangan",
            subtitle = "Tema, ekspor/impor JSON, CSV, dan info BzOne",
            icon = Icons.Default.Settings,
            iconColor = Color(0xFF455A64),
            route = "settings",
            testTag = "menu_settings"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Lainnya", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(item.route) }
                        .testTag(item.testTag),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.iconColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
