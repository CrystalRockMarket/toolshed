package it.kituwa.toolshed

import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {

    @Test
    fun `an empty query returns the whole catalogue`() {
        assertEquals(Catalog.services.size, Catalog.search("").size)
        assertEquals(Catalog.services.size, Catalog.search("   ").size)
    }

    @Test
    fun `results are alphabetical by default`() {
        val names = Catalog.search("").map { it.name.lowercase() }
        assertEquals(names.sorted(), names)
    }

    @Test
    fun `matching is case insensitive`() {
        val lower = Catalog.search("immich").map { it.id }
        val upper = Catalog.search("IMMICH").map { it.id }
        assertEquals(lower, upper)
        assertTrue(lower.contains("immich"))
    }

    @Test
    fun `multiple words narrow the result rather than widening it`() {
        val both = Catalog.search("photo library")
        val photo = Catalog.search("photo")
        assertTrue(both.size <= photo.size)
        assertTrue(both.isNotEmpty())
    }

    @Test
    fun `a word in the blurb matches`() {
        // 'fingerprinting' appears in the ntfy and vault notes rather than taglines.
        val hits = Catalog.search("federated")
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it.id == "element" })
    }

    @Test
    fun `a category name is searchable`() {
        val hits = Catalog.search("monitoring")
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.all { it.category == Category.MONITORING })
    }

    @Test
    fun `a category filter restricts and combines with the query`() {
        val security = Catalog.search("", Category.SECURITY)
        assertTrue(security.isNotEmpty())
        assertTrue(security.all { it.category == Category.SECURITY })

        val combined = Catalog.search("vpn", Category.SECURITY)
        assertTrue(combined.isNotEmpty())
        assertTrue(combined.all { it.category == Category.SECURITY })
    }

    @Test
    fun `a query that matches nothing returns nothing`() {
        assertTrue(Catalog.search("kubernetes helm operator").isEmpty())
    }

    @Test
    fun `search does not match on compose contents or notes`() {
        // The guide content should not pollute the list the person scans.
        assertTrue(Catalog.search("openssl").isEmpty())
    }

    @Test
    fun `padded queries are trimmed`() {
        assertEquals(Catalog.search("immich"), Catalog.search("   immich   "))
    }
}
