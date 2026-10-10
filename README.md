# BzOne — Universal Avian & Bird Management System

**BzOne** adalah aplikasi Android native modern, lengkap, ringan, dan 100% offline-first yang dirancang khusus untuk pemilik burung, penghobi, peternak (*breeder*), dan pengelola penangkaran (*aviary*).

Aplikasi ini bersifat **universal** untuk segala jenis burung tanpa batasan (seperti Murai Batu, Lovebird, Kenari, Cucak Ijo, Kacer, Parkit, Finch/Pipit, Merpati, Beo, Perkutut, Jalak, maupun jenis eksotis lainnya) yang dapat diinputkan secara bebas oleh pengguna.

---

## 🚀 Fitur Utama

### 1. 📊 Beranda & Dashboard Dinamis
- **Statistik Riil:** Menghitung otomatis total burung aktif, rasio jantan/betina/belum diketahui, jumlah kandang aktif, pasangan indukan ternak, telur dalam proses pengeraman, dan anakan yang dirawat.
- **Arus Kas Finansial:** Rekapitulasi pemasukan, pengeluaran, dan saldo kas bersih bulan berjalan.
- **Pengingat Terdekat:** Jadwal perawatan atau penetasan berikutnya yang akan jatuh tempo.
- **Aktivitas Terbaru:** Log aktivitas riil dari seluruh modul (burung baru, catatan perawatan, pemeriksaan kesehatan, transaksi keuangan).
- **Aksi Cepat (*Quick Actions*):** Tombol pintas untuk menambah data langsung dari beranda.

### 2. 🦜 Manajemen Data Burung (Universal)
- **Input Bebas & Fleksibel:** Jenis burung bersifat universal tanpa dibatasi daftar kaku.
- **Atribut Lengkap:** Nama burung, nomor/kode ring, ras/varietas, warna/mutasi, jenis kelamin, tanggal lahir (opsi perkiraan umur), asal perolehan, tanggal perolehan, harga beli (Rp), kondisi awal, penempatan kandang, silsilah indukan (Ayah & Ibu), status, dan catatan bebas.
- **Pencarian & Filter Cepat:** Cari berdasarkan nama, kode ring, jenis burung, kandang, atau status.
- **Operasi Massal (*Batch Operations*):** Pilih beberapa burung sekaligus untuk diarsipkan atau dihapus secara massal.
- **Detail Burung Komprehensif:** Silsilah indukan, riwayat perpindahan kandang, riwayat perawatan harian, dan rekam medis kesehatan.

### 3. 🏠 Manajemen Kandang (*Cages*)
- Pencatatan kode kandang, nama, jenis kandang (soliter, koloni, ternak, umbaran), lokasi, kapasitas terencana, dan status.
- **Peringatan Kapasitas:** Indikator visual otomatis jika jumlah burung melebihi kapasitas yang direncanakan.
- **Pelacakan Mutasi:** Menyimpan otomatis riwayat tanggal dan alasan perpindahan saat lokasi burung diubah.

### 4. 📋 Perawatan Harian (*Daily Care*)
- Tab **Agenda Hari Ini** dan **Semua Riwayat**.
- Aktivitas: Pemberian pakan, air minum, pembersihan kandang, penjemuran, mandi, latihan/pemasteran, dan pemeriksaan fisik.
- Tombol satu ketukan untuk menandai status selesai (*checklist*).

### 5. ❤️ Penangkaran & Pasangan Indukan (*Breeding*)
- Penjodohan indukan jantan dan betina dengan validasi pencegahan duplikasi kelamin.
- Siklus penangkaran dan pemantauan kandang ternak.

### 6. 🥚 Telur, Penetasan & Anakan (*Eggs & Chicks*)
- Pencatatan kelompok telur: tanggal bertelur, total butir, jumlah fertil, infertil, menetas, pecah, dan perkiraan tanggal menetas.
- Pencatatan anakan hasil tetasan.
- **Fitur "Jadikan Burung Mandiri":** Mengonversi catatan anakan secara instan menjadi data Burung baru dengan data silsilah indukan jantan dan betina langsung terhubung otomatis.

### 7. 📦 Stok Pakan & Perlengkapan (*Inventory*)
- Inventaris pakan biji, voer, buah/sayur, ekstra fooding, vitamin, obat, dan perlengkapan.
- **Peringatan Stok Menipis:** Indikator otomatis jika stok berada di bawah batas minimum (*threshold*).
- **Mutasi Stok:** Dialog pencatatan stok masuk, pemakaian rutin, stok keluar, dan penyesuaian fisik.

### 8. 💰 Keuangan & Pembukuan Kas (*Finance*)
- Pembukuan kas masuk dan keluar dalam format Rupiah (Rp).
- Kategori transaksi spesifik peternakan (penjualan burung, penjualan anakan, pakan, vitamin, kandang, perlengkapan, dll).
- Penghitungan otomatis saldo kas dan rekapitulasi keuangan.

### 9. 🩺 Kesehatan & Rekam Medis
- Pencatatan gejala, kondisi fisik, tindakan perawatan, obat/vitamin yang diberikan, nama dokter hewan, dan hasil tindak lanjut.
- Dilengkapi pengingat medis profesional.

### 10. ⏰ Pengingat & Jadwal
- Pengingat harian, mingguan, bulanan, atau sekali jalan untuk jadwal pakan, jemur, atau kontrol kesehatan.

