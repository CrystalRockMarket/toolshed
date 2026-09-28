package it.kituwa.toolshed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.kituwa.toolshed.data.Fit
import it.kituwa.toolshed.data.Machine
import it.kituwa.toolshed.data.ResourceTotal
import it.kituwa.toolshed.data.Service
import it.kituwa.toolshed.data.formatCores
import it.kituwa.toolshed.data.formatMb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    state: ShedUiState,
    onSelectMachine: (String) -> Unit,
    onOpenService: (String) -> Unit,
    onClearMarks: () -> Unit,
    onBack: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text("Resource planner", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Does it fit on the box you have?",
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
                actions = {
                    IconButton(onClick = onClearMarks) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear all marks")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            MachinePicker(state.machine, onSelectMachine)

            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Shortlist (${state.shortlist.size})") },
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Running (${state.running.size})") },
                )
            }

            val services = if (tab == 0) state.shortlist else state.running
            val total = if (tab == 0) state.shortlistTotal else state.runningTotal
            val fit = if (tab == 0) state.shortlistFit else state.runningFit

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    FitCard(total, fit, state.machine)
                }

                if (services.isEmpty()) {
                    item {
                        Text(
                            if (tab == 0) {
                                "Nothing shortlisted yet. Open a service and tap Shortlist to add it " +
                                    "to the plan."
                            } else {
                                "Nothing marked as running yet. Open a service and tap Mark running " +
                                    "once it is up."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    item { SectionHeader("In this plan") }
                    items(services, key = { it.id }) { service ->
                        PlannedServiceRow(service) { onOpenService(service.id) }
                    }
                }

                item { Spacer(24.dp) }
            }
        }
    }
}

@Composable
private fun MachinePicker(selected: Machine, onSelect: (String) -> Unit) {
    Column {
        SectionHeader("The box", Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(Machine.ALL, key = { it.id }) { machine ->
                FilterChip(
                    selected = machine.id == selected.id,
                    onClick = { onSelect(machine.id) },
                    label = { Text(machine.label) },
                )
            }
        }
    }
}

@Composable
private fun FitCard(total: ResourceTotal, fit: Fit, machine: Machine) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "On ${machine.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                machine.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(16.dp)
            UsageBar("RAM, typical load", fit.ramUsedPct, fit.level)
            UsageBar("Disk, application data", fit.diskUsedPct, fit.level)
            Spacer(6.dp)
            Text(
                fit.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = colorForFit(fit.level),
                fontWeight = FontWeight.Medium,
            )
            Spacer(14.dp)
            Text(
                "Total if all ${total.services.size} are running together: " +
                    "${formatMb(total.ramTypicalMb)} RAM typical (${formatMb(total.ramMinMb)} at minimum), " +
                    "${total.diskGb} GB of data, ${formatCores(total.cores)} cores of work.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlannedServiceRow(service: Service, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(service.category)
            Column(
                Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
            ) {
                Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(
                    "${formatMb(service.resources.ramTypicalMb)} · ${service.resources.diskGb} GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
