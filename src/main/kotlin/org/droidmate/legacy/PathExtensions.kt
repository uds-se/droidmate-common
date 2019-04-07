// Author: Konrad Jamrozik, github.com/konrad-jamrozik

package org.droidmate.legacy

import org.apache.commons.io.FilenameUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.streams.toList

/**
 * Returns [Path] pointing to a dir denoted by the [dir], resolved against the receiver.
 *
 * Throws an [IllegalStateException] if any of the assumptions are violated.
 */
fun Path.resolveDir(dir: String): Path {

    check(this.isDirectory) { "Failed check: receiver.isDirectory, where receiver is: $this" }

    checkNotNull(dir)
    check(dir.isNotEmpty()) { "Failed check: dir.length > 0, where dir is: '$dir'" }

    val resolvedDir = this.resolve(dir)

    check(resolvedDir.isDirectory) { "Failed check: resolvedDir.isDirectory, where resolvedDir is: $resolvedDir" }

    return resolvedDir
}

/**
 * Calls [Files.isRegularFile] with the receiver.
 */
val Path.isRegularFile: Boolean
    get() = Files.isRegularFile(this)

/**
 * Calls [Files.isDirectory] with the receiver.
 */
val Path.isDirectory: Boolean
    get() = Files.isDirectory(this)

/**
 * Calls [Files.createDirectories] with the receiver's parent.
 */

fun Path.mkdirs(): Path? {
    check(!this.isDirectory)
    return Files.createDirectories(this.parent)
}

fun Path.writeText(text: String) {
    check(this.isRegularFile)
    Files.write(this, text.toByteArray())
}

val Path.files: List<Path>
    get() {
        check(this.isDirectory)
        return Files.list(this)
            .filter(Path::isRegularFile)
            .toList()
    }

fun Path.replaceText(sourceText: String, replacementText: String) {
    check(this.isRegularFile)
    this.writeText(this.text.replace(sourceText, replacementText))
}

val Path.text: String
    get() {
        check(this.isRegularFile)
        return Files.readAllLines(this).joinToString(System.lineSeparator())
    }

fun Path.getExtension(): String {
    return FilenameUtils.getExtension(this.fileName.toString())
}

@Throws(IOException::class)
fun Path.deleteDirectoryRecursively() {
    if (Files.isDirectory(this, LinkOption.NOFOLLOW_LINKS)) {
        Files.newDirectoryStream(this).use { entries ->
            for (entry in entries) {
                entry.deleteDirectoryRecursively()
            }
        }
    }
    Files.delete(this)
}