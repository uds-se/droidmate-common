// Author: Konrad Jamrozik, github.com/konrad-jamrozik

package org.droidmate.legacy

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

val InputStream.text: String
    get() {
        var br: BufferedReader? = null
        val sb = StringBuilder()

        var line: String?
        try {

            br = BufferedReader(InputStreamReader(this))
            line = br.readLine()
            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        return sb.toString()
    }

val URL.text: String
    get() = this.readText()