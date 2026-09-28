package it.kituwa.toolshed

import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.FitLevel
import it.kituwa.toolshed.data.Machine
import it.kituwa.toolshed.data.evaluateFit
import it.kituwa.toolshed.data.formatCores
import it.kituwa.toolshed.data.formatMb
import it.kituwa.toolshed.data.totalOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerTest {

    private val mini = Machine.byId("minipc")
    private val pi = Machine.byId("pi5")
    private val tiny = Machine.byId("vps1")

    @Test
    fun `totals add up`() {
        val immich = Catalog.require("immich")
        val grafana = Catalog.require("grafana")
        val total = totalOf(listOf(immich, grafana))

        assertEquals(2, total.services.size)
        assertEquals(immich.resources.ramMinMb + grafana.resources.ramMinMb, total.ramMinMb)
        assertEquals(immich.resources.ramTypicalMb + grafana.resources.ramTypicalMb, total.ramTypicalMb)
        assertEquals(immich.resources.diskGb + grafana.resources.diskGb, total.diskGb)
        assertEquals(immich.resources.cores + grafana.resources.cores, total.cores, 0.001)
    }

    @Test
    fun `an empty plan is comfortable and says so`() {
        val fit = evaluateFit(totalOf(emptyList()), mini)
        assertEquals(FitLevel.COMFORTABLE, fit.level)
        assertTrue(fit.summary.contains("Nothing", ignoreCase = true))
    }

    @Test
    fun `a small selection fits a mini pc`() {
        val fit = evaluateFit(totalOf(Catalog.byIds(listOf("ntfy", "uptime-kuma", "homarr"))), mini)
        assertEquals(FitLevel.COMFORTABLE, fit.level)
        assertTrue(fit.ramHeadroomMb > 0)
    }

    @Test
    fun `a heavy selection does not fit a small vps`() {
        val fit = evaluateFit(
            totalOf(Catalog.byIds(listOf("immich", "nextcloud", "onlyoffice", "grafana"))),
            tiny,
        )
        assertEquals(FitLevel.OVER, fit.level)
        assertTrue(fit.ramHeadroomMb < 0)
        assertTrue(fit.summary.contains("Over by"))
    }

    @Test
    fun `disk pressure alone is enough to be over`() {
        // Small RAM, enormous disk appetite.
        val fit = evaluateFit(totalOf(Catalog.byIds(listOf("minio", "jellyfin"))), tiny)
        assertEquals(FitLevel.OVER, fit.level)
        assertTrue(fit.diskHeadroomGb < 0)
    }

    @Test
    fun `a tight fit is reported as tight, not comfortable`() {
        val fit = evaluateFit(totalOf(Catalog.byIds(listOf("authentik", "grafana", "prometheus"))), pi)
        assertTrue(
            "expected at least tight, got ${fit.level}",
            fit.level == FitLevel.TIGHT || fit.level == FitLevel.OVER,
        )
    }

    @Test
    fun `the whole catalogue does not fit anything in the picker`() {
        val everything = evaluateFit(totalOf(Catalog.services), mini)
        assertEquals(FitLevel.OVER, everything.level)
    }

    @Test
    fun `percentages stay in range for any machine`() {
        // Over-capacity is a legitimate, expected result, so the bounds here
        // are sanity limits on the arithmetic rather than on the fit verdict.
        Catalog.services.forEach { service ->
            Machine.ALL.forEach { machine ->
                val fit = evaluateFit(totalOf(listOf(service)), machine)
                assertTrue(
                    "${service.id} on ${machine.id}: ram ${fit.ramUsedPct}%",
                    fit.ramUsedPct in 0..2000,
                )
                assertTrue(
                    "${service.id} on ${machine.id}: disk ${fit.diskUsedPct}%",
                    fit.diskUsedPct in 0..100000,
                )
            }
        }
    }

    @Test
    fun `machines resolve by id with a sensible fallback`() {
        assertEquals("minipc", Machine.byId("minipc").id)
        assertEquals("minipc", Machine.byId("does-not-exist").id)
    }

    @Test
    fun `megabytes are formatted for humans`() {
        assertEquals("512 MB", formatMb(512))
        assertEquals("1 GB", formatMb(1024))
        assertEquals("1.5 GB", formatMb(1536))
        assertEquals("8 GB", formatMb(8192))
        assertEquals("3.2 GB", formatMb(3276))
    }

    @Test
    fun `cores are formatted without a pointless decimal`() {
        assertEquals("2", formatCores(2.0))
        assertEquals("0.5", formatCores(0.5))
        assertEquals("—", formatCores(0.0))
    }
}
