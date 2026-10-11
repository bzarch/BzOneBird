package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bird_types",
    indices = [Index(value = ["name"], unique = true)]
)
data class BirdTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cages",
    indices = [Index(value = ["code"])]
)
data class CageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val type: String = "Kandang Soliter", // Soliter, Koloni, Ternak, Umbaran, Karantina
    val location: String = "",
    val plannedCapacity: Int = 1,
    val notes: String = "",
    val status: String = "Aktif", // Aktif, Perbaikan, Nonaktif
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "birds",
    indices = [
        Index(value = ["ringNumber"]),
        Index(value = ["name"]),
        Index(value = ["typeName"]),
        Index(value = ["cageId"]),
        Index(value = ["isArchived"])
    ]
)
data class BirdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ringNumber: String = "",
    val name: String = "",
    val typeName: String, // Kenari, Murai Batu, etc.
    val variety: String = "", // e.g., F1, Yorkshire, Medan, Albino
    val colorMutation: String = "",
    val gender: String = "Belum Diketahui", // Jantan, Betina, Belum Diketahui
    val birthDate: Long? = null,
    val isBirthDateEstimated: Boolean = false,
    val origin: String = "Ternak Sendiri", // Ternak Sendiri, Beli, Hadiah, Adopsi, Lainnya
    val acquisitionDate: Long = System.currentTimeMillis(),
    val acquisitionCost: Double? = null,
    val conditionOnAcquisition: String = "Sehat",
    val cageId: Long? = null,
    val sireId: Long? = null, // Induk Jantan (Ayah)
    val damId: Long? = null,  // Induk Betina (Ibu)
    val photoUri: String? = null,
    val status: String = "Aktif", // Aktif, Dikembangbiakkan, Dijual, Dipindahkan, Mati, Lainnya
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cage_movements",
    indices = [Index(value = ["birdId"])]
)
data class CageMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdId: Long,
    val previousCageId: Long?,
    val previousCageName: String = "",
    val newCageId: Long?,
    val newCageName: String = "",
    val movementDate: Long = System.currentTimeMillis(),
    val reason: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "breeding_pairs",
    indices = [
        Index(value = ["code"]),
        Index(value = ["maleBirdId"]),
        Index(value = ["femaleBirdId"])
    ]
)
data class BreedingPairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val maleBirdId: Long,
    val femaleBirdId: Long,
    val pairedDate: Long = System.currentTimeMillis(),
    val separatedDate: Long? = null,
    val cageId: Long? = null,
    val status: String = "Aktif", // Aktif, Selesai, Dipisah, Gagal
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "breeding_cycles",
    indices = [Index(value = ["pairId"])]
)
data class BreedingCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pairId: Long,
    val cycleNumber: Int = 1,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val status: String = "Bertelur", // Bertelur, Mengeram, Menetas, Selesai, Gagal
    val notes: String = "",
    val resultSummary: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "eggs",
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["pairId"])
    ]
)
data class EggEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pairId: Long,
    val cycleId: Long? = null,
    val clutchDate: Long = System.currentTimeMillis(),
    val totalEggs: Int = 0,
    val fertileCount: Int = 0,
    val infertileCount: Int = 0,
    val hatchedCount: Int = 0,
    val brokenCount: Int = 0,
    val estimatedHatchDate: Long? = null,
    val status: String = "Pengeraman", // Pengeraman, Sebagian Menetas, Selesai Menetas, Gagal
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chicks",
    indices = [
        Index(value = ["birdId"]),
        Index(value = ["eggId"]),
        Index(value = ["cycleId"])
    ]
)
data class ChickEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdId: Long? = null, // Linked Bird record if created
    val eggId: Long? = null,
    val cycleId: Long? = null,
    val hatchDate: Long = System.currentTimeMillis(),
    val ringNumber: String = "",
    val gender: String = "Belum Diketahui",
    val typeName: String = "",
    val sireId: Long? = null,
    val damId: Long? = null,
    val status: String = "Dirawat", // Dirawat, Mandiri, Dipindahkan, Mati
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "care_records",
    indices = [
        Index(value = ["birdId"]),
        Index(value = ["cageId"]),
        Index(value = ["dateTime"])
    ]
)
data class CareRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdId: Long? = null,
    val cageId: Long? = null,
    val activityType: String, // Pemberian Pakan, Air Minum, Pembersihan Kandang, Penjemuran, Mandi, Latihan/Pemasteran, Pemeriksaan Kondisi, Lainnya
    val dateTime: Long = System.currentTimeMillis(),
    val status: String = "Selesai", // Selesai, Belum Selesai, Dibatalkan
    val notes: String = "",
    val nextScheduleDate: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "health_records",
    indices = [
        Index(value = ["birdId"]),
        Index(value = ["checkDate"])
    ]
)
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val birdId: Long,
    val checkDate: Long = System.currentTimeMillis(),
    val conditionDesc: String = "",
    val symptoms: String = "",
    val treatment: String = "",
    val medication: String = "",
    val vetName: String = "",
    val nextCheckDate: Long? = null,
    val photoUri: String? = null,
    val followupResult: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "feed_items",
    indices = [Index(value = ["name"])]
)
data class FeedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Pakan Biji", // Pakan Biji, Voer, Buah & Sayur, Ekstra Fooding/Kroto/Ulat, Vitamin & Suplemen, Perlengkapan
    val unit: String = "kg", // kg, gram, bungkus, botol, sachet, pcs
    val currentStock: Double = 0.0,
    val minStockThreshold: Double = 1.0,
    val purchasePrice: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val supplier: String = "",
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inventory_movements",
    indices = [Index(value = ["itemId"])]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val movementType: String, // Masuk, Keluar, Pemakaian, Penyesuaian
    val quantity: Double,
    val previousStock: Double,
    val resultingStock: Double,
    val date: Long = System.currentTimeMillis(),
    val reason: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["type"]),
        Index(value = ["category"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val type: String, // Pemasukan, Pengeluaran
    val category: String, // Penjualan Burung, Penjualan Anakan, Penjualan Telur, Pembelian Burung, Pakan, Perlengkapan, Perawatan, Kesehatan, Biaya Kandang, Biaya Penangkaran, Lainnya
    val amount: Double,
    val description: String = "",
    val relatedBirdId: Long? = null,
    val receiptPhotoUri: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["dateTime"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateTime: Long = System.currentTimeMillis(),
    val repeatInterval: String = "Sekali", // Sekali, Harian, Mingguan, Bulanan
    val category: String = "Umum", // Pakan, Air, Kandang, Jemur/Mandi, Kesehatan, Penetasan, Penangkaran, Umum
    val relatedBirdId: Long? = null,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    indices = [Index(value = ["date"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val relatedBirdId: Long? = null,
    val photoUri: String? = null,
    val date: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "masteran_audio",
    indices = [
        Index(value = ["title"]),
        Index(value = ["category"]),
        Index(value = ["sourceType"])
    ]
)
data class MasteranAudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceType: String = "Lokal", // "Lokal" or "YouTube"
    val sourceUri: String, // file URI or YouTube URL / Web URL
    val category: String = "Tembakan", // Tembakan, Ngeroll, Isian Mewah, Terapi Suara Air, Pancingan Bunyi, Campuran
    val targetSpecies: String = "Universal", // Universal or specific species
    val durationText: String = "", // e.g. "03:45"
    val notes: String = "",
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "competition_records",
    indices = [
        Index(value = ["eventName"]),
        Index(value = ["birdId"]),
        Index(value = ["date"])
    ]
)
data class CompetitionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventName: String,
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val birdId: Long? = null,
    val birdName: String = "",
    val ticketClass: String = "", // e.g. "Kelas Utama 24G", "Campuran Bebas"
    val gantanganNumber: String = "", // e.g. "No. 36"
    val rank: String = "Juara 1", // Juara 1, Juara 2, Juara 3, 10 Besar, Nominasi, Peserta
    val reward: String = "", // e.g. "Trofi + Piagam + Rp 1.500.000"
    val judgeNotes: String = "", // Catatan juri & performa burung
    val certificatePhotoUri: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

