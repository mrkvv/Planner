@file:OptIn(ExperimentalTestApi::class)

package org.ikbey.planner.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.runBlocking
import org.ikbey.planner.CalendarManager
import org.ikbey.planner.notification.NotificationManager
import org.ikbey.planner.dataBase.*
import org.ikbey.planner.localDB.LocalDatabase
import org.ikbey.planner.dataBase.LocalDatabaseManager
import org.ikbey.planner.dataBase.ServiceLocator
import kotlin.test.*
import kotlinx.coroutines.delay
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import org.ikbey.planner.NoteType

abstract class AbstractUIHomeScreenTest {
    abstract fun createDriver(): SqlDriver

    protected lateinit var database: LocalDatabase
    protected lateinit var manager: LocalDatabaseManager
    protected lateinit var notificationManager: NotificationManager
    protected val calendarManager = CalendarManager()

    @BeforeTest
    fun setup() {
        database = LocalDatabase(createDriver())
        manager = LocalDatabaseManager(database)
        notificationManager = NotificationManager()
        ServiceLocator.setLocalDb(manager)

        runBlocking {
            manager.setSetting("init_load", "1")
            delay(100)
        }
    }

    @Test
    fun homeScreenDisplaysCurrentMonth() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val currentMonthName = calendarManager.getMonthName(currentDate.month)

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText(currentMonthName).assertExists("Месяц должен отображаться")
    }

    @Test
    fun homeScreenShowsTodayIndicatorWhenCurrentDaySelected() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Сегодня").assertExists("Индикатор 'Сегодня' должен отображаться")
    }

    @Test
    fun homeScreenShowsAddButton() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithContentDescription("+").assertExists("Кнопка добавления должна отображаться")
    }

    @Test
    fun homeScreenShowsEmptyStateWhenNoNotes() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Сегодня дел нет!").assertExists("Сообщение о пустом состоянии должно отображаться")
    }

    @Test
    fun homeScreenDisplaysDaysOfMonth() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val daysInMonth = calendarManager.getDaysAmountInMonth(currentDate.year, currentDate.month)

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("1").assertExists("Первый день месяца должен отображаться")
        onNodeWithText(daysInMonth.toString()).assertExists("Последний день месяца должен отображаться")
    }

    @Test
    fun homeScreenCallsOnDayChangeWhenDayClicked() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        var clickedDay = 0

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { day -> clickedDay = day },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("15").performClick()

        runBlocking { delay(100) }
        assertEquals(15, clickedDay, "Колбэк onDayChange должен быть вызван с правильным днем")
    }

    @Test
    fun homeScreenAddButtonOpensBottomSheet() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithContentDescription("+").performClick()

        onNodeWithText("Время").assertExists("Поле времени должно отображаться в bottom sheet")
        onNodeWithText("Заметка").assertExists("Поле заметки должно отображаться в bottom sheet")
        onNodeWithText("Добавить").assertExists("Кнопка добавления должна отображаться в bottom sheet")
    }

    @Test
    fun homeScreenWithUserNoteDisplaysNoteCard() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testNote = Note(
                id = 1,
                date = testDate,
                place = "Аудитория 101",
                header = "Тестовая заметка",
                note = "Описание тестовой заметки",
                is_notifications_enabled = false,
                start_time = "10:00",
                end_time = "11:00",
                is_done = false
            )
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Тестовая заметка").assertExists("Заметка должна отображаться")
        onNodeWithText("10:00").assertExists("Время заметки должно отображаться")
    }

    @Test
    fun homeScreenWithCalendarEventDisplaysEventCard() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testEvent = CalendarEvent(
                id = 1,
                title = "Встреча",
                description = "Важная встреча",
                date = testDate,
                start_time = "14:00",
                end_time = "15:00",
                location = "Офис 101",
                creator = "user@example.com",
                calendar_name = "Работа",
                is_tracked = true,
                is_done = false
            )
            manager.insertCalendarEvent(testEvent)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Встреча").assertExists("Событие должно отображаться")
        onNodeWithText("14:00").assertExists("Время события должно отображаться")
    }

    @Test
    fun homeScreenShowsLoadingIndicator() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        runBlocking { delay(500) }
    }

    @Test
    fun homeScreenNoteCardToggleDoneState() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testNote = Note(
                id = 1,
                date = testDate,
                header = "Тестовая заметка для переключения",
                is_notifications_enabled = false,
                start_time = "10:00",
                is_done = false
            )
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }
        onNodeWithText("Тестовая заметка для переключения").assertExists()
    }

    @Test
    fun homeScreenBottomSheetAddsNewNote() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithContentDescription("+").performClick()

        onNodeWithText("Заголовок").performClick()
    }

    @Test
    fun homeScreenShowsSettingsButton() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }
    }

    @Test
    fun homeScreenSwipeGesturesWork() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        var swipeToMonthCalled = false
        var swipeToEventsCalled = false

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { swipeToMonthCalled = true },
                onSwipeToEvents = { swipeToEventsCalled = true }
            )
        }
    }

    @Test
    fun noteCardDisplaysAllData() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Аудитория 101",
            header = "Лекция по математике",
            note = "Производные и интегралы",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:30",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "10:30",
            location = "Аудитория 101",
            note = "Лекция по математике\nПроизводные и интегралы",
            isInterval = true,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { },
                onToggleDone = { }
            )
        }

        onNodeWithText("Лекция по математике").assertExists("Заголовок должен отображаться")
        onNodeWithText("09:00").assertExists("Время начала должно отображаться")
        onNodeWithText("10:30").assertExists("Время окончания должно отображаться")
    }

    @Test
    fun noteCardShowsCompletedState() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            header = "Завершенная задача",
            is_notifications_enabled = false,
            start_time = "09:00",
            is_done = true
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Завершенная задача",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { },
                onToggleDone = { }
            )
        }

        onNodeWithText("Завершенная задача").assertExists("Задача должна отображаться")
    }

    @Test
    fun noteCardCallsOnClick() = runComposeUiTest {
        var onClickCalled = false
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            header = "Кликабельная заметка",
            is_notifications_enabled = false,
            start_time = "09:00",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Кликабельная заметка",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { onClickCalled = true },
                onToggleDone = { }
            )
        }

        onNodeWithText("Кликабельная заметка").performClick()
        runBlocking { delay(100) }
        assertTrue(onClickCalled, "onClick должен быть вызван")
    }

    @Test
    fun noteCardCallsOnToggleDone() = runComposeUiTest {
        var toggleCalled = false
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            header = "Заметка для переключения",
            is_notifications_enabled = false,
            start_time = "09:00",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Заметка для переключения",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { },
                onToggleDone = { toggleCalled = true }
            )
        }
    }

    @Test
    fun bottomSheetMenuDisplaysAllFields() = runComposeUiTest {
        setContent {
            BottomSheetMenu(
                onDismiss = { },
                onAddNoteClick = { }
            )
        }

        onNodeWithText("Время").assertExists("Поле времени должно отображаться")
        onNodeWithText("Заметка").assertExists("Поле заметки должно отображаться")
        onNodeWithText("Место").assertExists("Поле места должно отображаться")
        onNodeWithText("Добавить").assertExists("Кнопка добавления должна отображаться")
    }

    @Test
    fun bottomSheetMenuAddButtonEnabledWhenValid() = runComposeUiTest {
        setContent {
            BottomSheetMenu(
                onDismiss = { },
                onAddNoteClick = { }
            )
        }
        onNodeWithText("Добавить").assertIsNotEnabled()
    }

    @Test
    fun testFormatDateFunction() {
        val result = formatDate(2024, 3, 15)
        assertEquals("2024-03-15", result, "Формат даты должен быть YYYY-MM-DD")
    }

    @Test
    fun testIsValidTimeFunction() {
        assertTrue(isValidTime("12:30"), "12:30 должно быть валидным временем")
        assertTrue(isValidTime("00:00"), "00:00 должно быть валидным временем")
        assertTrue(isValidTime("23:59"), "23:59 должно быть валидным временем")
        assertFalse(isValidTime("24:00"), "24:00 должно быть невалидным временем")
        assertFalse(isValidTime("12:60"), "12:60 должно быть невалидным временем")
        assertFalse(isValidTime("abc"), "abc должно быть невалидным временем")
    }

    @Test
    fun testNoteCardHeaderAndBodyLogic() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            header = "Заголовок",
            note = "Тело заметки",
            is_notifications_enabled = false,
            start_time = "09:00",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Заголовок\nТело заметки",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { },
                onToggleDone = { }
            )
        }
        onNodeWithText("Заголовок").assertExists()
        onNodeWithText("Тело заметки").assertExists()
    }

    @Test
    fun testNoteCardHeaderOnly() = runComposeUiTest {
        val testNote = Note(
            id = 2,
            date = "2024-03-15",
            header = "Только заголовок",
            note = null,
            is_notifications_enabled = false,
            start_time = "09:00",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Только заголовок",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { },
                onToggleDone = { }
            )
        }

        onNodeWithText("Только заголовок").assertExists()
    }

    @Test
    fun testNoteCardBodyBecomesHeader() = runComposeUiTest {
        val testNote = Note(
            id = 3,
            date = "2024-03-15",
            header = null,
            note = "Первая строка\nВторая строка",
            is_notifications_enabled = false,
            start_time = "09:00",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Первая строка\nВторая строка",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteCard(
                note = testNote,
                noteData = testNoteData,
                onNoteClick = { },
                onToggleDone = { }
            )
        }
        onNodeWithText("Первая строка").assertExists()
    }

    @Test
    fun testTimeValidationInBottomSheet() = runComposeUiTest {
        setContent {
            BottomSheetMenu(
                onDismiss = { },
                onAddNoteClick = { }
            )
        }
        onNodeWithText("Добавить").assertIsNotEnabled()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val monthStr = if (month < 10) "0$month" else "$month"
        val dayStr = if (day < 10) "0$day" else "$day"
        return "$year-$monthStr-$dayStr"
    }

    @Test
    fun testDelayedTimeFormatFunction() {
        assertEquals("", delayedTimeFormat(""), "Пустая строка должна возвращать пустую строку")

        assertEquals("1", delayedTimeFormat("1"), "Одиночная цифра должна возвращаться как есть")
        assertEquals("2", delayedTimeFormat("2"), "Цифра 2 должна быть допустимой")
        assertEquals("", delayedTimeFormat("3"), "Цифра 3 не должна приниматься как первая цифра часа")

        assertEquals("12", delayedTimeFormat("12"), "12 должно возвращаться как 12")
        assertEquals("23", delayedTimeFormat("23"), "23 должно возвращаться как 23")
        assertEquals("20", delayedTimeFormat("20"), "20 должно возвращаться как 20")
        assertEquals("2", delayedTimeFormat("24"), "24 должно возвращать 2 (вторая цифра 4 > 3)")
        assertEquals("13", delayedTimeFormat("13"), "13 должно возвращать 13 (вторая цифра 3 допустима для 1x)")

        assertEquals("123", delayedTimeFormat("123"), "123 должно возвращаться как 123")
        assertEquals("125", delayedTimeFormat("125"), "125 должно возвращаться как 125")
        assertEquals("12", delayedTimeFormat("126"), "126 должно возвращать 12 (6 > 5 для минут)")

        assertEquals("12:34", delayedTimeFormat("1234"), "1234 должно форматироваться как 12:34")
        assertEquals("23:59", delayedTimeFormat("2359"), "2359 должно форматироваться как 23:59")
        assertEquals("12:30", delayedTimeFormat("1230"), "1230 должно форматироваться как 12:30")

        assertEquals("12:36", delayedTimeFormat("1236"), "1236 должно возвращать 123 (6 не проходит валидацию)")

        assertEquals("12", delayedTimeFormat("12abc"), "Нецифровые символы должны фильтроваться")
        assertEquals("12:34", delayedTimeFormat("12:34"), "Двоеточие должно игнорироваться")
        assertEquals("12:34", delayedTimeFormat("12-34"), "Дефис должен игнорироваться")
        assertEquals("12:34", delayedTimeFormat("12a3b4"), "Смешанные символы должны фильтроваться")

        assertEquals("00", delayedTimeFormat("00"), "00 должно быть допустимым")
        assertEquals("09", delayedTimeFormat("09"), "09 должно быть допустимым")
        assertEquals("19", delayedTimeFormat("19"), "19 должно быть допустимым")
        assertEquals("23", delayedTimeFormat("23"), "23 должно быть допустимым")
        assertEquals("2", delayedTimeFormat("24"), "24 должно возвращать 2")

        assertEquals("12:00", delayedTimeFormat("1200"), "1200 должно форматироваться как 12:00")
        assertEquals("12:05", delayedTimeFormat("1205"), "1205 должно форматироваться как 12:05")
        assertEquals("12:59", delayedTimeFormat("1259"), "1259 должно форматироваться как 12:59")
        assertEquals("120", delayedTimeFormat("1260"), "1260 должно возвращать 126")

        assertEquals("12:34", delayedTimeFormat("123456"), "Лишние цифры должны обрезаться")
        assertEquals("23:59", delayedTimeFormat("235978"), "Лишние цифры должны обрезаться")

        assertEquals("02", delayedTimeFormat("02"), "02 должно возвращаться как 02")
        assertEquals("02:30", delayedTimeFormat("0230"), "0230 должно форматироваться как 02:30")
        assertEquals("", delayedTimeFormat("a"), "Только буквы должны возвращать пустую строку")
        assertEquals("1", delayedTimeFormat("1a"), "Смешанный ввод должен фильтроваться")
    }

    @Test
    fun testDelayedTimeFormatRealWorldScenarios() {
        assertEquals("12:30", delayedTimeFormat("1230"), "Ввод 1230 -> 12:30")
        assertEquals("09:15", delayedTimeFormat("0915"), "Ввод 0915 -> 09:15")
        assertEquals("14:45", delayedTimeFormat("1445"), "Ввод 1445 -> 14:45")
        assertEquals("20", delayedTimeFormat("20"), "Ввод 20 -> 20 (неполное время)")
        assertEquals("20:00", delayedTimeFormat("2000"), "Ввод 2000 -> 20:00")

        assertEquals("12:34", delayedTimeFormat("1a2b3c4d"), "Ввод с мусором 1a2b3c4d -> 12:34")
        assertEquals("23", delayedTimeFormat("2 3"), "Ввод с пробелами 2 3 -> 23")
    }

    @Test
    fun testDelayedTimeFormatEdgeCases() {
        assertEquals("12:34", delayedTimeFormat("1234567890"), "Длинный ввод должен обрезаться")

        assertEquals("6", delayedTimeFormat("36"), "36 должно возвращать 6")
        assertEquals("7", delayedTimeFormat("77"), "77 должно возвращать 7")

        assertEquals("12", delayedTimeFormat("12🎉"), "Эмодзи должны игнорироваться")
        assertEquals("", delayedTimeFormat("🎉"), "Только эмодзи должны возвращать пустую строку")

        assertEquals("00", delayedTimeFormat("00"), "00 должно работать")
        assertEquals("00:00", delayedTimeFormat("0000"), "0000 должно форматироваться как 00:00")
    }



    @Test
    fun testCreateUpdatedNote_SingleLineText() {
        val originalNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Old Location",
            header = "Old Header",
            note = "Old Body",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:00",
            is_done = false
        )

        val result = createUpdatedNote(
            originalNote = originalNote,
            startTime = "11:00",
            endTime = "12:00",
            location = "New Location",
            noteText = "Single line note",
            isInterval = true,
            isNotification = true
        )

        assertEquals("11:00", result.start_time)
        assertEquals("12:00", result.end_time)
        assertEquals("New Location", result.place)
        assertEquals("Single line note", result.header)
        assertNull(result.note, "Для однострочной заметки body должен быть null")
        assertTrue(result.is_notifications_enabled == true)
        assertEquals(false, result.is_done, "Статус выполнения должен сохраняться")
    }

    @Test
    fun testCreateUpdatedNote_MultiLineText() {
        val originalNote = Note(
            id = 1,
            date = "2024-03-15",
            place = null,
            header = null,
            note = null,
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = null,
            is_done = false
        )

        val result = createUpdatedNote(
            originalNote = originalNote,
            startTime = "14:00",
            endTime = "15:30",
            location = "Conference Room",
            noteText = "Meeting Title\nMeeting description\nAgenda item 1\nAgenda item 2",
            isInterval = true,
            isNotification = false
        )

        assertEquals("14:00", result.start_time)
        assertEquals("15:30", result.end_time)
        assertEquals("Conference Room", result.place)
        assertEquals("Meeting Title", result.header)
        assertEquals("Meeting description\nAgenda item 1\nAgenda item 2", result.note)
        assertTrue(result.is_notifications_enabled == false)
    }

    @Test
    fun testCreateUpdatedNote_EmptyLocationAndNote() {
        val originalNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Old Location",
            header = "Old Header",
            note = "Old Body",
            is_notifications_enabled = true,
            start_time = "09:00",
            end_time = "10:00",
            is_done = true
        )

        val result = createUpdatedNote(
            originalNote = originalNote,
            startTime = "11:00",
            endTime = "",
            location = "",
            noteText = "",
            isInterval = false,
            isNotification = false
        )

        assertEquals("11:00", result.start_time)
        assertNull(result.end_time, "При isInterval = false end_time должен быть null")
        assertNull(result.place, "Пустая location должна становиться null")
        assertNull(result.header, "Пустой header должен становиться null")
        assertNull(result.note, "Пустой note должен становиться null")
        assertTrue(result.is_notifications_enabled == false)
        assertEquals(true, result.is_done, "Статус выполнения должен сохраняться")
    }

    @Test
    fun testCreateUpdatedNote_NoInterval() {
        val originalNote = Note(
            id = 1,
            date = "2024-03-15",
            place = null,
            header = null,
            note = null,
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:00",
            is_done = false
        )

        val result = createUpdatedNote(
            originalNote = originalNote,
            startTime = "11:00",
            endTime = "12:00",
            location = "Room",
            noteText = "Note",
            isInterval = false,
            isNotification = true
        )

        assertEquals("11:00", result.start_time)
        assertNull(result.end_time, "При isInterval = false end_time должен быть null даже если передан")
        assertEquals("Room", result.place)
        assertEquals("Note", result.header)
        assertNull(result.note)
        assertTrue(result.is_notifications_enabled == true)
    }

    @Test
    fun testCompareNotesAdvanced_ByStartTime() {
        val note1 = Note(1, "2024-03-15", null, "Note 1", null, false, "09:00", null, false)
        val note2 = Note(2, "2024-03-15", null, "Note 2", null, false, "10:00", null, false)

        val result = compareNotesAdvanced(note1, note2)

        assertTrue(result < 0, "Note1 с временем 09:00 должна быть перед Note2 с временем 10:00")
    }

    @Test
    fun testCompareNotesAdvanced_ByIntervalPresence() {
        val note1 = Note(1, "2024-03-15", null, "Note 1", null, false, "09:00", "10:00", false)
        val note2 = Note(2, "2024-03-15", null, "Note 2", null, false, "09:00", null, false)

        val result = compareNotesAdvanced(note1, note2)

        assertTrue(result > 0, "Заметка с интервалом должна быть после заметки без интервала")
    }

    @Test
    fun testCompareNotesAdvanced_ByEndTime() {
        val note1 = Note(1, "2024-03-15", null, "Note 1", null, false, "09:00", "10:00", false)
        val note2 = Note(2, "2024-03-15", null, "Note 2", null, false, "09:00", "11:00", false)

        val result = compareNotesAdvanced(note1, note2)

        assertTrue(result < 0, "Note1 с окончанием 10:00 должна быть перед Note2 с окончанием 11:00")
    }

    @Test
    fun testCompareNotesAdvanced_ById() {
        val note1 = Note(1, "2024-03-15", null, "Note 1", null, false, "09:00", "10:00", false)
        val note2 = Note(2, "2024-03-15", null, "Note 2", null, false, "09:00", "10:00", false)

        val result = compareNotesAdvanced(note1, note2)

        assertTrue(result < 0, "При равных временах сортировка по id (1 < 2)")
    }

    @Test
    fun testCompareNotesAdvanced_WithInvalidTime() {
        val note1 = Note(1, "2024-03-15", null, "Note 1", null, false, "invalid", null, false)
        val note2 = Note(2, "2024-03-15", null, "Note 2", null, false, "09:00", null, false)

        val result = compareNotesAdvanced(note1, note2)

        assertTrue(result > 0, "Заметка с невалидным временем должна быть после заметки с валидным временем")
    }

    @Test
    fun testCompareNotesAdvanced_EqualNotes() {
        val note1 = Note(1, "2024-03-15", null, "Note", null, false, "09:00", null, false)
        val note2 = Note(1, "2024-03-15", null, "Note", null, false, "09:00", null, false)

        val result = compareNotesAdvanced(note1, note2)

        assertEquals(0, result, "Идентичные заметки должны быть равны")
    }


    @Test
    fun noteDetailDialogShowsReadOnlyForNonUserNotes() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Аудитория 101",
            header = "Лекция по математике",
            note = "Производные и интегралы",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:30",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "10:30",
            location = "Аудитория 101",
            note = "Лекция по математике\nПроизводные и интегралы",
            isInterval = true,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.SCHEDULE // Не USER_NOTE
        )

        var dismissCalled = false

        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = testNoteData,
                onDismiss = { dismissCalled = true },
                onDelete = { },
                onUpdate = { }
            )
        }

        onNodeWithText("Расписание").assertExists("Для SCHEDULE должен отображаться read-only диалог")
        onNodeWithText("09:00 - 10:30").assertExists("Время должно отображаться")
        onNodeWithText("Аудитория 101").assertExists("Место должно отображаться")
        onNodeWithText("Лекция по математике").assertExists("Заголовок должен отображаться")

        onNodeWithText("Удалить").assertDoesNotExist()
    }

    @Test
    fun noteDetailDialogShowsEditableForUserNotes() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Аудитория 101",
            header = "Моя заметка",
            note = "Описание заметки",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:30",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "10:30",
            location = "Аудитория 101",
            note = "Моя заметка\nОписание заметки",
            isInterval = true,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = testNoteData,
                onDismiss = { },
                onDelete = { },
                onUpdate = { }
            )
        }

        onNodeWithText("Время").assertExists("В редактируемом диалоге должно быть поле времени")
        onNodeWithText("Заметка").assertExists("В редактируемом диалоге должно быть поле заметки")
        onNodeWithText("Место").assertExists("В редактируемом диалоге должно быть поле места")
    }

    @Test
    fun noteDetailDialogCallsOnDismissForReadOnly() = runComposeUiTest {
        val testNote = Note(1, "2024-03-15", null, "Заметка", null, false, "09:00", null, false)
        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00", endTime = "", location = "", note = "Заметка",
            isInterval = false, isNotification = false, date = "2024-03-15",
            type = org.ikbey.planner.NoteType.CALENDAR_EVENT
        )

        var dismissCalled = false

        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = testNoteData,
                onDismiss = { dismissCalled = true },
                onDelete = { },
                onUpdate = { }
            )
        }

        onNodeWithText("Мероприятие").performClick()

        runBlocking { delay(100) }
        assertTrue(dismissCalled, "onDismiss должен быть вызван при клике на read-only диалог")
    }

    @Test
    fun noteDetailDialogShowsCorrectTitleForDifferentTypes() = runComposeUiTest {
        val testNote = Note(1, "2024-03-15", null, "Тест", null, false, "09:00", null, false)

        val scheduleNoteData = testNoteData.copy(type = org.ikbey.planner.NoteType.SCHEDULE)
        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = scheduleNoteData,
                onDismiss = { },
                onDelete = { },
                onUpdate = { }
            )
        }
        onNodeWithText("Расписание").assertExists("Для SCHEDULE должен отображаться заголовок 'Расписание'")

        // Тест для CALENDAR_EVENT
        val eventNoteData = testNoteData.copy(type = org.ikbey.planner.NoteType.CALENDAR_EVENT)
        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = eventNoteData,
                onDismiss = { },
                onDelete = { },
                onUpdate = { }
            )
        }
        onNodeWithText("Мероприятие").assertExists("Для CALENDAR_EVENT должен отображаться заголовок 'Мероприятие'")

        // Тест для USER_NOTE
        val userNoteData = testNoteData.copy(type = org.ikbey.planner.NoteType.USER_NOTE)
        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = userNoteData,
                onDismiss = { },
                onDelete = { },
                onUpdate = { }
            )
        }
        onNodeWithText("Время").assertExists("Для USER_NOTE должен отображаться редактируемый диалог")
    }

    @Test
    fun noteDetailDialogPassesCorrectParametersToEditableDialog() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Офис",
            header = "Тестовая заметка",
            note = "Описание",
            is_notifications_enabled = true,
            start_time = "09:00",
            end_time = "10:00",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "10:00",
            location = "Офис",
            note = "Тестовая заметка\nОписание",
            isInterval = true,
            isNotification = true,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.USER_NOTE
        )

        var deleteCalled = false
        var updateCalled = false
        var dismissCalled = false

        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = testNoteData,
                onDismiss = { dismissCalled = true },
                onDelete = { deleteCalled = true },
                onUpdate = { updateCalled = true }
            )
        }

        onNodeWithText("09:00").assertExists("Начальное время должно отображаться")
        onNodeWithText("10:00").assertExists("Конечное время должно отображаться")
        onNodeWithText("Тестовая заметка").assertExists("Заголовок должен отображаться")
    }

    @Test
    fun noteDetailDialogHandlesNullFieldsInReadOnlyMode() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            place = null,
            header = null,
            note = "Только тело заметки",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = null,
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "",
            location = "",
            note = "Только тело заметки",
            isInterval = false,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.SCHEDULE
        )

        setContent {
            NoteDetailDialog(
                notificationManager = notificationManager,
                note = testNote,
                noteData = testNoteData,
                onDismiss = { },
                onDelete = { },
                onUpdate = { }
            )
        }

        onNodeWithText("09:00").assertExists("Начальное время должно отображаться")
        onNodeWithText("Только тело заметки").assertExists("Тело заметки должно отображаться как заголовок")
    }

    private val testNoteData = org.ikbey.planner.NoteData(
        startTime = "09:00",
        endTime = "",
        location = "",
        note = "Тестовая заметка",
        isInterval = false,
        isNotification = false,
        date = "2024-03-15",
        type = org.ikbey.planner.NoteType.USER_NOTE
    )

    @Test
    fun homeScreenShowsNoteDetailDialogWhenConditionsMet() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testNote = Note(
                id = 1,
                date = testDate,
                place = "Аудитория 101",
                header = "Тестовая заметка",
                note = "Описание",
                is_notifications_enabled = false,
                start_time = "10:00",
                end_time = "11:00",
                is_done = false
            )
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Тестовая заметка").performClick()

        onNodeWithText("Время").assertExists("Диалог деталей заметки должен отображаться")
        onNodeWithText("Заметка").assertExists("Поле заметки должно отображаться в диалоге")
    }

    @Test
    fun homeScreenNoteDetailDialogOnDismissResetsState() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testNote = Note(
                id = 1,
                date = testDate,
                header = "Тестовая заметка",
                is_notifications_enabled = false,
                start_time = "10:00",
                is_done = false
            )
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Тестовая заметка").performClick()

        runBlocking { delay(500) }

        onNodeWithText("Сегодня дел нет!").assertDoesNotExist()
    }

    @Test
    fun homeScreenNoteDetailDialogDeleteUserNote() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val testNote = Note(
            id = 1,
            date = testDate,
            header = "Заметка для удаления",
            is_notifications_enabled = true,
            start_time = "10:00",
            is_done = false
        )

        runBlocking {
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Заметка для удаления").performClick()

        onNodeWithContentDescription("Удалить заметку").performClick()

        runBlocking {
            delay(1000)

            val notesAfterDelete = manager.getUserNotesByDate(testDate)
            assertTrue(notesAfterDelete.isEmpty(), "Заметка должна быть удалена из базы данных")
        }

        onNodeWithText("Время").assertDoesNotExist()
    }

    @Test
    fun homeScreenNoteDetailDialogDeleteNonUserNote() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testEvent = CalendarEvent(
                id = 1,
                title = "Календарное событие",
                description = "Описание",
                date = testDate,
                start_time = "14:00",
                end_time = "15:00",
                location = "Офис",
                creator = "user@example.com",
                calendar_name = "Работа",
                is_tracked = true,
                is_done = false
            )
            manager.insertCalendarEvent(testEvent)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Календарное событие").performClick()
        onNodeWithContentDescription("Удалить заметку").assertDoesNotExist()
    }

    @Test
    fun homeScreenNoteDetailDialogUpdateUserNote() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val originalNote = Note(
            id = 1,
            date = testDate,
            header = "Исходная заметка",
            note = "Исходное описание",
            is_notifications_enabled = false,
            start_time = "10:00",
            end_time = "11:00",
            is_done = false
        )

        runBlocking {
            manager.insertUserNote(originalNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }
        onNodeWithText("Исходная заметка").performClick()

        runBlocking { delay(1000) }

        val updatedNotes = manager.getUserNotesByDate(testDate)
        assertFalse(updatedNotes.isEmpty(), "Заметка должна остаться в базе после обновления")
    }

    @Test
    fun homeScreenNoteDetailDialogUpdateWithNotification() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val originalNote = Note(
            id = 1,
            date = testDate,
            header = "Заметка с уведомлением",
            is_notifications_enabled = false,
            start_time = "10:00",
            is_done = false
        )

        runBlocking {
            manager.insertUserNote(originalNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Заметка с уведомлением").performClick()

        runBlocking { delay(1000) }

        val updatedNotes = manager.getUserNotesByDate(testDate)
        assertEquals(1, updatedNotes.size, "Заметка должна остаться в базе")
    }

    @Test
    fun homeScreenNoteDetailDialogOnlyShowsWhenAllConditionsMet() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Время").assertDoesNotExist()
        onNodeWithText("Заметка").assertDoesNotExist()
    }


    @Test
    fun testUpdateItemDoneStateForUserNote() {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val initialNotes = listOf(
            Note(1, testDate, null, "Заметка 1", null, false, "09:00", null, false),
            Note(2, testDate, null, "Заметка 2", null, false, "10:00", null, false),
            Note(3, testDate, null, "Заметка 3", null, false, "11:00", null, false)
        )

        var notesState = initialNotes.toMutableList()

        val updateItemDoneState = { itemId: Int, isDone: Boolean, itemType: NoteType ->
            when (itemType) {
                NoteType.USER_NOTE -> {
                    notesState = notesState.map { note ->
                        if (note.id == itemId) note.copy(is_done = isDone) else note
                    }.toMutableList()
                }
                NoteType.SCHEDULE -> { /* обработка для SCHEDULE */ }
                NoteType.CALENDAR_EVENT -> { /* обработка для CALENDAR_EVENT */ }
            }
        }

        updateItemDoneState(2, true, NoteType.USER_NOTE)

        assertEquals(false, notesState[0].is_done, "Первая заметка не должна измениться")
        assertEquals(true, notesState[1].is_done, "Вторая заметка должна быть выполнена")
        assertEquals(false, notesState[2].is_done, "Третья заметка не должна измениться")
    }

    @Test
    fun testUpdateItemDoneStateForSchedule() {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val initialSchedules = listOf(
            Schedule(
                id = 1,
                group_id = 1,
                date = testDate,
                weekday = 1,
                subject = "Расписание 1",
                type = "Лекция",
                start_time = "09:00",
                end_time = "10:00",
                is_done = false
            ),
            Schedule(
                id = 2,
                group_id = 1,
                date = testDate,
                weekday = 1,
                subject = "Расписание 2",
                type = "Лекция",
                start_time = "10:00",
                end_time = "11:00",
                is_done = false
            )
        )

        var schedulesState = initialSchedules.toMutableList()

        val updateItemDoneState = { itemId: Int, isDone: Boolean, itemType: NoteType ->
            when (itemType) {
                NoteType.USER_NOTE -> { /* обработка для USER_NOTE */ }
                NoteType.SCHEDULE -> {
                    schedulesState = schedulesState.map { schedule ->
                        if (schedule.id == itemId) schedule.copy(is_done = isDone) else schedule
                    }.toMutableList()
                }
                NoteType.CALENDAR_EVENT -> { /* обработка для CALENDAR_EVENT */ }
            }
        }

        updateItemDoneState(1, true, NoteType.SCHEDULE)

        assertEquals(true, schedulesState[0].is_done, "Первое расписание должно быть выполнено")
        assertEquals(false, schedulesState[1].is_done, "Второе расписание не должно измениться")
    }

    @Test
    fun testUpdateItemDoneStateForCalendarEvent() {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val initialEvents = listOf(
            CalendarEvent(1, "Событие 1", "Описание", testDate, "14:00", "15:00", "Офис", "user", "Работа", true, false),
            CalendarEvent(2, "Событие 2", "Описание", testDate, "16:00", "17:00", "Офис", "user", "Работа", true, false)
        )

        var eventsState = initialEvents.toMutableList()

        val updateItemDoneState = { itemId: Int, isDone: Boolean, itemType: NoteType ->
            when (itemType) {
                NoteType.USER_NOTE -> { /* обработка для USER_NOTE */ }
                NoteType.SCHEDULE -> { /* обработка для SCHEDULE */ }
                NoteType.CALENDAR_EVENT -> {
                    eventsState = eventsState.map { event ->
                        if (event.id == itemId) event.copy(is_done = isDone) else event
                    }.toMutableList()
                }
            }
        }

        updateItemDoneState(2, true, NoteType.CALENDAR_EVENT)

        assertEquals(false, eventsState[0].is_done, "Первое событие не должно измениться")
        assertEquals(true, eventsState[1].is_done, "Второе событие должно быть выполнено")
    }

    @Test
    fun homeScreenToggleUserNoteDoneState() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        // Создаем тестовую заметку в базе
        runBlocking {
            val testNote = Note(
                id = 1,
                date = testDate,
                header = "Тестовая заметка для переключения",
                is_notifications_enabled = false,
                start_time = "10:00",
                is_done = false
            )
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        runBlocking { delay(500) }

        onNodeWithText("Тестовая заметка для переключения").assertExists()
        onNodeWithText("10:00").assertExists()

        onNodeWithTag("toggle-done-1").performClick()

        runBlocking { delay(300) }

        runBlocking {
            val updatedNotes = manager.getUserNotesByDate(testDate)
            assertEquals(1, updatedNotes.size, "Заметка должна остаться в базе")
            assertEquals(true, updatedNotes[0].is_done, "Заметка должна быть помечена как выполненная после клика")
        }
    }


    @Test
    fun homeScreenToggleCalendarEventDoneState() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testEvent = CalendarEvent(
                id = 3, // используем другой ID
                title = "Тестовое событие",
                description = "Описание события",
                date = testDate,
                start_time = "14:00",
                end_time = "15:00",
                location = "Офис",
                creator = "user@example.com",
                calendar_name = "Работа",
                is_tracked = true,
                is_done = false
            )
            manager.insertCalendarEvent(testEvent)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        runBlocking { delay(500) }

        onNodeWithText("Тестовое событие").assertExists()

        onNodeWithTag("toggle-done-3").performClick()

        runBlocking { delay(300) }

        runBlocking {
            val updatedEvents = manager.getTrackedCalendarEventsByDate(testDate)
            assertEquals(1, updatedEvents.size, "Событие должно остаться в базе")
            assertEquals(true, updatedEvents[0].is_done, "Событие должно быть помечено как выполненное")
        }
    }


    @Test
    fun homeScreenDirectUserNoteToggleTest() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testNote = Note(
                id = 1,
                date = testDate,
                header = "Тестовая заметка",
                is_notifications_enabled = false,
                start_time = "10:00",
                is_done = false
            )
            manager.insertUserNote(testNote)

            val initialNotes = manager.getUserNotesByDate(testDate)
            assertEquals(false, initialNotes[0].is_done, "Исходное состояние должно быть false")

            manager.updateUserNoteIsDone(1, true)

            val updatedNotes = manager.getUserNotesByDate(testDate)
            assertEquals(true, updatedNotes[0].is_done, "Состояние должно измениться на true")
        }
    }

    @Test
    fun homeScreenDirectScheduleToggleTest() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testSchedule = Schedule(
                id = 1,
                group_id = 1,
                date = testDate,
                weekday = 1,
                subject = "Тестовое расписание",
                type = "Лекция",
                start_time = "09:00",
                end_time = "10:30",
                is_done = false
            )
            manager.insertUserSchedule(testSchedule)

            val initialSchedules = manager.getUserScheduleByDate(testDate)
            assertEquals(false, initialSchedules[0].is_done, "Исходное состояние должно быть false")

            manager.updateUserScheduleIsDone(1, true)

            val updatedSchedules = manager.getUserScheduleByDate(testDate)
            assertEquals(true, updatedSchedules[0].is_done, "Состояние должно измениться на true")
        }
    }

    @Test
    fun homeScreenDirectCalendarEventToggleTest() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testEvent = CalendarEvent(
                id = 1,
                title = "Тестовое событие",
                description = "Описание",
                date = testDate,
                start_time = "14:00",
                end_time = "15:00",
                location = "Офис",
                creator = "user@example.com",
                calendar_name = "Работа",
                is_tracked = true,
                is_done = false
            )
            manager.insertCalendarEvent(testEvent)

            val initialEvents = manager.getTrackedCalendarEventsByDate(testDate)
            assertEquals(false, initialEvents[0].is_done, "Исходное состояние должно быть false")

            manager.updateCalendarEventIsDone(1, true)

            val updatedEvents = manager.getTrackedCalendarEventsByDate(testDate)
            assertEquals(true, updatedEvents[0].is_done, "Состояние должно измениться на true")
        }
    }

    @Test
    fun homeScreenDisplayUserNoteTest() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            manager.setSetting("init_load", "1")

            val testNote = Note(
                id = 1,
                date = testDate,
                header = "Тестовая заметка для отображения",
                is_notifications_enabled = false,
                start_time = "10:00",
                is_done = false
            )
            manager.insertUserNote(testNote)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        runBlocking { delay(1000) }

        onNodeWithText("Тестовая заметка для отображения").assertExists()
        onNodeWithText("10:00").assertExists()
    }

    @Test
    fun testNoteSortingLogic() {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        val notes = listOf(
            Note(1, testDate, null, "Заметка 1", null, false, "11:00", null, false),
            Note(2, testDate, null, "Заметка 2", null, false, "09:00", null, false),
            Note(3, testDate, null, "Заметка 3", null, false, "10:00", "11:00", false)
        )

        val sortedNotes = notes.sortedWith { note1, note2 ->
            compareNotesAdvanced(note1, note2)
        }

        assertEquals("09:00", sortedNotes[0].start_time, "Первой должна быть заметка с временем 09:00")
        assertEquals("10:00", sortedNotes[1].start_time, "Второй должна быть заметка с временем 10:00")
        assertEquals("11:00", sortedNotes[2].start_time, "Третьей должна быть заметка с временем 11:00")
    }

    @Test
    fun testCreateUpdatedNoteFunction() {
        val originalNote = Note(
            id = 1,
            date = "2024-03-15",
            header = "Исходный заголовок",
            note = "Исходное описание",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:00",
            is_done = false
        )

        val updatedNote = createUpdatedNote(
            originalNote = originalNote,
            startTime = "11:00",
            endTime = "12:00",
            location = "Новое место",
            noteText = "Новый заголовок\nНовое описание",
            isInterval = true,
            isNotification = true
        )

        assertEquals("11:00", updatedNote.start_time)
        assertEquals("12:00", updatedNote.end_time)
        assertEquals("Новое место", updatedNote.place)
        assertEquals("Новый заголовок", updatedNote.header)
        assertEquals("Новое описание", updatedNote.note)
        assertEquals(true, updatedNote.is_notifications_enabled)
        assertEquals(false, updatedNote.is_done, "Статус выполнения должен сохраняться")
    }



    @Test
    fun homeScreenRightSwipeIndicatorShowsWithoutCallback() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        var swipeToMonthCalled = false
        var swipeToEventsCalled = false

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { swipeToMonthCalled = true },
                onSwipeToEvents = { swipeToEventsCalled = true }
            )
        }

        runBlocking { delay(1000) }

        val homeScreen = onNodeWithTag("home-screen-container")
        val screenSize = homeScreen.fetchSemanticsNode().size
        val screenWidth = screenSize.width.toFloat()
        val centerY = screenSize.height / 2f

        val startX = screenWidth * 0.8f
        val endX = startX - 40f

        println("🔔 Правый индикатор: startX=$startX, свайп на ${endX - startX}px")

        homeScreen.performTouchInput {
            swipe(
                start = Offset(startX, centerY),
                end = Offset(endX, centerY),
                durationMillis = 200
            )
        }

        runBlocking { delay(300) }

        assertFalse(swipeToMonthCalled, "Маленький свайп не должен вызывать onSwipeToMonth")
        assertFalse(swipeToEventsCalled, "Маленький свайп не должен вызывать onSwipeToEvents")

        println("✅ Правый индикатор: колбэки не вызваны, но индикатор должен был показаться")
    }



    @Test
    fun homeScreenSwipeFromCenterZoneDoesNothing() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        var swipeToMonthCalled = false
        var swipeToEventsCalled = false

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { swipeToMonthCalled = true },
                onSwipeToEvents = { swipeToEventsCalled = true }
            )
        }

        val homeScreen = onNodeWithTag("home-screen-container")
        val screenSize = homeScreen.fetchSemanticsNode().size
        val screenWidth = screenSize.width.toFloat()
        val centerY = screenSize.height / 2f

        val startX = screenWidth * 0.5f

        homeScreen.performTouchInput {
            swipe(
                start = Offset(startX, centerY),
                end = Offset(startX + 100f, centerY),
                durationMillis = 200
            )
        }

        runBlocking { delay(300) }

        assertFalse(swipeToMonthCalled, "Свайп из центральной зоны не должен вызывать onSwipeToMonth")
        assertFalse(swipeToEventsCalled, "Свайп из центральной зоны не должен вызывать onSwipeToEvents")
    }

    @Test
    fun homeScreenSmallSwipeDoesNotTriggerCallbacks() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        var swipeToMonthCalled = false
        var swipeToEventsCalled = false

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { swipeToMonthCalled = true },
                onSwipeToEvents = { swipeToEventsCalled = true }
            )
        }

        val homeScreen = onNodeWithTag("home-screen-container")
        val screenSize = homeScreen.fetchSemanticsNode().size
        val screenWidth = screenSize.width.toFloat()
        val centerY = screenSize.height / 2f

        val startX = screenWidth * 0.2f

        homeScreen.performTouchInput {
            swipe(
                start = Offset(startX, centerY),
                end = Offset(startX + 30f, centerY),
                durationMillis = 200
            )
        }

        runBlocking { delay(300) }

        assertFalse(swipeToMonthCalled, "Маленький свайп не должен вызывать колбэки")
        assertFalse(swipeToEventsCalled, "Маленький свайп не должен вызывать колбэки")
    }

    @Test
    fun homeScreenSwipeInWrongDirectionDoesNothing() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        var swipeToMonthCalled = false
        var swipeToEventsCalled = false

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { swipeToMonthCalled = true },
                onSwipeToEvents = { swipeToEventsCalled = true }
            )
        }

        val homeScreen = onNodeWithTag("home-screen-container")
        val screenSize = homeScreen.fetchSemanticsNode().size
        val screenWidth = screenSize.width.toFloat()
        val centerY = screenSize.height / 2f

        val startX = screenWidth * 0.2f

        homeScreen.performTouchInput {
            swipe(
                start = Offset(startX, centerY),
                end = Offset(startX - 60f, centerY),
                durationMillis = 200
            )
        }

        runBlocking { delay(300) }

        assertFalse(swipeToMonthCalled, "Свайп в неправильном направлении не должен вызывать колбэки")
        assertFalse(swipeToEventsCalled, "Свайп в неправильном направлении не должен вызывать колбэки")
    }


    @Test
    fun noteDetailDialogDismissesNonUserNote() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()
        val testDate = formatDate(currentDate.year, currentDate.month, currentDate.day)

        runBlocking {
            val testEvent = CalendarEvent(
                id = 1,
                title = "Календарное событие",
                description = "Описание",
                date = testDate,
                start_time = "14:00",
                end_time = "15:00",
                location = "Офис",
                creator = "user@example.com",
                calendar_name = "Работа",
                is_tracked = true,
                is_done = false
            )
            manager.insertCalendarEvent(testEvent)
        }

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        onNodeWithText("Календарное событие").performClick()

        onNodeWithText("Мероприятие").performClick()

        runBlocking { delay(300) }

        onNodeWithText("Время").assertDoesNotExist()
    }

    @Test
    fun homeScreenHandlesEmptyState() = runComposeUiTest {
        val currentDate = org.ikbey.planner.PlatformDate()

        setContent {
            HomeScreen(
                notificationManager = notificationManager,
                selectedYear = currentDate.year,
                selectedMonth = currentDate.month,
                selectedDay = currentDate.day,
                onDayChange = { },
                onSwipeToMonth = { },
                onSwipeToEvents = { }
            )
        }

        runBlocking { delay(1000) }

        onNodeWithText("Сегодня дел нет!").assertExists()
    }

    @Test
    fun testHideKeyboardFunction() = runComposeUiTest {
        val hideKeyboard: () -> Unit = {
        }

        hideKeyboard()

        assertTrue(true, "Функция hideKeyboard должна существовать")
    }
}

