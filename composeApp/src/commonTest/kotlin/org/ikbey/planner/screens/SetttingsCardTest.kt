@file:OptIn(ExperimentalTestApi::class)

package org.ikbey.planner.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.test.runTest
import org.ikbey.planner.dataBase.Faculty
import org.ikbey.planner.dataBase.Group
import org.ikbey.planner.dataBase.LocalDatabaseManager
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.dataBase.SupabaseRepository
import org.ikbey.planner.dataBase.SyncManager
import org.ikbey.planner.localDB.LocalDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test

abstract class AbstractSettingsScreenTest {

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
        ServiceLocator.setLocalDb(manager)

        runTest {
            manager.insertFaculty(Faculty(1, "Институт компьютерных наук и кибербезопасности", "ИКНК"))
            manager.insertFaculty(Faculty(2, "Гуманитарный институт", "ГИ"))

            manager.insertGroup(Group(1, 125, "5130904/30107"))
            manager.insertGroup(Group(2, 125, "5130904/30106"))
            manager.insertGroup(Group(3, 101, "3834001/40003_2024"))
        }
    }


    @Test
    fun testSettingsCardDisplayTitles() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Выберите институт:").assertExists()
        onNodeWithText("Выберите группу:").assertExists()
    }

    @Test
    fun testSettingsCardDisplayButton() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Сохранить").assertExists()
    }

    @Test
    fun testInstituteDropdownShowsList() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Выберите институт").performClick()

        onNodeWithText("ИКНК").assertExists()
        onNodeWithText("ГИ").assertExists()
    }

    @Test
    fun testInstituteSelection() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Выберите институт").performClick()

        onNodeWithText("ГИ").performClick()

        onNodeWithText("ГИ").assertExists()
    }

    @Test
    fun testGroupDropdownShowsAfterInstituteSelection() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Выберите институт").performClick()
        onNodeWithText("ИКНК").performClick()

        onNodeWithText("Введите название группы").performTextInput("5130904/30107")

        onNodeWithText("5130904/30107").assertExists()
    }

    @Test
    fun testSaveButtonEnabledWhenInstituteAndGroupSelected() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Сохранить").assert(hasClickAction())

        onNodeWithText("Выберите институт").performClick()
        onNodeWithText("ИКНК").performClick()

        onNodeWithText("Введите название группы").performTextInput("5130904/30107")
        onNodeWithText("5130904/30107").assertExists()

        onNodeWithText("Сохранить").assert(hasClickAction())
    }

    @Test
    fun testNoGroupsFoundMessage() = runComposeUiTest {
        setContent {
            SettingsCard(onDismiss = {})
        }

        onNodeWithText("Выберите институт").performClick()
        onNodeWithText("ИКНК").performClick()

        onNodeWithText("Введите название группы").performTextInput("Несуществующая группа")

        onNodeWithText("Группы не найдены").assertExists()
    }
}