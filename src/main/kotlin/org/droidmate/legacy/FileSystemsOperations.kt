// Author: Konrad Jamrozik, github.com/konrad-jamrozik

package org.droidmate.legacy

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class FileSystemsOperations {
	companion object {
		@JvmStatic
		private fun copyPath(it: Path, src: Path, dest: Path): Path {
			assert(Files.isDirectory(src))
			assert(Files.isDirectory(dest))

			val itInDest = mapToDestination(it, src, dest)

			if (itInDest != dest) {
				assert(!Files.exists(itInDest))

				when {
					Files.isDirectory(it) -> Files.createDirectory(itInDest)
					Files.isRegularFile(it) -> Files.copy(it, itInDest)
					else -> assert(false)
				}
			}

			return itInDest
		}

		@JvmStatic
		private fun mapToDestination(path: Path, srcDir: Path, destDir: Path): Path {
			return destDir.resolve(srcDir.relativize(path).toString().replace(srcDir.fileSystem.separator, destDir.fileSystem.separator))
		}
	}

	fun copyDirRecursivelyToDirInDifferentFileSystem(dir: Path, dest: Path) {
		assert(Files.isDirectory(dir))
		assert(Files.isDirectory(dest))
		assert(dir.fileSystem != dest.fileSystem)
		assert(dir.parent != null)

		Files.walk(dir)
			.forEach { copyPath(it, dir.parent, dest) }
	}

	fun copyDirContentsRecursivelyToDirInDifferentFileSystem(dir: Path, dest: Path) {
		assert(Files.isDirectory(dir))
		assert(Files.isDirectory(dest))
		assert(dir.fileSystem != dest.fileSystem)

		Files.walk(dir)
				.forEach { copyPath(it, dir, dest) }
	}

	fun copyFilesToDirInDifferentFileSystem(files: List<Path>, dest: Path) {
		assert(Files.isDirectory(dest))
		files.forEach {
			assert(Files.isRegularFile(it))
			assert(it.parent != null)
			assert(Files.isDirectory(it.parent))
			assert(it.fileSystem != dest.fileSystem)
		}

		files.forEach {
			copyPath(it, it.parent, dest)
		}
	}

	fun copyDirContentsRecursivelyToDirInSameFileSystem(dir: Path, dest: Path) {
		assert(Files.isDirectory(dir))
		assert(Files.isDirectory(dest))
		assert(dir.fileSystem == dest.fileSystem)

		Files.walk(dir).toList()
				.forEach { copyPath(it, dir, dest) }
	}
}