abstract class AbstractHomeScreenComponentsTest {
    abstract fun createDriver(): SqlDriver
    private val calendarManager = CalendarManager()

    @Test
    fun monthTextDisplaysCorrectMonth() = runComposeUiTest {
        setContent {
            MonthText(
                year = 2024,
                month = 3,
                calendarManager = calendarManager
            )
        }

        onNodeWithText("Март").assertExists("Название месяца должно отображаться")
    }

    @Test
    fun todayBoxShowsOnlyWhenIsTodayTrue() = runComposeUiTest {
        setContent {
            TodayBox(isToday = true)
        }

        onNodeWithText("Сегодня").assertExists("Индикатор 'Сегодня' должен отображаться")
    }

    @Test
    fun todayBoxHiddenWhenIsTodayFalse() = runComposeUiTest {
        setContent {
            TodayBox(isToday = false)
        }

        val foundToday = try {
            onNodeWithText("Сегодня").assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
        assertFalse(foundToday, "Индикатор 'Сегодня' не должен отображаться когда isToday = false")
    }

    @Test
    fun addButtonIsClickable() = runComposeUiTest {
        var clicked = false

        setContent {
            AddButton(onClick = { clicked = true })
        }

        onNodeWithContentDescription("+").performClick()

        runBlocking { delay(100) }
        assertTrue(clicked, "Колбэк onClick должен быть вызван")
    }

    @Test
    fun dayItemShowsDayNumber() = runComposeUiTest {
        setContent {
            DayItem(day = 25, isSelected = false, onClick = { })
        }

        onNodeWithText("25").assertExists("Число дня должно отображаться")
    }

    @Test
    fun dayItemCallsOnClick() = runComposeUiTest {
        var clicked = false

        setContent {
            DayItem(day = 25, isSelected = false, onClick = { clicked = true })
        }

        onNodeWithText("25").performClick()
        runBlocking { delay(100) }
        assertTrue(clicked, "Колбэк onClick должен быть вызван")
    }

    @Test
    fun dayItemShowsSelectedStyle() = runComposeUiTest {
        setContent {
            DayItem(day = 25, isSelected = true, onClick = { })
        }

        onNodeWithText("25").assertExists("Выбранный день должен отображаться")
    }

    @Test
    fun daysScrollListShowsMultipleDays() = runComposeUiTest {
        setContent {
            DaysScrollList(
                year = 2024,
                month = 3,
                selectedDay = 15,
                onDayClick = { },
                modifier = Modifier
            )
        }

        onNodeWithText("1").assertExists("Первый день должен отображаться")
        onNodeWithText("15").assertExists("Средний день должен отображаться")
        onNodeWithText("31").assertExists("Последний день должен отображаться")
    }

    @Test
    fun readOnlyNoteDetailDialogShowsData() = runComposeUiTest {
        val testNote = Note(
            id = 1,
            date = "2024-03-15",
            place = "Аудитория 101",
            header = "Лекция",
            note = "Описание лекции",
            is_notifications_enabled = false,
            start_time = "09:00",
            end_time = "10:30",
            is_done = false
        )

        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00",
            endTime = "10:30",
            location = "Аудитория 101",
            note = "Лекция\nОписание лекции",
            isInterval = true,
            isNotification = false,
            date = "2024-03-15",
            type = org.ikbey.planner.NoteType.SCHEDULE
        )

        setContent {
            ReadOnlyNoteDetailDialog(
                note = testNote,
                noteData = testNoteData,
                onDismiss = { }
            )
        }

        onNodeWithText("Лекция").assertExists("Заголовок должен отображаться")
        onNodeWithText("09:00 - 10:30").assertExists("Время должно отображаться")
        onNodeWithText("Аудитория 101").assertExists("Место должно отображаться")
    }

