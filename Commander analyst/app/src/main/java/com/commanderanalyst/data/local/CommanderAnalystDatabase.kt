package com.commanderanalyst.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ContainerEntity::class,
        InventoryEntryEntity::class,
        DeckEntity::class,
        DeckSlotEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class CommanderAnalystDatabase : RoomDatabase() {
    abstract fun containerDao(): ContainerDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun deckDao(): DeckDao
    abstract fun deckSlotDao(): DeckSlotDao

    companion object {
        @Volatile
        private var instance: CommanderAnalystDatabase? = null

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inventory_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        containerId TEXT NOT NULL,
                        cardName TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        FOREIGN KEY(containerId) REFERENCES containers(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_entries_containerId ON inventory_entries(containerId)")
            }
        }

        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS decks (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        commanderName TEXT,
                        containerId TEXT,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS deck_slots (
                        id TEXT NOT NULL PRIMARY KEY,
                        deckId TEXT NOT NULL,
                        cardName TEXT NOT NULL,
                        desiredQuantity INTEGER NOT NULL,
                        section TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        FOREIGN KEY(deckId) REFERENCES decks(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_deck_slots_deckId ON deck_slots(deckId)")
            }
        }

        fun getInstance(context: Context): CommanderAnalystDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CommanderAnalystDatabase::class.java,
                    "commander-analyst.db"
                )
                    .addMigrations(migration1To2, migration2To3)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
