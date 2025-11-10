package org.ikbey.planner.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import org.ikbey.planner.DarkGreen
import org.ikbey.planner.IconType
import org.ikbey.planner.getIcon
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.ikbey.planner.*
import org.ikbey.planner.dataBase.CalendarEvent
import org.ikbey.planner.dataBase.ServiceLocator

@Composable
fun EventsScreen(
    onBackClick: () -> Unit,
    onSwipeToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localDb = ServiceLocator.localDatabaseManager

    var showSettings by remember {mutableStateOf(false)}
    var showFilters by remember {mutableStateOf(false)}
    var isSwipeActive by remember { mutableStateOf(false) }

    val calendarEvents = remember { mutableStateOf(emptyList<CalendarEvent>()) }
    val filteredEvents = remember { mutableStateOf(emptyList<CalendarEvent>()) }

    val selectedFilters by remember { mutableStateOf(FilterManager.selectedFilters) }

    LaunchedEffect(Unit) {
        val allEvents = localDb.getCalendarEvents()
        calendarEvents.value = removeDuplicates(allEvents)
        applyFilters(calendarEvents.value, FilterManager.selectedFilters, filteredEvents)
    }

    LaunchedEffect(FilterManager.selectedFilters) {
        applyFilters(calendarEvents.value, FilterManager.selectedFilters, filteredEvents)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Yellow)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { change ->
                        val start = change.x
                        val screenWidth = size.width

                        isSwipeActive = start < screenWidth * 0.40f
                    },
                    onHorizontalDrag = {change, dragAmount ->
                        if (isSwipeActive && dragAmount > 50f){
                            onSwipeToHome()
                        }
                    },

                    onDragEnd = {
                        isSwipeActive = false
                    }
                )
            }
    ){
        if (isSwipeActive) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color.Gray)
            )
        }

        if (showSettings) {
            SettingsCard(
                onDismiss = { showSettings = false }
            )
        }

        if (showFilters) {
            FilterPage(
                onDismiss = { showFilters = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(start= 10.dp, end = 10.dp)
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "События",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 28.sp,
                    color = Color.Black,
                )

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsButton(
                        isSettingsOpen = showSettings,
                        onClick = {showSettings = true}
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(48.dp)
                            .clickable { showFilters = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIcon(IconType.FILTER),
                            contentDescription = "Фильтр",
                            modifier = Modifier.size(32.dp),
                            tint = DarkGreen
                        )
                    }
                }
            }

            if (filteredEvents.value.isNotEmpty()) {
                EventsList(events = filteredEvents.value)
            }
            else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Пока мероприятий нет :(",
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 20.sp,
                        color = DarkGreen
                    )
                }
            }
        }
    }
}

private fun applyFilters(
    allEvents: List<CalendarEvent>,
    selectedFilters: Set<String>,
    filteredEvents: MutableState<List<CalendarEvent>>
) {
    if (selectedFilters.isEmpty()) {
        filteredEvents.value = allEvents
    } else {
        filteredEvents.value = allEvents.filter { event ->
            val eventCalendarName = event.calendar_name
            selectedFilters.contains(eventCalendarName)
        }
    }
}

private fun removeDuplicates(events: List<CalendarEvent>): List<CalendarEvent> {
    val currentDate = getCurrentDate()

    return events
        .distinctBy { event ->
            "${event.title}-${event.date}-${event.start_time}-${event.calendar_name}"
        }

        .filter { event ->
            isEventInFuture(event, currentDate)
        }
}

private fun isEventInFuture(event: CalendarEvent, currentDate: String): Boolean {
    val dateComparison = event.date.compareTo(currentDate)

    if (dateComparison > 0) {
        return true
    } else {
        return false
    }
}

private fun getCurrentDate(): String {
    val date = PlatformDate()
    return "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
}

private fun formatTime(timeString: String?): String {
    return timeString?.take(5) ?: ""
}

