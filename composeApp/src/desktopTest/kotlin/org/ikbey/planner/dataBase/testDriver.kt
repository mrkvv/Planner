package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.ikbey.planner.localDB.LocalDatabase
import org.ikbey.planner.screens.AbstractEventsScreenTest
import org.ikbey.planner.screens.AbstractSettingsScreenTest
import org.ikbey.planner.screens.AbstractUIMonthScreenTest
import org.ikbey.planner.screens.*


class SyncManagerTest : AbstractSyncManagerTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}

class LocalDatabaseManagerTest : AbstractLocalDatabaseManagerTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}

class UIMonthScreenTest : AbstractUIMonthScreenTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}

class EventsScreenTest : AbstractEventsScreenTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}

class SettingsCardTest : AbstractSettingsScreenTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}

class UIHomeScreenTest : AbstractUIHomeScreenTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}

class HomeScreenComponentsTest : AbstractHomeScreenComponentsTest() {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            LocalDatabase.Schema.create(this)
        }
    }
}