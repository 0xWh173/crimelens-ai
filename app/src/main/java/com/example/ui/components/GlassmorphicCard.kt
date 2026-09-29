package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppleCardBorder
import com.example.ui.theme.AppleCardSurface

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = AppleCardBorder,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(cornerRadius)
    val cardColors = CardDefaults.cardColors(containerColor = AppleCardSurface)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = cardShape,
            colors = cardColors
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    content()
                }
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = cardShape,
            colors = cardColors
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    content()
                }
            }
        }
    }
}

