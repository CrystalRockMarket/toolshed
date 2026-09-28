package it.kituwa.toolshed.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(versionName: String, onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text("About Toolshed", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Text(
                    "The shed where the self-hosted tools live.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(18.dp)
                AboutCard("What this is") {
                    Text(
                        "A reference for running services on your own hardware: what each one is " +
                            "for, how hard it is to install, what it costs in memory and disk, a " +
                            "working compose file, and the specific things that go wrong on a " +
                            "first attempt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(12.dp)
                AboutCard("Offline, and asking for nothing") {
                    Text(
                        "Every guide, every compose file, and every starter stack is inside the app. " +
                            "Its manifest declares no permissions of its own: no internet access, no " +
                            "storage, no files, no location, no identifiers. Nothing can be granted " +
                            "because nothing is requested, and the system enforces that rather than " +
                            "the author promising it. The only two things it can do are open a link " +
                            "in your browser and put text on your clipboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(12.dp)
                AboutCard("The numbers are estimates") {
                    Text(
                        "The memory, disk, and core figures are planning estimates taken from real " +
                            "installations, not vendor guarantees. They are deliberately conservative " +
                            "and they describe typical steady-state use rather than the peak. Use them " +
                            "to rule things out and to compare options, not to size a machine to " +
                            "the byte.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(12.dp)
                AboutCard("Licences worth reading") {
                    Text(
                        "Some services in this catalogue are not open source: n8n, Outline, " +
                            "Invoice Ninja, NocoDB, HashiCorp Vault, and Redis use source-available " +
                            "licences. They are listed because they are genuinely good at what they " +
                            "do, and each entry says so where it matters. Toolshed itself is " +
                            "GPL-3.0-or-later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(18.dp)
                SectionHeader("In the catalogue")
                Category.ALL.forEach { category ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                    ) {
                        CategoryDot(category)
                        Text(
                            category.label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .weight(1f),
                        )
                        Text(
                            Catalog.countIn(category).toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(18.dp)
                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(
                                ClipData.newPlainText(
                                    "Toolshed version",
                                    "Toolshed $versionName — GPL-3.0-or-later",
                                ),
                            )
                        },
                        label = { Text("Version $versionName") },
                        leadingIcon = { Icon(AppIcons.ContentCopy, null, Modifier.padding(0.dp)) },
                    )
                }
                Spacer(12.dp)
                Text(
                    "Brought to you by Kituwa IT. Prefer someone else to run your infrastructure? " +
                        "That is what we do. Toolshed stays free and self-hosted either way, and it " +
                        "is licensed GPL-3.0-or-later, so you are free to inspect it, change it, and " +
                        "run your own copy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(12.dp)
                AssistChip(
                    onClick = {
                        openUrl(context, "https://kituwa.com")
                    },
                    label = { Text("kituwa.com") },
                )
                Spacer(32.dp)
            }
        }
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(6.dp)
            content()
        }
    }
}
