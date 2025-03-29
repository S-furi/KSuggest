#!/usr/bin/env kotlin

import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.system.exitProcess

object KSuggest {
    private const val JAR_NAME = "kotlin-stdlib-2.1.10.jar"
    private const val KOTLIN_STDLIB_MAVEN_URL = "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.1.10/$JAR_NAME"
    private val ktStdLibPath = listOf("/tmp", JAR_NAME).joinToString(File.separator)

    fun simpleLazyFinder(name: String): Sequence<String> = lazyStdLibDeclarations().filter { it.simpleName.startsWith(name) }.map { it.name }

    private fun getKtStdLib() {
        if (File(this.ktStdLibPath).exists()) {
            return
        }
        val connection = (URI(this.KOTLIN_STDLIB_MAVEN_URL).toURL().openConnection() as HttpURLConnection).also { it.requestMethod = "GET" }
        val jar = connection.inputStream.buffered().use { it.readAllBytes() }
        File(this.ktStdLibPath).writeBytes(jar)
    }

    private fun lazyStdLibDeclarations() = sequence<Class<*>> {
        ktStdLibIsPresent()
        val jarFile = try {
            File(ktStdLibPath)
        } catch (e: NullPointerException) {
            throw IllegalStateException("Something went wrong")
        }

        val loader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), this.javaClass.classLoader)

        JarFile(jarFile).use { jar ->
            for (entry in jar.entries()) {
                if (entry.name.endsWith(".class") && !entry.name.contains("module-info.class")) {
                    val className = entry.name.removeSuffix(".class").replace("/", ".")
                    val clazz = loader.loadClass(className)
                    yield(clazz)
                }
            }
        }

    }

    private fun ktStdLibIsPresent() = File(ktStdLibPath).exists().takeIf { it } ?: getKtStdLib()
}

if (args.size != 1 || args[0].isEmpty()) {
    println("Usage: KSuggest <name>")
    exitProcess(1)
}

val nameToFind = args[0]
KSuggest.simpleLazyFinder(nameToFind).forEach(::println)
