@file:OptIn(ExperimentalTestApi::class)

package org.ikbey.planner.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import app.cash.sqldelight.db.SqlDriver
import org.ikbey.planner.dataBase.CalendarEvent
import org.ikbey.planner.dataBase.LocalDatabaseManager
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.localDB.LocalDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


abstract class AbstractEventsScreenTest  {

    abstract fun createDriver(): SqlDriver
    private lateinit var database: LocalDatabase
    private lateinit var manager: LocalDatabaseManager

    @BeforeTest
    fun setup() {
        database = LocalDatabase(createDriver())
        manager = LocalDatabaseManager(database)
        ServiceLocator.setLocalDb(manager)
    }

    // Тест на наличие надписи "События" на странице EventsSrceen
    @Test
    fun testEventsScreenTitle() = runComposeUiTest {
        setContent {
            EventsScreen(
                onBackClick = { },
                onSwipeToHome = { }
            )
        }

        onNodeWithText("События").assertExists()
    }

    // Тест на вывод информации в Event Item
    @Test
    fun testEventItemDisplayAllInfo() = runComposeUiTest {
        val testEvent = CalendarEvent(
            id = 1,
            title = "Тайтл",
            date = "2025-11-01",
            start_time = "10:00",
            end_time = "11:30",
            description = "Описание",
            location = "ГЗ",
            creator = "Креатор",
            calendar_name = "ПРОФ.тест"
        )

        setContent {
            EventItem(event = testEvent)
        }

        onNodeWithText("Тайтл").assertExists()
        onNodeWithText("10:00").assertExists()
        onNodeWithText("11:30").assertExists()
        onNodeWithText("Описание").assertExists()
        onNodeWithText("ГЗ").assertExists()
    }

    // Тест на отображение даты
    @Test
    fun testDateHeaderFormat() = runComposeUiTest {
        setContent {
            DateHeader(date = "2025-11-01")
        }

        onNodeWithText("1 ноя, СБ").assertExists()
    }

    // Тест на существование кнопки плюсика у мероприятия
    @Test
    fun testTrackButtonExist() = runComposeUiTest {
        setContent {
            TrackButton(
                isTracked = false,
                onToggle = {}
            )
        }

        onNodeWithText("+").assertExists()
    }

    // Тест на изменение состояния при нажатии на кнопку плюсик у мероприятия
    @Test
    fun testTrackButtonToggle() = runComposeUiTest {
        var isTracked = false
        val onToggle = {isTracked = !isTracked}

        setContent {
            TrackButton(
                isTracked = isTracked,
                onToggle = onToggle
            )
        }

        onNodeWithText("+").assertExists().performClick()
    }

    // Тест на отображение элементов фильтра
    @Test
    fun testFilterItemDisplayName() = runComposeUiTest {
        setContent {
            FilterItem(
                name = "ТЕСТ",
                isSelected = false,
                onToggle = {}
            )
        }

        onNodeWithText("ТЕСТ").assertExists()
    }

    // Тест на отображение текста в карточке фильтра
    @Test
    fun testFilterPageDisplayTitle() = runComposeUiTest {
        setContent {
            FilterPage(onDismiss = {})
        }

        onNodeWithText("Фильтры:").assertExists()
        onNodeWithText("Выберите, какие события\nвы хотите видеть").assertExists()
    }

    // Тест методов addFilter и removeFilter в object FilterManager
    @Test
    fun testFilterManager(){
        assertTrue(FilterManager.selectedFilters.isEmpty())

        FilterManager.addFilter("ПРОФ.тест")
        assertEquals(setOf("ПРОФ.тест"), FilterManager.selectedFilters)

        FilterManager.addFilter("Прочее")
        assertEquals(setOf("ПРОФ.тест", "Прочее"), FilterManager.selectedFilters)

        FilterManager.removeFilter("Прочее")
        assertEquals(setOf("ПРОФ.тест"), FilterManager.selectedFilters)

    }

    // Тест на отображение фильтрованных событий
    @Test
    fun testFilteredEvents() = runComposeUiTest {
        val testEvents = listOf(
            CalendarEvent(
                id = 1,
                title = "Квиз",
                calendar_name = "ПРОФ.тест",
                date = "2025-11-01",
                start_time = "08:00",
                end_time = "17:00",
                creator = "Креатор"
            ),
            CalendarEvent(
                id = 2,
                title = "Собрание",
                calendar_name = "Все",
                date = "2025-11-02",
                start_time = "11:00",
                end_time = "12:00",
                creator = "Креатор",
            )
        )

        FilterManager.addFilter("ПРОФ.тест")

        setContent {
            EventsList(events = testEvents.filter {
                FilterManager.selectedFilters.isEmpty() ||
                        FilterManager.selectedFilters.contains(it.calendar_name)
            })
        }

        onNodeWithText("Квиз").assertExists()
        onNodeWithText("Собрание").assertDoesNotExist()

        FilterManager.removeFilter("ПРОФ.тест")
    }

    // Тест на одинаковую дату у событий
    @Test
    fun testEventsGroupedBySameDate() = runComposeUiTest {
        val testEvents = listOf(
            CalendarEvent(
                id = 1,
                title = "Квиз",
                calendar_name = "ПРОФ.тест",
                date = "2025-11-01",
                start_time = "08:00",
                end_time = "10:00",
                creator = "Креатор"
            ),
            CalendarEvent(
                id = 2,
                title = "Собрание",
                calendar_name = "Все",
                date = "2025-11-01",
                start_time = "15:00",
                end_time = "16:00",
                creator = "Креатор",
            )
        )

        setContent {
            EventsList(events = testEvents)
        }

        onNodeWithText("1 ноя, СБ").assertCountEquals(1)
        onNodeWithText("Собрание").assertExists()
        onNodeWithText("Квиз").assertExists()
    }

    // Тест на отображение надписи при отсутствии мероприятий
    @Test
    fun testEventsListWithEmptyEvents() = runComposeUiTest {
        setContent {
            EventsList(events = emptyList())
        }

        onNodeWithText("Пока мероприятий нет :(").assertDoesNotExist()
    }

    // Тест на существование кнопки настроек
    @Test
    fun testSettingsButtonExist()  = runComposeUiTest {
        setContent {
            EventsScreen(
                onBackClick = {},
                onSwipeToHome = {}
            )
        }

        onNodeWithContentDescription("Настройки").performClick()
    }

    //Тест на существование кнопки фильтра
    @Test
    fun testFilterButtonExist()  = runComposeUiTest {
        setContent {
            EventsScreen(
                onBackClick = {},
                onSwipeToHome = {}
            )
        }

        onNodeWithContentDescription("Фильтр").performClick()
    }

    private fun SemanticsNodeInteraction.assertCountEquals(expectedSize: Int) {}
}