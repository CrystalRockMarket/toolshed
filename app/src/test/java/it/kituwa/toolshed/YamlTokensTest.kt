package it.kituwa.toolshed

import it.kituwa.toolshed.ui.YamlTokenKind
import it.kituwa.toolshed.ui.tokenizeYaml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YamlTokensTest {

    @Test
    fun `tokenising preserves the original text exactly`() {
        val yaml = """
            services:
              web:
                image: nginx:alpine
        """.trimIndent() + "\n"

        val rebuilt = tokenizeYaml(yaml).joinToString("") { it.text }
        assertEquals(yaml, rebuilt)
    }

    @Test
    fun `a key before a colon is marked as a key`() {
        val tokens = tokenizeYaml("  image: nginx:alpine\n")
        val key = tokens.first { it.kind == YamlTokenKind.KEY }
        assertEquals("image", key.text)
    }

    @Test
    fun `a trailing comment is a single comment token`() {
        val tokens = tokenizeYaml("  restart: always # keep it up\n")
        val comments = tokens.filter { it.kind == YamlTokenKind.COMMENT }
        assertEquals(1, comments.size)
        assertEquals("# keep it up", comments.first().text)
    }

    @Test
    fun `a quoted value keeps its quotes and is a string`() {
        val tokens = tokenizeYaml("  ports: '8080:80'\n")
        val string = tokens.first { it.kind == YamlTokenKind.STRING }
        assertEquals("'8080:80'", string.text)
    }

    @Test
    fun `a colon inside a quoted value does not split the key`() {
        val tokens = tokenizeYaml("  rule: Host(`example.com`)\n")
        val key = tokens.first { it.kind == YamlTokenKind.KEY }
        assertEquals("rule", key.text)
    }

    @Test
    fun `numbers and booleans are distinguished from bare text`() {
        val numbers = tokenizeYaml("  value: 8080\n").filter { it.kind == YamlTokenKind.NUMBER }
        assertEquals("8080", numbers.single().text)

        val booleans = tokenizeYaml("  insecure: false\n").filter { it.kind == YamlTokenKind.BOOLEAN }
        assertEquals("false", booleans.single().text)
    }

    @Test
    fun `a version with two dots is not a number`() {
        // '1.2.3' parses as a double, but it is an image tag and should read as text.
        val numbers = tokenizeYaml("  tag: 1.2.3\n").filter { it.kind == YamlTokenKind.NUMBER }
        assertTrue(numbers.isEmpty())
    }

    @Test
    fun `indentation is emitted as plain text`() {
        val tokens = tokenizeYaml("    image: alpine\n")
        assertEquals("    ", tokens.first { it.kind == YamlTokenKind.PLAIN }.text)
    }

    @Test
    fun `a full compose file round-trips`() {
        val yaml = """
            services:
              db:
                image: postgres:16
                environment:
                  - POSTGRES_PASSWORD=${'$'}{POSTGRES_PASSWORD}
                healthcheck:
                  test: ['CMD-SHELL', 'pg_isready -U postgres']
                  interval: 10s

        """.trimIndent()

        val rebuilt = tokenizeYaml(yaml).joinToString("") { it.text }
        assertEquals(yaml, rebuilt)
    }

    @Test
    fun `a block scalar body is left as plain text`() {
        val yaml = "command: >\n  redis-server\n    --maxmemory 512mb\n"
        val rebuilt = tokenizeYaml(yaml).joinToString("") { it.text }
        assertEquals(yaml, rebuilt)
    }
}
