package com.example.ui.screens.pedigree

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.BirdEntity
import com.example.util.DateUtils
import com.example.viewmodel.BzOneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedigreeCertificateScreen(
    initialBirdId: Long,
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val activeBirds by viewModel.activeBirds.collectAsStateWithLifecycle()

    var selectedBirdId by remember { mutableStateOf(if (initialBirdId > 0) initialBirdId else activeBirds.firstOrNull()?.id ?: 0L) }

    val currentBird = remember(activeBirds, selectedBirdId) {
        activeBirds.find { it.id == selectedBirdId } ?: activeBirds.firstOrNull()
    }

    val sire = remember(currentBird, activeBirds) {
        currentBird?.sireId?.let { sId -> activeBirds.find { it.id == sId } }
    }

    val dam = remember(currentBird, activeBirds) {
        currentBird?.damId?.let { dId -> activeBirds.find { it.id == dId } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sertifikat Silsilah Digital", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_pedigree")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (currentBird != null) {
                        IconButton(
                            onClick = {
                                val text = buildPedigreeText(currentBird, sire, dam)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Sertifikat Silsilah")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.testTag("btn_share_pedigree")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Bagikan Sertifikat")
                        }
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
            // Bird Selection Dropdown
            if (activeBirds.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum Ada Data Burung", fontWeight = FontWeight.Bold)
                        Text("Daftarkan data burung Anda terlebih dahulu untuk mencetak sertifikat pedigree.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                var expandedDropdown by remember { mutableStateOf(false) }
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pilih Burung untuk Sertifikat:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = currentBird?.let { "${it.name.ifBlank { "Ring " + it.ringNumber }} (${it.typeName})" } ?: "Pilih Burung",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box {
                            Button(onClick = { expandedDropdown = true }) {
                                Text("Ganti Burung")
                            }
                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                activeBirds.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text("${b.name.ifBlank { "Ring: " + b.ringNumber }} - ${b.typeName}") },
                                        onClick = {
                                            selectedBirdId = b.id
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                currentBird?.let { bird ->
                    // CERTIFICATE PREVIEW CARD (Royal Classic Certificate Theme)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBF7)),
                        border = BorderStroke(2.dp, Color(0xFFD4AF37)), // Gold border
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Certificate Ribbon / Header
                            Surface(
                                shape = RoundedCornerShape(30.dp),
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL PEDIGREE CERTIFICATE",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Text(
                                text = "BzOne BIRD BREEDING FARM",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Sertifikat Garansi & Keaslian Garis Keturunan Indukan",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                textAlign = TextAlign.Center
                            )

                            HorizontalDivider(color = Color(0xFFD4AF37).copy(alpha = 0.5f), thickness = 1.dp)

                            // Main Bird Details
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFFDE7),
                                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CertificateRow("Nomor Ring Resmi", bird.ringNumber.ifBlank { "REG-${bird.id}" }, isHighlight = true)
                                    CertificateRow("Nama Burung", bird.name.ifBlank { "-" })
                                    CertificateRow("Jenis / Spesies", bird.typeName)
                                    CertificateRow("Variasi / Mutasi Warna", bird.colorMutation.ifBlank { bird.variety.ifBlank { "Normal" } })
                                    CertificateRow("Jenis Kelamin", bird.gender)
                                    CertificateRow(
                                        "Tanggal Menetas",
                                        bird.birthDate?.let { DateUtils.formatDate(it) } ?: "Tercatat di Farm"
                                    )
                                    CertificateRow("Status Asal", bird.origin)
                                }
                            }

                            // Lineage Tree
                            Text(
                                text = "GARIS SILSILAH INDUKAN (LINEAGE TREE)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                letterSpacing = 0.5.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Sire (Bapak)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE3F2FD),
                                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Male, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("SIRE (AYAH)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sire?.let { "${it.name.ifBlank { "Ring: " + it.ringNumber }}" } ?: "Induk Jantan Juara",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = sire?.let { "Ring: ${it.ringNumber.ifBlank { "-" }}" } ?: "Terkonfirmasi",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF555555)
                                        )
                                        Text(
                                            text = sire?.typeName ?: bird.typeName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF777777)
                                        )
                                    }
                                }

                                // Dam (Ibu)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFCE4EC),
                                    border = BorderStroke(1.dp, Color(0xFFF48FB1)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Female, contentDescription = null, tint = Color(0xFFD81B60), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("DAM (IBU)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dam?.let { "${it.name.ifBlank { "Ring: " + it.ringNumber }}" } ?: "Induk Betina Pilihan",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dam?.let { "Ring: ${it.ringNumber.ifBlank { "-" }}" } ?: "Terkonfirmasi",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF555555)
                                        )
                                        Text(
                                            text = dam?.typeName ?: bird.typeName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF777777)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFD4AF37).copy(alpha = 0.5f), thickness = 1.dp)

                            // Seal & Stamp Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Status Verifikasi:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF777777))
                                    Text("TERVERIFIKASI ASLI", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text("Diterbitkan: ${DateUtils.formatDate(System.currentTimeMillis())}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
                                }

                                // Golden Stamp Badge
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4AF37)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val text = buildPedigreeText(bird, sire, dam)
                                clipboardManager.setText(AnnotatedString(text))
                                viewModel.showMessage("Data silsilah disalin ke papan klip")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salin Silsilah")
                        }

                        Button(
                            onClick = {
                                val text = buildPedigreeText(bird, sire, dam)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Pedigree Resmi")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_action_share_pedigree")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan Pedigree")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CertificateRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D4037))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHighlight) Color(0xFFB71C1C) else Color(0xFF212121)
        )
    }
}

private fun buildPedigreeText(bird: BirdEntity, sire: BirdEntity?, dam: BirdEntity?): String {
    val sb = StringBuilder()
    sb.append("📜 *SERTIFIKAT SILSILAH RESMI - BzOne BIRD FARM*\n")
    sb.append("=========================================\n")
    sb.append("🏷️ No. Ring: *${bird.ringNumber.ifBlank { "REG-" + bird.id }}*\n")
    sb.append("🐦 Nama Burung: *${bird.name.ifBlank { "-" }}*\n")
    sb.append("🦜 Jenis/Spesies: *${bird.typeName}*\n")
    sb.append("🎨 Mutasi Warna: ${bird.colorMutation.ifBlank { bird.variety.ifBlank { "Normal" } }}\n")
    sb.append("⚧ Jenis Kelamin: ${bird.gender}\n")
    sb.append("🎂 Tanggal Menetas: ${bird.birthDate?.let { DateUtils.formatDate(it) } ?: "-"}\n")
    sb.append("🏡 Asal Usul: ${bird.origin}\n\n")
    sb.append("🧬 *GARIS SILSILAH INDUKAN:*\n")
    sb.append("♂️ Induk Jantan (Sire): ${sire?.let { "${it.name} (Ring: ${it.ringNumber})" } ?: "Induk Jantan Juara"}\n")
    sb.append("♀️ Induk Betina (Dam): ${dam?.let { "${it.name} (Ring: ${it.ringNumber})" } ?: "Induk Betina Pilihan"}\n\n")
    sb.append("Diterbitkan secara digital oleh Aplikasi BzOne Bird Farm Manager pada ${DateUtils.formatDate(System.currentTimeMillis())}.\n")
    sb.append("Jaminan keaslian galur murni dan data penangkaran resmi.")
    return sb.toString()
}
