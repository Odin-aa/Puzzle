package com.example.puzzle

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.puzzle.ui.theme.PuzzleTheme
import kotlinx.coroutines.selects.select
import java.util.*

class MainActivity : AppCompatActivity() {

//    val TAG: String = "MainActivity"

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println()
        setContent {
            MyApp {

                val imageList = mutableListOf<Int>()

                imageList.add(R.drawable.layer2_1)
                imageList.add(R.drawable.layer2_2)
                imageList.add(R.drawable.layer2_3)
                imageList.add(R.drawable.layer2_4)
                imageList.add(R.drawable.layer2_5)
                imageList.add(R.drawable.layer2_6)
                imageList.add(R.drawable.layer2_7)
                imageList.add(R.drawable.layer2_8)
//                imageList.add( R.drawable.layer1_9)
//                imageList.add( R.drawable.layer1_10)
//                imageList.add( R.drawable.layer1_11)
//                imageList.add( R.drawable.layer1_12)
//                imageList.add( R.drawable.layer1_13)
//                imageList.add( R.drawable.layer1_14)
//                imageList.add( R.drawable.layer1_15)

                imageList.add(
                    resources.getIdentifier(
                        "layer0_100",
                        "drawable",
                        applicationContext.packageName
                    )
                )

                PuzzleScaffold {
                    PuzzleView(matrixSize = 3, resource = resources, list = imageList)
                }


            }


        }
    }
}


@Composable
fun PuzzleScaffold(content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.Center)
    ) {

        content()
    }
}

@Composable
fun MyApp(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    PuzzleTheme(darkTheme = darkTheme, content = content)

}


@ExperimentalAnimationApi
@Composable
fun PuzzleView(matrixSize: Int, resource: Resources?, list: MutableList<Int>) {

    val isDone = remember { mutableStateOf(false) }


    //calculating inversions in list
    val inversion: Int = countInversions(resource = resource, list = list)

    //making puzzle solvable
    makeItSolvable(matrixSize = matrixSize, inversion = inversion, list = list)


    Column(
        modifier = Modifier
            .defaultMinSize()
            .aspectRatio(1f, false), horizontalAlignment = Alignment.CenterHorizontally
    ) {


        val actionDown = remember { mutableStateOf(MotionEvent.ACTION_UP) }

        if (!isDone.value) {

            val touchDownCoors = IntArray(2)
            var animatedX = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }


            var tileWidth = 0f
            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = Color.White)
                .pointerInteropFilter {

                    when (it.action) {

                        MotionEvent.ACTION_DOWN -> {

                            touchDownCoors[0] = it.x.toInt()
                            touchDownCoors[1] = it.y.toInt()


                            /////////
                            val columnIndex = touchDownCoors[0] / (tileWidth).toInt()
                            val rowIndex = (touchDownCoors[1] / (tileWidth)).toInt()
                            val tileTouchedIndex = columnIndex + rowIndex * matrixSize

                            //swap Touched Tile with space Tile code
                            val indexOfSpace = list.indexOf(R.drawable.layer0_100)

                            val spaceAdjIndices = getAdjacentIndices(
                                matrixSize,
                                (indexOfSpace / matrixSize),
                                (indexOfSpace % matrixSize)
                            )

                            if (spaceAdjIndices.contains(tileTouchedIndex)) {

                                Collections.swap(list, tileTouchedIndex, indexOfSpace)

                            }

                            //////////
                            actionDown.value = it.action
                            true
                        }

                        else -> {
                            actionDown.value = it.action
                            true
                        }
                    }
                }) {


                actionDown.value.let {

                    //Check if done
                    val done: Boolean =
                        checkIfDone(
                            resource = resource,
                            matrixSize = matrixSize,
                            list = list
                        )

                    if (done) {
                        isDone.value = true
                        return@Canvas
                    }

                    //calculate required indices from touch position
                    tileWidth = (size.width) / (matrixSize)
                    val margin = (tileWidth * 0.02).toInt()


                    //Drawing Tiles
                    val tileInPixel = tileWidth.toInt()
                    val halfMargin = margin / 2
                    for (j in 0 until matrixSize) {
                        for (i in 0 until matrixSize) {

                            val index = i + (j * matrixSize)
                            if (index < matrixSize * matrixSize) {
                                val bitmap =
                                    BitmapFactory.decodeResource(
                                        resource,
                                        list[index]
                                    )


                                drawIntoCanvas {

                                    val x = i * tileInPixel + margin
                                    val y = j * tileInPixel + margin


                                    translate(animatedX.value.x, animatedX.value.y) {

                                        it.nativeCanvas.drawBitmap(
                                            bitmap,
                                            null,
                                            Rect(
                                                x,
                                                y,
                                                x + tileInPixel - halfMargin,
                                                y + tileInPixel - halfMargin
                                            ),
                                            null
                                        )

                                    }
                                }

                            }
                        }
                    }


                }
            }

        } else {
            //shown when puzzle solved
            Column {
                Text(
                    text = "Well Done",
                    modifier = Modifier.wrapContentSize(align = Alignment.Center),
                    style = MaterialTheme.typography.h2,
                    color = Color.White
                )
                Button(onClick = {
                    startOver(resource = resource, matrixSize = matrixSize, list = list)
                    isDone.value = false
                }) {
                    Text(text = "Start Over")
                }

            }


        }
    }


}


