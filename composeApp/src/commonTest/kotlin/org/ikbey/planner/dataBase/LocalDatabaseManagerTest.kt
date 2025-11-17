package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.test.runTest
import org.ikbey.planner.localDB.LocalDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractLocalDatabaseManagerTest {
    abstract fun createDriver(): SqlDriver
    private lateinit var database: LocalDatabase
    private lateinit var manager: LocalDatabaseManager

    @BeforeTest
    fun setup() {
        database = LocalDatabase(createDriver())
        manager = LocalDatabaseManager(database)
    }

    @Test
    fun testInsertGetAndDeleteFaculties() = runTest {
        val faculty = Faculty(125, "Институт Компьютерных Наук и Кибербезопасности", "ИКНК")

        manager.insertFaculty(faculty)
        var faculties = manager.getFaculties()

        assertEquals(1, faculties.size)
        assertEquals("Институт Компьютерных Наук и Кибербезопасности", faculties[0].name)
        assertEquals("ИКНК", faculties[0].abbr)

        manager.deleteAllFaculties()
        faculties = manager.getFaculties()
        assertEquals(0, faculties.size)
    }

    @Test
    fun testInsertGetAndDeleteGroups() = runTest {
        val group1 = Group(1, 125, "5130904/30107")
        val group2 = Group(2, 125, "5130904/30106")
        val group3 = Group(3, 120, "12087/120")
        val group4 = Group(4, 119, "11987/119")

        manager.insertGroup(group1)
        manager.insertGroup(group2)
        manager.insertGroup(group3)
        manager.insertGroup(group4)

        val allGroups = manager.getGroups()
        assertEquals(4, allGroups.size)
        assertEquals(125, allGroups[0].faculty_id)

        val groupsByFaculty = manager.getGroupsByFaculty(125)
        assertEquals(2, groupsByFaculty.size)
        assertEquals(125, groupsByFaculty[0].faculty_id)

        manager.deleteAllGroups()
        val emptyList = manager.getGroups()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun testCalendarEvents() = runTest {
        val calEvent1 = CalendarEvent(1, "ПРОФ.ИКНК: Квизмафия", "desc",
            "2025-10-01", "18:00:00", "20:00:00", "ГЗ1",
            "event@profunion.pro", "ПРОФ.ИКНК", true, false)
        val calEvent2 = CalendarEvent(2, "ПРОФ.ИКНК: Квизмафия", "desc",
            "2025-10-10", "19:00:00", "21:00:00", "ГЗ2",
            "event@profunion.pro", "ПРОФ.ИКНК", true, true)
        val calEvent3 = CalendarEvent(3, "ПРОФ.ИКНК: Квизмафия", "desc",
            "2025-10-20", "20:00:00", "22:00:00", "ГЗ3",
            "event@profunion.pro", "ПРОФ.ИКНК", false, true)
        val calEvent4 = CalendarEvent(4, "ПРОФ.ИКНК: Квизмафия", "desc",
            "2025-10-30", "21:00:00", "23:00:00", "ГЗ4",
            "event@profunion.pro", "ПРОФ.ИКНК", false, false)
        val calEvent5forDelete = CalendarEvent(5, "ПРОФ.ИКНК: Квизмафия", "desc",
            "2025-11-11", "21:00:00", "23:00:00", "ГЗ4",
            "event@profunion.pro", "ПРОФ.ИКНК", true, true)

        manager.insertCalendarEvent(calEvent1)
        manager.insertCalendarEvent(calEvent2)
        manager.insertCalendarEvent(calEvent3)
        manager.insertCalendarEvent(calEvent4)
        manager.insertCalendarEvent(calEvent5forDelete)

        manager.updateCalendarEventTracking(4, true)
        manager.updateCalendarEventIsDone(4, true)

        var allCalEvents = manager.getCalendarEvents()
        val calEventsByDate = manager.getCalendarEventsByDate("2025-10-20")
        val calEventsByDateRange = manager.getCalendarEventsByDateRange("2025-10-20", "2025-10-30")
        val trackedCalEvents = manager.getTrackedCalendarEvents()
        val trackedCalEventsByDate = manager.getTrackedCalendarEventsByDate("2025-10-01")

        assertEquals(5, allCalEvents.size)
        assertEquals(1, calEventsByDate.size)
        assertEquals("2025-10-20", calEventsByDate[0].date)
        assertEquals(2, calEventsByDateRange.size)
        assertEquals(4, trackedCalEvents.size)
        assertEquals(1, trackedCalEventsByDate.size)

        manager.deleteCalendarEvent(5)
        allCalEvents = manager.getCalendarEvents()
        assertEquals(4, allCalEvents.size)

        manager.deleteAllCalendarEvents()
        allCalEvents = manager.getCalendarEvents()
        assertEquals(0, allCalEvents.size)
    }

    @Test
    fun testSchedule() = runTest {
        val schedule1 = Schedule(1, 42799, "2025-10-01", 1, "КПО",
            "Практика", "12:00:00", "14:00:00", "Юркин Владимир Андреевич",
            "107", "3 уч. к.", false)
        val schedule2 = Schedule(2, 42799, "2025-10-10", 1, "КПО",
            "Практика", "12:00:00", "14:00:00", "Юркин Владимир Андреевич",
            "107", "3 уч. к.", false)
        val schedule3 = Schedule(3, 42799, "2025-10-20", 1, "КПО",
            "Практика", "12:00:00", "14:00:00", "Юркин Владимир Андреевич",
            "107", "3 уч. к.", true)

        manager.insertUserSchedule(schedule1)
        manager.insertUserSchedule(schedule2)
        manager.insertUserSchedule(schedule3)

        manager.updateUserScheduleIsDone(2, true)

        var allSchedule = manager.getUserSchedule()
        val scheduleByDate = manager.getUserScheduleByDate("2025-10-10")
        val scheduleByDateRange = manager.getUserScheduleByDateRange("2025-10-10", "2025-10-20")

        assertEquals(3, allSchedule.size)
        assertEquals(true, allSchedule[1].is_done)
        assertEquals(1, scheduleByDate.size)
        assertEquals(2, scheduleByDateRange.size)

        manager.deleteUserSchedule()
        allSchedule = manager.getUserSchedule()
        assertEquals(0, allSchedule.size)
    }

    @Test
    fun testUserNotes() = runTest {
        val note1 = Note(1, "2025-10-01", "Дом", "Навести порядок", "Не забыть пропылесосить!",
            false, "16:00:00", "18:00:00", false)
        val note2 = Note(2, "2025-10-10", "Дом", "Навести порядок", "Не забыть пропылесосить!",
            false, "16:00:00", "18:00:00", false)
        val note3 = Note(3, "2025-10-20", "Дом", "Навести порядок", "Не забыть пропылесосить!",
            true, "16:00:00", "18:00:00", true)
        val note4 = Note(4, "2025-10-20", "Дом", "Навести порядок", "Не забыть пропылесосить!",
            true, "16:00:00", "18:00:00", true)

        manager.insertUserNote(note1)
        manager.insertUserNote(note2)
        manager.insertUserNote(note3)
        manager.insertUserNote(note4)

        manager.updateUserNoteIsDone(2, true)

        var allNotes = manager.getAllUserNotes()
        val notesByDate = manager.getUserNotesByDate("2025-10-10")
        assertEquals(4, allNotes.size)
        assertEquals(false, allNotes[0].is_done)
        assertEquals(true, allNotes[1].is_done)
        assertEquals(1, notesByDate.size)

        manager.deleteUserNote(4)
        allNotes = manager.getAllUserNotes()
        assertEquals(3, allNotes.size)

        manager.deleteAllUserNotes()
        allNotes = manager.getAllUserNotes()
        assertEquals(0, allNotes.size)
    }

    @Test
    fun testStickyNotes() = runTest {
        val stickyNote1 = StickyNote(1, "Аптека", "Сходить после пар")
        val stickyNote2 = StickyNote(2, "Пропылесосить", "Обязательно!")
        val stickyNote3 = StickyNote(3, "Провести выступление котят", "Театр оперы и балета")

        manager.insertStickyNote(stickyNote1)
        manager.insertStickyNote(stickyNote2)
        manager.insertStickyNote(stickyNote3)

        var allStickyNotes = manager.getAllStickyNotes()
        assertEquals(3, allStickyNotes.size)
        assertEquals("Аптека", allStickyNotes[0].header)

        val changedStickyNote1 = StickyNote(1, "Аптека на Ветеранов", "Сходить после пар")
        manager.updateStickyNote(changedStickyNote1)
        allStickyNotes = manager.getAllStickyNotes()
        assertEquals("Аптека на Ветеранов", allStickyNotes[0].header)

        var stickyNoteById = manager.getStickyNoteById(1)
        assertEquals("Аптека на Ветеранов", stickyNoteById[0].header)

        manager.deleteStickyNote(1)
        stickyNoteById = manager.getStickyNoteById(1)
        assertEquals(0, stickyNoteById.size)

        manager.deleteAllStickyNotes()
        allStickyNotes = manager.getAllStickyNotes()
        assertEquals(0, allStickyNotes.size)
    }

    @Test
    fun testSettings() = runTest {
        manager.setSetting("set_key1", "here")
        manager.setSetting("set_key2", "not_here")

        val set1 = manager.getSetting("set_key1")
        val set2 = manager.getSetting("set_key2")
        val set3 = manager.getSetting("set_key3")

        assertEquals("here", set1)
        assertEquals("not_here", set2)
        assertEquals(null, set3)

        manager.deleteSetting("set_key1")
        val deletedSet1 = manager.getSetting("set_key1")
        assertEquals(null, deletedSet1)
    }
}