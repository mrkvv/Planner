package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        throw NotImplementedError("Desktop database not implemented")
    }
}