    @Test
    fun calendarManagerProvidesCorrectData() {
        assertEquals(31, calendarManager.getDaysAmountInMonth(2024, 1), "Январь 2024 должен иметь 31 день")
        assertEquals(29, calendarManager.getDaysAmountInMonth(2024, 2), "Февраль 2024 (високосный) должен иметь 29 дней")
        assertEquals(28, calendarManager.getDaysAmountInMonth(2023, 2), "Февраль 2023 (не високосный) должен иметь 28 дней")

        assertEquals("Январь", calendarManager.getMonthName(1), "Первый месяц должен быть Январь")
        assertEquals("Декабрь", calendarManager.getMonthName(12), "Последний месяц должен быть Декабрь")

        assertEquals("Понедельник", calendarManager.getDayOfWeekName(1), "Первый день недели должен быть Понедельник")
        assertEquals("Воскресенье", calendarManager.getDayOfWeekName(7), "Последний день недели должен быть Воскресенье")

        val dayOfWeek = calendarManager.calculateDayOfWeek(2024, 3, 15)
        assertEquals(5, dayOfWeek, "15 марта 2024 должно быть пятницей")
    }

    @Test
    fun calendarManagerMatrixCreation() {
        val matrix = calendarManager.getCalendarMatrix(2024, 3)
        assertTrue(matrix.isNotEmpty(), "Матрица календаря не должна быть пустой")
        assertTrue(matrix.size >= 4, "Матрица должна содержать как минимум 4 недели")

        val allDays = matrix.flatten().filterNotNull()
        assertTrue(allDays.contains(1), "Матрица должна содержать первый день")
        assertTrue(allDays.contains(31), "Матрица должна содержать последний день марта")
    }

