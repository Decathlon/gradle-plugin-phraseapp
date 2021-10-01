package phraseapp.parsers.xml

import org.w3c.dom.NamedNodeMap

fun NamedNodeMap.getAll(): Map<String, String> =
    (0 until length).map { item(it) }.associateBy({ it.nodeName }, { it.nodeValue })

operator fun NamedNodeMap.get(key: String): String = getNamedItem(key).nodeValue