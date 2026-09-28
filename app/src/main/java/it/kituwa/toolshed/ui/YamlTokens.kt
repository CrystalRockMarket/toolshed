package it.kituwa.toolshed.ui

/**
 * A deliberately small YAML tokeniser. It is not a parser and does not need to
 * be: the input is compose files we wrote, and the goal is colour that helps
 * someone spot a key, a value, and a comment at a glance.
 *
 * Kept free of Compose types so it can be tested without an Android runtime.
 */
enum class YamlTokenKind { KEY, STRING, NUMBER, BOOLEAN, COMMENT, PLAIN }

data class YamlToken(val text: String, val kind: YamlTokenKind)

/**
 * Emits one newline token per newline character in the input, so concatenating
 * the token text always reproduces the original exactly. String.lines() cannot
 * be used here because it yields a trailing empty element for text that ends
 * in a newline, which would add a phantom line.
 */
fun tokenizeYaml(yaml: String): List<YamlToken> {
    val tokens = mutableListOf<YamlToken>()
    var index = 0
    while (index < yaml.length) {
        val end = yaml.indexOf('\n', index)
        val line = if (end == -1) yaml.substring(index) else yaml.substring(index, end)
        if (line.isNotEmpty()) tokenizeLine(line, tokens)
        if (end == -1) break
        tokens += YamlToken("\n", YamlTokenKind.PLAIN)
        index = end + 1
    }
    return tokens
}

private fun tokenizeLine(line: String, out: MutableList<YamlToken>) {
    var i = 0
    val indent = line.takeWhile { it == ' ' }
    if (indent.isNotEmpty()) {
        out += YamlToken(indent, YamlTokenKind.PLAIN)
        i = indent.length
    }

    // A comment runs to the end of the line, unless it is inside a quoted
    // scalar, which the value scanner below has already consumed.
    if (i < line.length && line[i] == '#') {
        out += YamlToken(line.substring(i), YamlTokenKind.COMMENT)
        return
    }

    val keyEnd = findKeyEnd(line, i)
    if (keyEnd > i) {
        out += YamlToken(line.substring(i, keyEnd), YamlTokenKind.KEY)
        out += YamlToken(line.substring(keyEnd, keyEnd + 1), YamlTokenKind.PLAIN)
        i = keyEnd + 1
    }

    if (i < line.length && line[i] == ' ') {
        out += YamlToken(" ", YamlTokenKind.PLAIN)
        i++
    }

    if (i < line.length && line[i] == '#') {
        out += YamlToken(line.substring(i), YamlTokenKind.COMMENT)
        return
    }

    val value = scanValue(line, i, out)
    i = value

    // Whatever is left is either a trailing comment or trailing whitespace.
    // The space that separates a value from its comment is emitted as plain
    // text so the round trip stays exact.
    if (i < line.length) {
        val rest = line.substring(i)
        when {
            rest.startsWith(" #") -> {
                out += YamlToken(" ", YamlTokenKind.PLAIN)
                out += YamlToken(rest.substring(1), YamlTokenKind.COMMENT)
            }
            rest.isNotBlank() -> out += YamlToken(rest, YamlTokenKind.PLAIN)
        }
    }
}

/** Index of the colon that ends a key, or -1 when the line has no key. */
private fun findKeyEnd(line: String, from: Int): Int {
    var inSingle = false
    var inDouble = false
    var i = from
    while (i < line.length) {
        when (val c = line[i]) {
            '\'' -> if (!inDouble) inSingle = !inSingle
            '"' -> if (!inSingle) inDouble = !inDouble
            ':' -> if (!inSingle && !inDouble && (i + 1 == line.length || line[i + 1] == ' ')) return i
            '#' -> if (!inSingle && !inDouble && i > from && line[i - 1] == ' ') return -1
        }
        i++
    }
    return -1
}

/** Emits the value tokens and returns the index reached. */
private fun scanValue(line: String, from: Int, out: MutableList<YamlToken>): Int {
    var i = from
    val rest = line.substring(from)
    if (rest.isEmpty()) return from

    val quote = rest[0]
    if (quote == '\'' || quote == '"') {
        val end = findQuoteEnd(line, i, quote)
        val text = line.substring(i, end)
        out += YamlToken(text, YamlTokenKind.STRING)
        return end
    }

    val end = rest.indexOf(" #").let { if (it == -1) line.length else from + it }
    val text = line.substring(i, end)
    if (text.isNotEmpty()) {
        out += YamlToken(text, kindOfScalar(text))
    }
    return end
}

private fun kindOfScalar(text: String): YamlTokenKind = when {
    text.equals("true", true) || text.equals("false", true) -> YamlTokenKind.BOOLEAN
    text.toLongOrNull() != null || text.toDoubleOrNull() != null -> YamlTokenKind.NUMBER
    else -> YamlTokenKind.PLAIN
}

private fun findQuoteEnd(line: String, from: Int, quote: Char): Int {
    var i = from + 1
    while (i < line.length) {
        if (line[i] == '\\' && quote == '"') {
            i += 2
            continue
        }
        if (line[i] == quote) {
            // Two quotes in a row is an escaped quote, not the end.
            if (quote == '\'' && i + 1 < line.length && line[i + 1] == '\'') {
                i += 2
                continue
            }
            return i + 1
        }
        i++
    }
    return line.length
}