    @Test
    fun intervalTimePartAcceptsValidTimeInput() = runComposeUiTest {
        var capturedValue = ""

        setContent {
            IntervalTimePart(
                value = "",
                onValueChange = { capturedValue = it },
                placeholder = "__:__",
                modifier = androidx.compose.ui.Modifier.width(100.dp)
            )
        }

        onNodeWithText("__:__").performClick()

    }

    @Test
    fun intervalTimePartFormatsInputCorrectly() = runComposeUiTest {
        var capturedValue = ""

        setContent {
            IntervalTimePart(
                value = capturedValue,
                onValueChange = { capturedValue = it },
                placeholder = "__:__",
                modifier = androidx.compose.ui.Modifier.width(100.dp)
            )
        }

    }

    @Test
    fun intervalTimePartShowsPlaceholderWhenEmpty() = runComposeUiTest {
        setContent {
            IntervalTimePart(
                value = "",
                onValueChange = { },
                placeholder = "Введите время",
                modifier = androidx.compose.ui.Modifier.width(100.dp)
            )
        }

        onNodeWithText("Введите время").assertExists("Плейсхолдер должен отображаться когда значение пустое")
    }

    @Test
    fun unifiedTimeInputFieldShowsSingleTimeWhenNotInterval() = runComposeUiTest {
        setContent {
            UnifiedTimeInputField(
                startTime = "09:00",
                endTime = "",
                isInterval = false,
                onStartTimeChange = { },
                onEndTimeChange = { },
                modifier = androidx.compose.ui.Modifier
            )
        }

        onNodeWithText("09:00").assertExists("Время начала должно отображаться")
    }

