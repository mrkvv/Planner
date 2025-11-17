@file:OptIn(ExperimentalTestApi::class)

package org.ikbey.planner.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import app.cash.sqldelight.db.SqlDriver
import org.ikbey.planner.CalendarManager
import org.ikbey.planner.dataBase.LocalDatabaseManager
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.dataBase.StickyNote
import org.ikbey.planner.dataBase.SupabaseRepository
import org.ikbey.planner.dataBase.SyncManager
import org.ikbey.planner.localDB.LocalDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test

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
    fun stickyNotePageTest() = runComposeUiTest {

        val stickyNote = StickyNote(1, "header", "note")
        setContent {
            StickyNotePage(
                stickyNote = stickyNote,
                onDismissRequest = {},
                onChangeInStickyNotesList = {}
            )
        }

        onNodeWithText("header").assertExists()
    }



}