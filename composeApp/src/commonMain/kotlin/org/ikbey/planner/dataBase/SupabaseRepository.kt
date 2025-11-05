package org.ikbey.planner.dataBase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Класс для работы с облачной базой данных.
 * # Для его работы нужно вставить API-ключ */
class SupabaseRepository {
    private val baseUrl = "https://pjcbyabqlgpjvkozojvc.supabase.co/rest/v1"
    private val apiKey = "API_KEY"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    suspend fun getAllFaculties(): List<Faculty> {
        return try {
            val response = client.get("$baseUrl/faculties?apikey=$apiKey")
            response.body()
        } catch (e: Exception) {
            println("Ошибка в SupabaseRepository.getAllFaculties(): ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllGroupsByFaculty(facultyId: Int): List<Group> {
        return try {
            val response = client.get("$baseUrl/groups?apikey=$apiKey&faculty_id=eq.$facultyId&select=*")
            response.body()
        } catch (e: Exception) {
            println("Ошибка в SupabaseRepository.getAllGroups(): ${e.message}")
            emptyList()
        }
    }

    suspend fun getScheduleByGroup(groupId: Int): List<Schedule> {
        return try {
            val response = client.get("$baseUrl/schedule?group_id=eq.$groupId&apikey=$apiKey")
            response.body()
        } catch (e: Exception) {
            println("Ошибка в SupabaseRepository.getScheduleByGroup(группа) для группы $groupId: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllCalendarEvents(): List<CalendarEvent> {
        return try {
            val response = client.get("$baseUrl/calendar_events?apikey=$apiKey")
            response.body()
        } catch (e: Exception) {
            println("Ошибка в SupabaseRepository.getAllCalendarEvents(): ${e.message}")
            emptyList()
        }
    }
}