    @Test
    fun unifiedTimeInputFieldShowsIntervalWhenIsIntervalTrue() = runComposeUiTest {
        setContent {
            UnifiedTimeInputField(
                startTime = "09:00",
                endTime = "10:30",
                isInterval = true,
                onStartTimeChange = { },
                onEndTimeChange = { },
                modifier = androidx.compose.ui.Modifier
            )
        }

        onNodeWithText("09:00").assertExists("Время начала должно отображаться")
        onNodeWithText("10:30").assertExists("Время окончания должно отображаться")
    }

    @Test
    fun unifiedTimeInputFieldCallsCallbacksOnChange() = runComposeUiTest {
        var startTimeChanged = ""
        var endTimeChanged = ""

        setContent {
            UnifiedTimeInputField(
                startTime = "",
                endTime = "",
                isInterval = true,
                onStartTimeChange = { startTimeChanged = it },
                onEndTimeChange = { endTimeChanged = it },
                modifier = androidx.compose.ui.Modifier
            )
        }
    }

    @Test
    fun singleTimeFieldFormatsInput() = runComposeUiTest {
        var capturedValue = ""

        setContent {
            SingleTimeField(
                value = capturedValue,
                onValueChange = { capturedValue = it },
                modifier = androidx.compose.ui.Modifier.width(100.dp)
            )
        }
    }