private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            val calendarManager = CalendarManager()
            val dayOfWeek = calendarManager.calculateDayOfWeek(year, month, day)
            val dayOfWeekName = when (dayOfWeek) {
                1 -> "ПН"
                2 -> "ВТ"
                3 -> "СР"
                4 -> "ЧТ"
                5 -> "ПТ"
                6 -> "СБ"
                7 -> "ВС"
                else -> ""
            }

            val monthName = when (month) {
                1 -> "янв"
                2 -> "фев"
                3 -> "мар"
                4 -> "апр"
                5 -> "май"
                6 -> "июн"
                7 -> "июл"
                8 -> "авг"
                9 -> "сен"
                10 -> "окт"
                11 -> "ноя"
                12 -> "дек"
                else -> ""
            }

            "$day $monthName, $dayOfWeekName"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = formatDate(date),
        fontSize = 20.sp,
        fontFamily = getInterFont(InterFontType.SEMI_BOLD),
        color = DarkOrange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp, start = 12.dp)
    )
}

@Composable
fun EventItem(event: CalendarEvent) {
    val localDb = ServiceLocator.localDatabaseManager
    var isTracked by remember { mutableStateOf(event.is_tracked)}
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightGreen
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = formatTime(event.start_time),
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = DarkGreen
                )
                Text(
                    text = formatTime(event.end_time),
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = DarkGreen
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "•",
                fontSize = 20.sp,
                color = DarkGreen
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                val eventTitle = event.title

                Text(
                    text = eventTitle,
                    fontSize = 20.sp,
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    color = DarkGreen,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!event.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.description,
                        fontSize = 14.sp,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        color = DarkGreen,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!event.location.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.location,
                        fontSize = 14.sp,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        color = DarkGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            TrackButton(
                isTracked = isTracked,
                onToggle = {
                    isTracked = !isTracked
                    coroutineScope.launch {
                        localDb.updateCalendarEventTracking(event.id, isTracked)
                    }
                },
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun TrackButton(
    isTracked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onToggle() }
            .background(
                color = if (isTracked) DarkGreen else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.5.dp,
                color = DarkGreen,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            fontSize = 20.sp,
            fontWeight = FontWeight.W300,
            color = if (isTracked) Color.White else DarkGreen
        )
    }
}


@Composable
fun EventsList(events: List<CalendarEvent>) {
    val eventsByDate = events.groupBy { it.date }
        .toList()
        .sortedBy { it.first }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Yellow, Color.Transparent),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }

        eventsByDate.forEach { (date, dayEvents) ->
            item {
                DateHeader(date = date)
            }

            items(dayEvents.sortedBy { it.start_time }) { event ->
                EventItem(event = event)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }


}

object FilterManager {
    private var _selectedFilters = mutableStateOf<Set<String>>(emptySet())
    val selectedFilters: Set<String> get() = _selectedFilters.value

    fun addFilter(filter: String) {
        _selectedFilters.value = _selectedFilters.value + filter
    }

    fun removeFilter(filter: String) {
        _selectedFilters.value = _selectedFilters.value - filter
    }
}

@Composable
fun FilterPage(onDismiss: () -> Unit){
    val localDb = ServiceLocator.localDatabaseManager
    val calendarEvents = remember { mutableStateOf(emptyList<CalendarEvent>()) }
    val selectedFilters = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        calendarEvents.value = localDb.getCalendarEvents()

        selectedFilters.clear()
        selectedFilters.addAll(FilterManager.selectedFilters)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    text = "Фильтры:",
                    fontSize = 24.sp,
                    color = Black,
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Выберите, какие события\nвы хотите видеть",
                    fontSize = 20.sp,
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    color = TransparentDarkGreen,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(LightGreen, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    val uniqueCalendarNames = calendarEvents.value
                        .map { it.calendar_name }
                        .distinct()
                        .sorted()

                    items(uniqueCalendarNames) { calendarName ->
                        FilterItem(
                            name = calendarName,
                            isSelected = selectedFilters.contains(calendarName),
                            onToggle = {
                                if (selectedFilters.contains(calendarName)) {
                                    selectedFilters.remove(calendarName)
                                    FilterManager.removeFilter(calendarName)
                                }
                                else {
                                    selectedFilters.add(calendarName)
                                    FilterManager.addFilter(calendarName)
                                }
                            }
                        )
                        if (calendarName != uniqueCalendarNames.last()) {
                            Divider(
                                thickness = 0.5.dp,
                                color = Color.Transparent,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterItem(
    name: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isSelected) TransparentDarkGreen else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) TransparentDarkGreen else DarkGreen,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkGreen, CircleShape)

                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = DarkGreen,
            modifier = Modifier.weight(1f)
        )
    }
}
