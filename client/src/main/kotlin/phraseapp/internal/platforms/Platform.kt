package phraseapp.internal.platforms

import phraseapp.internal.xml.ArbPrinterScanner
import phraseapp.internal.xml.Visitor
import phraseapp.internal.xml.XmlPrinterScanner
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType
import phraseapp.repositories.operations.ResFolderType
import java.io.File

sealed class Platform {
    abstract val printer: Visitor
    abstract val defaultStringsFile: String
    abstract val format: String
    abstract fun getFilename(type: ResFolderType): String
    abstract fun getResPath(type: ResFolderType): String
    abstract fun getStringsFilesExceptDefault(resFolder: String): List<File>
}

object Android : Platform() {
    override val printer: Visitor
        get() = XmlPrinterScanner()

    override val defaultStringsFile: String
        get() = "strings.xml"

    override val format: String
        get() = "xml"

    override fun getFilename(type: ResFolderType): String {
        return defaultStringsFile
    }

    override fun getResPath(type: ResFolderType): String = when {
        type.isDefault -> "values"
        type is LanguageType -> "values-${type.language.lowercase()}"
        type is LocaleType -> "values-${type.language.lowercase()}-r${type.country.uppercase()}"
        else -> throw NotImplementedError()
    }

    override fun getStringsFilesExceptDefault(resFolder: String): List<File> {
        val file = File(resFolder)
        if (file.exists().not()) return emptyList()
        val listFiles = file.listFiles { _, name ->
            if (name == null) return@listFiles false
            return@listFiles "^values-[a-z]{2}(-r[A-Z]{2})?\$".toRegex().matches(name)
        } ?: return emptyList()
        return listFiles.map { File("${it.absolutePath}${File.separator}${defaultStringsFile}") }
    }
}

object iOS : Platform() {
    override val printer: Visitor
        get() = TODO("not implemented")

    override val defaultStringsFile: String
        get() = "Localizable.strings"

    override val format: String
        get() = "strings"

    override fun getFilename(type: ResFolderType): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getResPath(type: ResFolderType): String = when  {
        type.isDefault -> "Base.lproj"
        type is LanguageType -> "${type.language.lowercase()}.lproj"
        type is LocaleType -> "${type.language.lowercase()}-${type.country.uppercase()}.lproj"
        else -> throw NotImplementedError()
    }

    override fun getStringsFilesExceptDefault(resFolder: String): List<File> {
        val file = File(resFolder)
        if (file.exists().not()) return emptyList()
        val listFiles = file.listFiles { _, name ->
            if (name == null) return@listFiles false
            return@listFiles "^.*\\.lproj\$".toRegex().matches(name) && ("Base.lproj" == name).not()
        } ?: return emptyList()
        return listFiles.map { File("${it.absolutePath}${File.separator}${defaultStringsFile}") }
    }
}

object Flutter : Platform() {
    override val printer: Visitor
        get() = ArbPrinterScanner()

    override val defaultStringsFile: String
        get() = "strings_en.arb"

    override val format: String
        get() = "xml"

    override fun getFilename(type: ResFolderType): String = when  {
        type.isDefault -> defaultStringsFile
        type is LanguageType -> {
            "strings_${type.language.lowercase()}.arb"
        }
        type is LocaleType -> {
            "strings_${type.language.lowercase()}_${type.country.uppercase()}.arb"
        }
        else -> throw NotImplementedError()
    }

    override fun getResPath(type: ResFolderType): String = "values"

    override fun getStringsFilesExceptDefault(resFolder: String): List<File> {
        val file = File("$resFolder${File.separator}values")
        if (file.exists().not()) return emptyList()
        val listFiles = file.listFiles { _, name ->
            if (name == null) return@listFiles false
            if (name == defaultStringsFile) return@listFiles false
            return@listFiles true
        } ?: return emptyList()
        return listFiles.map { File(it.absolutePath) }
    }
}