### 11. 📝 Catatan Tambahan (*Notes*)
- Buku catatan bebas yang dapat dikaitkan ke burung tertentu atau catatan umum peternakan.

### 12. 📈 Laporan & Statistik
- Grafik distribusi jenis burung dan persentase kepemilikan.
- Demografi kelamin burung.
- Pemanfaatan kapasitas kandang (*occupancy rate*).
- Rasio keberhasilan tetas (*hatching rate* %).
- Rekapitulasi finansial pemasukan vs pengeluaran.

### 13. 🗑️ Tempat Sampah & Pemulihan (*Soft Delete*)
- Fitur arsip data untuk menjaga integritas riwayat penangkaran dan transaksi.
- Opsi **Pulihkan** (*Restore*) ke daftar aktif atau **Hapus Permanen**.

### 14. 💾 Pencadangan & Pemulihan Data (*Backup & Restore*)
- **Ekspor JSON:** Menyimpan seluruh database ke file JSON via Android Storage Access Framework (SAF).
- **Impor JSON:** Memulihkan seluruh data cadangan secara aman dengan validasi integritas skema.
- **Ekspor CSV:** Mengekspor data daftar burung ke format CSV untuk diolah di Microsoft Excel atau Google Sheets.
- **Reset Data:** Tombol hapus seluruh data dengan dialog konfirmasi keamanan ganda.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa Pemrograman:** Kotlin 2.x
- **UI Framework:** Jetpack Compose (Declarative UI)
- **Desain Sistem:** Material Design 3 (M3) dengan tema alam *Forest Green* (Mendukung Dark & Light Mode)
- **Database Lokal:** SQLite via Android Room Database 2.7.0 (KSP compiler)
- **Pola Arsitektur:** MVVM (Model-View-ViewModel) & Repository Pattern
- **Asinkron & Reaktivitas:** Kotlin Coroutines & StateFlow / SharedFlow
- **Navigasi:** Jetpack Navigation Compose
- **Ikon Peluncur:** Adaptive Icon kustom Material You dengan resolusi tinggi

---

## 📂 Struktur Proyek

```
/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── BzOneDatabase.kt
│       │   │   │   │   ├── dao/Daos.kt
│       │   │   │   │   └── entities/Entities.kt
│       │   │   │   └── repository/BzOneRepository.kt
│       │   │   ├── ui/
│       │   │   │   ├── components/CommonComponents.kt
│       │   │   │   ├── navigation/AppNavigation.kt
│       │   │   │   ├── screens/
│       │   │   │   │   ├── dashboard/DashboardScreen.kt
│       │   │   │   │   ├── birds/
│       │   │   │   │   ├── cages/
│       │   │   │   │   ├── care/
│       │   │   │   │   ├── breeding/
│       │   │   │   │   ├── inventory/
│       │   │   │   │   ├── finance/
│       │   │   │   │   ├── health/
│       │   │   │   │   ├── reminders/
│       │   │   │   │   ├── notes/
│       │   │   │   │   ├── reports/
│       │   │   │   │   ├── archive/
│       │   │   │   │   └── settings/
│       │   │   │   └── theme/
│       │   │   ├── util/Formatters.kt
│       │   │   └── viewmodel/BzOneViewModel.kt
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       └── values/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 📦 File APK Siap Pakai & Sinkronisasi GitHub

### 1. File APK di Repositori
File `.apk` siap pasang telah di-build dan disertakan langsung di dalam repositori pada direktori:
```
apk/BzOne-v1.0.apk
```
Pengguna yang mengklon atau mengunduh repositori ini di GitHub dapat langsung mengunduh file APK tersebut tanpa harus meng-compile ulang di Android Studio.

### 2. Otomasi GitHub Actions (Build APK Otomatis)
Proyek ini dilengkapi dengan workflow GitHub Actions di `.github/workflows/build-apk.yml`:
- **Setiap kali `push` atau `pull_request` ke branch `main`:** GitHub Actions akan otomatis menguji dan meng-compile APK terbaru, lalu menyediakannya sebagai **Artifact** unduhan (*BzOne-Debug-APK*).
- **Setiap kali membuat Git Tag (misal: `v1.0.0`):** GitHub Actions akan otomatis membuat **GitHub Release** baru dan melampirkan file `.apk` ke halaman Releases repositori GitHub Anda.

### 3. Langkah Sinkronisasi ke Repositori GitHub Anda
Jalankan perintah berikut di terminal lokal untuk menghubungkan dan mengunggah kode ke GitHub:
```bash
git add .
git commit -m "feat: release BzOne v1.0 with APK and GitHub Actions"
git branch -M main
git remote add origin https://github.com/USERNAME/NAMA_REPO_ANDA.git
git push -u origin main
```

### 4. Build Ulang APK Mandiri
Jika ingin meng-compile ulang secara manual:
```bash
./gradlew assembleDebug
```
File APK hasil build manual akan berada di: `app/build/outputs/apk/debug/app-debug.apk`.

**Build Release APK:**
```bash
gradle assembleRelease
```
File APK release akan dihasilkan di:
`app/build/outputs/apk/release/app-release-unsigned.apk` (atau signed jika konfigurasi keystore aktif).

### 3. Menjalankan Unit Test
```bash
gradle :app:testDebugUnitTest
```

---

## 📄 Lisensi
Hak Cipta © 2026 BzOne. Seluruh hak cipta dilindungi.
Aplikasi ini ditujukan untuk mempermudah komunitas pecinta dan peternak burung di seluruh Indonesia.
