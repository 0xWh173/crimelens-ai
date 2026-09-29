package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningModule
import com.example.data.model.UserAchievement
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.adaptive

@Composable
fun LearningGamificationScreen(
    achievements: List<UserAchievement>,
    learningModules: List<LearningModule>,
    userPoints: Int,
    onCompleteQuiz: (Int) -> Unit
) {
    var activeModuleForQuiz by remember { mutableStateOf<LearningModule?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Learn",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ).adaptive(),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scam awareness guides and interactive quizzes",
                    style = MaterialTheme.typography.bodyLarge.adaptive(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Rank Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AppleBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "XP",
                            tint = AppleBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Security Rank Progress",
                            style = MaterialTheme.typography.labelMedium.adaptive(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$userPoints XP Earned",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold).adaptive(),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Badges Section
        item {
            Column {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ).adaptive(),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        achievements.forEachIndexed { index, badge ->
                            AppleBadgeRow(badge = badge)
                            if (index < achievements.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Learning Academy Modules
        item {
            Text(
                text = "Guides & Quizzes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ).adaptive(),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(learningModules) { module ->
            AppleLessonModuleCard(
                module = module,
                onStartQuiz = { activeModuleForQuiz = module }
            )
        }
    }

    if (activeModuleForQuiz != null) {
        QuizDialog(
            module = activeModuleForQuiz!!,
            onDismiss = { activeModuleForQuiz = null },
            onQuizCompleted = { score ->
                onCompleteQuiz(score)
                activeModuleForQuiz = null
            }
        )
    }
}

@Composable
fun AppleBadgeRow(badge: UserAchievement) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (badge.isUnlocked) AppleBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (badge.isUnlocked) Icons.Outlined.Shield else Icons.Outlined.Lock,
                contentDescription = badge.title,
                tint = if (badge.isUnlocked) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = badge.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodyMedium.adaptive(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (badge.isUnlocked) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Unlocked",
                tint = AppleGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AppleLessonModuleCard(
    module: LearningModule,
    onStartQuiz: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold).adaptive(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodyMedium.adaptive(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(14.dp)
            ) {
                Text(
                    text = module.lessonContent,
                    style = MaterialTheme.typography.bodyMedium.adaptive(),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("start_quiz_${module.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = "Quiz", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Take Quiz (+50 XP)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold).adaptive(),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun QuizDialog(
    module: LearningModule,
    onDismiss: () -> Unit,
    onQuizCompleted: (Int) -> Unit
) {
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOptionIdx by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var showExplanation by remember { mutableStateOf(false) }

    val question = module.quiz.getOrNull(currentQuestionIdx)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold).adaptive(),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (question != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Question ${currentQuestionIdx + 1} of ${module.quiz.size}",
                        style = MaterialTheme.typography.labelSmall.adaptive(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    question.options.forEachIndexed { idx, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOptionIdx = idx }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedOptionIdx == idx,
                                onClick = { selectedOptionIdx = idx },
                                colors = RadioButtonDefaults.colors(selectedColor = AppleBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium.adaptive(),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (showExplanation) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium.adaptive(),
                                color = AppleBlue
                            )
                        }
                    }
                }
            } else {
                Text("Quiz completed! Detective XP added.", style = MaterialTheme.typography.bodyLarge.adaptive(), color = MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question != null) {
                        if (!showExplanation) {
                            if (selectedOptionIdx == question.correctAnswerIndex) {
                                score += 1
                            }
                            showExplanation = true
                        } else {
                            if (currentQuestionIdx + 1 < module.quiz.size) {
                                currentQuestionIdx += 1
                                selectedOptionIdx = -1
                                showExplanation = false
                            } else {
                                onQuizCompleted(score * 5)
                            }
                        }
                    } else {
                        onDismiss()
                    }
                },
                enabled = selectedOptionIdx != -1,
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (showExplanation) "Next" else "Submit",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
