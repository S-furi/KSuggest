package io.github.sfuri

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLClassLoader
import java.util.jar.JarFile

class KSuggest {
    private val jarName = "kotlin-stdlib-2.1.10.jar"
    private val ktStdLibMvn = "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.1.10/$jarName"
    private val ktStdLibPath= listOf("src", "main", "resources", jarName).joinToString(File.separator)

    fun simpleLazyFinder(name: String): Sequence<String> = lazyStdLibDeclarations().filter { it.simpleName.startsWith(name) }.map { it.name }

    fun concurrentLazyFinder(name: String, bufSize: Int = 1_000): List<String> = runBlocking {
        lazyStdLibDeclarations()
            .windowed(size = bufSize, step = bufSize, partialWindows = true)
            .map { chunk ->
                async {
                    chunk.filter { it.simpleName.startsWith(name) }.map { it.name }
                }
            }.toList().awaitAll().flatten()
    }

    private fun getKtStdLib() {
        if (File(this.ktStdLibPath).exists()) {
            println("$jarName already exists")
            return
        }
        val connection = (URI(this.ktStdLibMvn).toURL().openConnection() as HttpURLConnection).also { it.requestMethod = "GET" }
        val jar = connection.inputStream.buffered().use { it.readAllBytes() }
        File(this.ktStdLibPath).writeBytes(jar)
        println("$jarName downloaded successfully at ${this.ktStdLibPath}")
    }

    private fun lazyStdLibDeclarations() = sequence<Class<*>> {
        ktStdLibIsPresent()
        val jarFile = try {
            File(ktStdLibPath)
        } catch (e: NullPointerException) {
            throw IllegalStateException("Something went wrong")
        }

        JarFile(jarFile).use { jar ->
            for (entry in jar.entries()) {
                if (entry.name.endsWith(".class") && !entry.name.contains("module-info.class")) {
                    val className = entry.name.removeSuffix(".class").replace("/", ".")
                    val clazz = URLClassLoader(arrayOf(jarFile.toURI().toURL())).loadClass(className)
                    yield(clazz)
                }
            }
        }
    }

    private fun ktStdLibIsPresent() = File(ktStdLibPath).exists().takeIf { it } ?: getKtStdLib()
}


fun main(args: Array<String>) {
    if (args.size != 1 || args[0].isEmpty()) {
        println("Usage: KSuggest <name>")
        return
    }

    val nameToFind = args[0]
    val kSuggest = KSuggest()
    println("Simple Lazy Finder results: ")
    kSuggest.simpleLazyFinder(nameToFind).forEach(::println)
    println("Concurrent Lazy Finder results: ")
    kSuggest.concurrentLazyFinder(nameToFind, bufSize = 100).forEach(::println)
}