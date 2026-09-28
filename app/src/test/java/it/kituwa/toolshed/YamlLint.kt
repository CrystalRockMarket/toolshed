package it.kituwa.toolshed

/**
 * A small structural linter for the compose files bundled in the catalogue.
 *
 * It is not a YAML parser and does not need to be. It looks for the four
 * mistakes that actually turn up when a compose file is written by hand:
 * tabs, duplicate keys in the same mapping, unterminated quoted values, and
 * lines that look like keys but have no name.
 */
object YamlLint {

    fun firstProblem(yaml: String): String? {
        val topLevel = mutableSetOf<String>()
        var scopeIndent = -1
        val keysByIndent = mutableMapOf<Int, MutableSet<String>>()

        yaml.lines().forEachIndexed { index, raw ->
            val lineNumber = index + 1

            if (raw.contains('\t')) return problem(lineNumber, "tab character")

            val trimmed = raw.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            val indent = raw.length - raw.trimStart(' ').length

            if (indent == 0) {
                val key = keyOf(trimmed) ?: return@forEachIndexed
                if (!topLevel.add(key)) {
                    return problem(lineNumber, "duplicate top-level key '$key'")
                }
                scopeIndent = 0
                keysByIndent.clear()
                keysByIndent.getOrPut(0) { mutableSetOf() }
                return@forEachIndexed
            }

            if (scopeIndent < 0) return@forEachIndexed

            // Leaving the current mapping resets the narrower scopes.
            while (scopeIndent >= 0 && indent <= scopeIndent && scopeIndent > 0) {
                keysByIndent.remove(scopeIndent)
                scopeIndent--
            }
            if (indent > scopeIndent) {
                scopeIndent = indent
                keysByIndent.getOrPut(indent) { mutableSetOf() }
            }

            if (trimmed.startsWith("- ")) {
                val item = trimmed.removePrefix("- ").trim()
                // A key on the list item line opens a new mapping for its siblings.
                if (keyOf(item) != null && !item.contains(" #")) {
                    keysByIndent.getOrPut(indent + 2) { mutableSetOf() }
                    scopeIndent = indent + 2
                }
                return@forEachIndexed
            }

            if (trimmed == "-") return@forEachIndexed

            if (trimmed.startsWith(":")) {
                return problem(lineNumber, "mapping entry with no key")
            }

            val key = keyOf(trimmed)
            if (key == null) {
                // Block scalars and continuation lines are legitimate.
                return@forEachIndexed
            }

            val seen = keysByIndent.getOrPut(indent) { mutableSetOf() }
            if (!seen.add(key)) {
                return problem(lineNumber, "duplicate key '$key' at the same level")
            }

            unterminatedQuote(raw)?.let { return problem(lineNumber, it) }
        }

        return null
    }

    /** Returns the key name if this line opens a mapping entry, else null. */
    private fun keyOf(trimmed: String): String? {
        if (trimmed.startsWith("-")) return null
        val match = KEY.find(trimmed) ?: return null
        return match.groupValues[1]
    }

    private fun unterminatedQuote(raw: String): String? {
        val colon = raw.indexOf(':')
        if (colon == -1) return null
        val value = raw.substring(colon + 1).trimStart()
        val quote = value.firstOrNull() ?: return null
        if (quote != '\'' && quote != '"') return null
        if (stripComment(value).length < 2) return "unterminated $quote quoted value"
        val body = value.substring(1)
        val closing = if (quote == '"') {
            body.indexOf("\"")
        } else {
            body.indexOf('\'')
        }
        return if (closing < 0) "unterminated $quote quoted value" else null
    }

    private fun stripComment(value: String): String {
        val hash = value.indexOf(" #")
        return if (hash >= 0) value.substring(0, hash) else value
    }

    private fun problem(line: Int, message: String) = "line $line: $message"

    private val KEY = Regex("^([A-Za-z0-9_.\\-/]+):(\\s|$)")
}
