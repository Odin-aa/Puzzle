package com.example.puzzle

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.puzzle.ui.theme.PuzzleTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope


class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PuzzleTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {

                }
            }
        }
    }
}


@Preview
@Composable
fun something() {
    val scaffoldState = rememberScaffoldState()

// Consider negative values to mean 'cut corner' and positive values to mean 'round corner'
    val sharpEdgePercent = -50f
    val roundEdgePercent = 45f
// Start with sharp edges
    val animatedProgress = remember { Animatable(sharpEdgePercent) }
// Create a coroutineScope for the animation
    val coroutineScope = rememberCoroutineScope()
// animation value to animate shape
    val progress = animatedProgress.value.roundToInt()

// When progress is 0, there is no modification to the edges so we are just drawing a rectangle.
// This allows for a smooth transition between cut corners and round corners.
    val fabShape = when {
        progress < 0 -> {
            CutCornerShape(abs(progress))
        }
        progress == roundEdgePercent.toInt() -> {
            CircleShape
        }
        else -> {
            RoundedCornerShape(progress)
        }
    }
// lambda to call to trigger shape animation
    val changeShape: () -> Unit = {
        val target = animatedProgress.targetValue
        val nextTarget = if (target == roundEdgePercent) sharpEdgePercent else roundEdgePercent
        coroutineScope.launch {
            animatedProgress.animateTo(
                targetValue = nextTarget,
                animationSpec = TweenSpec(durationMillis = 600)
            )
        }
    }


    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { Text("Drawer content") },
        topBar = { TopAppBar(title = { Text("Scaffold with bottom cutout") }) },
        bottomBar = {
            BottomAppBar(cutoutShape = fabShape) {
                IconButton(
                    onClick = {
                        coroutineScope.launch { scaffoldState.drawerState.open() }
                    }
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Localized description")
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Change shape") },
                onClick = changeShape,
                shape = fabShape
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        content = { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                items(100) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(color = Color.Black)
                    )
                }
            }
        }
    )
}