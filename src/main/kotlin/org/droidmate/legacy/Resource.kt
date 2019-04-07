// Author: Konrad Jamrozik, github.com/konrad-jamrozik

package org.droidmate.legacy

import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.*

class Resource @JvmOverloads constructor(val name: String, val allowAmbiguity: Boolean = false) {

	val urls: List<URL> = {

		val urls = ClassLoader.getSystemResources(name).toList()

		if (urls.isEmpty())
			throw IOException("No resource URLs found for path \"$name\"")

		if (!allowAmbiguity && urls.size > 1)
			throw IOException("More than one resource URL found for path $name. " +
					"The found URLs:\n${urls.joinToString(separator = "\n")}")

		urls
	}()

	val text: String by lazy {
		url.text
	}

	val url: URL by lazy {
		check(!allowAmbiguity) { "check failed: !allowAmbiguity" }
		urls.single()
	}

	val path: Path by lazy {
		when {
			url.protocol == "jar" -> {
				val connection = url.openConnection() as JarURLConnection
				Paths.get(connection.jarFileURL.toURI())
			}
			url.protocol == "file" -> Paths.get(urls.single().toURI())
			else -> error("cannot get path on a resource whose protocol is not 'file'. " +
					"The protocol is instead '${urls.single().protocol}'")
		}
	}

	val file: Path by lazy {
		check(!allowAmbiguity) { "check failed: !allowAmbiguity" }
		Paths.get(url.toURI())
	}

	private fun copyBesideContainer(url: URL): Path {

		val jarUrlConnection = url.openConnection() as JarURLConnection
		val jarFile = Paths.get(jarUrlConnection.jarFileURL.toURI())
		// Example jarFile: C:\my\local\repos\github\utilities\build\resources\test\topLevel.jar
		val jarDir = jarFile.parent
		// Example jarDir: C:\my\local\repos\github\utilities\build\resources\test
		val jarEntry = jarUrlConnection.jarEntry.toString()
		// Example jarEntry: nested.jar
		val targetPath = jarDir.resolve(jarEntry)
		// Example targetPath: C:\my\local\repos\github\utilities\build\resources\test\nested.jar
		Files.copy(url.openStream(), targetPath)
		return targetPath
	}

	fun withExtractedPath(block: Path.() -> Unit) {

		if (url.protocol == "file")
			Paths.get(url.toURI()).block()
		else {

			val extractedPath = copyBesideContainer(url)

			extractedPath.block()

			check(extractedPath.isRegularFile
			) { ("Failure: extracted path $extractedPath has been deleted while being processed in the 'withExtractedPath' block.") }

			Files.delete(extractedPath)
		}
	}

	@JvmOverloads
	fun extractTo(targetDir: Path, asDirectory: Boolean = false): Path {
		val targetFile = if (url.protocol == "file") {
			targetDir.resolve(name)
		} else {
			val jarUrlConnection = url.openConnection() as JarURLConnection
			targetDir.resolve(jarUrlConnection.jarEntry.toString())
		}

		if (asDirectory) {
			targetFile.mkdirs()
			if (Files.exists(targetFile))
				Files.delete(targetFile)
			Files.createDirectory(targetFile)
			FileSystemsOperations().copyDirContentsRecursivelyToDirInSameFileSystem(Paths.get(url.toURI()), targetFile)
		}
		else {
			targetFile.mkdirs()
			Files.copy(url.openStream(), targetFile, StandardCopyOption.REPLACE_EXISTING)
		}

		return targetFile
	}
}