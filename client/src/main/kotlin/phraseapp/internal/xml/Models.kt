package phraseapp.internal.xml

interface Translation {
    fun accept(visitor: Visitor)
}

interface StringsTranslationNode : Translation {
    val key: String
    val comment: CommentTranslation
    val translatable: Boolean
    val resFolder: String
}

data class Resource(val strings: List<StringsTranslationNode>) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class StringTranslation(
    override val key: String,
    val value: String,
    override val resFolder: String,
    override val comment: CommentTranslation = CommentTranslation(""),
    override val translatable: Boolean = true
) : StringsTranslationNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class StringsArrayTranslation(
    override val key: String,
    val values: List<StringTranslation>,
    override val resFolder: String,
    override val comment: CommentTranslation = CommentTranslation(""),
    override val translatable: Boolean = true
) : StringsTranslationNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class PluralsTranslation(
    override val key: String,
    val plurals: List<StringTranslation>,
    override val resFolder: String,
    override val comment: CommentTranslation = CommentTranslation(""),
    override val translatable: Boolean = true
) : StringsTranslationNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class CommentTranslation(val text: String) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}