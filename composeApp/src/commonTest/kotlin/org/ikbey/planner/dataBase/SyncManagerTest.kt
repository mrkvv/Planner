package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import org.ikbey.planner.localDB.LocalDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class AbstractSyncManagerTest {
    abstract fun createDriver(): SqlDriver
    private lateinit var database: LocalDatabase
    private lateinit var manager: LocalDatabaseManager
    private lateinit var supabaseRepository: SupabaseRepository
    private lateinit var syncManager: SyncManager

    @BeforeTest
    fun setup() {
        database = LocalDatabase(createDriver())
        manager = LocalDatabaseManager(database)
        supabaseRepository = SupabaseRepository()
        syncManager = SyncManager(manager, supabaseRepository)
    }

    @Test
    fun testLastSyncTime() = runTest {
        assertNull(syncManager.getLastSyncTime())

        val timestamp = 1234567890L
        syncManager.setLastSyncTime(timestamp)

        assertEquals(timestamp, syncManager.getLastSyncTime())

        syncManager.clearLastSyncTime()
        assertNull(syncManager.getLastSyncTime())
    }

    @Test
    fun testGroupIdManipulations() = runTest {
        assertNull(syncManager.getGroupId())

        syncManager.setGroupId(42799)
        assertEquals("42799", syncManager.getGroupId())
    }

    @Test
    fun testSyncIfNeeded() = runTest {
        syncManager.clearLastSyncTime()
        manager.deleteSetting("init_load")
        manager.deleteSetting("group_id")

        // 1) Инит лоад - должны загрузиться факультеты и группы + поставиться настройка "init_load" = "1"
        assertEquals(null, manager.getSetting("init_load"))
        var wasNeeded = syncManager.syncIfNeeded()

        assertTrue(wasNeeded)
        // Проверка на появление факультетов и групп
        val faculties = manager.getFaculties()
        val groups = manager.getGroups()
        assertNotEquals(0, faculties.size)
        assertNotEquals(0, groups.size)
        assertThat(faculties.any {
            it.id == 125 &&
                    it.name == "Институт компьютерных наук и кибербезопасности" &&
                    it.abbr == "ИКНК"
        }).isTrue()
        assertThat(groups.any {
            it.id == 42799 && it.faculty_id == 125 && it.name == "5130904/30107"
        }).isTrue()
        // Проверяем, поставился ли флаг
        assertEquals("1", manager.getSetting("init_load"))


        // 2. Не нужная синхронизация, при НЕ выбранной группе
        wasNeeded = syncManager.syncIfNeeded()
        assertFalse(wasNeeded)

        // 3. Полная синхронизация
        manager.setSetting("group_id", "42799")
        wasNeeded = syncManager.syncIfNeeded()
        assertTrue(wasNeeded)

        // Какое-то время должно было поставиться
        assertNotNull(syncManager.getLastSyncTime())

        // Должны были появиться занятия
        assertNotEquals(0, manager.getCalendarEvents().size)
        assertNotEquals(0, manager.getUserSchedule().size)

    }

}