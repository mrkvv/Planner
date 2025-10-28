package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.ikbey.planner.localDB.LocalDatabase

/** Ios-реализация создания драйвера для локальной базы данных SQLdelight */
actual class DriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = LocalDatabase.Schema,
            name = "local.db"
        )
    }
}
