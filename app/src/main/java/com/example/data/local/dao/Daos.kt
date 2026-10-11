package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdTypeDao {
    @Query("SELECT * FROM bird_types WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<BirdTypeEntity>>

    @Query("SELECT * FROM bird_types WHERE isArchived = 1 ORDER BY name ASC")
    fun getAllArchived(): Flow<List<BirdTypeEntity>>

    @Query("SELECT * FROM bird_types WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BirdTypeEntity?

    @Query("SELECT * FROM bird_types WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): BirdTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BirdTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BirdTypeEntity>)

    @Update
    suspend fun update(entity: BirdTypeEntity)

    @Query("UPDATE bird_types SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE bird_types SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM bird_types WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM bird_types WHERE isArchived = 0")
    fun getActiveCount(): Flow<Int>
}

@Dao
interface CageDao {
    @Query("SELECT * FROM cages WHERE isArchived = 0 ORDER BY code ASC, name ASC")
    fun getAllActive(): Flow<List<CageEntity>>

    @Query("SELECT * FROM cages WHERE isArchived = 1 ORDER BY code ASC, name ASC")
    fun getAllArchived(): Flow<List<CageEntity>>

    @Query("SELECT * FROM cages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CageEntity>)

    @Update
    suspend fun update(entity: CageEntity)

    @Query("UPDATE cages SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE cages SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM cages WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM cages WHERE isArchived = 0")
    fun getActiveCount(): Flow<Int>
}

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActive(): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getAllArchived(): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BirdEntity?

    @Query("SELECT * FROM birds WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<BirdEntity?>

    @Query("SELECT * FROM birds WHERE cageId = :cageId AND isArchived = 0")
    fun getBirdsInCage(cageId: Long): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds WHERE isArchived = 0 AND gender = 'Jantan'")
    fun getActiveMales(): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds WHERE isArchived = 0 AND gender = 'Betina'")
    fun getActiveFemales(): Flow<List<BirdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BirdEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BirdEntity>)

    @Update
    suspend fun update(entity: BirdEntity)

    @Query("UPDATE birds SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE birds SET isArchived = 1 WHERE id IN (:ids)")
    suspend fun archiveMultiple(ids: List<Long>)

    @Query("UPDATE birds SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE birds SET isArchived = 0 WHERE id IN (:ids)")
    suspend fun restoreMultiple(ids: List<Long>)

    @Query("DELETE FROM birds WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM birds WHERE id IN (:ids)")
    suspend fun deleteMultiplePermanently(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM birds WHERE isArchived = 0")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM birds WHERE isArchived = 0 AND gender = 'Jantan'")
    fun getMaleCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM birds WHERE isArchived = 0 AND gender = 'Betina'")
    fun getFemaleCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM birds WHERE isArchived = 0 AND gender NOT IN ('Jantan', 'Betina')")
    fun getUnknownGenderCount(): Flow<Int>

    @Query("SELECT typeName, COUNT(*) as count FROM birds WHERE isArchived = 0 GROUP BY typeName ORDER BY count DESC")
    fun getSpeciesCounts(): Flow<List<SpeciesCountTuple>>

    @Query("UPDATE birds SET cageId = :newCageId, updatedAt = :updatedAt WHERE id = :birdId")
    suspend fun updateCage(birdId: Long, newCageId: Long?, updatedAt: Long = System.currentTimeMillis())
}

data class SpeciesCountTuple(
    val typeName: String,
    val count: Int
)

@Dao
interface CageMovementDao {
    @Query("SELECT * FROM cage_movements WHERE birdId = :birdId ORDER BY movementDate DESC")
    fun getMovementsForBird(birdId: Long): Flow<List<CageMovementEntity>>

    @Query("SELECT * FROM cage_movements ORDER BY movementDate DESC LIMIT 50")
    fun getRecentMovements(): Flow<List<CageMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CageMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CageMovementEntity>)

    @Query("DELETE FROM cage_movements WHERE birdId = :birdId")
    suspend fun deleteForBird(birdId: Long)
}

@Dao
interface BreedingPairDao {
    @Query("SELECT * FROM breeding_pairs WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<BreedingPairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getAllArchived(): Flow<List<BreedingPairEntity>>

    @Query("SELECT * FROM breeding_pairs WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BreedingPairEntity?

    @Query("SELECT * FROM breeding_pairs WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<BreedingPairEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BreedingPairEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BreedingPairEntity>)

    @Update
    suspend fun update(entity: BreedingPairEntity)

    @Query("UPDATE breeding_pairs SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE breeding_pairs SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM breeding_pairs WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM breeding_pairs WHERE isArchived = 0 AND status = 'Aktif'")
    fun getActiveCount(): Flow<Int>
}

@Dao
interface BreedingCycleDao {
    @Query("SELECT * FROM breeding_cycles WHERE pairId = :pairId AND isArchived = 0 ORDER BY cycleNumber DESC")
    fun getCyclesForPair(pairId: Long): Flow<List<BreedingCycleEntity>>

    @Query("SELECT * FROM breeding_cycles WHERE isArchived = 0 ORDER BY startDate DESC")
    fun getAllActive(): Flow<List<BreedingCycleEntity>>

    @Query("SELECT * FROM breeding_cycles WHERE isArchived = 1 ORDER BY startDate DESC")
    fun getAllArchived(): Flow<List<BreedingCycleEntity>>

    @Query("SELECT * FROM breeding_cycles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BreedingCycleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BreedingCycleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BreedingCycleEntity>)

    @Update
    suspend fun update(entity: BreedingCycleEntity)

    @Query("UPDATE breeding_cycles SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE breeding_cycles SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM breeding_cycles WHERE id = :id")
    suspend fun deletePermanently(id: Long)
}

@Dao
interface EggDao {
    @Query("SELECT * FROM eggs WHERE pairId = :pairId AND isArchived = 0 ORDER BY clutchDate DESC")
    fun getEggsForPair(pairId: Long): Flow<List<EggEntity>>

    @Query("SELECT * FROM eggs WHERE cycleId = :cycleId AND isArchived = 0 ORDER BY clutchDate DESC")
    fun getEggsForCycle(cycleId: Long): Flow<List<EggEntity>>

    @Query("SELECT * FROM eggs WHERE isArchived = 0 ORDER BY clutchDate DESC")
    fun getAllActive(): Flow<List<EggEntity>>

    @Query("SELECT * FROM eggs WHERE isArchived = 1 ORDER BY clutchDate DESC")
    fun getAllArchived(): Flow<List<EggEntity>>

    @Query("SELECT * FROM eggs WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EggEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EggEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<EggEntity>)

    @Update
    suspend fun update(entity: EggEntity)

    @Query("UPDATE eggs SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE eggs SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM eggs WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COALESCE(SUM(totalEggs - hatchedCount - brokenCount), 0) FROM eggs WHERE isArchived = 0 AND status = 'Pengeraman'")
    fun getIncubatingEggCount(): Flow<Int>
}

@Dao
interface ChickDao {
    @Query("SELECT * FROM chicks WHERE isArchived = 0 ORDER BY hatchDate DESC")
    fun getAllActive(): Flow<List<ChickEntity>>

    @Query("SELECT * FROM chicks WHERE isArchived = 1 ORDER BY hatchDate DESC")
    fun getAllArchived(): Flow<List<ChickEntity>>

    @Query("SELECT * FROM chicks WHERE eggId = :eggId AND isArchived = 0")
    fun getChicksForEgg(eggId: Long): Flow<List<ChickEntity>>

    @Query("SELECT * FROM chicks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChickEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChickEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ChickEntity>)

    @Update
    suspend fun update(entity: ChickEntity)

    @Query("UPDATE chicks SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE chicks SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM chicks WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM chicks WHERE isArchived = 0 AND status IN ('Dirawat', 'Sehat')")
    fun getActiveChickCount(): Flow<Int>
}

@Dao
interface CareRecordDao {
    @Query("SELECT * FROM care_records WHERE isArchived = 0 ORDER BY dateTime DESC")
    fun getAllActive(): Flow<List<CareRecordEntity>>

    @Query("SELECT * FROM care_records WHERE isArchived = 1 ORDER BY dateTime DESC")
    fun getAllArchived(): Flow<List<CareRecordEntity>>

    @Query("SELECT * FROM care_records WHERE birdId = :birdId AND isArchived = 0 ORDER BY dateTime DESC")
    fun getRecordsForBird(birdId: Long): Flow<List<CareRecordEntity>>

    @Query("SELECT * FROM care_records WHERE dateTime >= :startOfDay AND dateTime <= :endOfDay AND isArchived = 0 ORDER BY dateTime ASC")
    fun getRecordsForToday(startOfDay: Long, endOfDay: Long): Flow<List<CareRecordEntity>>

    @Query("SELECT * FROM care_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CareRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CareRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CareRecordEntity>)

    @Update
    suspend fun update(entity: CareRecordEntity)

    @Query("UPDATE care_records SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE care_records SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE care_records SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM care_records WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM care_records WHERE dateTime >= :startOfDay AND dateTime <= :endOfDay AND isArchived = 0 AND status != 'Selesai'")
    fun getTodayPendingCount(startOfDay: Long, endOfDay: Long): Flow<Int>
}

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE isArchived = 0 ORDER BY checkDate DESC")
    fun getAllActive(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE isArchived = 1 ORDER BY checkDate DESC")
    fun getAllArchived(): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE birdId = :birdId AND isArchived = 0 ORDER BY checkDate DESC")
    fun getRecordsForBird(birdId: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HealthRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HealthRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<HealthRecordEntity>)

    @Update
    suspend fun update(entity: HealthRecordEntity)

    @Query("UPDATE health_records SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE health_records SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deletePermanently(id: Long)
}

@Dao
interface FeedItemDao {
    @Query("SELECT * FROM feed_items WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items WHERE isArchived = 1 ORDER BY name ASC")
    fun getAllArchived(): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FeedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FeedItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FeedItemEntity>)

    @Update
    suspend fun update(entity: FeedItemEntity)

    @Query("UPDATE feed_items SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE feed_items SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM feed_items WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT * FROM feed_items WHERE isArchived = 0 AND currentStock <= minStockThreshold")
    fun getLowStockItems(): Flow<List<FeedItemEntity>>

    @Query("UPDATE feed_items SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double)
}

@Dao
interface InventoryMovementDao {
    @Query("SELECT * FROM inventory_movements WHERE itemId = :itemId ORDER BY date DESC")
    fun getMovementsForItem(itemId: Long): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements ORDER BY date DESC LIMIT 100")
    fun getRecentMovements(): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InventoryMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<InventoryMovementEntity>)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isArchived = 0 ORDER BY date DESC")
    fun getAllActive(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isArchived = 1 ORDER BY date DESC")
    fun getAllArchived(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate AND isArchived = 0 ORDER BY date DESC")
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TransactionEntity>)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("UPDATE transactions SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE transactions SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'Pemasukan' AND isArchived = 0 AND date >= :startDate AND date <= :endDate")
    fun getIncomeInRange(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'Pengeluaran' AND isArchived = 0 AND date >= :startDate AND date <= :endDate")
    fun getExpenseInRange(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'Pemasukan' AND isArchived = 0")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'Pengeluaran' AND isArchived = 0")
    fun getTotalExpense(): Flow<Double>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isArchived = 0 ORDER BY dateTime ASC")
    fun getAllActive(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isArchived = 1 ORDER BY dateTime ASC")
    fun getAllArchived(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isArchived = 0 AND isCompleted = 0 ORDER BY dateTime ASC")
    fun getPendingReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isArchived = 0 AND isCompleted = 0 ORDER BY dateTime ASC LIMIT 1")
    fun getNextUpcomingReminder(): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ReminderEntity>)

    @Update
    suspend fun update(entity: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("UPDATE reminders SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE reminders SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deletePermanently(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActive(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getAllArchived(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE relatedBirdId = :birdId AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getNotesForBird(birdId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NoteEntity>)

    @Update
    suspend fun update(entity: NoteEntity)

    @Query("UPDATE notes SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE notes SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletePermanently(id: Long)
}

@Dao
interface MasteranAudioDao {
    @Query("SELECT * FROM masteran_audio WHERE isArchived = 0 ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllActive(): Flow<List<MasteranAudioEntity>>

    @Query("SELECT * FROM masteran_audio WHERE isArchived = 0 AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<MasteranAudioEntity>>

    @Query("SELECT * FROM masteran_audio WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MasteranAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MasteranAudioEntity): Long

    @Update
    suspend fun update(entity: MasteranAudioEntity)

    @Query("UPDATE masteran_audio SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE masteran_audio SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    @Query("UPDATE masteran_audio SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM masteran_audio WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM masteran_audio WHERE isArchived = 0")
    fun getActiveCount(): Flow<Int>
}

@Dao
interface CompetitionRecordDao {
    @Query("SELECT * FROM competition_records WHERE isArchived = 0 ORDER BY date DESC")
    fun getAllActive(): Flow<List<CompetitionRecordEntity>>

    @Query("SELECT * FROM competition_records WHERE birdId = :birdId AND isArchived = 0 ORDER BY date DESC")
    fun getByBirdId(birdId: Long): Flow<List<CompetitionRecordEntity>>

    @Query("SELECT * FROM competition_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CompetitionRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CompetitionRecordEntity): Long

    @Update
    suspend fun update(entity: CompetitionRecordEntity)

    @Query("UPDATE competition_records SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM competition_records WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT COUNT(*) FROM competition_records WHERE isArchived = 0")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM competition_records WHERE isArchived = 0 AND rank = 'Juara 1'")
    fun getFirstRankCount(): Flow<Int>
}

