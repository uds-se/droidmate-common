// Author: Konrad Jamrozik, github.com/konrad-jamrozik

package org.droidmate.legacy

class OS {
    companion object {
        val isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows")
    }
}