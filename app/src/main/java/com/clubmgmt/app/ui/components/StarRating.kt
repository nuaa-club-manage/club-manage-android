package com.clubmgmt.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.ui.theme.Yellow400

@Composable
fun StarRating(
    rating: Float,
    onRatingChange: ((Int) -> Unit)? = null,
    readOnly: Boolean = false,
    starSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    var hoverRating by remember { mutableIntStateOf(0) }
    val currentRating = if (hoverRating > 0) hoverRating else rating.toInt()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (star in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "评价 $star 星",
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (!readOnly) {
                            Modifier.clickable {
                                onRatingChange?.invoke(star)
                            }
                        } else Modifier
                    ),
                tint = if (currentRating >= star) Yellow400 else Gray400
            )
        }
    }
}

private val Gray400 = Color(0xFF9CA3AF)
