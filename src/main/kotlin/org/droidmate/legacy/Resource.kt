// DroidMate, an automated execution generator for Android apps.
// Copyright (C) 2012-2018. Saarland University
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// Current Maintainers:
// Nataniel Borges Jr. <nataniel dot borges at cispa dot saarland>
// Jenny Hotzkow <jenny dot hotzkow at cispa dot saarland>
//
// Former Maintainers:
// Konrad Jamrozik <jamrozik at st dot cs dot uni-saarland dot de>
//
// web: www.droidmate.org

package org.droidmate.legacy

import java.io.IOException
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class Resource @JvmOverloads constructor(val name: String, val allowAmbiguity: Boolean = false) {

    val urls: List<URL> = {

        val urls = ClassLoader.getSystemResources(name).toList()

        if (urls.isEmpty())
            throw IOException("No resource URLs found for path \"$name\"")

        if (!allowAmbiguity && urls.size > 1)
            throw IOException(
                "More than one resource URL found for path $name. " +
                        "The found URLs:\n${urls.joinToString(separator = "\n")}"
            )

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
            else -> error(
                "cannot get path on a resource whose protocol is not 'file'. " +
                        "The protocol is instead '${urls.single().protocol}'"
            )
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

    fun <T> withExtractedPath(block: (Path) -> T): T {

        return if (url.protocol == "file")
            block(Paths.get(url.toURI()))
        else {
            val extractedPath = copyBesideContainer(url)

            try {
                check(extractedPath.isRegularFile) {
                    ("Failure: extracted path $extractedPath has been deleted while being processed in the " +
                            "'withExtractedPath' block.")
                }
                block(extractedPath)
            } finally {
                Files.delete(extractedPath)
            }
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
        } else {
            targetFile.mkdirs()
            Files.copy(url.openStream(), targetFile, StandardCopyOption.REPLACE_EXISTING)
        }

        return targetFile
    }
}