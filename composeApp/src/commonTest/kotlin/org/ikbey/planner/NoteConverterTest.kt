package org.ikbey.planner

import org.ikbey.planner.dataBase.Note
import kotlin.test.*

class NoteConverterTest {

    @BeforeTest
    fun setUp() {
        initializeNoteIdCounter(0)
    }

    // ========== Тесты для formatDate ==========
    @Test
    fun testFormatDate() {
        assertEquals("2024-05-01", formatDate(2024, 5, 1))
        assertEquals("2024-12-25", formatDate(2024, 12, 25))
        assertEquals("2024-01-09", formatDate(2024, 1, 9))
        // Граничные случаи
        assertEquals("2024-10-10", formatDate(2024, 10, 10))
        assertEquals("2024-09-09", formatDate(2024, 9, 9))
    }

    // ========== Тесты для formatTime ==========
    @Test
    fun testFormatTime() {
        // Короткое время (без изменений)
        assertEquals("10:30", formatTime("10:30"))
        assertEquals("", formatTime(""))

        // Длинное время (обрезание)
        assertEquals("10:30", formatTime("10:30:00"))
        assertEquals("14:05", formatTime("14:05:30"))
        assertEquals("23:59", formatTime("23:59:59"))

        // Пограничные случаи
        assertEquals("10:30", formatTime("10:30:"))
        assertEquals("10:30", formatTime("10:30:0"))
    }

    // ========== Тесты для generateNoteId ==========
    @Test
    fun testGenerateNoteId() {
        assertEquals(1, generateNoteId())
        assertEquals(2, generateNoteId())
        assertEquals(3, generateNoteId())

        // Тест инициализации счетчика
        initializeNoteIdCounter(100)
        assertEquals(101, generateNoteId())
        assertEquals(102, generateNoteId())

        // Сброс и повторное использование
        initializeNoteIdCounter(0)
        assertEquals(1, generateNoteId())
    }

    // ========== Тесты для NoteData.toUserNote() ==========
    @Test
    fun testNoteDataToUserNote_SingleLine() {
        // Одна строка
        val noteData = NoteData(
            startTime = "10:00",
            endTime = "11:00",
            location = "Classroom",
            note = "Simple note",
            isInterval = true,
            isNotification = true,
            date = "2024-05-01",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals(1, result.id)
        assertEquals("2024-05-01", result.date)
        assertEquals("Classroom", result.place)
        assertEquals("Simple note", result.header)
        assertNull(result.note)
        assertEquals(true, result.is_notifications_enabled)
        assertEquals("10:00", result.start_time)
        assertEquals("11:00", result.end_time)
    }

    @Test
    fun testNoteDataToUserNote_MultiLine() {
        // Несколько строк
        val noteData = NoteData(
            startTime = "14:00",
            endTime = "",
            location = "Office",
            note = "Header\nBody line 1\nBody line 2",
            isInterval = false,
            isNotification = false,
            date = "2024-05-02",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals("Header", result.header)
        assertEquals("Body line 1\nBody line 2", result.note)
        assertEquals("14:00", result.start_time)
        assertNull(result.end_time)
        assertEquals(false, result.is_notifications_enabled)
    }

    @Test
    fun testNoteDataToUserNote_EmptyNote() {
        // Пустая заметка
        val noteData = NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "",
            isInterval = false,
            isNotification = false,
            date = "2024-05-03",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertNull(result.header)  // ← ИСПРАВЛЕНО: header.ifEmpty { null } конвертирует "" в null
        assertNull(result.note)    // ← body тоже будет null
        assertEquals("", result.place)  // ← location передается как есть
        assertEquals("09:00", result.start_time)
        assertNull(result.end_time)
    }

    @Test
    fun testNoteDataToUserNote_TwoLines() {
        // Ровно две строки
        val noteData = NoteData(
            startTime = "15:00",
            endTime = "16:00",
            location = "Room",
            note = "Title\nDescription",
            isInterval = true,
            isNotification = true,
            date = "2024-05-04",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals("Title", result.header)
        assertEquals("Description", result.note)
        assertEquals("Room", result.place)
        assertEquals(true, result.is_notifications_enabled)
    }

    @Test
    fun testNoteDataToUserNote_ManyLines() {
        // Много строк
        val noteData = NoteData(
            startTime = "08:00",
            endTime = "09:00",
            location = "Auditorium",
            note = "Main Title\nLine 1\nLine 2\nLine 3\nLine 4",
            isInterval = true,
            isNotification = false,
            date = "2024-05-05",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals("Main Title", result.header)
        assertEquals("Line 1\nLine 2\nLine 3\nLine 4", result.note)
    }

    // ========== Тесты для Schedule.toNoteData() ==========
    @Test
    fun testScheduleToNoteData_WithAllFields() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 1,
            group_id = 123,
            date = "2024-05-01",
            weekday = 1,
            subject = "Mathematics",
            type = "Lecture",
            start_time = "09:00:00",
            end_time = "10:30:00",
            teacher = "Dr. Smith",
            place = "Main Building",
            audithory = "A101",
            is_done = false
        )

        val result = schedule.toNoteData()

        assertEquals("09:00", result.startTime)
        assertEquals("10:30", result.endTime)
        assertEquals("Main Building, A101", result.location)
        assertTrue(result.note.contains("Mathematics (Lecture)"))
        assertTrue(result.note.contains("Преподаватель: Dr. Smith"))
        assertEquals(true, result.isInterval)
        assertEquals(false, result.isNotification)
        assertEquals("2024-05-01", result.date)
        assertEquals(NoteType.SCHEDULE, result.type)
    }

    @Test
    fun testScheduleToNoteData_WithoutTeacher() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 2,
            group_id = 123,
            date = "2024-05-01",
            weekday = 1,
            subject = "Physics",
            type = "Practice",
            start_time = "11:00:00",
            end_time = "12:30:00",
            teacher = null,
            place = "Science Building",
            audithory = "Lab 1",
            is_done = false
        )

        val result = schedule.toNoteData()

        assertEquals("Physics (Practice)", result.note)
        assertFalse(result.note.contains("Преподаватель:"))
        assertEquals("Science Building, Lab 1", result.location)
    }

