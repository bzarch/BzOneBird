package com.example.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.DetailSectionCard
import com.example.viewmodel.BzOneViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BzOneViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // File exporters and importers via SAF
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonContent = viewModel.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(jsonContent.toByteArray())
                    }
                    viewModel.showMessage("Cadangan JSON berhasil disimpan")
                } catch (e: Exception) {
                    viewModel.showMessage("Gagal menyimpan cadangan: ${e.localizedMessage}")
                }
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val csvContent = viewModel.exportCsv()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(csvContent.toByteArray())
                    }
                    viewModel.showMessage("Data burung berhasil diekspor ke CSV")
                } catch (e: Exception) {
                    viewModel.showMessage("Gagal ekspor CSV: ${e.localizedMessage}")
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val sb = StringBuilder()
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BufferedReader(InputStreamReader(stream)).use { reader ->
                            var line = reader.readLine()
                            while (line != null) {
                                sb.append(line)
                                line = reader.readLine()
                            }
                        }
                    }
                    viewModel.importJson(sb.toString()) { success, msg ->
                        // message shown via viewModel.showMessage
                    }
                } catch (e: Exception) {
                    viewModel.showMessage("Gagal membaca file: ${e.localizedMessage}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Cadangan", fontWeight = FontWeight.Bold) },
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
            // Backup & Restore
            item {
                DetailSectionCard(title = "Pencadangan & Pemulihan Data") {
                    Text(
                        text = "Data disimpan secara permanen di database lokal HP Anda. Anda dapat membuat file cadangan JSON untuk disimpan di Google Drive atau memindahkan data ke HP lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { exportJsonLauncher.launch("BzOne_Backup_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.fillMaxWidth().testTag("export_json_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ekspor Seluruh Data ke JSON")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { importJsonLauncher.launch(arrayOf("application/json", "text/*")) },
                        modifier = Modifier.fillMaxWidth().testTag("import_json_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Impor & Pulihkan dari Cadangan JSON")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { exportCsvLauncher.launch("BzOne_Daftar_Burung_${System.currentTimeMillis()}.csv") },
                        modifier = Modifier.fillMaxWidth().testTag("export_csv_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ekspor Daftar Burung ke CSV (Excel)")
                    }
                }
            }

            // About BzOne
            item {
                DetailSectionCard(title = "Tentang BzOne") {
                    Text(
                        text = "BzOne - Bird Management System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Versi 1.0.0 (Native Android Offline-First)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikasi manajemen burung modern yang dirancang untuk pemilik, penghobi, peternak, dan penangkar burung di Indonesia. Mengelola data burung, kandang, perawatan harian, kesehatan, pasangan indukan, telur, anakan, stok pakan, serta keuangan tanpa memerlukan koneksi internet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Panduan Singkat
            item {
                DetailSectionCard(title = "Panduan Penggunaan Cepat") {
                    Text("1. Masukkan kandang terlebih dahulu di menu Lainnya > Kandang.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Tambahkan burung koleksi Anda di menu Data Burung.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. Gunakan menu Penangkaran untuk menjodohkan indukan jantan & betina.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("4. Telur yang menetas dapat langsung dijadikan catatan anakan, lalu diubah menjadi burung mandiri dengan silsilah otomatis terhubung.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("5. Catat pemasukan dan pengeluaran di menu Keuangan untuk melacak arus kas.", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Danger Zone
            item {
                DetailSectionCard(title = "Area Berbahaya") {
                    Text(
                        text = "Mereset data akan menghapus semua catatan burung, kandang, transaksi, dan riwayat yang ada di aplikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("reset_all_data_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset & Hapus Seluruh Data")
                    }
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        ConfirmDialog(
            title = "Konfirmasi Hapus Seluruh Data",
            message = "PERINGATAN KERAS: Seluruh data burung, kandang, penangkaran, transaksi, dan pengaturan akan dihapus permanen. Disarankan untuk mengekspor cadangan JSON terlebih dahulu. Lanjutkan?",
            confirmText = "Hapus Semua Data",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllData {
                    showResetConfirmDialog = false
                    onNavigateBack()
                }
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }
}
