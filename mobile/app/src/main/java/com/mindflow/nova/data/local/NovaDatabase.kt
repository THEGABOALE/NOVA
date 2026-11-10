package com.mindflow.nova.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [
        CachedUserEntity::class, CachedLevelsEntity::class, CachedMissionEntity::class,
        CachedProgressEntity::class, PendingAttemptEntity::class, RejectedNoticeEntity::class,
        ActiveLessonEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun dao(): NovaDao

    companion object {
        // Excluida del respaldo en backup_rules.xml y data_extraction_rules.xml.
        const val NAME = "nova.db"

        /** La versión 2 suma los avisos de rechazo; los pendientes de la versión 1 se conservan. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `rejected_notice` (`clientAttemptId` TEXT NOT NULL, " +
                        "`userId` INTEGER NOT NULL, `message` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`clientAttemptId`))"
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_rejected_notice_userId` ON `rejected_notice` (`userId`)"
                )
            }
        }

        /** La versión 3 guarda la pausa del reloj y el potenciador de cada pendiente. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `pending_attempt` ADD COLUMN `pausedSeconds` INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE `pending_attempt` ADD COLUMN `usedExtraTime` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** La caché antigua no tiene dueño verificable: se descarta, nunca se reasigna. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("DROP TABLE `cached_mission`")
                connection.execSQL("CREATE TABLE `cached_mission` (`userId` INTEGER NOT NULL, `missionId` INTEGER NOT NULL, `json` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`, `missionId`))")
                connection.execSQL("CREATE TABLE `active_lesson` (`userId` INTEGER NOT NULL, `attemptId` TEXT NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY(`userId`, `attemptId`))")
            }
        }

        fun create(context: Context): NovaDatabase =
            Room.databaseBuilder(context, NovaDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }
}
