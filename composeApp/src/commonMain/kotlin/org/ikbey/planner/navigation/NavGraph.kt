package org.ikbey.planner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.ikbey.planner.CalendarManager
import org.ikbey.planner.notification.NotificationManager
import org.ikbey.planner.screens.EventsScreen
import org.ikbey.planner.screens.HomeScreen
import org.ikbey.planner.screens.MonthScreen

@Composable
fun NavGraph(notificationManager: NotificationManager) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    var selectedYear by remember { mutableStateOf(CalendarManager().getCurrentYear()) }
    var selectedMonth by remember { mutableStateOf(CalendarManager().getCurrentMonth()) }
    var selectedDay by remember { mutableStateOf(CalendarManager().getCurrentDay()) }

    when (currentScreen) {
        Screen.Home -> HomeScreen(
            notificationManager = notificationManager, // Передача менеджера уведомлений
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay,
            onDayChange = { day -> selectedDay = day },
            onSwipeToMonth = { currentScreen = Screen.Month },
            onSwipeToEvents = { currentScreen = Screen.Events }
        )
        Screen.Month -> MonthScreen(
            onCalendarDaySelect = { year, month, day ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                currentScreen = Screen.Home
            },
            onSwipeToHome = { currentScreen = Screen.Home }
        )
        Screen.Events -> EventsScreen(
            onBackClick = { currentScreen = Screen.Home },
            onSwipeToHome = { currentScreen = Screen.Home }
        )
    }
}