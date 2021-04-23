package phraseapp.parsers.xml

import org.w3c.dom.Document
import org.w3c.dom.Node

operator fun Document.get(vararg keys: String): List<Node> =
        keys.map { NodeList(getElementsByTagName(it)) }.flatten()