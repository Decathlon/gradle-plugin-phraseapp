package phraseapp.extensions

import com.google.gson.JsonParser
import phraseapp.internal.xml.*
import phraseapp.parsers.xml.*
import java.io.File

fun String.writeTo(outputTargetFile: String) {
    val outputFile = File(outputTargetFile)
    if (outputFile.exists().not()) {
        outputFile.parentFile.mkdirs()
    }
    outputFile.writeText(text = this)
}

fun String.parse(file: File, ignoreComments: Boolean = false): ResourceTranslation {
    return if (file.absolutePath.contains(".arb")) {
        parseArb(resFolder = file.absolutePath)
    } else {
        parseXML(ignoreComments = ignoreComments, resFolder = file.absolutePath)
    }
}

fun String.parse(format: String, ignoreComments: Boolean = false): ResourceTranslation {
    return if (format.equals("arb")) {
        parseArb(resFolder = format)
    } else {
        parseXML(ignoreComments = ignoreComments, resFolder = format)
    }
}

private fun String.parseXML(ignoreComments: Boolean, resFolder: String): ResourceTranslation {
    val content = this
        .replace("&amp;", "[[MARKER]]&amp;[[MARKER]]")
        .replace("&lt;", "[[MARKER]]&lt;[[MARKER]]")
        .replace("&gt;", "[[MARKER]]&gt;[[MARKER]]")
        .replace("&quot;", "[[MARKER]]&quot;[[MARKER]]")
        .replace("&apos;", "[[MARKER]]&apos;[[MARKER]]")
        .replace(Regex("\\[\\[MARKER]]&lt;\\[\\[MARKER]](?=\\{[0-9]})"), "&lt;")
        .replace("<!\\[CDATA\\[(.*)]]>".toRegex()) { "&lt;![CDATA[${it.groups[it.groups.size - 1]!!.value}]]&gt;" }
        .replace("<(?!(/)?resources|(/)?string|(/)?plurals|(/)?string-array|(/)?item|\\?xml|!--)([^>]*)>".toRegex()) { "&lt;${it.groups[it.groups.size - 1]!!.value}&gt;" }
    val resources = try {
        XmlParser(xml = content, ignoreComments = ignoreComments).document["resources"][0]
    } catch (e: Throwable) {
        throw e
    }
    val strings = resources.childs
        .filter { it.nodeName == "string" }
        .map {
            StringTranslation(
                key = it.attributes["name"],
                value = it.text,
                resFolder = resFolder,
                comment = CommentTranslation(it.comment ?: ""),
                translatable = it.attributes.getOrElse("translatable", "true").toBoolean()
            )
        }
    val plurals = resources.childs.filter { it.nodeName == "plurals" }
        .map {
            return@map PluralsTranslation(
                key = it.attributes["name"],
                plurals = it.childs.map { child ->
                    StringTranslation(
                        key = child.attributes["quantity"],
                        value = child.text,
                        resFolder = resFolder,
                        comment = CommentTranslation(child.comment ?: "")
                    )
                },
                resFolder = resFolder,
                comment = CommentTranslation(it.comment ?: ""),
                translatable = it.attributes.getOrElse("translatable", "true").toBoolean()
            )
        }
    val arrays = resources.childs.filter { it.nodeName == "string-array" }
        .map {
            return@map StringsArrayTranslation(
                key = it.attributes["name"],
                values = it.childs.map { child ->
                    StringTranslation(
                        key = "",
                        value = child.text,
                        resFolder = resFolder,
                        comment = CommentTranslation(child.comment ?: "")
                    )
                },
                resFolder = resFolder,
                comment = CommentTranslation(it.comment ?: ""),
                translatable = it.attributes.getOrElse("translatable", "true").toBoolean()
            )
        }
    return ResourceTranslation(strings, plurals, arrays)
}

private fun String.parseArb(resFolder: String): ResourceTranslation {
    val strings = arrayListOf<StringTranslation>()
    for ((key, value) in JsonParser.parseString(this).asJsonObject.entrySet()) {
        if (key != "@@last_modified" && !key.startsWith("@")) {
            strings.add(StringTranslation(key = key, value = value.asString, resFolder = resFolder))
        }
    }
    return ResourceTranslation(strings, emptyList(), emptyList())
}

data class ResourceTranslation(
    val strings: List<StringTranslation>,
    val plurals: List<PluralsTranslation>,
    val arrays: List<StringsArrayTranslation>
) {
    val keys: List<String>
        get() = strings.map { it.key } + plurals.map { it.key } + arrays.map { it.key }

    fun toResource(): Resource = Resource(strings + plurals + arrays)
}
