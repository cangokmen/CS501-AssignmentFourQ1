package com.example.assignmentfourqnewnew

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.assignmentfourqnewnew.ui.theme.AssignmentFourQNewnewTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Data class to hold lifecycle event information ---
data class LifecycleEvent(
    val name: String,
    val timestamp: String,
    val color: Color
)

// --- ViewModel to store and manage lifecycle event logs ---
class LifeCycleLoggerVW : ViewModel() {
    private val _events = MutableStateFlow<List<LifecycleEvent>>(emptyList())
    val events: StateFlow<List<LifecycleEvent>> = _events

    private fun addEvent(eventName: String, color: Color) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val newEvent = LifecycleEvent(name = eventName, timestamp = timestamp, color = color)
        _events.value = _events.value + newEvent
        Log.i("LifeTracker", "Logged event: $eventName at $timestamp")
    }

    // --- Mappings for event colors ---
    private val colors = mapOf(
        "onCreate" to Color(0xFF277A2A),
        "onStart" to Color(0xFF13558C),
        "onResume" to Color(0xFF9A7607),
        "onPause" to Color(0xFF935804),
        "onStop" to Color(0xFF690802),
        "onDestroy" to Color(0xFF06638D),
        "onAny" to Color.Gray
    )

    fun logEvent(event: Lifecycle.Event) {
        val eventName = "on" + event.name.substring(2).lowercase().replaceFirstChar { it.titlecase() }
        val color = colors[eventName] ?: Color.Black
        addEvent(eventName, color)
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: LifeCycleLoggerVW by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssignmentFourQNewnewTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                // This observer will be tied to the activity's lifecycle
                LifecycleObserver(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    showSnackbarOnTransition = true // Configurable setting
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    LifeTrackerScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun LifecycleObserver(
    viewModel: LifeCycleLoggerVW,
    snackbarHostState: SnackbarHostState,
    showSnackbarOnTransition: Boolean, // Setting to control snackbar
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val scope = rememberCoroutineScope() // Get a coroutine scope

    // Use DisposableEffect to tie the observer's lifecycle to the composable's lifecycle
    DisposableEffect(lifecycleOwner) {
        // Corrected: Use LifecycleEventObserver to match the onStateChanged method signature
        val observer = LifecycleEventObserver { _, event ->
            viewModel.logEvent(event)
            if (showSnackbarOnTransition) {
                val eventName = "on" + event.name.substring(2).lowercase().replaceFirstChar { it.titlecase() }
                // Launch the coroutine in the correct scope
                scope.launch {
                    snackbarHostState.showSnackbar("Lifecycle Transition: $eventName")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // The onDispose block is called when the composable is removed from the tree
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun LifeTrackerScreen(modifier: Modifier = Modifier, viewModel: LifeCycleLoggerVW) {
    val events by viewModel.events.collectAsState()

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "LifeTracker",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events.reversed()) { event -> // reversed to show newest first
                LifecycleEventRow(event)
            }
        }
    }
}

@Composable
fun LifecycleEventRow(event: LifecycleEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(event.color)
            )
            Text(
                text = event.name,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = event.timestamp,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LifeTrackerScreenPreview() {
    AssignmentFourQNewnewTheme {
        // Create a fake ViewModel for preview purposes
        val previewViewModel = LifeCycleLoggerVW().apply {
            logEvent(Lifecycle.Event.ON_CREATE)
            logEvent(Lifecycle.Event.ON_START)
            logEvent(Lifecycle.Event.ON_RESUME)
        }
        LifeTrackerScreen(viewModel = previewViewModel)
    }
}
