package org.ikbey.planner

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.navigation.NavGraph
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) {
        ServiceLocator.syncManager.syncIfNeeded()
    }

    MaterialTheme {
        NavGraph()
    }
}