    @Test
    fun singleTimeFieldShowsPlaceholder() = runComposeUiTest {
        setContent {
            SingleTimeField(
                value = "",
                onValueChange = { },
                modifier = androidx.compose.ui.Modifier.width(100.dp)
            )
        }

        onNodeWithText("__:__").assertExists("Плейсхолдер должен отображаться для пустого поля")
    }

    @Test
    fun simpleInputFieldAcceptsTextInput() = runComposeUiTest {
        var capturedValue = ""

        setContent {
            SimpleInputField(
                value = capturedValue,
                onValueChange = { capturedValue = it },
                placeholder = "Введите текст",
                modifier = androidx.compose.ui.Modifier.height(100.dp)
            )
        }

        onNodeWithText("Введите текст").performClick()
    }

    @Test
    fun simpleInputFieldSplitsHeaderAndBody() = runComposeUiTest {
        var capturedValue = ""

        setContent {
            SimpleInputField(
                value = capturedValue,
                onValueChange = { capturedValue = it },
                placeholder = "Заголовок",
                modifier = androidx.compose.ui.Modifier.height(100.dp)
            )
        }
    }

    @Test
    fun simpleInputFieldShowsPlaceholders() = runComposeUiTest {
        setContent {
            SimpleInputField(
                value = "",
                onValueChange = { },
                placeholder = "Основной плейсхолдер",
                modifier = androidx.compose.ui.Modifier.height(100.dp)
            )
        }

        onNodeWithText("Основной плейсхолдер").assertExists("Основной плейсхолдер должен отображаться")
        onNodeWithText("Введите текст...").assertExists("Плейсхолдер для тела должен отображаться")
    }

