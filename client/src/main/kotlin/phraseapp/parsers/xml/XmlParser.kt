package phraseapp.parsers.xml

import org.w3c.dom.Document
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

const val DEFAULT_IGNORE_COMMENTS = false

class XmlParser(stream: InputStream, ignoreComments: Boolean = DEFAULT_IGNORE_COMMENTS) {
    constructor(xml: String, ignoreComments: Boolean = DEFAULT_IGNORE_COMMENTS) : this(xml.byteInputStream(), ignoreComments)

    val document: Document

    init {
        val documentFactory = DocumentBuilderFactory.newInstance().apply {
            isIgnoringComments = ignoreComments
        }
        document = documentFactory.newDocumentBuilder().parse(stream)
        document.documentElement.normalize()
    }
}