package it.kituwa.toolshed

import it.kituwa.toolshed.data.Catalog
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards against the catalogue contradicting itself.
 *
 * The failure this exists for is specific and was real: six entries advertised
 * a port in the detail header that their own bundled compose file did not
 * publish. A person follows the compose file, then goes to the address the app
 * told them to use, and finds nothing listening.
 */
class PortConsistencyTest {

    @Test
    fun `a service's advertised port appears in its own compose file`() {
        Catalog.services.forEach { service ->
            val port = service.defaultPort
            val compose = service.compose
            val reachable = exposes(compose, port) ||
                publishes(compose, port) ||
                // Published only to the loopback interface, as with a proxy
                // that the operator is expected to front themselves.
                Regex("""(?:127\.0\.0\.1|localhost):\s*$port:""").containsMatchIn(compose) ||
                // No host port at all: the service runs on the host network and
                // picks its own, which the guide has to say in prose.
                hostNetwork(compose) ||
                // Exposed only to a reverse proxy by label.
                compose.contains("loadbalancer.server.port=$port")

            assertTrue(
                "${service.id} advertises port $port but its compose file never mentions it. " +
                    "Either fix defaultPort or add the mapping to the compose file.",
                reachable,
            )
        }
    }

    @Test
    fun `a port in the header is the one the guide tells you to open`() {
        // Where the install steps name an explicit port, the header should
        // agree with it rather than leaving two numbers in play.
        Catalog.services.forEach { service ->
            val port = service.defaultPort
            val otherPorts = Regex("""(?:open|visit|browse|goto|go to)\s+(?:https?://[^\s]*:)?(\d{2,5})""")
                .findAll(service.installSteps.joinToString("\n") { it.body })
                .map { it.groupValues[1] }
                .toSet()
            otherPorts.forEach { mentioned ->
                assertTrue(
                    "${service.id} advertises $port but a step tells the reader to open $mentioned",
                    mentioned == port || service.notes.any { it.body.contains(":$mentioned") },
                )
            }
        }
    }

    @Test
    fun `no two services in a stack claim the same host port`() {
        // Sharing traefik, postgres, or redis between stacks is deliberate and
        // fine. Two services binding the same host port inside one stack is a
        // conflict the person copying the file will hit immediately.
        Catalog.stacks.forEach { stack ->
            val claims = mutableMapOf<String, String>()
            stack.serviceIds.forEach { id ->
                val service = Catalog.require(id)
                publishedHostPorts(service.compose).forEach { port ->
                    val previous = claims.put(port, id)
                    assertTrue(
                        "stack ${stack.id}: $id and ${previous ?: "?"} both bind host port $port",
                        previous == null || previous == id,
                    )
                }
            }
        }
    }

    /** Host-side ports only, deduplicated, ignoring the protocol. */
    private fun publishedHostPorts(compose: String): Set<String> =
        Regex(
            """^\s*-\s*'?\"?[0-9.]*:(\d{2,5}):\d{2,5}(?:/(?:tcp|udp))?'?\"?\s*$""",
            RegexOption.MULTILINE,
        ).findAll(compose).map { it.groupValues[1] }.toSet()

    private fun publishes(compose: String, port: String) =
        Regex("""^\s*-\s*'?\"?[0-9.]*:?$port:\d{2,5}(?:/(?:tcp|udp))?'?\"?\s*$""", RegexOption.MULTILINE)
            .containsMatchIn(compose)

    private fun exposes(compose: String, port: String) =
        Regex("""^\s*-\s*'$port'\s*$""", RegexOption.MULTILINE).containsMatchIn(compose)

    private fun hostNetwork(compose: String) = compose.contains("network_mode: host")

}
