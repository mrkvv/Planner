@file:OptIn(ExperimentalTestApi::class)

package org.ikbey.planner.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.runBlocking
import org.ikbey.planner.CalendarManager
import org.ikbey.planner.dataBase.LocalDatabaseManager
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.dataBase.StickyNote
import org.ikbey.planner.dataBase.SupabaseRepository
import org.ikbey.planner.dataBase.SyncManager
import org.ikbey.planner.localDB.LocalDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractUIMonthScreenTest {
    abstract fun createDriver(): SqlDriver
    private lateinit var database: LocalDatabase
    private lateinit var manager: LocalDatabaseManager
    private lateinit var supabaseRepository: SupabaseRepository
    private lateinit var syncManager: SyncManager
    private val calendarManager = CalendarManager()

    @BeforeTest
    fun setup() {
        database = LocalDatabase(createDriver())
        manager = LocalDatabaseManager(database)
        supabaseRepository = SupabaseRepository()
        syncManager = SyncManager(manager, supabaseRepository)
        ServiceLocator.setLocalDb(manager)
    }

    @Test
    fun calendarHeaderTest() = runComposeUiTest {
        setContent {
            CalendarHeader(
                modifier = Modifier,
                calendarManager = calendarManager,
                year = 2005,
                month = 12,
                onHeaderClick = { }
            )
        }

        onNodeWithText("Декабрь").assertExists()
        onNodeWithText("2005").assertExists()
    }

    @Test
    fun calendarElementTest() = runComposeUiTest {
        setContent {
            CalendarElement(
                modifier = Modifier,
                calendarManager = calendarManager,
                year = 2000,
                month = 12,
                num = 13,
                onCalendarDaySelect = {year, month, day -> }
            )
        }
        onNodeWithText("13").assertExists()
    }

    @Test
    fun calendarWindowTest() = runComposeUiTest {
        setContent {
            CalendarWindow(
                modifier = Modifier,
                calendarManager = calendarManager,
                year = 2025,
                month = 12,
                onMonthChangeUp = {},
                onMonthChangeDown = {},
                onCalendarDaySelect = {year, month, day -> }
            )
        }

        onNodeWithText("31").assertExists()
    }

    @Test
    fun stickyNotePageViewAndEditTest() = runComposeUiTest {
        val stickyNote = StickyNote(1, "Аптека", "Купить парацетамол")
        runBlocking { ServiceLocator.localDatabaseManager.insertStickyNote(stickyNote) }

        setContent {
            StickyNotePage(
                stickyNote = stickyNote,
                onDismissRequest = {},
                onChangeInStickyNotesList = {}
            )
        }

        onNodeWithText("Аптека").assertExists()
        onNodeWithText("Купить парацетамол").assertExists()

        // Изменение хедера
        onNode(hasText("Аптека"))
            .performTextClearance()
        onNode(hasText(""))
            .performTextInput("Аптека123")
        onNode(hasText("Аптека123")).assertExists()

        onNodeWithTag("StickyNoteDialog")
            .performTouchInput {
                // Кликаем вне области диалога
                click(Offset(x = -10f, y = (height / 2).toFloat()))
            }
        mainClock.advanceTimeBy(1000)

        var stickyNotes = emptyList<StickyNote>()
        runBlocking {
            stickyNotes = ServiceLocator.localDatabaseManager.getAllStickyNotes()
        }
        assertEquals("Аптека123", stickyNotes[0].header)
    }

    @Test
    fun stickyNotePageCreationTest() = runComposeUiTest {
        val stickyNote = StickyNote(1, "Аптека", "Купить парацетамол")

        setContent {
            StickyNotePage(
                stickyNote = null,
                onDismissRequest = {},
                onChangeInStickyNotesList = {}
            )
        }

        onNodeWithText("Добавить заголовок").assertExists()
        onNodeWithText("Добавить описание").assertExists()

        // Добавление заметки
        onNode(hasText("Добавить заголовок"))
            .performTextInput("Аптека")
        onNode(hasText("Аптека")).assertExists()

        onNodeWithTag("StickyNoteDialog")
            .performTouchInput {
                // Кликаем вне области диалога
                click(Offset(x = -10f, y = (height / 2).toFloat()))
            }
        mainClock.advanceTimeBy(1000)

        var stickyNotes = emptyList<StickyNote>()
        runBlocking {
            stickyNotes = ServiceLocator.localDatabaseManager.getAllStickyNotes()
        }
        assertEquals("Аптека", stickyNotes[0].header)
        assertEquals(1, stickyNotes.size)
    }

    @Test
    fun stickyNotePageDeleteTest() = runComposeUiTest {
        val stickyNote = StickyNote(1, "Аптека", "Купить парацетамол")

        runBlocking {
            ServiceLocator.localDatabaseManager.insertStickyNote(stickyNote)
        }
        setContent {
            StickyNotePage(
                stickyNote = stickyNote,
                onDismissRequest = {},
                onChangeInStickyNotesList = {}
            )
        }
        onNodeWithContentDescription("Удалить sticky note").performClick()
        var stickyNotes = emptyList<StickyNote>()
        runBlocking {
            stickyNotes = ServiceLocator.localDatabaseManager.getAllStickyNotes()
        }
        assertEquals(0, stickyNotes.size)
    }

    @Test
    fun stickyNoteAddingButtonTest() = runComposeUiTest {
        setContent {
            StickyNoteAddingButton(Modifier, {})
        }
        onNodeWithText("Добавить заметку...").assertExists()
    }

    @Test
    fun stickyNoteElementTest() = runComposeUiTest {
        val stickyNote = StickyNote(1, "Аптека", "Купить парацетамол")
        setContent {
            StickyNoteElement(
                modifier = Modifier,
                stickyNote = stickyNote,
                onStickyNoteClick = {}
            )
        }
        onNodeWithText("Аптека").assertExists()
    }

    @Test
    fun stickyNotesAreaTest() = runComposeUiTest {
        val stickyNote1 = StickyNote(1, "Аптека", "Купить парацетамол")
        val stickyNote2 = StickyNote(2, "Купить арбуз", "Рынок")
        runBlocking {
            ServiceLocator.localDatabaseManager.insertStickyNote(stickyNote1)
            ServiceLocator.localDatabaseManager.insertStickyNote(stickyNote2)
        }
        setContent {
            StickyNotesArea(Modifier)
        }

        onNodeWithText("Аптека").assertExists()
        onNodeWithText("Купить арбуз").assertExists()
        onNodeWithText("Добавить заметку...").assertExists()

        onNodeWithText("Купить арбуз").performClick()
        mainClock.advanceTimeBy(1000)
        onNodeWithText("Рынок").assertExists()
    }

    @Test
    fun monthScreenTest() = runComposeUiTest {
        setContent {
            MonthScreen(
                onCalendarDaySelect = { year, month, day -> },
                onSwipeToHome = {}
            )
        }

        onNodeWithText("2025").assertExists()
        onNodeWithText("Ноябрь").assertExists()
        onNodeWithText("<").assertExists()
        onNodeWithText("<").performClick()
        onNodeWithText("Октябрь").assertExists()
        onNodeWithText(">").performClick()
        onNodeWithText(">").performClick()
        onNodeWithText(">").performClick()
        onNodeWithText("2026").assertExists()
        onNodeWithText("Январь").assertExists()
        for(i in 0..12) {
            onNodeWithText("<").performClick()
        }
        onNodeWithText("2024").assertExists()
        onNodeWithText("Декабрь").assertExists()

        onNodeWithText("Декабрь").performClick()
        onNodeWithText("2025").assertExists()
        onNodeWithText("Ноябрь").assertExists()
    }
}
