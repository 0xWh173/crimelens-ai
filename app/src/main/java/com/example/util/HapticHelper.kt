package com.example.util

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

enum class HapticFeedbackStyle {
    LIGHT_TAP,
    KEYBOARD_PRESS,
    LONG_PRESS,
    CONFIRM_SUCCESS,
    REJECT_ERROR,
    ALERT_WARNING
}

class HapticHelper(
    private val view: View?,
    private val vibrator: Vibrator?
) {
    fun trigger(style: HapticFeedbackStyle = HapticFeedbackStyle.KEYBOARD_PRESS) {
        try {
            when (style) {
                HapticFeedbackStyle.LIGHT_TAP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        view?.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                    } else {
                        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                }
                HapticFeedbackStyle.KEYBOARD_PRESS -> {
                    view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                HapticFeedbackStyle.LONG_PRESS -> {
                    view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
                HapticFeedbackStyle.CONFIRM_SUCCESS -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    } else {
                        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
                HapticFeedbackStyle.REJECT_ERROR -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    } else {
                        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
                HapticFeedbackStyle.ALERT_WARNING -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 80, 50, 80),
                                -1
                            )
                        )
                    } else {
                        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
            }
        } catch (e: Exception) {
            // Graceful fallback
            try {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun rememberHapticFeedbackHelper(): HapticHelper {
    val view = LocalView.current
    val context = LocalContext.current
    val vibrator = remember(context) {
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
    }
    return remember(view, vibrator) {
        HapticHelper(view, vibrator)
    }
}
