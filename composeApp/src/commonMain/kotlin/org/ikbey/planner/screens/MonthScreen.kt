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
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MonthScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("левый экранчик - фулл месяц + стики ноутс (квадратики такие с важными делами)")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackClick) {
            Text("назад")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthScreenPreview() {
    MonthScreen( onBackClick = {} )
}
