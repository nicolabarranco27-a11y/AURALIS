package com.auralis.player.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Deque
import java.util.LinkedList

/**
 * Comprobacion arquitectonica: domain/ debe ser Kotlin puro.
 * Recorre los fuentes del dominio y verifica que no existan imports
 * de android.* ni androidx.*.
 */
class DomainPurityTest {

    private val forbiddenPrefixes = listOf("android.", "androidx.")

    private fun locateDomainDir(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            val candidate = dir.resolve("src/main/java/com/auralis/player/domain")
            if (candidate.isDirectory) return candidate
            dir = dir.parentFile
        }
        error("No se encontro el directorio domain/")
    }

    private fun ktFiles(root: File): List<File> {
        val files = mutableListOf<File>()
        val pending: Deque<File> = LinkedList()
        pending.push(root)
        while (pending.isNotEmpty()) {
            val current = pending.pop()
            if (current.isDirectory) {
                current.listFiles()?.forEach { pending.push(it) }
            } else if (current.name.endsWith(".kt")) {
                files.add(current)
            }
        }
        return files.sortedBy { it.path }
    }

    @Test
    fun `domain no importa android ni androidx`() {
        val domainDir = locateDomainDir()
        val files = ktFiles(domainDir)

        assertTrue("domain/ no contiene fuentes Kotlin", files.isNotEmpty())

        val violations = buildList {
            for (file in files) {
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import ")) {
                        val target = trimmed.removePrefix("import ").trim()
                        if (forbiddenPrefixes.any { target.startsWith(it) }) {
                            add("${file.name}:$index -> $target")
                        }
                    }
                }
            }
        }

        assertTrue(
            "Imports prohibidos en domain/: $violations",
            violations.isEmpty(),
        )
    }
}