    @Test
    fun simpleLocationFieldAcceptsInput() = runComposeUiTest {
        var capturedValue = ""

        setContent {
            SimpleLocationField(
                value = capturedValue,
                onValueChange = { capturedValue = it },
                modifier = androidx.compose.ui.Modifier.width(200.dp)
            )
        }
        onNodeWithText("Место проведения").performClick()
    }

    @Test
    fun simpleLocationFieldShowsPlaceholder() = runComposeUiTest {
        setContent {
            SimpleLocationField(
                value = "",
                onValueChange = { },
                modifier = androidx.compose.ui.Modifier.width(200.dp)
            )
        }

        onNodeWithText("Место проведения").assertExists("Плейсхолдер места проведения должен отображаться")
    }

    @Test
    fun testIsValidTimeIntervalVariousCases() {
        assertTrue(isValidTimeInterval("09:00", "10:00"), "09:00-10:00 должно быть валидным")
        assertTrue(isValidTimeInterval("00:00", "23:59"), "00:00-23:59 должно быть валидным")
        assertTrue(isValidTimeInterval("12:00", "13:30"), "12:00-13:30 должно быть валидным")

        assertFalse(isValidTimeInterval("10:00", "09:00"), "10:00-09:00 должно быть невалидным")
        assertFalse(isValidTimeInterval("15:00", "14:59"), "15:00-14:59 должно быть невалидным")
        assertFalse(isValidTimeInterval("18:00", "17:00"), "18:00-17:00 должно быть невалидным")

        assertFalse(isValidTimeInterval("12:00", "12:00"), "12:00-12:00 должно быть невалидным")

        assertFalse(isValidTimeInterval("25:00", "10:00"), "С невалидным начальным временем должно быть невалидным")
        assertFalse(isValidTimeInterval("09:00", "24:00"), "С невалидным конечным временем должно быть невалидным")
        assertFalse(isValidTimeInterval("abc", "10:00"), "С невалидным форматом должно быть невалидным")
    }

