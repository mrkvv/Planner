package org.ikbey.planner.dataBase

import app.cash.sqldelight.db.SqlDriver
import org.ikbey.planner.localDB.LocalDatabase

/** Вспомогательный класс для создания базы данных. Использует платформо-зависимый класс [DriverFactory]*/
object DatabaseFactory {
    fun createDatabase(): LocalDatabase {
        val driver = DriverFactory().createDriver()
        return LocalDatabase(driver)
    }
}

/** Класс, который возвращает SqlDriver для локальной базы данных.
 * Основная реализация находится в платформ-специфических разделах в одноименных файликах. */
expect class DriverFactory() {
    fun createDriver(): SqlDriver
}
