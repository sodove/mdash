package ru.sodovaya.mdash.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import ru.sodovaya.mdash.composables.PercentageToColor
import ru.sodovaya.mdash.composables.manipulateColor

@Composable
fun Gauge(
    modifier: Modifier = Modifier.aspectRatio(1f),
    percentage : Float = 0f,
    text: String,
    gaugeColor : Color = PercentageToColor(percentage = percentage),
    strokeWidth: Float = 60f,
    additionalText: String? = null
) {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }

    var sweepAngle by remember {
        mutableStateOf(0f)
    }

    val color by animateColorAsState(
        targetValue = gaugeColor,
        animationSpec = tween(
            delayMillis = 100,
            durationMillis = 300,
            easing = LinearEasing
        )
    )

    val animatedSweepAngle by animateFloatAsState(
        targetValue = sweepAngle,
        animationSpec = tween(
            delayMillis = 100,
            durationMillis = 300,
            easing = LinearEasing
        )
    )

    LaunchedEffect(key1 = percentage){
        sweepAngle = (240 * percentage) / 100
    }

    Box(
        modifier = modifier.onSizeChanged {
            size = it
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = modifier
        ) {
            drawArc(
                color = manipulateColor(color, 0.4f),
                size = Size(size.width.toFloat(), size.height.toFloat()),
                startAngle = -210f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            drawArc(
                color = color,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                startAngle = -210f,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = TextStyle(
                    color = color,
                    fontSize = (strokeWidth / 3).sp,
                    fontWeight = FontWeight.Bold
                )
            )
            additionalText?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        color = color,
                        fontSize = (strokeWidth / 3).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}