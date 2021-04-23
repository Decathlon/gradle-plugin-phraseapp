package phraseapp.parsers.xml

import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.Test

class XmlParserTest {
    @Test
    fun shouldGetTreeNode() {
        val xml = """
<resources>
    <string name="hello">Hello, World!</string>
</resources>
""".trimIndent()
        val document = XmlParser(xml).document
        val nodes = document["resources"]
        assertk.assert(nodes.size).isEqualTo(expected = 1)

        val resourcesNode = nodes[0]
        assertk.assert(resourcesNode.isArray()).isTrue()
        assertk.assert(resourcesNode.childs.size).isEqualTo(expected = 1)

        val string = resourcesNode.childs[0]
        assertk.assert(string.isElement()).isTrue()

        val attributes = string.attributes
        assertk.assert(attributes["name"]).isEqualTo("hello")

        assertk.assert(string.textContent).isEqualTo("Hello, World!")
    }

    @Test
    fun shouldGetAllAttributes() {
        val xml = """
<resources>
    <string name="hello" key="value" toto="tata">Hello, World!</string>
</resources>
""".trimIndent()
        val document = XmlParser(xml).document
        val attributes = document["resources"][0].childs[0].attributes
        assertk.assert(attributes.getAll()).containsAll(
                "name" to "hello", "key" to "value", "toto" to "tata"
        )
    }

    @Test
    fun shouldGetCommentsForAllNodes() {
        val xml = """
<resources>
    <!-- Hello, World! -->
    <string name="hello">Hello, World!</string>
</resources>
""".trimIndent()
        val document = XmlParser(xml).document
        val string = document["resources"][0].childs[0]
        assertk.assert(string.comment).isEqualTo("Hello, World!")
    }
}