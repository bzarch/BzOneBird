package com.example.data.repository

import com.example.data.local.BzOneDatabase
import com.example.data.local.dao.SpeciesCountTuple
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BzOneRepository(private val database: BzOneDatabase) {

    private val birdDao = database.birdDao()
    private val birdTypeDao = database.birdTypeDao()
    private val cageDao = database.cageDao()
    private val cageMovementDao = database.cageMovementDao()
    private val breedingPairDao = database.breedingPairDao()
    private val breedingCycleDao = database.breedingCycleDao()
    private val eggDao = database.eggDao()
    private val chickDao = database.chickDao()
    private val careRecordDao = database.careRecordDao()
    private val healthRecordDao = database.healthRecordDao()
    private val feedItemDao = database.feedItemDao()
    private val inventoryMovementDao = database.inventoryMovementDao()
    private val transactionDao = database.transactionDao()
    private val reminderDao = database.reminderDao()
    private val noteDao = database.noteDao()
    private val masteranAudioDao = database.masteranAudioDao()
    private val competitionRecordDao = database.competitionRecordDao()

    // BIRDS
    val activeBirds: Flow<List<BirdEntity>> = birdDao.getAllActive()
    val archivedBirds: Flow<List<BirdEntity>> = birdDao.getAllArchived()
    val activeBirdCount: Flow<Int> = birdDao.getActiveCount()
    val maleBirdCount: Flow<Int> = birdDao.getMaleCount()
    val femaleBirdCount: Flow<Int> = birdDao.getFemaleCount()
    val unknownGenderCount: Flow<Int> = birdDao.getUnknownGenderCount()
    val speciesCounts: Flow<List<SpeciesCountTuple>> = birdDao.getSpeciesCounts()

    suspend fun getBirdById(id: Long): BirdEntity? = birdDao.getById(id)
    fun getBirdByIdFlow(id: Long): Flow<BirdEntity?> = birdDao.getByIdFlow(id)

    suspend fun saveBird(bird: BirdEntity, previousCageId: Long? = null, previousCageName: String = "", newCageName: String = ""): Long {
        return withContext(Dispatchers.IO) {
            val birdId: Long
            if (bird.id == 0L) {
                birdId = birdDao.insert(bird)
                if (bird.cageId != null) {
                    cageMovementDao.insert(
                        CageMovementEntity(
                            birdId = birdId,
                            previousCageId = null,
                            previousCageName = "-",
                            newCageId = bird.cageId,
                            newCageName = newCageName.ifBlank { "Kandang #${bird.cageId}" },
                            reason = "Penempatan Awal",
                            notes = "Pencatatan burung baru"
                        )
                    )
                }
            } else {
                birdDao.update(bird.copy(updatedAt = System.currentTimeMillis()))
                birdId = bird.id
                if (previousCageId != bird.cageId) {
                    cageMovementDao.insert(
                        CageMovementEntity(
                            birdId = birdId,
                            previousCageId = previousCageId,
                            previousCageName = previousCageName.ifBlank { "-" },
                            newCageId = bird.cageId,
                            newCageName = newCageName.ifBlank { if (bird.cageId != null) "Kandang #${bird.cageId}" else "Tanpa Kandang" },
                            reason = "Perpindahan Kandang",
                            notes = "Pembaruan lokasi penempatan burung"
                        )
                    )
                }
            }
            birdId
        }
    }

    suspend fun archiveBird(id: Long) = birdDao.archive(id)
    suspend fun archiveMultipleBirds(ids: List<Long>) = birdDao.archiveMultiple(ids)
    suspend fun restoreBird(id: Long) = birdDao.restore(id)
    suspend fun restoreMultipleBirds(ids: List<Long>) = birdDao.restoreMultiple(ids)
    suspend fun deleteBirdPermanently(id: Long) = birdDao.deletePermanently(id)
    suspend fun deleteMultipleBirdsPermanently(ids: List<Long>) = birdDao.deleteMultiplePermanently(ids)

    fun getBirdsInCage(cageId: Long): Flow<List<BirdEntity>> = birdDao.getBirdsInCage(cageId)
    fun getMovementsForBird(birdId: Long): Flow<List<CageMovementEntity>> = cageMovementDao.getMovementsForBird(birdId)

    // BIRD TYPES
    val activeBirdTypes: Flow<List<BirdTypeEntity>> = birdTypeDao.getAllActive()
    val archivedBirdTypes: Flow<List<BirdTypeEntity>> = birdTypeDao.getAllArchived()
    suspend fun saveBirdType(type: BirdTypeEntity): Long {
        return if (type.id == 0L) birdTypeDao.insert(type) else {
            birdTypeDao.update(type)
            type.id
        }
    }
    suspend fun archiveBirdType(id: Long) = birdTypeDao.archive(id)
    suspend fun restoreBirdType(id: Long) = birdTypeDao.restore(id)
    suspend fun deleteBirdTypePermanently(id: Long) = birdTypeDao.deletePermanently(id)

    // CAGES
    val activeCages: Flow<List<CageEntity>> = cageDao.getAllActive()
    val archivedCages: Flow<List<CageEntity>> = cageDao.getAllArchived()
    val activeCageCount: Flow<Int> = cageDao.getActiveCount()
    suspend fun getCageById(id: Long): CageEntity? = cageDao.getById(id)
    suspend fun saveCage(cage: CageEntity): Long {
        return if (cage.id == 0L) cageDao.insert(cage) else {
            cageDao.update(cage)
            cage.id
        }
    }
    suspend fun archiveCage(id: Long) = cageDao.archive(id)
    suspend fun restoreCage(id: Long) = cageDao.restore(id)
    suspend fun deleteCagePermanently(id: Long) = cageDao.deletePermanently(id)

    // BREEDING PAIRS
    val activeBreedingPairs: Flow<List<BreedingPairEntity>> = breedingPairDao.getAllActive()
    val archivedBreedingPairs: Flow<List<BreedingPairEntity>> = breedingPairDao.getAllArchived()
    val activeBreedingPairCount: Flow<Int> = breedingPairDao.getActiveCount()
    suspend fun getBreedingPairById(id: Long): BreedingPairEntity? = breedingPairDao.getById(id)
    fun getBreedingPairByIdFlow(id: Long): Flow<BreedingPairEntity?> = breedingPairDao.getByIdFlow(id)
    suspend fun saveBreedingPair(pair: BreedingPairEntity): Long {
        return if (pair.id == 0L) breedingPairDao.insert(pair) else {
            breedingPairDao.update(pair)
            pair.id
        }
    }
    suspend fun archiveBreedingPair(id: Long) = breedingPairDao.archive(id)
    suspend fun restoreBreedingPair(id: Long) = breedingPairDao.restore(id)
    suspend fun deleteBreedingPairPermanently(id: Long) = breedingPairDao.deletePermanently(id)

    // BREEDING CYCLES
    val activeCycles: Flow<List<BreedingCycleEntity>> = breedingCycleDao.getAllActive()
    val archivedCycles: Flow<List<BreedingCycleEntity>> = breedingCycleDao.getAllArchived()
    fun getCyclesForPair(pairId: Long): Flow<List<BreedingCycleEntity>> = breedingCycleDao.getCyclesForPair(pairId)
    suspend fun saveBreedingCycle(cycle: BreedingCycleEntity): Long {
        return if (cycle.id == 0L) breedingCycleDao.insert(cycle) else {
            breedingCycleDao.update(cycle)
            cycle.id
        }
    }
    suspend fun archiveBreedingCycle(id: Long) = breedingCycleDao.archive(id)
    suspend fun restoreBreedingCycle(id: Long) = breedingCycleDao.restore(id)
    suspend fun deleteBreedingCyclePermanently(id: Long) = breedingCycleDao.deletePermanently(id)

    // EGGS
    val activeEggs: Flow<List<EggEntity>> = eggDao.getAllActive()
    val archivedEggs: Flow<List<EggEntity>> = eggDao.getAllArchived()
    val incubatingEggCount: Flow<Int> = eggDao.getIncubatingEggCount()
    fun getEggsForPair(pairId: Long): Flow<List<EggEntity>> = eggDao.getEggsForPair(pairId)
    fun getEggsForCycle(cycleId: Long): Flow<List<EggEntity>> = eggDao.getEggsForCycle(cycleId)
    suspend fun saveEgg(egg: EggEntity): Long {
        return if (egg.id == 0L) eggDao.insert(egg) else {
            eggDao.update(egg)
            egg.id
        }
    }
    suspend fun archiveEgg(id: Long) = eggDao.archive(id)
    suspend fun restoreEgg(id: Long) = eggDao.restore(id)
    suspend fun deleteEggPermanently(id: Long) = eggDao.deletePermanently(id)

    // CHICKS
    val activeChicks: Flow<List<ChickEntity>> = chickDao.getAllActive()
    val archivedChicks: Flow<List<ChickEntity>> = chickDao.getAllArchived()
    val activeChickCount: Flow<Int> = chickDao.getActiveChickCount()
    fun getChicksForEgg(eggId: Long): Flow<List<ChickEntity>> = chickDao.getChicksForEgg(eggId)
    suspend fun saveChick(chick: ChickEntity): Long {
        return if (chick.id == 0L) chickDao.insert(chick) else {
            chickDao.update(chick)
            chick.id
        }
    }
    suspend fun archiveChick(id: Long) = chickDao.archive(id)
    suspend fun restoreChick(id: Long) = chickDao.restore(id)
    suspend fun deleteChickPermanently(id: Long) = chickDao.deletePermanently(id)

    suspend fun convertChickToBird(chick: ChickEntity, name: String, cageId: Long?): Long {
        return withContext(Dispatchers.IO) {
            val newBird = BirdEntity(
                name = name.ifBlank { "Anakan ${chick.ringNumber}".trim() },
                ringNumber = chick.ringNumber,
                typeName = chick.typeName.ifBlank { "Burung" },
                gender = chick.gender,
                birthDate = chick.hatchDate,
                isBirthDateEstimated = false,
                origin = "Ternak Sendiri",
                acquisitionDate = chick.hatchDate,
                conditionOnAcquisition = "Sehat",
                cageId = cageId,
                sireId = chick.sireId,
                damId = chick.damId,
                status = "Aktif",
                notes = "Dibuat dari catatan anakan #${chick.id}. ${chick.notes}"
            )
            val birdId = birdDao.insert(newBird)
            chickDao.update(chick.copy(birdId = birdId, status = "Mandiri"))
            birdId
        }
    }

    // CARE RECORDS
    val activeCareRecords: Flow<List<CareRecordEntity>> = careRecordDao.getAllActive()
    val archivedCareRecords: Flow<List<CareRecordEntity>> = careRecordDao.getAllArchived()
    fun getRecordsForBird(birdId: Long): Flow<List<CareRecordEntity>> = careRecordDao.getRecordsForBird(birdId)
    fun getRecordsForToday(startOfDay: Long, endOfDay: Long): Flow<List<CareRecordEntity>> = careRecordDao.getRecordsForToday(startOfDay, endOfDay)
    fun getTodayPendingCount(startOfDay: Long, endOfDay: Long): Flow<Int> = careRecordDao.getTodayPendingCount(startOfDay, endOfDay)
    suspend fun saveCareRecord(record: CareRecordEntity): Long {
        return if (record.id == 0L) careRecordDao.insert(record) else {
            careRecordDao.update(record)
            record.id
        }
    }
    suspend fun updateCareStatus(id: Long, status: String) = careRecordDao.updateStatus(id, status)
    suspend fun archiveCareRecord(id: Long) = careRecordDao.archive(id)
    suspend fun restoreCareRecord(id: Long) = careRecordDao.restore(id)
    suspend fun deleteCareRecordPermanently(id: Long) = careRecordDao.deletePermanently(id)

    // HEALTH RECORDS
    val activeHealthRecords: Flow<List<HealthRecordEntity>> = healthRecordDao.getAllActive()
    val archivedHealthRecords: Flow<List<HealthRecordEntity>> = healthRecordDao.getAllArchived()
    fun getHealthRecordsForBird(birdId: Long): Flow<List<HealthRecordEntity>> = healthRecordDao.getRecordsForBird(birdId)
    suspend fun saveHealthRecord(record: HealthRecordEntity): Long {
        return if (record.id == 0L) healthRecordDao.insert(record) else {
            healthRecordDao.update(record)
            record.id
        }
    }
    suspend fun archiveHealthRecord(id: Long) = healthRecordDao.archive(id)
    suspend fun restoreHealthRecord(id: Long) = healthRecordDao.restore(id)
    suspend fun deleteHealthRecordPermanently(id: Long) = healthRecordDao.deletePermanently(id)

    // FEED & INVENTORY
    val activeFeedItems: Flow<List<FeedItemEntity>> = feedItemDao.getAllActive()
    val archivedFeedItems: Flow<List<FeedItemEntity>> = feedItemDao.getAllArchived()
    val lowStockFeedItems: Flow<List<FeedItemEntity>> = feedItemDao.getLowStockItems()
    fun getMovementsForItem(itemId: Long): Flow<List<InventoryMovementEntity>> = inventoryMovementDao.getMovementsForItem(itemId)
    suspend fun saveFeedItem(item: FeedItemEntity): Long {
        return if (item.id == 0L) feedItemDao.insert(item) else {
            feedItemDao.update(item)
            item.id
        }
    }
    suspend fun archiveFeedItem(id: Long) = feedItemDao.archive(id)
    suspend fun restoreFeedItem(id: Long) = feedItemDao.restore(id)
    suspend fun deleteFeedItemPermanently(id: Long) = feedItemDao.deletePermanently(id)

    suspend fun recordStockMovement(
        itemId: Long,
        type: String, // Masuk, Keluar, Pemakaian, Penyesuaian
        quantity: Double,
        reason: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val item = feedItemDao.getById(itemId) ?: return@withContext
        val prev = item.currentStock
        val resulting = when (type) {
            "Masuk" -> prev + quantity
            "Keluar", "Pemakaian" -> (prev - quantity).coerceAtLeast(0.0)
            "Penyesuaian" -> quantity
            else -> prev
        }
        feedItemDao.updateStock(itemId, resulting)
        inventoryMovementDao.insert(
            InventoryMovementEntity(
                itemId = itemId,
                movementType = type,
                quantity = quantity,
                previousStock = prev,
                resultingStock = resulting,
                reason = reason,
                notes = notes
            )
        )
    }

    // TRANSACTIONS / FINANCE
    val activeTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllActive()
    val archivedTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllArchived()
    val totalIncome: Flow<Double> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double> = transactionDao.getTotalExpense()
    fun getIncomeInRange(startDate: Long, endDate: Long): Flow<Double> = transactionDao.getIncomeInRange(startDate, endDate)
    fun getExpenseInRange(startDate: Long, endDate: Long): Flow<Double> = transactionDao.getExpenseInRange(startDate, endDate)
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsInRange(startDate, endDate)
    suspend fun saveTransaction(tx: TransactionEntity): Long {
        return if (tx.id == 0L) transactionDao.insert(tx) else {
            transactionDao.update(tx)
            tx.id
        }
    }
    suspend fun archiveTransaction(id: Long) = transactionDao.archive(id)
    suspend fun restoreTransaction(id: Long) = transactionDao.restore(id)
    suspend fun deleteTransactionPermanently(id: Long) = transactionDao.deletePermanently(id)

    // REMINDERS
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getAllActive()
    val archivedReminders: Flow<List<ReminderEntity>> = reminderDao.getAllArchived()
    val pendingReminders: Flow<List<ReminderEntity>> = reminderDao.getPendingReminders()
    val nextUpcomingReminder: Flow<ReminderEntity?> = reminderDao.getNextUpcomingReminder()
    suspend fun saveReminder(reminder: ReminderEntity): Long {
        return if (reminder.id == 0L) reminderDao.insert(reminder) else {
            reminderDao.update(reminder)
            reminder.id
        }
    }
    suspend fun setReminderCompleted(id: Long, completed: Boolean) = reminderDao.setCompleted(id, completed)
    suspend fun archiveReminder(id: Long) = reminderDao.archive(id)
    suspend fun restoreReminder(id: Long) = reminderDao.restore(id)
    suspend fun deleteReminderPermanently(id: Long) = reminderDao.deletePermanently(id)

    // NOTES
    val activeNotes: Flow<List<NoteEntity>> = noteDao.getAllActive()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getAllArchived()
    fun getNotesForBird(birdId: Long): Flow<List<NoteEntity>> = noteDao.getNotesForBird(birdId)
    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) noteDao.insert(note) else {
            noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }
    suspend fun archiveNote(id: Long) = noteDao.archive(id)
    suspend fun restoreNote(id: Long) = noteDao.restore(id)
    suspend fun deleteNotePermanently(id: Long) = noteDao.deletePermanently(id)

    // BACKUP & RESTORE (JSON & CSV)
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "BzOne")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        fun birdToJson(b: BirdEntity) = JSONObject().apply {
            put("id", b.id)
            put("ringNumber", b.ringNumber)
            put("name", b.name)
            put("typeName", b.typeName)
            put("variety", b.variety)
            put("colorMutation", b.colorMutation)
            put("gender", b.gender)
            put("birthDate", b.birthDate ?: JSONObject.NULL)
            put("isBirthDateEstimated", b.isBirthDateEstimated)
            put("origin", b.origin)
            put("acquisitionDate", b.acquisitionDate)
            put("acquisitionCost", b.acquisitionCost ?: JSONObject.NULL)
            put("conditionOnAcquisition", b.conditionOnAcquisition)
            put("cageId", b.cageId ?: JSONObject.NULL)
            put("sireId", b.sireId ?: JSONObject.NULL)
            put("damId", b.damId ?: JSONObject.NULL)
            put("photoUri", b.photoUri ?: JSONObject.NULL)
            put("status", b.status)
            put("notes", b.notes)
            put("isArchived", b.isArchived)
            put("createdAt", b.createdAt)
            put("updatedAt", b.updatedAt)
        }

        val birdsArray = JSONArray()
        // include both active and archived to preserve entire state
        birdDao.getAllActive() // to get list, let's query directly
        val rawActive = database.openHelper.readableDatabase.query("SELECT * FROM birds")
        while (rawActive.moveToNext()) {
            val b = BirdEntity(
                id = rawActive.getLong(rawActive.getColumnIndexOrThrow("id")),
                ringNumber = rawActive.getString(rawActive.getColumnIndexOrThrow("ringNumber")) ?: "",
                name = rawActive.getString(rawActive.getColumnIndexOrThrow("name")) ?: "",
                typeName = rawActive.getString(rawActive.getColumnIndexOrThrow("typeName")),
                variety = rawActive.getString(rawActive.getColumnIndexOrThrow("variety")) ?: "",
                colorMutation = rawActive.getString(rawActive.getColumnIndexOrThrow("colorMutation")) ?: "",
                gender = rawActive.getString(rawActive.getColumnIndexOrThrow("gender")) ?: "Belum Diketahui",
                birthDate = if (rawActive.isNull(rawActive.getColumnIndexOrThrow("birthDate"))) null else rawActive.getLong(rawActive.getColumnIndexOrThrow("birthDate")),
                isBirthDateEstimated = rawActive.getInt(rawActive.getColumnIndexOrThrow("isBirthDateEstimated")) == 1,
                origin = rawActive.getString(rawActive.getColumnIndexOrThrow("origin")) ?: "",
                acquisitionDate = rawActive.getLong(rawActive.getColumnIndexOrThrow("acquisitionDate")),
                acquisitionCost = if (rawActive.isNull(rawActive.getColumnIndexOrThrow("acquisitionCost"))) null else rawActive.getDouble(rawActive.getColumnIndexOrThrow("acquisitionCost")),
                conditionOnAcquisition = rawActive.getString(rawActive.getColumnIndexOrThrow("conditionOnAcquisition")) ?: "",
                cageId = if (rawActive.isNull(rawActive.getColumnIndexOrThrow("cageId"))) null else rawActive.getLong(rawActive.getColumnIndexOrThrow("cageId")),
                sireId = if (rawActive.isNull(rawActive.getColumnIndexOrThrow("sireId"))) null else rawActive.getLong(rawActive.getColumnIndexOrThrow("sireId")),
                damId = if (rawActive.isNull(rawActive.getColumnIndexOrThrow("damId"))) null else rawActive.getLong(rawActive.getColumnIndexOrThrow("damId")),
                photoUri = rawActive.getString(rawActive.getColumnIndexOrThrow("photoUri")),
                status = rawActive.getString(rawActive.getColumnIndexOrThrow("status")) ?: "Aktif",
                notes = rawActive.getString(rawActive.getColumnIndexOrThrow("notes")) ?: "",
                isArchived = rawActive.getInt(rawActive.getColumnIndexOrThrow("isArchived")) == 1,
                createdAt = rawActive.getLong(rawActive.getColumnIndexOrThrow("createdAt")),
                updatedAt = rawActive.getLong(rawActive.getColumnIndexOrThrow("updatedAt"))
            )
            birdsArray.put(birdToJson(b))
        }
        rawActive.close()
        root.put("birds", birdsArray)

        // Cages
        val cagesArray = JSONArray()
        val rawCages = database.openHelper.readableDatabase.query("SELECT * FROM cages")
        while (rawCages.moveToNext()) {
            val c = JSONObject().apply {
                put("id", rawCages.getLong(rawCages.getColumnIndexOrThrow("id")))
                put("code", rawCages.getString(rawCages.getColumnIndexOrThrow("code")))
                put("name", rawCages.getString(rawCages.getColumnIndexOrThrow("name")))
                put("type", rawCages.getString(rawCages.getColumnIndexOrThrow("type")))
                put("location", rawCages.getString(rawCages.getColumnIndexOrThrow("location")))
                put("plannedCapacity", rawCages.getInt(rawCages.getColumnIndexOrThrow("plannedCapacity")))
                put("notes", rawCages.getString(rawCages.getColumnIndexOrThrow("notes")))
                put("status", rawCages.getString(rawCages.getColumnIndexOrThrow("status")))
                put("isArchived", rawCages.getInt(rawCages.getColumnIndexOrThrow("isArchived")) == 1)
                put("createdAt", rawCages.getLong(rawCages.getColumnIndexOrThrow("createdAt")))
            }
            cagesArray.put(c)
        }
        rawCages.close()
        root.put("cages", cagesArray)

        // Transactions
        val txArray = JSONArray()
        val rawTx = database.openHelper.readableDatabase.query("SELECT * FROM transactions")
        while (rawTx.moveToNext()) {
            val t = JSONObject().apply {
                put("id", rawTx.getLong(rawTx.getColumnIndexOrThrow("id")))
                put("date", rawTx.getLong(rawTx.getColumnIndexOrThrow("date")))
                put("type", rawTx.getString(rawTx.getColumnIndexOrThrow("type")))
                put("category", rawTx.getString(rawTx.getColumnIndexOrThrow("category")))
                put("amount", rawTx.getDouble(rawTx.getColumnIndexOrThrow("amount")))
                put("description", rawTx.getString(rawTx.getColumnIndexOrThrow("description")))
                put("isArchived", rawTx.getInt(rawTx.getColumnIndexOrThrow("isArchived")) == 1)
                put("createdAt", rawTx.getLong(rawTx.getColumnIndexOrThrow("createdAt")))
            }
            txArray.put(t)
        }
        rawTx.close()
        root.put("transactions", txArray)

        root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "BzOne") {
                return@withContext Result.failure(IllegalArgumentException("Format file tidak sesuai (bukan cadangan BzOne)"))
            }
            var count = 0
            if (root.has("birds")) {
                val array = root.getJSONArray("birds")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val bird = BirdEntity(
                        id = obj.optLong("id", 0L),
                        ringNumber = obj.optString("ringNumber", ""),
                        name = obj.optString("name", ""),
                        typeName = obj.optString("typeName", "Burung"),
                        variety = obj.optString("variety", ""),
                        colorMutation = obj.optString("colorMutation", ""),
                        gender = obj.optString("gender", "Belum Diketahui"),
                        birthDate = if (obj.isNull("birthDate")) null else obj.optLong("birthDate"),
                        isBirthDateEstimated = obj.optBoolean("isBirthDateEstimated", false),
                        origin = obj.optString("origin", "Ternak Sendiri"),
                        acquisitionDate = obj.optLong("acquisitionDate", System.currentTimeMillis()),
                        acquisitionCost = if (obj.isNull("acquisitionCost")) null else obj.optDouble("acquisitionCost"),
                        conditionOnAcquisition = obj.optString("conditionOnAcquisition", "Sehat"),
                        cageId = if (obj.isNull("cageId")) null else obj.optLong("cageId"),
                        sireId = if (obj.isNull("sireId")) null else obj.optLong("sireId"),
                        damId = if (obj.isNull("damId")) null else obj.optLong("damId"),
                        photoUri = if (obj.isNull("photoUri")) null else obj.optString("photoUri"),
                        status = obj.optString("status", "Aktif"),
                        notes = obj.optString("notes", ""),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    birdDao.insert(bird)
                    count++
                }
            }
            if (root.has("cages")) {
                val array = root.getJSONArray("cages")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val cage = CageEntity(
                        id = obj.optLong("id", 0L),
                        code = obj.optString("code", "K"),
                        name = obj.optString("name", "Kandang"),
                        type = obj.optString("type", "Kandang Soliter"),
                        location = obj.optString("location", ""),
                        plannedCapacity = obj.optInt("plannedCapacity", 1),
                        notes = obj.optString("notes", ""),
                        status = obj.optString("status", "Aktif"),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    cageDao.insert(cage)
                    count++
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportBirdsToCsv(): String = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Nama,No Ring,Jenis Burung,Jenis Kelamin,Status,Kandang ID,Tanggal Lahir,Asal,Harga Beli,Catatan\n")
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM birds WHERE isArchived = 0 ORDER BY id ASC")
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: ""
            val ring = cursor.getString(cursor.getColumnIndexOrThrow("ringNumber")) ?: ""
            val type = cursor.getString(cursor.getColumnIndexOrThrow("typeName")) ?: ""
            val gender = cursor.getString(cursor.getColumnIndexOrThrow("gender")) ?: ""
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: ""
            val cageId = if (cursor.isNull(cursor.getColumnIndexOrThrow("cageId"))) "-" else cursor.getLong(cursor.getColumnIndexOrThrow("cageId")).toString()
            val birthDate = if (cursor.isNull(cursor.getColumnIndexOrThrow("birthDate"))) "-" else sdf.format(Date(cursor.getLong(cursor.getColumnIndexOrThrow("birthDate"))))
            val origin = cursor.getString(cursor.getColumnIndexOrThrow("origin")) ?: ""
            val cost = if (cursor.isNull(cursor.getColumnIndexOrThrow("acquisitionCost"))) "0" else cursor.getDouble(cursor.getColumnIndexOrThrow("acquisitionCost")).toString()
            val notes = (cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "").replace(",", " ").replace("\n", " ")

            sb.append("\"$id\",\"$name\",\"$ring\",\"$type\",\"$gender\",\"$status\",\"$cageId\",\"$birthDate\",\"$origin\",\"$cost\",\"$notes\"\n")
        }
        cursor.close()
        sb.toString()
    }

    suspend fun clearAllData(): Unit = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    // MASTERAN AUDIO
    val activeMasteranAudio: Flow<List<MasteranAudioEntity>> = masteranAudioDao.getAllActive()
    val favoriteMasteranAudio: Flow<List<MasteranAudioEntity>> = masteranAudioDao.getFavorites()
    val activeMasteranCount: Flow<Int> = masteranAudioDao.getActiveCount()

    suspend fun getMasteranById(id: Long): MasteranAudioEntity? = masteranAudioDao.getById(id)

    suspend fun saveMasteran(masteran: MasteranAudioEntity): Long = withContext(Dispatchers.IO) {
        if (masteran.id == 0L) {
            masteranAudioDao.insert(masteran)
        } else {
            masteranAudioDao.update(masteran)
            masteran.id
        }
    }

    suspend fun toggleMasteranFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        masteranAudioDao.toggleFavorite(id, isFavorite)
    }

    suspend fun incrementMasteranPlayCount(id: Long) = withContext(Dispatchers.IO) {
        masteranAudioDao.incrementPlayCount(id)
    }

    suspend fun deleteMasteran(id: Long) = withContext(Dispatchers.IO) {
        masteranAudioDao.deletePermanently(id)
    }

    suspend fun archiveMasteran(id: Long) = withContext(Dispatchers.IO) {
        masteranAudioDao.archive(id)
    }

    // COMPETITION RECORDS
    val activeCompetitionRecords: Flow<List<CompetitionRecordEntity>> = competitionRecordDao.getAllActive()
    val activeCompetitionCount: Flow<Int> = competitionRecordDao.getActiveCount()
    val firstRankCompetitionCount: Flow<Int> = competitionRecordDao.getFirstRankCount()

    fun getCompetitionsForBird(birdId: Long): Flow<List<CompetitionRecordEntity>> = competitionRecordDao.getByBirdId(birdId)

    suspend fun getCompetitionById(id: Long): CompetitionRecordEntity? = competitionRecordDao.getById(id)

    suspend fun saveCompetition(record: CompetitionRecordEntity): Long = withContext(Dispatchers.IO) {
        if (record.id == 0L) {
            competitionRecordDao.insert(record)
        } else {
            competitionRecordDao.update(record)
            record.id
        }
    }

    suspend fun deleteCompetition(id: Long) = withContext(Dispatchers.IO) {
        competitionRecordDao.deletePermanently(id)
    }

    suspend fun archiveCompetition(id: Long) = withContext(Dispatchers.IO) {
        competitionRecordDao.archive(id)
    }
}

