package it.kituwa.toolshed

import it.kituwa.toolshed.BuildConfig
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the F-Droid metadata against drifting from the build.
 *
 * A submission is rejected for the boring reasons: a missing changelog for the
 * version being built, a short description over the limit, or versions.yml
 * that no longer matches the build config. None of that is caught by the
 * compiler, so it is checked here instead.
 */
class MetadataTest {

    private val root = File("../").canonicalFile
    private fun metadata(vararg parts: String) = File(root, "metadata/android/en-US/${parts.joinToString("/")}")

    @Test
    fun `title, descriptions and changelog all exist`() {
        listOf(
            metadata("title.txt"),
            metadata("short_description.txt"),
            metadata("full_description.txt"),
            metadata("changelogs", "${BuildConfig.VERSION_CODE}.txt"),
        ).forEach { file ->
            assertTrue("missing ${file.path}", file.isFile)
            assertTrue("empty ${file.name}", file.readText().isNotBlank())
        }
    }

    @Test
    fun `short description fits the F-Droid limit`() {
        val text = metadata("short_description.txt").readText().trim()
        assertTrue(
            "short_description is ${text.length} characters, limit is 255",
            text.length <= 255,
        )
    }

    @Test
    fun `full description is valid-ish HTML and mentions the version's scope`() {
        val text = metadata("full_description.txt").readText()
        assertTrue("no opening <p>", text.contains("<p>"))
        assertTrue("no closing <p>", text.contains("</p>"))
        assertTrue("brackets are unbalanced", text.count { it == '<' } == text.count { it == '>' })
        assertTrue("does not state the licence", text.contains("GPL-3.0-or-later"))
    }

    @Test
    fun `the versions file matches the version being built`() {
        val yml = File(root, "fdroid/versions.yml").readText()
        assertTrue(
            "versions.yml has no versionCode ${BuildConfig.VERSION_CODE}:\n$yml",
            yml.contains("versionCode: ${BuildConfig.VERSION_CODE}"),
        )
        assertTrue(
            "versions.yml has no versionName ${BuildConfig.VERSION_NAME}:\n$yml",
            yml.contains("versionName: ${BuildConfig.VERSION_NAME}"),
        )
    }

    @Test
    fun `the icon is a 512 pixel PNG`() {
        val icon = metadata("images", "icon", "icon.png")
        assertTrue("missing icon.png", icon.isFile)
        val header = icon.readBytes().take(33)
        assertTrue("not a PNG", header[1] == 'P'.code.toByte() && header[2] == 'N'.code.toByte())
        // IHDR width and height are big-endian 32-bit values at offsets 16 and 20.
        val width = ((header[16].toInt() and 0xFF) shl 24) or ((header[17].toInt() and 0xFF) shl 16) or
            ((header[18].toInt() and 0xFF) shl 8) or (header[19].toInt() and 0xFF)
        val height = ((header[20].toInt() and 0xFF) shl 24) or ((header[21].toInt() and 0xFF) shl 16) or
            ((header[22].toInt() and 0xFF) shl 8) or (header[23].toInt() and 0xFF)
        assertEquals("F-Droid expects a 512x512 icon", 512, width)
        assertEquals("F-Droid expects a 512x512 icon", 512, height)
    }

    @Test
    fun `there are screenshots, and none of them is enormous`() {
        val dir = metadata("images", "phoneScreenshots")
        assertTrue("no phoneScreenshots directory", dir.isDirectory)
        val shots = dir.listFiles { f -> f.name.endsWith(".png") }.orEmpty()
        assertTrue("no screenshots at all", shots.isNotEmpty())
        assertTrue("F-Droid shows at most 8, found ${shots.size}", shots.size in 1..8)
        shots.forEach { shot ->
            assertTrue("${shot.name} is ${shot.length() / 1024} KB, too large", shot.length() < 1_500_000)
        }
    }

    @Test
    fun `the licence file is GPL-3`() {
        val licence = File(root, "LICENSE")
        assertTrue("no LICENSE file", licence.isFile)
        val head = licence.readText().take(400)
        assertTrue("LICENSE is not GPL", head.contains("GNU GENERAL PUBLIC LICENSE"))
        assertTrue("LICENSE is not version 3", head.contains("Version 3"))
    }
}
