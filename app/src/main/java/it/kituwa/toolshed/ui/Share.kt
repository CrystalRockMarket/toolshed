package it.kituwa.toolshed.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * The two things this app is allowed to do outside itself: hand a URL to
 * whatever browser the person has, and put text on their clipboard. Both are
 * handled here so the failure case is one place, and a device with no browser
 * gets a no-op rather than a crash.
 */

fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: Exception) {
        // No browser, or nothing registered for http(s). Nothing to do.
    }
}

fun copyToClipboard(context: Context, text: String, label: String = "docker-compose.yml") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun shareText(context: Context, text: String, subject: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(intent, subject))
    } catch (_: Exception) {
        // Nothing installed that can receive text.
    }
}
