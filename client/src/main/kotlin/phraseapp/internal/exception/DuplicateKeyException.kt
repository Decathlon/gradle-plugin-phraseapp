package phraseapp.internal.exception

import phraseapp.internal.xml.PluralsTranslation
import phraseapp.internal.xml.StringTranslation
import phraseapp.internal.xml.StringsArrayTranslation
import phraseapp.internal.xml.StringsTranslationNode
import java.io.File


/**
 * Duplicate key exception
 * Throw an exception with duplicated items
 */
class DuplicateKeyException(translations: List<List<StringsTranslationNode>>) :
    Throwable(translations.filter { it.isNotEmpty() }.joinToString("\n\n") { formattedError(it) })

private fun formattedError(translations: List<StringsTranslationNode>) =
    when (val translation = translations.first()) {
        is StringTranslation -> error(type = "String", key = translation.key, translations = translations)
        is StringsArrayTranslation -> error(type = "Array", key = translation.key, translations = translations)
        is PluralsTranslation -> error(type = "Plural", key = translation.key, translations = translations)
        else -> throw NotImplementedError()
    }

private fun error(type: String, key: String, translations: List<StringsTranslationNode>) =
    "Error: Found item ${type}${File.separator}${key} more than one time \n" +
            translations.joinToString("\n") { it.resFolder }