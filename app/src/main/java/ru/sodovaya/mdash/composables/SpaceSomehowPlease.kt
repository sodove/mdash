package ru.sodovaya.mdash.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Stable
val Arrangement.SpaceSomehowPlease: Arrangement.HorizontalOrVertical
    get() = object : Arrangement.HorizontalOrVertical {
        override val spacing = 0.dp

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray
        ) = if (layoutDirection == LayoutDirection.Ltr) {
            placeSpaceBetween(totalSize, sizes, outPositions, reverseInput = false)
        } else {
            placeSpaceBetween(totalSize, sizes, outPositions, reverseInput = true)
        }

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            outPositions: IntArray
        ) = placeSpaceBetween(totalSize, sizes, outPositions, reverseInput = false)

        override fun toString() = "Arrangement#SpaceSomehowPlease"
    }

internal fun placeSpaceEvenly(
    totalSize: Int,
    size: IntArray,
    outPosition: IntArray,
    reverseInput: Boolean
) {
    val consumedSize = size.fold(0) { a, b -> a + b }
    val gapSize = (totalSize - consumedSize).toFloat() / (size.size + 1)
    var current = gapSize
    size.forEachIndexed(reverseInput) { index, it ->
        outPosition[index] = current.roundToInt()
        current += it.toFloat() + gapSize
    }
}

internal fun placeSpaceBetween(
    totalSize: Int,
    size: IntArray,
    outPosition: IntArray,
    reverseInput: Boolean
) {
    if (size.isEmpty()) return

    val consumedSize = size.fold(0) { a, b -> a + b }
    val singleItemSize = consumedSize / size.size
    val maximumItemsInRow = totalSize / singleItemSize
    val maximumConsumedSize = singleItemSize * maximumItemsInRow

    val noOfGaps = maxOf(maximumItemsInRow - 1, 1)
    val gapSize = (totalSize - maximumConsumedSize).toFloat() / noOfGaps

    var current = 0f
    if (reverseInput && size.size == 1) {
        // If the layout direction is right-to-left and there is only one gap,
        // we start current with the gap size. That forces the single item to be right-aligned.
        current = gapSize
    }
    size.forEachIndexed(reverseInput) { index, it ->
        outPosition[index] = current.roundToInt()
        current += it.toFloat() + gapSize
    }
}

internal fun placeSpaceAround(
    totalSize: Int,
    size: IntArray,
    outPosition: IntArray,
    reverseInput: Boolean
) {
    val consumedSize = size.fold(0) { a, b -> a + b }
    val gapSize = if (size.isNotEmpty()) {
        (totalSize - consumedSize).toFloat() / size.size
    } else {
        0f
    }
    var current = gapSize / 2
    size.forEachIndexed(reverseInput) { index, it ->
        outPosition[index] = current.roundToInt()
        current += it.toFloat() + gapSize
    }
}

private inline fun IntArray.forEachIndexed(reversed: Boolean, action: (Int, Int) -> Unit) {
    if (!reversed) {
        forEachIndexed(action)
    } else {
        for (i in (size - 1) downTo 0) {
            action(i, get(i))
        }
    }
}
