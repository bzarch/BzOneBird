package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.BzOneDatabase
import com.example.data.local.dao.SpeciesCountTuple
import com.example.data.local.entities.*
import com.example.data.repository.BzOneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class BirdSortOrder {
    TERBARU,
    NAMA_ASC,
    NAMA_DESC,
    NO_RING_ASC,
    JENIS_ASC
}

data class DashboardStats(
    val activeBirds: Int = 0,
    val maleBirds: Int = 0,
    val femaleBirds: Int = 0,
    val unknownGenderBirds: Int = 0,
    val activeCages: Int = 0,
    val activePairs: Int = 0,
    val incubatingEggs: Int = 0,
    val activeChicks: Int = 0,
    val todayPendingCare: Int = 0,
    val thisMonthIncome: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val thisMonthBalance: Double = 0.0,
    val nextReminder: ReminderEntity? = null,
    val speciesCounts: List<SpeciesCountTuple> = emptyList()
)

data class RecentActivity(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val category: String, // BURUNG, PERAWATAN, KESEHATAN, KEUANGAN, KANDANG
    val iconType: String
)

class BzOneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BzOneRepository = BzOneRepository(BzOneDatabase.getDatabase(application))

    // UI feedback event (Snackbar / Toast message)
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    // --- BIRDS & FILTERING ---
    val activeBirds: StateFlow<List<BirdEntity>> = repository.activeBirds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedBirds: StateFlow<List<BirdEntity>> = repository.archivedBirds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val birdSearchQuery = MutableStateFlow("")
    val selectedSpeciesFilter = MutableStateFlow<String?>(null)
    val selectedGenderFilter = MutableStateFlow<String?>(null)
    val selectedCageFilter = MutableStateFlow<Long?>(null)
    val selectedStatusFilter = MutableStateFlow<String?>(null)
    val birdSortOrder = MutableStateFlow(BirdSortOrder.TERBARU)

    val selectedBirdIds = MutableStateFlow<Set<Long>>(emptySet())

    private data class BirdFilterCriteria(
        val query: String,
        val species: String?,
        val gender: String?,
        val cageId: Long?,
        val status: String?,
        val sort: BirdSortOrder
    )

    private val filterCriteria = combine(
        combine(birdSearchQuery, selectedSpeciesFilter, selectedGenderFilter) { q, sp, g -> Triple(q, sp, g) },
        combine(selectedCageFilter, selectedStatusFilter, birdSortOrder) { c, st, so -> Triple(c, st, so) }
    ) { (q, sp, g), (c, st, so) ->
        BirdFilterCriteria(q, sp, g, c, st, so)
    }

    val filteredBirds: StateFlow<List<BirdEntity>> = combine(
        activeBirds,
        filterCriteria
    ) { birds, crit ->
        var list = birds

        if (crit.query.isNotBlank()) {
            val q = crit.query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.ringNumber.lowercase().contains(q) ||
                it.typeName.lowercase().contains(q) ||
                it.variety.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }

        if (!crit.species.isNullOrBlank()) {
            list = list.filter { it.typeName.equals(crit.species, ignoreCase = true) }
        }

        if (!crit.gender.isNullOrBlank()) {
            list = list.filter { it.gender.equals(crit.gender, ignoreCase = true) }
        }

        if (crit.cageId != null) {
            list = list.filter { it.cageId == crit.cageId }
        }

        if (!crit.status.isNullOrBlank()) {
            list = list.filter { it.status.equals(crit.status, ignoreCase = true) }
        }

        when (crit.sort) {
            BirdSortOrder.TERBARU -> list.sortedByDescending { it.updatedAt }
            BirdSortOrder.NAMA_ASC -> list.sortedBy { it.name.ifBlank { it.ringNumber }.lowercase() }
            BirdSortOrder.NAMA_DESC -> list.sortedByDescending { it.name.ifBlank { it.ringNumber }.lowercase() }
            BirdSortOrder.NO_RING_ASC -> list.sortedBy { it.ringNumber.lowercase() }
            BirdSortOrder.JENIS_ASC -> list.sortedBy { it.typeName.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun clearBirdFilters() {
        birdSearchQuery.value = ""
        selectedSpeciesFilter.value = null
        selectedGenderFilter.value = null
        selectedCageFilter.value = null
        selectedStatusFilter.value = null
        birdSortOrder.value = BirdSortOrder.TERBARU
    }

    fun toggleBirdSelection(id: Long) {
        val current = selectedBirdIds.value
        selectedBirdIds.value = if (current.contains(id)) current - id else current + id
    }

    fun selectAllFilteredBirds() {
        selectedBirdIds.value = filteredBirds.value.map { it.id }.toSet()
    }

    fun clearBirdSelection() {
        selectedBirdIds.value = emptySet()
    }

    fun archiveSelectedBirds() {
        val ids = selectedBirdIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.archiveMultipleBirds(ids)
            clearBirdSelection()
            showMessage("${ids.size} burung berhasil dipindahkan ke Arsip")
        }
    }

    fun deleteSelectedBirdsPermanently() {
        val ids = selectedBirdIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteMultipleBirdsPermanently(ids)
            clearBirdSelection()
            showMessage("${ids.size} burung berhasil dihapus permanen")
        }
    }

    fun restoreSelectedBirds(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.restoreMultipleBirds(ids)
            showMessage("${ids.size} burung berhasil dipulihkan dari Arsip")
        }
    }

    fun saveBird(bird: BirdEntity, previousCageId: Long? = null, previousCageName: String = "", newCageName: String = "") {
        viewModelScope.launch {
            try {
                repository.saveBird(bird, previousCageId, previousCageName, newCageName)
                showMessage(if (bird.id == 0L) "Burung berhasil ditambahkan" else "Data burung berhasil diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan burung: ${e.localizedMessage}")
            }
        }
    }

    fun archiveBird(id: Long) {
        viewModelScope.launch {
            repository.archiveBird(id)
            showMessage("Burung berhasil diarsipkan")
        }
    }

    fun restoreBird(id: Long) {
        viewModelScope.launch {
            repository.restoreBird(id)
            showMessage("Burung berhasil dipulihkan")
        }
    }

    fun deleteBirdPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteBirdPermanently(id)
            showMessage("Burung berhasil dihapus permanen")
        }
    }

    fun getBirdMovements(birdId: Long): Flow<List<CageMovementEntity>> = repository.getMovementsForBird(birdId)

    // --- BIRD TYPES ---
    val activeBirdTypes: StateFlow<List<BirdTypeEntity>> = repository.activeBirdTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedBirdTypes: StateFlow<List<BirdTypeEntity>> = repository.archivedBirdTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBirdType(type: BirdTypeEntity) {
        viewModelScope.launch {
            try {
                repository.saveBirdType(type)
                showMessage("Jenis burung disimpan")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan jenis burung: ${e.localizedMessage}")
            }
        }
    }

    fun archiveBirdType(id: Long) {
        viewModelScope.launch {
            repository.archiveBirdType(id)
            showMessage("Jenis burung diarsipkan")
        }
    }

    fun restoreBirdType(id: Long) {
        viewModelScope.launch {
            repository.restoreBirdType(id)
            showMessage("Jenis burung dipulihkan")
        }
    }

    fun deleteBirdTypePermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteBirdTypePermanently(id)
            showMessage("Jenis burung dihapus permanen")
        }
    }

    // --- CAGES ---
    val activeCages: StateFlow<List<CageEntity>> = repository.activeCages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedCages: StateFlow<List<CageEntity>> = repository.archivedCages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBirdsInCage(cageId: Long): Flow<List<BirdEntity>> = repository.getBirdsInCage(cageId)

    fun saveCage(cage: CageEntity) {
        viewModelScope.launch {
            try {
                repository.saveCage(cage)
                showMessage(if (cage.id == 0L) "Kandang berhasil ditambahkan" else "Data kandang diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan kandang: ${e.localizedMessage}")
            }
        }
    }

    fun archiveCage(id: Long) {
        viewModelScope.launch {
            repository.archiveCage(id)
            showMessage("Kandang diarsipkan")
        }
    }

    fun restoreCage(id: Long) {
        viewModelScope.launch {
            repository.restoreCage(id)
            showMessage("Kandang dipulihkan")
        }
    }

    fun deleteCagePermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteCagePermanently(id)
            showMessage("Kandang dihapus permanen")
        }
    }

    // --- BREEDING PAIRS & CYCLES ---
    val activeBreedingPairs: StateFlow<List<BreedingPairEntity>> = repository.activeBreedingPairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedBreedingPairs: StateFlow<List<BreedingPairEntity>> = repository.archivedBreedingPairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCyclesForPair(pairId: Long): Flow<List<BreedingCycleEntity>> = repository.getCyclesForPair(pairId)
    fun getEggsForPair(pairId: Long): Flow<List<EggEntity>> = repository.getEggsForPair(pairId)

    fun saveBreedingPair(pair: BreedingPairEntity) {
        viewModelScope.launch {
            try {
                repository.saveBreedingPair(pair)
                showMessage(if (pair.id == 0L) "Pasangan indukan dibuat" else "Data pasangan diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan pasangan: ${e.localizedMessage}")
            }
        }
    }

    fun archiveBreedingPair(id: Long) {
        viewModelScope.launch {
            repository.archiveBreedingPair(id)
            showMessage("Pasangan diarsipkan")
        }
    }

    fun restoreBreedingPair(id: Long) {
        viewModelScope.launch {
            repository.restoreBreedingPair(id)
            showMessage("Pasangan dipulihkan")
        }
    }

    fun deleteBreedingPairPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteBreedingPairPermanently(id)
            showMessage("Pasangan dihapus permanen")
        }
    }

    fun saveBreedingCycle(cycle: BreedingCycleEntity) {
        viewModelScope.launch {
            try {
                repository.saveBreedingCycle(cycle)
                showMessage("Siklus penangkaran disimpan")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan siklus: ${e.localizedMessage}")
            }
        }
    }

    // --- EGGS & CHICKS ---
    val activeEggs: StateFlow<List<EggEntity>> = repository.activeEggs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedEggs: StateFlow<List<EggEntity>> = repository.archivedEggs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChicks: StateFlow<List<ChickEntity>> = repository.activeChicks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedChicks: StateFlow<List<ChickEntity>> = repository.archivedChicks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveEgg(egg: EggEntity) {
        viewModelScope.launch {
            try {
                repository.saveEgg(egg)
                showMessage(if (egg.id == 0L) "Catatan telur dicatat" else "Data telur diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan telur: ${e.localizedMessage}")
            }
        }
    }

    fun archiveEgg(id: Long) {
        viewModelScope.launch {
            repository.archiveEgg(id)
            showMessage("Catatan telur diarsipkan")
        }
    }

    fun restoreEgg(id: Long) {
        viewModelScope.launch {
            repository.restoreEgg(id)
            showMessage("Catatan telur dipulihkan")
        }
    }

    fun deleteEggPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteEggPermanently(id)
            showMessage("Catatan telur dihapus permanen")
        }
    }

    fun saveChick(chick: ChickEntity) {
        viewModelScope.launch {
            try {
                repository.saveChick(chick)
                showMessage(if (chick.id == 0L) "Anakan baru dicatat" else "Data anakan diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan anakan: ${e.localizedMessage}")
            }
        }
    }

    fun archiveChick(id: Long) {
        viewModelScope.launch {
            repository.archiveChick(id)
            showMessage("Data anakan diarsipkan")
        }
    }

    fun restoreChick(id: Long) {
        viewModelScope.launch {
            repository.restoreChick(id)
            showMessage("Data anakan dipulihkan")
        }
    }

    fun deleteChickPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteChickPermanently(id)
            showMessage("Data anakan dihapus permanen")
        }
    }

    fun convertChickToBird(chick: ChickEntity, name: String, cageId: Long?) {
        viewModelScope.launch {
            try {
                val newBirdId = repository.convertChickToBird(chick, name, cageId)
                showMessage("Anakan berhasil dijadikan Burung Mandiri (#$newBirdId)")
            } catch (e: Exception) {
                showMessage("Gagal memindahkan anakan: ${e.localizedMessage}")
            }
        }
    }

    // --- CARE RECORDS ---
    val activeCareRecords: StateFlow<List<CareRecordEntity>> = repository.activeCareRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedCareRecords: StateFlow<List<CareRecordEntity>> = repository.archivedCareRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCareRecord(record: CareRecordEntity) {
        viewModelScope.launch {
            try {
                repository.saveCareRecord(record)
                showMessage("Catatan perawatan disimpan")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan perawatan: ${e.localizedMessage}")
            }
        }
    }

    fun updateCareStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateCareStatus(id, status)
            showMessage("Status perawatan diubah menjadi $status")
        }
    }

    fun archiveCareRecord(id: Long) {
        viewModelScope.launch {
            repository.archiveCareRecord(id)
            showMessage("Catatan perawatan diarsipkan")
        }
    }

    fun restoreCareRecord(id: Long) {
        viewModelScope.launch {
            repository.restoreCareRecord(id)
            showMessage("Catatan perawatan dipulihkan")
        }
    }

    fun deleteCareRecordPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteCareRecordPermanently(id)
            showMessage("Catatan perawatan dihapus permanen")
        }
    }

    fun getRecordsForBird(birdId: Long): Flow<List<CareRecordEntity>> = repository.getRecordsForBird(birdId)

    // --- HEALTH RECORDS ---
    val activeHealthRecords: StateFlow<List<HealthRecordEntity>> = repository.activeHealthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedHealthRecords: StateFlow<List<HealthRecordEntity>> = repository.archivedHealthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHealthRecordsForBird(birdId: Long): Flow<List<HealthRecordEntity>> = repository.getHealthRecordsForBird(birdId)

    fun saveHealthRecord(record: HealthRecordEntity) {
        viewModelScope.launch {
            try {
                repository.saveHealthRecord(record)
                showMessage("Catatan kesehatan berhasil disimpan")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan catatan kesehatan: ${e.localizedMessage}")
            }
        }
    }

    fun archiveHealthRecord(id: Long) {
        viewModelScope.launch {
            repository.archiveHealthRecord(id)
            showMessage("Catatan kesehatan diarsipkan")
        }
    }

    fun restoreHealthRecord(id: Long) {
        viewModelScope.launch {
            repository.restoreHealthRecord(id)
            showMessage("Catatan kesehatan dipulihkan")
        }
    }

    fun deleteHealthRecordPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteHealthRecordPermanently(id)
            showMessage("Catatan kesehatan dihapus permanen")
        }
    }

    // --- FEED & INVENTORY ---
    val activeFeedItems: StateFlow<List<FeedItemEntity>> = repository.activeFeedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedFeedItems: StateFlow<List<FeedItemEntity>> = repository.archivedFeedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockFeedItems: StateFlow<List<FeedItemEntity>> = repository.lowStockFeedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMovementsForItem(itemId: Long): Flow<List<InventoryMovementEntity>> = repository.getMovementsForItem(itemId)

    fun saveFeedItem(item: FeedItemEntity) {
        viewModelScope.launch {
            try {
                repository.saveFeedItem(item)
                showMessage(if (item.id == 0L) "Barang pakan/perlengkapan ditambahkan" else "Data barang diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan barang: ${e.localizedMessage}")
            }
        }
    }

    fun recordStockMovement(itemId: Long, type: String, quantity: Double, reason: String, notes: String) {
        viewModelScope.launch {
            try {
                repository.recordStockMovement(itemId, type, quantity, reason, notes)
                showMessage("Pergerakan stok $type ($quantity) berhasil dicatat")
            } catch (e: Exception) {
                showMessage("Gagal mencatat mutasi stok: ${e.localizedMessage}")
            }
        }
    }

    fun archiveFeedItem(id: Long) {
        viewModelScope.launch {
            repository.archiveFeedItem(id)
            showMessage("Barang diarsipkan")
        }
    }

    fun restoreFeedItem(id: Long) {
        viewModelScope.launch {
            repository.restoreFeedItem(id)
            showMessage("Barang dipulihkan")
        }
    }

    fun deleteFeedItemPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteFeedItemPermanently(id)
            showMessage("Barang dihapus permanen")
        }
    }

    // --- TRANSACTIONS & FINANCIAL SUMMARY ---
    val activeTransactions: StateFlow<List<TransactionEntity>> = repository.activeTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedTransactions: StateFlow<List<TransactionEntity>> = repository.archivedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun saveTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.saveTransaction(tx)
                showMessage(if (tx.id == 0L) "Transaksi dicatat" else "Transaksi diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan transaksi: ${e.localizedMessage}")
            }
        }
    }

    fun archiveTransaction(id: Long) {
        viewModelScope.launch {
            repository.archiveTransaction(id)
            showMessage("Transaksi diarsipkan")
        }
    }

    fun restoreTransaction(id: Long) {
        viewModelScope.launch {
            repository.restoreTransaction(id)
            showMessage("Transaksi dipulihkan")
        }
    }

    fun deleteTransactionPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionPermanently(id)
            showMessage("Transaksi dihapus permanen")
        }
    }

    // --- REMINDERS ---
    val activeReminders: StateFlow<List<ReminderEntity>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedReminders: StateFlow<List<ReminderEntity>> = repository.archivedReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextUpcomingReminder: StateFlow<ReminderEntity?> = repository.nextUpcomingReminder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                repository.saveReminder(reminder)
                showMessage(if (reminder.id == 0L) "Pengingat dibuat" else "Pengingat diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan pengingat: ${e.localizedMessage}")
            }
        }
    }

    fun toggleReminderCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setReminderCompleted(id, completed)
            showMessage(if (completed) "Pengingat ditandai selesai" else "Pengingat diaktifkan kembali")
        }
    }

    fun archiveReminder(id: Long) {
        viewModelScope.launch {
            repository.archiveReminder(id)
            showMessage("Pengingat diarsipkan")
        }
    }

    fun restoreReminder(id: Long) {
        viewModelScope.launch {
            repository.restoreReminder(id)
            showMessage("Pengingat dipulihkan")
        }
    }

    fun deleteReminderPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteReminderPermanently(id)
            showMessage("Pengingat dihapus permanen")
        }
    }

    // --- NOTES ---
    val activeNotes: StateFlow<List<NoteEntity>> = repository.activeNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedNotes: StateFlow<List<NoteEntity>> = repository.archivedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getNotesForBird(birdId: Long): Flow<List<NoteEntity>> = repository.getNotesForBird(birdId)

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.saveNote(note)
                showMessage(if (note.id == 0L) "Catatan dibuat" else "Catatan diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan catatan: ${e.localizedMessage}")
            }
        }
    }

    fun archiveNote(id: Long) {
        viewModelScope.launch {
            repository.archiveNote(id)
            showMessage("Catatan diarsipkan")
        }
    }

    fun restoreNote(id: Long) {
        viewModelScope.launch {
            repository.restoreNote(id)
            showMessage("Catatan dipulihkan")
        }
    }

    fun deleteNotePermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteNotePermanently(id)
            showMessage("Catatan dihapus permanen")
        }
    }

    // --- DASHBOARD REAL DATA COMPUTATION ---
    private data class BirdDemographics(val bCount: Int, val mCount: Int, val fCount: Int, val uCount: Int, val speciesCounts: List<SpeciesCountTuple>)
    private data class BreedingCounts(val cCount: Int, val pCount: Int, val eggCount: Int, val chickCount: Int)

    private val birdDemographicsFlow: Flow<BirdDemographics> = activeBirds.map { birds ->
        val bCount = birds.size
        val mCount = birds.count { it.gender == "Jantan" }
        val fCount = birds.count { it.gender == "Betina" }
        val uCount = birds.count { it.gender !in listOf("Jantan", "Betina") }
        val spCounts = birds.groupBy { it.typeName }
            .map { (type, list) -> SpeciesCountTuple(type, list.size) }
            .sortedByDescending { it.count }
        BirdDemographics(bCount, mCount, fCount, uCount, spCounts)
    }

    private val breedingCountsFlow: Flow<BreedingCounts> = combine(
        activeCages,
        activeBreedingPairs,
        activeEggs,
        activeChicks
    ) { cages, pairs, eggs, chicks ->
        BreedingCounts(
            cCount = cages.size,
            pCount = pairs.count { it.status == "Aktif" },
            eggCount = eggs.filter { it.status == "Pengeraman" }.sumOf { (it.totalEggs - it.hatchedCount - it.brokenCount).coerceAtLeast(0) },
            chickCount = chicks.count { it.status in listOf("Dirawat", "Sehat") }
        )
    }

    val dashboardStats: StateFlow<DashboardStats> = combine(
        birdDemographicsFlow,
        breedingCountsFlow,
        activeTransactions,
        nextUpcomingReminder
    ) { demo, breed, txs, reminder ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        val monthTxs = txs.filter { it.date >= startOfMonth }
        val monthIncome = monthTxs.filter { it.type == "Pemasukan" }.sumOf { it.amount }
        val monthExpense = monthTxs.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
        val monthBalance = monthIncome - monthExpense

        DashboardStats(
            activeBirds = demo.bCount,
            maleBirds = demo.mCount,
            femaleBirds = demo.fCount,
            unknownGenderBirds = demo.uCount,
            activeCages = breed.cCount,
            activePairs = breed.pCount,
            incubatingEggs = breed.eggCount,
            activeChicks = breed.chickCount,
            todayPendingCare = 0,
            thisMonthIncome = monthIncome,
            thisMonthExpense = monthExpense,
            thisMonthBalance = monthBalance,
            nextReminder = reminder,
            speciesCounts = demo.speciesCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // RECENT ACTIVITIES (Combined real stream)
    val recentActivities: StateFlow<List<RecentActivity>> = combine(
        activeBirds,
        activeCareRecords,
        activeHealthRecords,
        activeTransactions
    ) { birds, cares, healths, txs ->
        val list = mutableListOf<RecentActivity>()

        birds.take(5).forEach {
            list.add(
                RecentActivity(
                    id = "bird_${it.id}",
                    title = "Burung: ${it.name.ifBlank { it.ringNumber.ifBlank { it.typeName } }}",
                    description = "${it.typeName} (${it.gender}) • Status: ${it.status}",
                    timestamp = it.updatedAt,
                    category = "BURUNG",
                    iconType = "bird"
                )
            )
        }

        cares.take(5).forEach {
            list.add(
                RecentActivity(
                    id = "care_${it.id}",
                    title = it.activityType,
                    description = "Status: ${it.status}${if (it.notes.isNotBlank()) " • " + it.notes else ""}",
                    timestamp = it.dateTime,
                    category = "PERAWATAN",
                    iconType = "care"
                )
            )
        }

        healths.take(5).forEach {
            list.add(
                RecentActivity(
                    id = "health_${it.id}",
                    title = "Pemeriksaan Kesehatan",
                    description = "${it.conditionDesc.ifBlank { it.symptoms }} • ${it.treatment.ifBlank { "Pemeriksaan rutin" }}",
                    timestamp = it.checkDate,
                    category = "KESEHATAN",
                    iconType = "health"
                )
            )
        }

        txs.take(5).forEach {
            list.add(
                RecentActivity(
                    id = "tx_${it.id}",
                    title = "${it.type}: ${it.category}",
                    description = "${it.description.ifBlank { "Transaksi tercatat" }}",
                    timestamp = it.date,
                    category = "KEUANGAN",
                    iconType = if (it.type == "Pemasukan") "income" else "expense"
                )
            )
        }

        list.sortedByDescending { it.timestamp }.take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // BACKUP & RESTORE
    suspend fun exportJson(): String = repository.exportToJson()
    suspend fun exportCsv(): String = repository.exportBirdsToCsv()

    fun importJson(content: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(content)
            result.onSuccess { count ->
                showMessage("Berhasil memulihkan $count data dari cadangan")
                onComplete(true, "Berhasil memulihkan $count data.")
            }.onFailure { err ->
                showMessage("Gagal mengimpor file: ${err.localizedMessage}")
                onComplete(false, err.localizedMessage ?: "File cadangan tidak valid")
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            showMessage("Seluruh data berhasil dihapus dan direset")
            onComplete()
        }
    }

    // ==========================================
    // MASTERAN AUDIO & PLAYER
    // ==========================================
    val masteranList: StateFlow<List<MasteranAudioEntity>> = repository.activeMasteranAudio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMasteranList: StateFlow<List<MasteranAudioEntity>> = repository.favoriteMasteranAudio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedMasteranCategory = MutableStateFlow<String?>(null)

    val filteredMasteranList: StateFlow<List<MasteranAudioEntity>> = combine(
        masteranList,
        selectedMasteranCategory
    ) { list, cat ->
        if (cat.isNullOrBlank()) list else list.filter { it.category.equals(cat, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audio Playback State
    private var mediaPlayer: MediaPlayer? = null
    private var progressUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null

    val currentlyPlayingAudio = MutableStateFlow<MasteranAudioEntity?>(null)
    val isAudioPlaying = MutableStateFlow(false)
    val audioProgressMs = MutableStateFlow(0)
    val audioDurationMs = MutableStateFlow(0)
    val isAudioLooping = MutableStateFlow(true)
    val sleepTimerMinutes = MutableStateFlow(0) // 0 = off

    fun playMasteran(masteran: MasteranAudioEntity, context: Context) {
        viewModelScope.launch {
            try {
                if (currentlyPlayingAudio.value?.id == masteran.id && mediaPlayer != null) {
                    if (isAudioPlaying.value) {
                        mediaPlayer?.pause()
                        isAudioPlaying.value = false
                    } else {
                        mediaPlayer?.start()
                        isAudioPlaying.value = true
                        startProgressUpdates()
                    }
                    return@launch
                }

                // Stop previous
                stopAudio()

                if (masteran.sourceType == "Lokal") {
                    val uri = Uri.parse(masteran.sourceUri)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, uri)
                        isLooping = isAudioLooping.value
                        setOnPreparedListener { mp ->
                            mp.start()
                            isAudioPlaying.value = true
                            currentlyPlayingAudio.value = masteran
                            audioDurationMs.value = mp.duration
                            startProgressUpdates()
                        }
                        setOnCompletionListener {
                            if (!isAudioLooping.value) {
                                isAudioPlaying.value = false
                                audioProgressMs.value = 0
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            showMessage("Gagal memutar file audio lokal")
                            isAudioPlaying.value = false
                            true
                        }
                        prepareAsync()
                    }
                    repository.incrementMasteranPlayCount(masteran.id)
                } else {
                    // YouTube / Web Link: jika link audio langsung http, putar via MediaPlayer, jika link YouTube umum
                    val uriStr = masteran.sourceUri.trim()
                    if (uriStr.endsWith(".mp3", ignoreCase = true) || uriStr.endsWith(".m4a", ignoreCase = true) || uriStr.endsWith(".ogg", ignoreCase = true)) {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(uriStr)
                            isLooping = isAudioLooping.value
                            setOnPreparedListener { mp ->
                                mp.start()
                                isAudioPlaying.value = true
                                currentlyPlayingAudio.value = masteran
                                audioDurationMs.value = mp.duration
                                startProgressUpdates()
                            }
                            setOnCompletionListener {
                                if (!isAudioLooping.value) {
                                    isAudioPlaying.value = false
                                    audioProgressMs.value = 0
                                }
                            }
                            setOnErrorListener { _, _, _ ->
                                showMessage("Gagal memutar stream audio web")
                                isAudioPlaying.value = false
                                true
                            }
                            prepareAsync()
                        }
                    } else {
                        // Mark as active playing entity for YouTube player card
                        currentlyPlayingAudio.value = masteran
                        isAudioPlaying.value = true
                    }
                    repository.incrementMasteranPlayCount(masteran.id)
                }
            } catch (e: Exception) {
                showMessage("Gagal memutar audio: ${e.localizedMessage}")
                isAudioPlaying.value = false
            }
        }
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (isAudioPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        audioProgressMs.value = mp.currentPosition
                        audioDurationMs.value = mp.duration
                    }
                }
                delay(500)
            }
        }
    }

    fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        isAudioPlaying.value = false
    }

    fun resumeAudio() {
        mediaPlayer?.let {
            it.start()
            isAudioPlaying.value = true
            startProgressUpdates()
        } ?: run {
            if (currentlyPlayingAudio.value != null) {
                isAudioPlaying.value = true
            }
        }
    }

    fun stopAudio() {
        progressUpdateJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isAudioPlaying.value = false
        audioProgressMs.value = 0
        audioDurationMs.value = 0
    }

    fun seekAudio(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        audioProgressMs.value = positionMs
    }

    fun toggleAudioLoop() {
        val newLoop = !isAudioLooping.value
        isAudioLooping.value = newLoop
        mediaPlayer?.isLooping = newLoop
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()
        if (minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                stopAudio()
                sleepTimerMinutes.value = 0
                showMessage("Timer selesai: Pemasteran dimatikan otomatis")
            }
            showMessage("Timer pemasteran diatur: $minutes Menit")
        } else {
            showMessage("Timer pemasteran dimatikan")
        }
    }

    suspend fun getMasteranById(id: Long): MasteranAudioEntity? = repository.getMasteranById(id)

    fun saveMasteran(masteran: MasteranAudioEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val isNew = masteran.id == 0L
            repository.saveMasteran(masteran)
            showMessage(if (isNew) "Masteran berhasil ditambahkan" else "Masteran berhasil diperbarui")
            onComplete()
        }
    }

    fun toggleMasteranFavorite(masteran: MasteranAudioEntity) {
        viewModelScope.launch {
            val newFav = !masteran.isFavorite
            repository.toggleMasteranFavorite(masteran.id, newFav)
            showMessage(if (newFav) "Ditambahkan ke favorit" else "Dihapus dari favorit")
        }
    }

    fun deleteMasteran(id: Long) {
        viewModelScope.launch {
            if (currentlyPlayingAudio.value?.id == id) {
                stopAudio()
                currentlyPlayingAudio.value = null
            }
            repository.deleteMasteran(id)
            showMessage("Audio masteran berhasil dihapus")
        }
    }

    // ==========================================
    // PRESTASI LOMBA & GANTANGAN (COMPETITION)
    // ==========================================
    val competitionRecords: StateFlow<List<CompetitionRecordEntity>> = repository.activeCompetitionRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCompetitions: StateFlow<Int> = repository.activeCompetitionCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val firstRankCount: StateFlow<Int> = repository.firstRankCompetitionCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getCompetitionsForBird(birdId: Long): Flow<List<CompetitionRecordEntity>> = repository.getCompetitionsForBird(birdId)

    suspend fun getCompetitionById(id: Long): CompetitionRecordEntity? = repository.getCompetitionById(id)

    fun saveCompetition(record: CompetitionRecordEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val isNew = record.id == 0L
            repository.saveCompetition(record)
            showMessage(if (isNew) "Prestasi lomba berhasil dicatat" else "Prestasi lomba berhasil diperbarui")
            onComplete()
        }
    }

    fun deleteCompetition(id: Long) {
        viewModelScope.launch {
            repository.deleteCompetition(id)
            showMessage("Data prestasi lomba berhasil dihapus")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}

