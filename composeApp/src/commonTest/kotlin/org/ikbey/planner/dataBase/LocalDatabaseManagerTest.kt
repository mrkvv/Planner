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
    fun testInsertAndGetFaculty() = runTest {
        val faculty = Faculty(1, "Институт какой-то", "ИК-Т")

        manager.insertFaculty(faculty)
        val faculties = manager.getFaculties()

        assertEquals(1, faculties.size)
        assertEquals("Институт какой-то", faculties[0].name)
        assertEquals("ИК-Т", faculties[0].abbr)
    }
}