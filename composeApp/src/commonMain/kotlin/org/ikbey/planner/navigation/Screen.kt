package org.ikbey.planner.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Month : Screen("month")
    object Events : Screen("events")
    object AddNote : Screen("add_note")
}