package phraseapp.internal.xml

import java.text.SimpleDateFormat
import java.util.*

interface Visitor {
    fun visit(resource: Resource)
    fun visit(string: StringTranslation)
    fun visit(stringsArray: StringsArrayTranslation)
    fun visit(plural: PluralsTranslation)
    fun visit(comment: CommentTranslation)
}

class XmlPrinterScanner : Visitor {
    private val contexts = Stack<Context>()

    fun start(resource: Resource): String {
        enter(ResultContext(""))
        scan(resource)
        return (exit() as ResultContext).content
    }

    private fun scan(elements: List<Translation>) = elements.forEach { scan(it) }

    private fun scan(element: Translation) = element.accept(this)

    private fun enter(context: Context) {
        contexts.push(context)
    }

    private fun exit() = contexts.pop()

    override fun visit(resource: Resource) {
        enter(ResourceContext())
        scan(resource.strings.filter { it.translatable })
        val context = exit() as ResourceContext

        when (val parentContext = contexts.peek()) {
            is ResultContext -> parentContext.content = """<?xml version="1.0" encoding="UTF-8"?>
<resources>
${context.items.joinToString("\n")}
</resources>"""
            else -> TODO()
        }
    }

    override fun visit(string: StringTranslation) {
        val parentContext = contexts.peek()
        enter(StringContext(parentContext.level + 1))
        scan(string.comment)
        val context = exit() as StringContext

        val value = string.value
            .replace("|1<", "|1&lt;")
        when (parentContext) {
            is ResourceContext -> parentContext.items.add("""${context.comment}${context.tabs()}<string name="${string.key}">$value</string>""")
            is PluralsContext -> parentContext.items.add("""${context.comment}${context.tabs()}<item quantity="${string.key}">$value</item>""")
            is StringsArrayContext -> parentContext.items.add("""${context.comment}${context.tabs()}<item>$value</item>""")
            else -> TODO()
        }
    }

    override fun visit(stringsArray: StringsArrayTranslation) {
        enter(StringsArrayContext())
        scan(stringsArray.comment)
        scan(stringsArray.values)
        val context = exit() as StringsArrayContext

        when (val parentContext = contexts.peek()) {
            is ResourceContext -> parentContext.items.add(
                """${context.comment}${context.tabs()}<string-array name="${stringsArray.key}">
${context.items.joinToString("\n")}
${context.tabs()}</string-array>"""
            )
            else -> TODO()
        }
    }

    override fun visit(plural: PluralsTranslation) {
        enter(PluralsContext())
        scan(plural.comment)
        scan(plural.plurals)
        val context = exit() as PluralsContext

        when (val parentContext = contexts.peek()) {
            is ResourceContext -> parentContext.items.add(
                """${context.comment}${context.tabs()}<plurals name="${plural.key}">
${context.items.joinToString("\n")}
${context.tabs()}</plurals>"""
            )
            else -> TODO()
        }
    }

    override fun visit(comment: CommentTranslation) {
        if (comment.text.isEmpty()) return
        val parentContext = contexts.peek()
        val commentNode = """${parentContext.tabs()}<!--
${"\t"}${parentContext.tabs()}${comment.text.split("\n").joinToString("\n\t${parentContext.tabs()}")}
${parentContext.tabs()}-->
"""
        when (parentContext) {
            is StringContext -> parentContext.comment = commentNode
            is StringsArrayContext -> parentContext.comment = commentNode
            is PluralsContext -> parentContext.comment = commentNode
            else -> TODO()
        }
    }
}

class ArbPrinterScanner : Visitor {
    private val contexts = Stack<Context>()
    private var dateStr: String? = null

    fun start(resource: Resource): String {
        enter(ResultContext(""))
        scan(resource)
        return (exit() as ResultContext).content
    }

    private fun scan(elements: List<Translation>) = elements.forEach { scan(it) }

    private fun scan(element: Translation) = element.accept(this)

    private fun enter(context: Context) {
        contexts.push(context)
    }

    private fun exit() = contexts.pop()

    override fun visit(resource: Resource) {
        enter(ResourceContext())
        scan(resource.strings)
        val context = exit() as ResourceContext

        when (val parentContext = contexts.peek()) {
            is ResultContext -> {
                val dateStr = getDate()
                val displayDate = "\t\"@@last_modified\":\"$dateStr\""
                var items = arrayListOf<String>()
                items.add(displayDate)
                items.addAll(context.items)
                parentContext.content = """{
${items.joinToString(",\n")}
}"""
            }
            else -> TODO()
        }
    }

    override fun visit(string: StringTranslation) {
        val parentContext = contexts.peek()
        enter(StringContext(parentContext.level + 1))
        scan(string.comment)
        val context = exit() as StringContext

        val value = string.value
            .replace("|1<", "|1&lt;")
            .replace("\\'", "'")
            .replace("\"", "\\\"")
        when (parentContext) {
            is ResourceContext -> parentContext.items.add("""${context.comment}${context.tabs()}"${string.key}":"$value"""")
            else -> TODO()
        }
    }

    override fun visit(stringsArray: StringsArrayTranslation) {
        enter(StringsArrayContext())
        scan(stringsArray.comment)
        scan(stringsArray.values)
    }

    override fun visit(plural: PluralsTranslation) {
        enter(PluralsContext())
        scan(plural.comment)
        scan(plural.plurals)
    }

    override fun visit(comment: CommentTranslation) {
        return
    }

    fun getDate(): String {
        if (dateStr == null) {
            val date = Date()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssSSS")
            dateStr = simpleDateFormat.format(date)
        }
        return dateStr!!
    }
}

interface Context {
    val level: Int
    fun tabs(): String = (0 until level).joinToString("") { "\t" }
}

class ResultContext(var content: String) : Context {
    override val level: Int = 0
}

class ResourceContext(val items: MutableList<String> = arrayListOf()) : Context {
    override val level: Int = 0
}

class StringContext(override val level: Int = 1, var comment: String = "") : Context
class StringsArrayContext(
    override val level: Int = 1,
    val items: MutableList<String> = arrayListOf(),
    var comment: String = ""
) : Context

class PluralsContext(
    override val level: Int = 1,
    val items: MutableList<String> = arrayListOf(),
    var comment: String = ""
) : Context