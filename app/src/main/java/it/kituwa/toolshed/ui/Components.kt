package it.kituwa.toolshed.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.kituwa.toolshed.data.Category
import it.kituwa.toolshed.data.Difficulty
import it.kituwa.toolshed.data.FitLevel
import it.kituwa.toolshed.data.GuideStep
import it.kituwa.toolshed.data.formatCores
import it.kituwa.toolshed.data.formatMb

object ShedColors {
    val running = Color(0xFF2E7D52)
    val planned = Color(0xFFC9832F)
    val comfortable = Color(0xFF2E7D52)
    val tight = Color(0xFFC98A1E)
    val over = Color(0xFFC0392B)
}

fun colorForFit(level: FitLevel): Color = when (level) {
    FitLevel.COMFORTABLE -> ShedColors.comfortable
    FitLevel.TIGHT -> ShedColors.tight
    FitLevel.OVER -> ShedColors.over
}

fun colorForCategory(category: Category): Color = when (category) {
    Category.PHOTOS_MEDIA -> Color(0xFF8E5FB0)
    Category.FILES_DOCS -> Color(0xFF3F7FB0)
    Category.PRODUCTIVITY -> Color(0xFF2E7D52)
    Category.COLLABORATION -> Color(0xFFB0562F)
    Category.AUTOMATION -> Color(0xFF7A6BC0)
    Category.MONITORING -> Color(0xFF2C8A8A)
    Category.SECURITY -> Color(0xFF9C3B57)
    Category.NETWORKING -> Color(0xFF5A7A2E)
    Category.PLATFORM -> Color(0xFF5B6472)
    Category.BUSINESS -> Color(0xFF9A6A1E)
    Category.HOME -> Color(0xFF2E6F8A)
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 6.dp),
    )
}

@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DifficultyChip(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val colour = when (difficulty) {
        Difficulty.EASY -> ShedColors.comfortable
        Difficulty.MODERATE -> ShedColors.planned
        Difficulty.FOCUSSED -> ShedColors.over
    }
    Row(
        modifier
            .background(colour.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(8.dp).background(colour, CircleShape))
        Text(
            difficulty.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun CategoryDot(category: Category, modifier: Modifier = Modifier) {
    Box(modifier.size(10.dp).background(colorForCategory(category), CircleShape))
}

/** RAM, disk and cores for one service, or for a whole selection. */
@Composable
fun ResourceRow(
    ramMinMb: Int,
    ramTypicalMb: Int,
    diskGb: Int,
    cores: Double,
    modifier: Modifier = Modifier,
    showMinimum: Boolean = true,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResourceStat(
            AppIcons.Memory,
            formatMb(ramTypicalMb),
            if (showMinimum) "min ${formatMb(ramMinMb)}" else "RAM",
        )
        ResourceStat(AppIcons.Storage, "$diskGb GB", "disk")
        ResourceStat(
            rememberVectorPainter(Icons.Filled.Build),
            formatCores(cores),
            "cores",
        )
    }
}

@Composable
private fun ResourceStat(icon: Painter, value: String, caption: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(Modifier.padding(start = 7.dp)) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A horizontal bar showing how full a resource is, coloured by fit. */
@Composable
fun UsageBar(label: String, usedPct: Int, level: FitLevel, modifier: Modifier = Modifier) {
    val colour by animateColorAsState(colorForFit(level), label = "usage")
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$usedPct%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colour,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(50)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(usedPct.coerceIn(2, 100) / 100f)
                    .height(8.dp)
                    .background(colour, RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
fun NumberedStep(index: Int, step: GuideStep, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Box(
            Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$index",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                step.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                step.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun NoteCard(step: GuideStep, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(step.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                step.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
fun RunningBadge(modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(ShedColors.running.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = ShedColors.running,
        )
        Text("Running", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun CodeBlock(yaml: String, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Box(
            Modifier
                .horizontalScroll(scroll)
                .padding(12.dp),
        ) {
            Text(
                text = highlightedYaml(yaml),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun highlightedYaml(yaml: String): AnnotatedString {
    val styles = mapOf(
        YamlTokenKind.KEY to SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        ),
        YamlTokenKind.STRING to SpanStyle(color = ShedColors.running),
        YamlTokenKind.NUMBER to SpanStyle(color = MaterialTheme.colorScheme.tertiary),
        YamlTokenKind.BOOLEAN to SpanStyle(color = MaterialTheme.colorScheme.tertiary),
        YamlTokenKind.COMMENT to SpanStyle(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
        ),
    )

    return buildAnnotatedString {
        tokenizeYaml(yaml).forEach { token ->
            val style = styles[token.kind]
            if (style != null) withStyle(style) { append(token.text) } else append(token.text)
        }
    }
}

@Composable
fun MetaRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Spacer(height: Dp) {
    Box(Modifier.height(height))
}
