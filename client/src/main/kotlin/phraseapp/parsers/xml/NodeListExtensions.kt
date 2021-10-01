package phraseapp.parsers.xml

import org.w3c.dom.Node
import org.w3c.dom.NodeList

operator fun NodeList.get(index: Int): Node? = item(index)

internal class NodeList(private val list: NodeList?) : Iterable<Node> {
    private val currentSize: Int = list?.length ?: 0

    override fun iterator(): Iterator<Node> {
        return object : Iterator<Node> {
            private var currentIndex = 0

            override fun hasNext(): Boolean =
                currentIndex < currentSize && list!![currentIndex] != null

            override fun next(): Node = list!![currentIndex++]!!
        }
    }
}