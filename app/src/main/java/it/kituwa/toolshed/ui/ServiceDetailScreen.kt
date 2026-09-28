package it.kituwa.toolshed.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.Service

private val TABS = listOf("Guide", "Compose", "Notes")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    running: Boolean,
    shortlisted: Boolean,
    onToggleRunning: () -> Unit,
    onToggleShortlist: () -> Unit,
    onOpenService: (String) -> Unit,
    onBack: () -> Unit,
) {
    val service = Catalog.byId(serviceId)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    if (service == null) {
        EmptyState(
            title = "Not in this shed",
            body = "There is no service called \u201c$serviceId\u201d in the catalogue.",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text(service.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { copyToClipboard(context, service.compose) }) {
                        Icon(AppIcons.ContentCopy, contentDescription = "Copy compose file")
                    }
                    IconButton(
                        onClick = { shareText(context, service.compose, "${service.name} compose file") },
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share compose file")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                TABS.forEachIndexed { index, label ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(label) },
                    )
                }
            }
            when (tab) {
                0 -> GuideTab(
                    service = service,
                    running = running,
                    shortlisted = shortlisted,
                    onToggleRunning = onToggleRunning,
                    onToggleShortlist = onToggleShortlist,
                    onOpenService = onOpenService,
                )
                1 -> ComposeTab(service)
                else -> NotesTab(service)
            }
        }
    }
}

@Composable
private fun GuideTab(
    service: Service,
    running: Boolean,
    shortlisted: Boolean,
    onToggleRunning: () -> Unit,
    onToggleShortlist: () -> Unit,
    onOpenService: (String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    service.tagline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DifficultyChip(service.difficulty)
                    TagChip("Port ${service.defaultPort}")
                    TagChip(service.license)
                }
                Text(
                    service.blurb,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(16.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = running,
                        onClick = onToggleRunning,
                        label = { Text(if (running) "Running" else "Mark running") },
                    )
                    FilterChip(
                        selected = shortlisted,
                        onClick = onToggleShortlist,
                        label = { Text(if (shortlisted) "Shortlisted" else "Shortlist") },
                    )
                }
                Spacer(20.dp)
                SectionHeader("Footprint")
                ResourceRow(
                    ramMinMb = service.resources.ramMinMb,
                    ramTypicalMb = service.resources.ramTypicalMb,
                    diskGb = service.resources.diskGb,
                    cores = service.resources.cores,
                )
                Spacer(20.dp)
                SectionHeader("What it is good for")
                service.bestFor.forEach {
                    Text(
                        "· $it",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                Spacer(20.dp)
                SectionHeader("Links")
                LinkRow("Project site", service.homepage)
                LinkRow("Documentation", service.docsUrl)
                Spacer(20.dp)
                SectionHeader("Installation guide")
            }
        }

        itemsIndexed(service.installSteps) { index, step ->
            NumberedStep(index + 1, step, Modifier.padding(horizontal = 16.dp))
        }

        if (service.alsoConsider.isNotEmpty()) {
            item {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Also consider")
                    service.alsoConsider.forEach { id ->
                        val other = Catalog.byId(id) ?: return@forEach
                        AssistChip(
                            onClick = { onOpenService(other.id) },
                            label = { Text(other.name) },
                            modifier = Modifier.padding(vertical = 3.dp),
                        )
                    }
                    Spacer(24.dp)
                }
            }
        } else {
            item { Spacer(24.dp) }
        }
    }
}

@Composable
private fun ComposeTab(service: Service) {
    val context = LocalContext.current

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "A working compose file, with the values that usually cause first-run trouble " +
                        "left as environment variables. Put those in a .env file beside it, then run " +
                        "docker compose up -d.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(12.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { copyToClipboard(context, service.compose) },
                        label = { Text("Copy") },
                        leadingIcon = {
                            Icon(AppIcons.ContentCopy, null, Modifier.size(18.dp))
                        },
                    )
                    AssistChip(
                        onClick = { shareText(context, service.compose, "${service.name} compose file") },
                        label = { Text("Share") },
                        leadingIcon = { Icon(Icons.Filled.Share, null, Modifier.size(18.dp)) },
                    )
                }
                Spacer(14.dp)
                CodeBlock(service.compose)
                Spacer(20.dp)
                SectionHeader("Bringing it up")
                CodeLine("docker compose up -d")
                CodeLine("docker compose logs -f")
                CodeLine("docker compose down")
                Spacer(24.dp)
            }
        }
    }
}

@Composable
private fun NotesTab(service: Service) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "The things that go wrong, and why they go wrong.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(service.notes) { _, note ->
            NoteCard(note, Modifier.padding(horizontal = 16.dp))
        }
        item { Spacer(24.dp) }
    }
}

@Composable
private fun CodeLine(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

@Composable
private fun LinkRow(label: String, url: String) {
    val context = LocalContext.current
    AssistChip(
        onClick = { openUrl(context, url) },
        label = { Text(label) },
        leadingIcon = { Icon(AppIcons.OpenInNew, null, Modifier.size(18.dp)) },
        modifier = Modifier.padding(vertical = 3.dp),
    )
}
