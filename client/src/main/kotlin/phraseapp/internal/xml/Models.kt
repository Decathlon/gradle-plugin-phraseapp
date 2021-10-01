package phraseapp.internal.xml

interface Translation {
    fun accept(visitor: Visitor)
}

data class Resource(val strings: List<Translation>) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class StringTranslation(
    val key: String,
    val value: String,
    val comment: CommentTranslation = CommentTranslation("")
) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class StringsArrayTranslation(
    val key: String,
    val values: List<StringTranslation>,
    val comment: CommentTranslation = CommentTranslation("")
) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class PluralsTranslation(
    val key: String,
    val plurals: List<StringTranslation>,
    val comment: CommentTranslation = CommentTranslation("")
) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class CommentTranslation(val text: String) : Translation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}