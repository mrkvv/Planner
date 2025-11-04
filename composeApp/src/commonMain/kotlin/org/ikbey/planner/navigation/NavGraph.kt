package org.ikbey.planner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.ikbey.planner.screens.AddNoteScreen
import org.ikbey.planner.screens.EventsScreen
import org.ikbey.planner.screens.HomeScreen
import org.ikbey.planner.screens.MonthScreen

@Composable
fun NavGraph() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Контент в зависимости от текущего экрана
    when (currentScreen) {
        Screen.Home -> HomeScreen(
            onMonthClick = { currentScreen = Screen.Month },
            onEventsClick = { currentScreen = Screen.Events },
            onAddNoteClick = { noteData ->
            }
        )
        Screen.Month -> MonthScreen(
            onBackClick = { currentScreen = Screen.Home }
        )
        Screen.Events -> EventsScreen(
            onBackClick = { currentScreen = Screen.Home }
        )
        Screen.AddNote -> AddNoteScreen(
            onBackClick = { currentScreen = Screen.Home },
            onSaveClick = { currentScreen = Screen.Home }
        )
    }
}