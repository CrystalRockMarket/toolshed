package it.kituwa.toolshed.data

/**
 * The bundled catalogue. Everything here ships inside the APK: no network
 * calls, no remote content, and nothing that can change without a release.
 */
object Catalog {
    val services: List<Service> = (
        PhotosMedia.services +
            FilesDocs.services +
            Productivity.services +
            Collaboration.services +
            Automation.services +
            Monitoring.services +
            Security.services +
            Networking.services +
            Platform.services +
            Business.services +
            Home.services
        ).sortedBy { it.name.lowercase() }

    val stacks: List<StarterStack> = StarterStacks.stacks.sortedBy { it.name.lowercase() }

    private val byId: Map<String, Service> = services.associateBy { it.id }

    /**
     * Pre-lowercased search text, built once at class load. Extension
     * properties cannot hold state, and rebuilding this per keystroke for
     * fifty services is wasted work.
     */
    private val searchIndex: Map<String, String> = services.associate { it.id to it.haystack() }

    fun byId(id: String): Service? = byId[id]

    fun require(id: String): Service =
        byId[id] ?: error("No service in the catalogue with id '$id'")

    fun byIds(ids: List<String>): List<Service> = ids.mapNotNull { byId[it] }

    fun inCategory(category: Category): List<Service> =
        services.filter { it.category == category }

    fun countIn(category: Category): Int = services.count { it.category == category }

    /**
     * Case-insensitive search across the fields a person would plausibly type.
     * Every whitespace-separated term must match somewhere, so "photo backup"
     * narrows rather than widening.
     */
    fun search(query: String, category: Category? = null): List<Service> {
        val scoped = category?.let { c -> services.filter { it.category == c } } ?: services
        val terms = query.trim().lowercase().split(WHITESPACE).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return scoped
        return scoped.filter { service ->
            val haystack = searchIndex[service.id].orEmpty()
            terms.all { term -> haystack.contains(term) }
        }
    }

    private val WHITESPACE = Regex("\\s+")
}

/** Everything a search should be able to match, lowercased once. */
private fun Service.haystack(): String = buildString {
    append(name.lowercase()).append(' ')
    append(tagline.lowercase()).append(' ')
    append(category.label.lowercase()).append(' ')
    append(blurb.lowercase()).append(' ')
    bestFor.forEach { append(it.lowercase()).append(' ') }
}
