package com.mindflow.nova.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Las v1-v3 no exportaban esquema: recreamos su DDL y Room valida la v4 al abrir. */
@RunWith(AndroidJUnit4::class)
class NovaDatabaseMigrationTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun allLegacyVersionsKeepPendingAndDiscardUnownedCache() = runBlocking {
        for (version in 1..3) {
            val name = "migration-$version.db"
            context.deleteDatabase(name)
            context.getDatabasePath(name).parentFile?.mkdirs()
            val legacy = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null)
            listOf("cached_user", "cached_levels", "cached_progress").forEach {
                legacy.execSQL("CREATE TABLE `$it` (`userId` INTEGER NOT NULL PRIMARY KEY, `json` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL)")
            }
            legacy.execSQL("CREATE TABLE `cached_mission` (`missionId` INTEGER NOT NULL PRIMARY KEY, `json` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL)")
            val pauseColumns = if (version >= 3) ", `pausedSeconds` INTEGER NOT NULL DEFAULT 0, `usedExtraTime` INTEGER NOT NULL DEFAULT 0" else ""
            legacy.execSQL("CREATE TABLE `pending_attempt` (`clientAttemptId` TEXT NOT NULL PRIMARY KEY, `userId` INTEGER NOT NULL, `missionId` INTEGER NOT NULL, `startedAt` TEXT NOT NULL, `finishedAt` TEXT NOT NULL, `tzOffsetMinutes` INTEGER NOT NULL, `timedOut` INTEGER NOT NULL, `answersJson` TEXT NOT NULL, `provisionalJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL$pauseColumns)")
            legacy.execSQL("CREATE INDEX `index_pending_attempt_userId` ON `pending_attempt` (`userId`)")
            if (version >= 2) {
                legacy.execSQL("CREATE TABLE `rejected_notice` (`clientAttemptId` TEXT NOT NULL PRIMARY KEY, `userId` INTEGER NOT NULL, `message` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                legacy.execSQL("CREATE INDEX `index_rejected_notice_userId` ON `rejected_notice` (`userId`)")
                legacy.execSQL("INSERT INTO `rejected_notice` VALUES ('notice', 7, 'rechazado', 0)")
            }
            legacy.execSQL("INSERT INTO `cached_mission` VALUES (9, '{}', 0)")
            legacy.execSQL("INSERT INTO `pending_attempt` (clientAttemptId,userId,missionId,startedAt,finishedAt,tzOffsetMinutes,timedOut,answersJson,provisionalJson,createdAt) VALUES ('pending',7,9,'start','finish',-360,0,'[]','{}',0)")
            if (version == 3) legacy.execSQL("UPDATE pending_attempt SET pausedSeconds=45, usedExtraTime=1")
            legacy.version = version
            legacy.close()

            val db = Room.databaseBuilder(context, NovaDatabase::class.java, name)
                .addMigrations(NovaDatabase.MIGRATION_1_2, NovaDatabase.MIGRATION_2_3, NovaDatabase.MIGRATION_3_4).build()
            try {
                val dao = db.dao()
                val pending = dao.pending(7).single()
                assertEquals("pending", pending.clientAttemptId)
                assertEquals(if (version == 3) 45 else 0, pending.pausedSeconds)
                assertEquals(version == 3, pending.usedExtraTime)
                assertNull(dao.mission(7, 9))
                dao.upsertMission(CachedMissionEntity(7, 9, "seven", 0))
                dao.upsertMission(CachedMissionEntity(8, 9, "eight", 0))
                assertEquals("seven", dao.mission(7, 9)?.json)
                assertEquals("eight", dao.mission(8, 9)?.json)
                dao.upsertActiveLesson(ActiveLessonEntity(7, "active", "{}"))
                dao.clearAccount(7)
                assertNull(dao.mission(7, 9))
                assertNull(dao.activeLesson(7, "active"))
                assertEquals("eight", dao.mission(8, 9)?.json)
                assertEquals(1, dao.pending(7).size)
            } finally {
                db.close()
                context.deleteDatabase(name)
            }
        }
    }
}
