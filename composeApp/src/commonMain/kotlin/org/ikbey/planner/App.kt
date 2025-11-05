package org.ikbey.planner

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.navigation.NavGraph
import org.ikbey.planner.notification.NotificationManager
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(notificationManager: NotificationManager) {
    LaunchedEffect(Unit) {
        ServiceLocator.syncManager.syncIfNeeded()
    }

    MaterialTheme {
        NavGraph(notificationManager = notificationManager)
    }
}