    @Test
    fun testScheduleToNoteData_EmptyType() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 3,
            group_id = 123,
            date = "2024-05-01",
            weekday = 1,
            subject = "Chemistry",
            type = "",
            start_time = "13:00:00",
            end_time = "14:30:00",
            teacher = "Prof. Brown",
            place = null,
            audithory = "B202",
            is_done = false
        )

        val result = schedule.toNoteData()

        // ВНИМАНИЕ: при пустом типе скобки не добавляются
        assertEquals("Chemistry\nПреподаватель: Prof. Brown", result.note) // ← ИСПРАВЛЕНО
        assertEquals("B202", result.location) // Только аудитория
    }

    @Test
    fun testScheduleToNoteData_OnlyPlace() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 4,
            group_id = 123,
            date = "2024-05-01",
            weekday = 1,
            subject = "Biology",
            type = "Seminar",
            start_time = "15:00:00",
            end_time = "16:30:00",
            teacher = null,
            place = "Online",
            audithory = null,
            is_done = false
        )

        val result = schedule.toNoteData()

        assertEquals("Online", result.location) // Только место
    }

    @Test
    fun testScheduleToNoteData_NoLocation() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 5,
            group_id = 123,
            date = "2024-05-01",
            weekday = 1,
            subject = "History",
            type = "Lecture",
            start_time = "17:00:00",
            end_time = "18:30:00",
            teacher = "Dr. White",
            place = null,
            audithory = null,
            is_done = false
        )

        val result = schedule.toNoteData()

        assertEquals("", result.location) // Пустая локация
        assertTrue(result.note.contains("History (Lecture)"))
        assertTrue(result.note.contains("Преподаватель: Dr. White"))
    }

    // ========== Тесты для Schedule.toNote() ==========
    @Test
    fun testScheduleToNote_WithTeacher() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 1,
            group_id = 123,
            date = "2024-05-01",
            weekday = 2,
            subject = "Physics",
            type = "Lab",
            start_time = "09:00:00",
            end_time = "10:30:00",
            teacher = "Prof. Johnson",
            place = "Science Building",
            audithory = "Lab 3",
            is_done = false
        )

        val result = schedule.toNote()

        assertEquals(1, result.id)
        assertEquals("2024-05-01", result.date)
        assertEquals("Science Building, Lab 3", result.place)
        assertEquals("Physics (Lab)", result.header)
        assertEquals("Преподаватель: Prof. Johnson", result.note)
        assertEquals("09:00", result.start_time)
        assertEquals("10:30", result.end_time)
        assertEquals(false, result.is_done)
    }

    @Test
    fun testScheduleToNote_WithoutTeacher() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 2,
            group_id = 123,
            date = "2024-05-01",
            weekday = 2,
            subject = "Math",
            type = "Lecture",
            start_time = "11:00:00",
            end_time = "12:30:00",
            teacher = null,
            place = "Main Building",
            audithory = "A101",
            is_done = true
        )

        val result = schedule.toNote()

        assertEquals("Math (Lecture)", result.header)
        assertNull(result.note) // Пустое тело заметки
        assertEquals(true, result.is_done)
    }

    @Test
    fun testScheduleToNote_EmptyType() {
        val schedule = org.ikbey.planner.dataBase.Schedule(
            id = 3,
            group_id = 123,
            date = "2024-05-01",
            weekday = 2,
            subject = "English",
            type = "",
            start_time = "13:00:00",
            end_time = "14:30:00",
            teacher = "Mrs. Smith",
            place = null,
            audithory = "B202",
            is_done = false
        )

        val result = schedule.toNote()

        assertEquals("English", result.header) // Без скобок
        assertEquals("Преподаватель: Mrs. Smith", result.note)
        assertEquals("B202", result.place) // Только аудитория
    }

    // ========== Тесты для CalendarEvent.toNoteData() ==========
    @Test
    fun testCalendarEventToNoteData_WithAllFields() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 1,
            title = "Team Meeting",
            description = "Weekly team sync",
            date = "2024-05-01",
            start_time = "15:00:00",
            end_time = "16:00:00",
            location = "Conference Room A",
            creator = "manager@company.com",
            calendar_name = "Work Calendar",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNoteData()

        assertEquals("15:00", result.startTime)
        assertEquals("16:00", result.endTime)
        assertEquals("Conference Room A", result.location)
        assertTrue(result.note.contains("Team Meeting"))
        assertTrue(result.note.contains("Weekly team sync"))
        assertTrue(result.note.contains("Место: Conference Room A"))
        assertTrue(result.note.contains("Организатор: Work Calendar"))
        assertEquals(true, result.isInterval)
        assertEquals(false, result.isNotification)
        assertEquals(NoteType.CALENDAR_EVENT, result.type)
    }

    @Test
    fun testCalendarEventToNoteData_WithoutDescription() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 2,
            title = "Doctor Appointment",
            description = null,
            date = "2024-05-01",
            start_time = "10:00:00",
            end_time = "11:00:00",
            location = "Medical Center",
            creator = "user@email.com",
            calendar_name = "Personal",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNoteData()

        assertTrue(result.note.contains("Doctor Appointment"))
        assertFalse(result.note.contains("\n\n")) // Не должно быть пустых строк
        assertTrue(result.note.contains("Место: Medical Center"))
        assertTrue(result.note.contains("Организатор: Personal"))
    }

    @Test
    fun testCalendarEventToNoteData_WithoutLocation() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 3,
            title = "Birthday Party",
            description = "Celebration",
            date = "2024-05-01",
            start_time = "18:00:00",
            end_time = "23:00:00",
            location = null,
            creator = "friend@email.com",
            calendar_name = "Social",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNoteData()

        assertEquals("", result.location)
        assertTrue(result.note.contains("Birthday Party"))
        assertTrue(result.note.contains("Celebration"))
        assertFalse(result.note.contains("Место:")) // Не должно быть секции места
        assertTrue(result.note.contains("Организатор: Social"))
    }

    @Test
    fun testCalendarEventToNoteData_MinimalFields() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 4,
            title = "Simple Event",
            description = null,
            date = "2024-05-01",
            start_time = "12:00:00",
            end_time = "13:00:00",
            location = null,
            creator = "test@test.com",
            calendar_name = "Test Calendar",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNoteData()

        // ВНИМАНИЕ: даже при минимальных полях все равно добавляется организатор
        assertEquals("Simple Event\nОрганизатор: Test Calendar", result.note.trim()) // ← ИСПРАВЛЕНО
        assertEquals("", result.location)
    }

    // ========== Тесты для CalendarEvent.toNote() ==========
    @Test
    fun testCalendarEventToNote_WithAllFields() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 1,
            title = "Doctor Appointment",
            description = "Annual checkup",
            date = "2024-05-01",
            start_time = "15:00:00",
            end_time = "16:00:00",
            location = "Medical Center",
            creator = "user@email.com",
            calendar_name = "Personal",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNote()

        assertEquals(1, result.id)
        assertEquals("2024-05-01", result.date)
        assertEquals("Medical Center", result.place)
        assertEquals("Doctor Appointment", result.header)
        assertEquals("Annual checkup\nМесто: Medical Center\nОрганизатор: Personal", result.note)
        assertEquals("15:00", result.start_time)
        assertEquals("16:00", result.end_time)
        assertEquals(false, result.is_done)
    }

    @Test
    fun testCalendarEventToNote_OnlyDescription() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 2,
            title = "Meeting",
            description = "Important discussion",
            date = "2024-05-01",
            start_time = "10:00:00",
            end_time = "11:00:00",
            location = null,
            creator = "boss@company.com",
            calendar_name = "Work",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNote()

        assertEquals("Important discussion\nОрганизатор: Work", result.note)
        assertNull(result.place)
    }

    @Test
    fun testCalendarEventToNote_OnlyLocation() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 3,
            title = "Conference",
            description = null,
            date = "2024-05-01",
            start_time = "09:00:00",
            end_time = "17:00:00",
            location = "Convention Center",
            creator = "org@conf.com",
            calendar_name = "Professional",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNote()

        assertEquals("Место: Convention Center\nОрганизатор: Professional", result.note)
        assertEquals("Convention Center", result.place)
    }

    @Test
    fun testCalendarEventToNote_OnlyCalendarName() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 4,
            title = "Private Event",
            description = null,
            date = "2024-05-01",
            start_time = "20:00:00",
            end_time = "22:00:00",
            location = null,
            creator = "me@personal.com",
            calendar_name = "My Calendar",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNote()

        assertEquals("Организатор: My Calendar", result.note)
        assertNull(result.place)
    }

    @Test
    fun testCalendarEventToNote_EmptyBody() {
        val calendarEvent = org.ikbey.planner.dataBase.CalendarEvent(
            id = 5,
            title = "Simple Event",
            description = null,
            date = "2024-05-01",
            start_time = "12:00:00",
            end_time = "13:00:00",
            location = null,
            creator = "test@test.com",
            calendar_name = "",
            is_tracked = false,
            is_done = false
        )

        val result = calendarEvent.toNote()

        assertNull(result.note) // Пустое тело заметки
        assertNull(result.place)
    }

    // ========== Тесты для Note.toNoteData() ==========
    @Test
    fun testNoteToNoteData_WithHeaderAndBody() {
        val note = Note(
            id = 1,
            date = "2024-05-01",
            place = "Home",
            header = "Shopping List",
            note = "Milk\nEggs\nBread",
            is_notifications_enabled = true,
            start_time = "10:00",
            end_time = "11:00",
            is_done = false
        )

        val result = note.toNoteData()

        assertEquals("10:00", result.startTime)
        assertEquals("11:00", result.endTime)
        assertEquals("Home", result.location)
        assertEquals("Shopping List\nMilk\nEggs\nBread", result.note)
        assertEquals(true, result.isInterval)
        assertEquals(true, result.isNotification)
        assertEquals("2024-05-01", result.date)
        assertEquals(NoteType.USER_NOTE, result.type)
    }

    @Test
    fun testNoteToNoteData_OnlyHeader() {
        val note = Note(
            id = 2,
            date = "2024-05-01",
            place = "Office",
            header = "Meeting",
            note = null,
            is_notifications_enabled = false,
            start_time = "14:00",
            end_time = null,
            is_done = false
        )

        val result = note.toNoteData()

        assertEquals("14:00", result.startTime)
        assertEquals("", result.endTime)
        assertEquals("Office", result.location)
        assertEquals("Meeting", result.note)
        assertEquals(false, result.isInterval)
        assertEquals(false, result.isNotification)
    }

    @Test
    fun testNoteToNoteData_OnlyBody() {
        val note = Note(
            id = 3,
            date = "2024-05-01",
            place = null,
            header = null,
            note = "Remember to call John",
            is_notifications_enabled = true,
            start_time = "15:00",
            end_time = "15:30",
            is_done = false
        )

        val result = note.toNoteData()

        assertEquals("15:00", result.startTime)
        assertEquals("15:30", result.endTime)
        assertEquals("", result.location)
        assertEquals("Remember to call John", result.note)
        assertEquals(true, result.isInterval)
        assertEquals(true, result.isNotification)
    }

    @Test
    fun testNoteToNoteData_NoHeaderNoBody() {
        val note = Note(
            id = 4,
            date = "2024-05-01",
            place = null,
            header = null,
            note = null,
            is_notifications_enabled = null,
            start_time = null,
            end_time = null,
            is_done = false
        )

        val result = note.toNoteData()

        assertEquals("", result.startTime)
        assertEquals("", result.endTime)
        assertEquals("", result.location)
        assertEquals("", result.note)
        assertEquals(false, result.isInterval)
        assertEquals(false, result.isNotification)
    }

    @Test
    fun testNoteToNoteData_NullNotificationEnabled() {
        val note = Note(
            id = 5,
            date = "2024-05-01",
            place = "Test",
            header = "Test",
            note = "Test",
            is_notifications_enabled = null,
            start_time = "09:00",
            end_time = "10:00",
            is_done = false
        )

        val result = note.toNoteData()

        assertEquals(false, result.isNotification) // null должно конвертироваться в false
    }

    // ========== Тесты для граничных случаев ==========

    @Test
    fun testEmptyAndNullValues() {
        val noteData = NoteData(
            startTime = "",
            endTime = "",
            location = "",
            note = "",
            isInterval = false,
            isNotification = false,
            date = "2024-05-01",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals("2024-05-01", result.date)
        assertEquals("", result.place)  // ← ИСПРАВЛЕНО: location передается как есть, без конвертации в null
        assertNull(result.header)  // ← ИСПРАВЛЕНО: header.ifEmpty { null } конвертирует "" в null
        assertNull(result.note)
        assertEquals("", result.start_time)
        assertNull(result.end_time)
    }

    @Test
    fun testNoteDataWithNullLocation() {
        val noteData = NoteData(
            startTime = "10:00",
            endTime = "",
            location = "Some location",
            note = "Test note",
            isInterval = false,
            isNotification = false,
            date = "2024-05-01",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals("Some location", result.place)  // Непустая location остается как есть
        assertEquals("Test note", result.header)
        assertNull(result.note)
    }

    @Test
    fun testNoteDataWithEmptyLocation() {
        val noteData = NoteData(
            startTime = "10:00",
            endTime = "",
            location = "",  // Пустая location
            note = "Test note",
            isInterval = false,
            isNotification = false,
            date = "2024-05-01",
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertEquals("", result.place)  // Пустая location остается пустой строкой
        assertEquals("Test note", result.header)
        assertNull(result.note)
    }

    @Test
    fun testNoteDataWithNullDate() {
        val noteData = NoteData(
            startTime = "10:00",
            endTime = "",
            location = "Test",
            note = "Test note",
            isInterval = false,
            isNotification = false,
            date = null,
            type = NoteType.USER_NOTE
        )

        val result = noteData.toUserNote()

        assertNull(result.date)
        assertEquals("Test", result.place)
        assertEquals("Test note", result.header)
        assertNull(result.note)
    }
}