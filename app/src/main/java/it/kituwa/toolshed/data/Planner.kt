package it.kituwa.toolshed.data

import kotlin.math.roundToInt

enum class FitLevel { COMFORTABLE, TIGHT, OVER }

data class Fit(
    val level: FitLevel,
    val ramUsedPct: Int,
    val diskUsedPct: Int,
    val ramHeadroomMb: Int,
    val diskHeadroomGb: Int,
    val summary: String,
)

/**
 * A deliberately blunt comparison. Real capacity depends on load, disk speed,
 * and what else is running, so this answers one question only: would this
 * selection plausibly fit, and how much room is left.
 *
 * [ResourceTotal.ramTypicalMb] is used rather than the minimum, because a plan
 * built on minimums is a plan that stops working on a busy Tuesday.
 */
fun evaluateFit(total: ResourceTotal, machine: Machine): Fit {
    if (total.isEmpty) {
        return Fit(
            level = FitLevel.COMFORTABLE,
            ramUsedPct = 0,
            diskUsedPct = 0,
            ramHeadroomMb = machine.ramMb,
            diskHeadroomGb = machine.diskGb,
            summary = "Nothing in this list yet. Shortlist a few services to see whether they fit.",
        )
    }

    val ramUsedPct = percentOf(total.ramTypicalMb, machine.ramMb)
    val diskUsedPct = percentOf(total.diskGb, machine.diskGb)
    val ramHeadroomMb = machine.ramMb - total.ramTypicalMb
    val diskHeadroomGb = machine.diskGb - total.diskGb

    val level = when {
        total.ramTypicalMb > machine.ramMb -> FitLevel.OVER
        total.diskGb > machine.diskGb -> FitLevel.OVER
        ramUsedPct >= 70 || diskUsedPct >= 80 -> FitLevel.TIGHT
        else -> FitLevel.COMFORTABLE
    }

    val summary = when (level) {
        FitLevel.OVER -> when {
            ramHeadroomMb < 0 -> "Over by ${formatMb(-ramHeadroomMb)} of RAM against " +
                "${machine.label}. Drop something or move to a larger box."
            else -> "Over by ${diskHeadroomGb} GB of disk against ${machine.label}. " +
                "Drop something or attach more storage."
        }
        FitLevel.TIGHT -> "It fits, but there is not much room. Roughly ${formatMb(ramHeadroomMb)} " +
            "of RAM would be left once these are running and busy, and that is before the OS."
        FitLevel.COMFORTABLE -> "Comfortable. About ${formatMb(ramHeadroomMb)} of RAM and " +
            "${diskHeadroomGb} GB of disk left on ${machine.label}."
    }

    return Fit(level, ramUsedPct, diskUsedPct, ramHeadroomMb, diskHeadroomGb, summary)
}

private fun percentOf(used: Int, total: Int): Int =
    if (total <= 0) 0 else (used.toDouble() / total * 100).roundToInt()

/** 1536 becomes "1.5 GB", 1024 becomes "1 GB", 512 stays "512 MB". */
fun formatMb(mb: Int): String = when {
    mb >= 1024 && mb % 1024 == 0 -> "${mb / 1024} GB"
    mb >= 1024 -> {
        val gb = mb / 1024.0
        "${(gb * 10).roundToInt() / 10.0} GB"
    }
    else -> "$mb MB"
}

fun formatCores(cores: Double): String = when {
    cores <= 0.0 -> "—"
    cores == cores.toInt().toDouble() -> cores.toInt().toString()
    else -> cores.toString()
}
