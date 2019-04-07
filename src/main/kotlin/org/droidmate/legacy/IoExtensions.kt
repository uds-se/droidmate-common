// Author: Konrad Jamrozik, github.com/konrad-jamrozik

package org.droidmate.legacy

import java.io.InputStream
import java.net.URL
import java.io.IOException
import java.io.InputStreamReader
import java.io.BufferedReader

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