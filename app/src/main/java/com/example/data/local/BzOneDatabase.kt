package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BirdEntity::class,
        BirdTypeEntity::class,
        CageEntity::class,
        BreedingPairEntity::class,
        BreedingCycleEntity::class,
        EggEntity::class,
        ChickEntity::class,
        CareRecordEntity::class,
        HealthRecordEntity::class,
        CageMovementEntity::class,
        FeedItemEntity::class,
        InventoryMovementEntity::class,
        TransactionEntity::class,
        ReminderEntity::class,
        NoteEntity::class,
        MasteranAudioEntity::class,
        CompetitionRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BzOneDatabase : RoomDatabase() {

    abstract fun birdDao(): BirdDao
    abstract fun birdTypeDao(): BirdTypeDao
    abstract fun cageDao(): CageDao
    abstract fun breedingPairDao(): BreedingPairDao
    abstract fun breedingCycleDao(): BreedingCycleDao
    abstract fun eggDao(): EggDao
    abstract fun chickDao(): ChickDao
    abstract fun careRecordDao(): CareRecordDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun cageMovementDao(): CageMovementDao
    abstract fun feedItemDao(): FeedItemDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao
    abstract fun masteranAudioDao(): MasteranAudioDao
    abstract fun competitionRecordDao(): CompetitionRecordDao

    companion object {
        @Volatile
        private var INSTANCE: BzOneDatabase? = null

        fun getDatabase(context: Context): BzOneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BzOneDatabase::class.java,
                    "bzone_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
