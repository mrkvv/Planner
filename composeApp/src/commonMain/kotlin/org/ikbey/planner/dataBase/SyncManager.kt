package org.ikbey.planner.dataBase

import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/** Класс для синхронизации работы облачной и локальных БД.
 * Обновляет данные автоматически, если пользователь открыл приложение и до этого не открывал его больше 24 часов. */
class SyncManager(
    private val localDb: LocalDatabaseManager,
    private val remoteDb: SupabaseRepository,

) {
    companion object {
        private val SYNC_INTERVAL = 24.toDuration(DurationUnit.HOURS)
        private val LAST_SYNC_TIME_DB_KEY = "last_sync_time"
        private val GROUP_ID_DB_KEY = "group_id"
    }
    suspend fun getLastSyncTime(): Long? {
        return localDb.getSetting(LAST_SYNC_TIME_DB_KEY)?.toLongOrNull()
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        localDb.setSetting(LAST_SYNC_TIME_DB_KEY, timestamp.toString())
    }

    suspend fun clearLastSyncTime() {
        localDb.deleteSetting(LAST_SYNC_TIME_DB_KEY)
    }

    suspend fun getGroupId(): String? {
        return localDb.getSetting(GROUP_ID_DB_KEY)
    }

    /** Любая смена группы = полная синхронизация (не знаю стоит ли прям так делать, если что уберем) */
    suspend fun setGroupId(groupId: Int) {
        localDb.setSetting(GROUP_ID_DB_KEY, groupId.toString())
        performFullSync()
    }

    private suspend fun performFullSync() {
        try {
            val remoteFaculties = remoteDb.getAllFaculties()
            localDb.deleteAllFaculties()
            remoteFaculties.forEach { localDb.insertFaculty(it) }

            val remoteGroups = remoteDb.getAllGroups()
            localDb.deleteAllGroups()
            remoteGroups.forEach { localDb.insertGroup(it) }

            val remoteCalendarEvents = remoteDb.getAllCalendarEvents()
            localDb.deleteAllCalendarEvents()
            remoteCalendarEvents.forEach { localDb.insertCalendarEvent(it) }

            val groupId = getGroupId()
            val remoteSchedule = remoteDb.getScheduleByGroup(groupId?.toInt() ?: 0)
            localDb.deleteUserSchedule()
            remoteSchedule.forEach { localDb.insertUserSchedule(it) }
        } catch (e: Exception) {
            println("Ошибка в SyncManager.performFullSync(): ${e.message}")
            throw e
        }
    }


    @OptIn(ExperimentalTime::class)
    suspend fun forceSync() {
        performFullSync()
        setLastSyncTime(Clock.System.now().toEpochMilliseconds())
    }

    @OptIn(ExperimentalTime::class)
    suspend fun syncIfNeeded(): Boolean {
        val lastSync = getLastSyncTime()
        val now = Clock.System.now().toEpochMilliseconds()

        return if (lastSync == null || (now - lastSync) > SYNC_INTERVAL.inWholeMilliseconds) {
            performFullSync()
            setLastSyncTime(now)
            true
        } else false
    }
}
