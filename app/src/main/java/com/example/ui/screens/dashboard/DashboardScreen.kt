package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeader
import com.example.util.Formatters
import com.example.viewmodel.BzOneViewModel
import com.example.viewmodel.RecentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BzOneViewModel,
    onNavigateToBirds: () -> Unit,
    onNavigateToCare: () -> Unit,
    onNavigateToBreeding: () -> Unit,
    onNavigateToCages: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onAddBird: () -> Unit,
    onAddCare: () -> Unit,
    onAddBreeding: () -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BzOne",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manajemen Burung & Penangkaran",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToReminders,
                        modifier = Modifier.testTag("dashboard_reminders_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (stats.nextReminder != null) {
                                    Badge { Text("1") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Pengingat")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBird,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Burung") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("dashboard_add_bird_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Quick Actions Horizontal Row
            item {
                Text(
                    text = "Aksi Cepat",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Pets,
                            label = "+ Burung",
                            onClick = onAddBird
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.EventAvailable,
                            label = "+ Perawatan",
                            onClick = onAddCare
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Favorite,
                            label = "+ Pasangan",
                            onClick = onAddBreeding
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "+ Transaksi",
                            onClick = onAddTransaction
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Next Reminder Banner (if any)
            item {
                stats.nextReminder?.let { rem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToReminders() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pengingat: ${rem.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "${Formatters.formatDateTime(rem.dateTime)} (${rem.repeatInterval})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Summary Section
            item {
                SectionHeader(title = "Ringkasan Koleksi & Penangkaran")
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Main Birds card
                    MetricStatCard(
                        title = "TOTAL BURUNG AKTIF",
                        value = "${stats.activeBirds} Ekor",
                        subtitle = "Jantan: ${stats.maleBirds} • Betina: ${stats.femaleBirds} • Lainnya: ${stats.unknownGenderBirds}",
                        icon = Icons.Default.Pets,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onNavigateToBirds
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                title = "KANDANG AKTIF",
                                value = "${stats.activeCages}",
                                subtitle = "Kandang terisi",
                                icon = Icons.Default.HomeWork,
                                onClick = onNavigateToCages
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                title = "PASANGAN TERNAK",
                                value = "${stats.activePairs}",
                                subtitle = "Pasangan aktif",
                                icon = Icons.Default.Favorite,
                                onClick = onNavigateToBreeding
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                title = "TELUR MENGERAM",
                                value = "${stats.incubatingEggs} Butir",
                                subtitle = "Dalam proses",
                                icon = Icons.Default.Egg,
                                onClick = onNavigateToBreeding
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                title = "ANAKAN DIRAWAT",
                                value = "${stats.activeChicks} Ekor",
                                subtitle = "Dalam pantauan",
                                icon = Icons.Default.ChildCare,
                                onClick = onNavigateToBreeding
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Financial Summary Card
            item {
                SectionHeader(
                    title = "Keuangan Bulan Ini",
                    actionText = "Detail Keuangan",
                    onActionClick = onNavigateToFinance
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToFinance() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saldo Bulan Ini",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val balanceColor = if (stats.thisMonthBalance >= 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                            Text(
                                text = Formatters.formatRupiah(stats.thisMonthBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = balanceColor
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Pemasukan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatRupiah(stats.thisMonthIncome),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Pengeluaran",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatRupiah(stats.thisMonthExpense),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Species breakdown horizontal chips
            if (stats.speciesCounts.isNotEmpty()) {
                item {
                    SectionHeader(title = "Sebaran Jenis Burung")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stats.speciesCounts) { sp ->
                            SuggestionChip(
                                onClick = onNavigateToBirds,
                                label = { Text("${sp.typeName}: ${sp.count}") },
                                icon = {
                                    Icon(
                                        Icons.Default.FilterVintage,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Recent Activities
            item {
                SectionHeader(title = "Aktivitas Terbaru")
            }

            if (recentActivities.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "Belum Ada Aktivitas",
                        description = "Tambahkan burung atau catat perawatan pertama Anda untuk melihat riwayat di sini.",
                        actionButtonText = "Tambah Burung",
                        onActionClick = onAddBird
                    )
                }
            } else {
                items(recentActivities, key = { it.id }) { act ->
                    ActivityItemRow(activity = act)
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivityItemRow(activity: RecentActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, color) = when (activity.category) {
            "BURUNG" -> Icons.Default.Pets to MaterialTheme.colorScheme.primary
            "PERAWATAN" -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.secondary
            "KESEHATAN" -> Icons.Default.Healing to MaterialTheme.colorScheme.tertiary
            "KEUANGAN" -> Icons.Default.Receipt to MaterialTheme.colorScheme.primary
            else -> Icons.Default.Info to MaterialTheme.colorScheme.outline
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = Formatters.formatDate(activity.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
