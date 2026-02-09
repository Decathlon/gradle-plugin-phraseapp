package phraseapp.parsers.xml

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
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
        assertThat(nodes.size).isEqualTo(1)

        val resourcesNode = nodes[0]
        assertThat(resourcesNode.isArray()).isTrue()
        assertThat(resourcesNode.childs.size).isEqualTo(1)

        val string = resourcesNode.childs[0]
        assertThat(string.isElement()).isTrue()

        val attributes = string.attributes
        assertThat(attributes["name"]).isEqualTo("hello")

        assertThat(string.textContent).isEqualTo("Hello, World!")
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
        assertThat(attributes.getAll()).containsAll(
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
        assertThat(string.comment).isEqualTo("Hello, World!")
    }

    @Test
    fun shouldNotGetCommentsIfIgnored() {
        val xml = """
<resources>
    <!-- Hello, World! -->
    <string name="hello">Hello, World!</string>
</resources>
""".trimIndent()
        val document = XmlParser(xml, ignoreComments = true).document
        val string = document["resources"][0].childs[0]
        assertThat(string.comment).isNull()
    }

    @Test
    fun shouldHaveOnlyValidCharactersInValue() {
        val xml = """
<resources>
    <string name="cnc">Click&amp;Collect</string>
</resources>
""".trimIndent()
        val document = XmlParser(xml).document
        val nodes = document["resources"]
        assertThat(nodes.size).isEqualTo(1)

        val resourcesNode = nodes[0]
        assertThat(resourcesNode.isArray()).isTrue()
        assertThat(resourcesNode.childs.size).isEqualTo(1)

        val string = resourcesNode.childs[0]
        assertThat(string.isElement()).isTrue()
        assertThat(string.text).isEqualTo("Click&Collect")
    }
}