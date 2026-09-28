package it.kituwa.toolshed

import it.kituwa.toolshed.data.Catalog
import it.kituwa.toolshed.data.Category
import it.kituwa.toolshed.data.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The catalogue ships as code, so the ways it can be wrong are structural:
 * a duplicate id, a dangling cross-reference, an empty guide. These catch the
 * mistakes that are otherwise only visible to whoever tries that service.
 */
class CatalogIntegrityTest {

    @Test
    fun `every service has a unique id`() {
        val duplicates = Catalog.services.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue("Duplicate service ids: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `every stack has a unique id`() {
        val duplicates = Catalog.stacks.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue("Duplicate stack ids: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `also-consider links all resolve`() {
        val known = Catalog.services.map { it.id }.toSet()
        val dangling = Catalog.services.flatMap { service ->
            service.alsoConsider.filterNot { it in known }.map { "${service.id} -> $it" }
        }
        assertTrue("Dangling also-consider links: $dangling", dangling.isEmpty())
    }

    @Test
    fun `stacks only reference services that exist`() {
        val known = Catalog.services.map { it.id }.toSet()
        val dangling = Catalog.stacks.flatMap { stack ->
            stack.serviceIds.filterNot { it in known }.map { "${stack.id} -> $it" }
        }
        assertTrue("Stacks reference unknown services: $dangling", dangling.isEmpty())
    }

    @Test
    fun `every required field is filled in`() {
        Catalog.services.forEach { service ->
            fun check(value: String, field: String) {
                assertTrue("${service.id}: $field is blank", value.isNotBlank())
            }
            check(service.name, "name")
            check(service.tagline, "tagline")
            check(service.blurb, "blurb")
            check(service.license, "license")
            check(service.defaultPort, "defaultPort")
            check(service.homepage, "homepage")
            check(service.docsUrl, "docsUrl")
            check(service.compose, "compose")
            assertTrue("${service.id}: bestFor is empty", service.bestFor.isNotEmpty())
            assertTrue("${service.id}: no install steps", service.installSteps.isNotEmpty())
            assertTrue("${service.id}: no notes", service.notes.isNotEmpty())
        }
    }

    @Test
    fun `every compose file looks like a compose file`() {
        Catalog.services.forEach { service ->
            val compose = service.compose
            assertTrue(
                "${service.id}: compose has no services block",
                compose.contains("services:") || compose.contains("\nservices:"),
            )
            assertTrue("${service.id}: compose has no image", compose.contains("image:"))
            assertTrue("${service.id}: compose does not start cleanly", compose.firstLine().isBlank())
            assertTrue(
                "${service.id}: unexpanded placeholder in compose",
                !compose.contains("\${'$'}{'$'}"),
            )
        }
    }

    @Test
    fun `every compose file is valid yaml`() {
        Catalog.services.forEach { service ->
            val problem = YamlLint.firstProblem(service.compose)
            assertTrue("${service.id}: $problem", problem == null)
        }
    }

    @Test
    fun `every stack compose file is valid yaml`() {
        Catalog.stacks.forEach { stack ->
            val problem = YamlLint.firstProblem(stack.compose)
            assertTrue("${stack.id}: $problem", problem == null)
        }
    }

    @Test
    fun `resource figures are plausible`() {
        Catalog.services.forEach { service ->
            val r = service.resources
            assertTrue("${service.id}: negative RAM", r.ramMinMb > 0)
            assertTrue(
                "${service.id}: typical RAM (${r.ramTypicalMb}) below minimum (${r.ramMinMb})",
                r.ramTypicalMb >= r.ramMinMb,
            )
            assertTrue("${service.id}: negative disk", r.diskGb > 0)
            assertTrue("${service.id}: implausible cores (${r.cores})", r.cores in 0.1..64.0)
        }
    }

    @Test
    fun `ports are numeric`() {
        Catalog.services.forEach { service ->
            val port = service.defaultPort.toIntOrNull()
            assertTrue("${service.id}: port '${service.defaultPort}' is not a number", port != null)
            assertTrue("${service.id}: port out of range", port!! in 1..65535)
        }
    }

    @Test
    fun `licences are named`() {
        Catalog.services.forEach { service ->
            assertTrue(
                "${service.id}: licence '${service.license}' does not look like one",
                service.license.length >= 3,
            )
        }
    }

    @Test
    fun `links are absolute http urls`() {
        Catalog.services.forEach { service ->
            listOf(service.homepage to "homepage", service.docsUrl to "docsUrl").forEach { (url, field) ->
                assertTrue("${service.id}: $field is '$url'", url.startsWith("https://"))
            }
        }
    }

    @Test
    fun `every category has entries`() {
        Category.ALL.forEach { category ->
            assertTrue(
                "Category ${category.label} has no services",
                Catalog.countIn(category) > 0,
            )
        }
    }

    @Test
    fun `stacks describe their own services`() {
        Catalog.stacks.forEach { stack ->
            assertTrue("${stack.id}: name blank", stack.name.isNotBlank())
            assertTrue("${stack.id}: description blank", stack.description.isNotBlank())
            assertTrue("${stack.id}: target hardware blank", stack.targetHardware.isNotBlank())
            assertTrue("${stack.id}: no steps", stack.installSteps.isNotEmpty())
            assertNotNull("No stacks at all", stack.difficulty)
        }
    }

    @Test
    fun `difficulty distribution is not degenerate`() {
        val levels = Catalog.services.groupBy { it.difficulty }.keys
        assertTrue("Only one difficulty used across the catalogue", levels.size >= 2)
        assertTrue(
            "No service is marked easy, which cannot be right",
            Catalog.services.any { it.difficulty == Difficulty.EASY },
        )
    }

    private fun String.firstLine(): String = lineSequence().firstOrNull().orEmpty()
}
