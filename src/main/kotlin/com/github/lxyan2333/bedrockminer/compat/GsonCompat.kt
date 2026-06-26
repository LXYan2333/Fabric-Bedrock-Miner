package com.github.lxyan2333.bedrockminer.compat

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.File
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path

object GsonCompat {
    fun parseReader(reader: Reader): JsonElement = JsonParser().parse(reader)

    fun parseFile(file: File): JsonElement? {
        if (!file.exists()) return null
        return file.reader().use { parseReader(it) }
    }

    fun parseFile(path: Path): JsonElement? {
        if (!Files.exists(path)) return null
        return Files.newBufferedReader(path).use { parseReader(it) }
    }

    fun asString(element: JsonElement): String = element.getAsString()
}
