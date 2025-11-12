package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.ikbey.planner.localDB.LocalDatabase

class LocalDatabaseManagerTest : AbstractLocalDatabaseManagerTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}