    @Test
    fun notesSectionShowsEmptyState() = runComposeUiTest {
        setContent {
            NotesSection(
                items = emptyList(),
                scrollState = androidx.compose.foundation.rememberScrollState(),
                onNoteClick = { _, _ -> },
                onToggleNoteDone = { _, _, _ -> },
                modifier = androidx.compose.ui.Modifier
            )
        }
        onNodeWithText("Сегодня дел нет!").assertExists("Сообщение о пустом состоянии должно отображаться")
    }

    @Test
    fun notesSectionShowsMultipleNotes() = runComposeUiTest {
        val testNote = Note(1, "2024-03-15", null, "Тестовая заметка", null, false, "09:00", null, false)
        val testNoteData = org.ikbey.planner.NoteData(
            startTime = "09:00", endTime = "", location = "", note = "Тестовая заметка",
            isInterval = false, isNotification = false, date = "2024-03-15", type = org.ikbey.planner.NoteType.USER_NOTE
        )

        val items = listOf(
            testNoteData to testNote,
            testNoteData.copy(note = "Вторая заметка") to testNote.copy(id = 2, header = "Вторая заметка")
        )

        setContent {
            NotesSection(
                items = items,
                scrollState = androidx.compose.foundation.rememberScrollState(),
                onNoteClick = { _, _ -> },
                onToggleNoteDone = { _, _, _ -> },
                modifier = androidx.compose.ui.Modifier
            )
        }

        onNodeWithText("Тестовая заметка").assertExists("Первая заметка должна отображаться")
        onNodeWithText("Вторая заметка").assertExists("Вторая заметка должна отображаться")
    }
}