fun startOver(resource: Resources?, matrixSize: Int, list: MutableList<Int>) {

    list.shuffle()
    val inversion = countInversions(resource = resource, list = list)
    makeItSolvable(matrixSize = matrixSize, inversion = inversion, list = list)
}

fun makeItSolvable(matrixSize: Int, inversion: Int, list: MutableList<Int>) {
    val e = (list.indexOf(R.drawable.layer0_100) / matrixSize) + 1
    println("Inversion make it solvable:$inversion e:$e")

    if (matrixSize % 2 == 0) {
        if ((inversion + e) % 2 != 0) {
            if (e == 1) {//swap element far away from space
                Collections.swap(list, 14, 15)
            } else {
                Collections.swap(list, 0, 1)
            }
            println("Inversion + e is odd changing it to:even")
        }
        println("Inversions for even:$inversion")

    } else {
        println("Inversions for odd:$inversion")
        if (inversion % 2 != 0) {
            if (e != 1) {
                Collections.swap(list, 0, 1)
            } else {
                Collections.swap(list, 7, 8)
            }
        }
    }
}

fun countInversions(resource: Resources?, list: MutableList<Int>): Int {
    var inversion = 0
    for (index in 0 until list.size - 1) {

        val i = resource?.getResourceName(list[index])?.split("layer")?.last()
            ?.split("_")?.last()?.toInt() ?: 0
        if (i == 100 || i == 0) {
            continue
        }

        for (item in index + 1 until list.size) {
            val j = resource?.getResourceName(list[item])?.split("layer")?.last()
                ?.split("_")?.last()?.toInt() ?: 0

            //Omit calculating inversions with empty tile which is of value 100
            if (j == 0 || j == 100) {
                println("Omitting j:$j")
                continue
            }

            if (i > j) {
                inversion++
            }
        }
    }

    return inversion
}

fun checkIfDone(resource: Resources?, matrixSize: Int, list: MutableList<Int>): Boolean {
    var done = true
    for (item in 0 until list.size) {
        val value = resource?.getResourceName(list[item])
            ?.split("layer")?.last()
            ?.split("_")?.last()?.toInt() ?: 0
        if (item == list.size - 1) {
            //omitting last item space
            continue
        }
        val second = item + 1
        println("checkIfDone:$value")
        val j: Int = if (second < matrixSize * matrixSize) {
            resource?.getResourceName(list[second])
                ?.split("layer")?.last()
                ?.split("_")?.last()?.toInt() ?: 0
        } else {
            item
        }
        if (value > j) {
            println("not complete yet")
            done = false
            break
        }

    }

    return done
}


//n,m are zero based indices, matrix size is either its width or height
fun getAdjacentIndices(matrixSize: Int, n: Int, m: Int): MutableList<Int> {
    println("size:$matrixSize ,space n:$n , m:$m")
    val list = mutableListOf<Int>()
    if (n - 1 >= 0) {
        list.add(matrixSize * (n - 1) + m)
    }

    if (n + 1 < matrixSize) {
        list.add(matrixSize * (n + 1) + m)
    }

    if (m - 1 >= 0) {
        list.add(matrixSize * n + m - 1)
    }

    if (m + 1 < matrixSize) {
        list.add(matrixSize * n + m + 1)
    }

    for (item in list) {
        println("getAdjacentIndices: $item")
    }
    return list
}
