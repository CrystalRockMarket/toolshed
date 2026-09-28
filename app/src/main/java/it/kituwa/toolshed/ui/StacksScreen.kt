package it.kituwa.toolshed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.StarterStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StacksScreen(onOpenStack: (String) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text("Starter stacks", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Combinations that work together",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Each of these is a complete compose file with a consistent set of services, " +
                        "networks, and environment variables. Copy it, put the values in a .env file, " +
                        "and adjust the paths.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(Catalog.stacks, key = { it.id }) { stack ->
                StackCard(stack) { onOpenStack(stack.id) }
            }
            item { Spacer(24.dp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StackCard(stack: StarterStack, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stack.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stack.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(12.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    SectionHeader("Sizing")
                    Text(
                        stack.targetHardware,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DifficultyChip(stack.difficulty, Modifier.padding(top = 18.dp))
            }
            Spacer(10.dp)
            FlowRow(stack.serviceIds.mapNotNull { Catalog.byId(it)?.name })
        }
    }
}

/** A minimal wrapping row of service names. */
@Composable
private fun FlowRow(names: List<String>) {
    val rows = names.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { TagChip(it) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackDetailScreen(
    stackId: String,
    onOpenService: (String) -> Unit,
    onBack: () -> Unit,
) {
    val stack = Catalog.stacks.firstOrNull { it.id == stackId }
    if (stack == null) {
        EmptyState(
            title = "No such stack",
            body = "There is no starter stack called “$stackId”.",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text(stack.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { copyToClipboard(context, stack.compose) }) {
                        Icon(AppIcons.ContentCopy, contentDescription = "Copy compose file")
                    }
                    IconButton(
                        onClick = { shareText(context, stack.compose, "${stack.name} compose file") },
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share compose file")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Plan", "Compose", "Steps").forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> StackPlanTab(stack, onOpenService)
                1 -> StackComposeTab(stack)
                else -> StackStepsTab(stack)
            }
        }
    }
}

@Composable
private fun StackPlanTab(stack: StarterStack, onOpenService: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stack.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(18.dp)
                SectionHeader("Target hardware")
                Text(stack.targetHardware, style = MaterialTheme.typography.bodyMedium)
                Spacer(10.dp)
                DifficultyChip(stack.difficulty)
                Spacer(18.dp)
                SectionHeader("In this stack")
                stack.serviceIds.forEach { id ->
                    val service = Catalog.byId(id) ?: return@forEach
                    AssistChip(
                        onClick = { onOpenService(service.id) },
                        label = { Text("${service.name} · ${service.tagline}") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    )
                }
                Spacer(18.dp)
                SectionHeader("If you add all of it")
                val total = it.kituwa.toolshed.data.totalOf(Catalog.byIds(stack.serviceIds))
                ResourceRow(
                    ramMinMb = total.ramMinMb,
                    ramTypicalMb = total.ramTypicalMb,
                    diskGb = total.diskGb,
                    cores = total.cores,
                )
                Spacer(12.dp)
                Text(
                    "That is the sum of the individual estimates. Shared databases usually make it " +
                        "lower in practice, and concurrent first-run tasks such as OCR make it higher.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(24.dp)
            }
        }
    }
}

@Composable
private fun StackComposeTab(stack: StarterStack) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Every value the file expects is listed at the bottom of the compose file itself, " +
                        "with a note on how to generate it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(14.dp)
                CodeBlock(stack.compose)
                Spacer(24.dp)
            }
        }
    }
}

@Composable
private fun StackStepsTab(stack: StarterStack) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Bringing it up")
                Text(
                    "docker network create proxy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    "docker compose up -d",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    "docker compose logs -f",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(20.dp)
                SectionHeader("In order")
            }
        }
        items(stack.installSteps.size) { index ->
            NumberedStep(index + 1, stack.installSteps[index], Modifier.padding(horizontal = 16.dp))
        }
        item { Spacer(24.dp) }
    }
}
