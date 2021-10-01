package phraseapp.parsers.xml

import org.w3c.dom.Element
import org.w3c.dom.Node

val Node.comment: String?
    get() = nodeComment?.textContent?.replace("\\n +".toRegex(), "\n")?.trim()

val Node.nodeComment: Node?
    get() {
        if (previousSibling == null) return null
        return when {
            previousSibling.isText() -> previousSibling.nodeComment
            previousSibling.isComment() -> previousSibling
            else -> null
        }
    }

val Node.childs: List<Node>
    get() = NodeList(childNodes).toList().filter { it.isElement() || it.isArray() }

fun Node.isComment(): Boolean = nodeType == Element.COMMENT_NODE
fun Node.isText(): Boolean = nodeType == Element.TEXT_NODE
fun Node.isArray(): Boolean =
    nodeType == Element.ELEMENT_NODE && childNodes != null && childNodes.length > 0

fun Node.isElement(): Boolean =
    nodeType == Element.ELEMENT_NODE && (childNodes == null || (childNodes.length == 1 && childNodes.item(0).isText()))