package org.ikbey.planner.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(
    onMonthClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAddNoteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("главная страница - расписание на день")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onMonthClick) {
            Text("левый экран с фулл месяцем")
        }

        Button(onClick = onEventsClick) {
            Text("ивенты профчика")
        }

        Button(onClick = onAddNoteClick) {
            Text("экран заметок")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onMonthClick = {},
        onEventsClick = {},
        onAddNoteClick = {}
    )
}
