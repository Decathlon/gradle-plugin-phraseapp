package phraseapp.repositories.operations.helpers

import com.google.gson.Gson
import phraseapp.extensions.associateWithList
import phraseapp.internal.platforms.Flutter
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.internal.xml.ArbPrinterScanner
import phraseapp.internal.xml.Resource
import phraseapp.internal.xml.Visitor
import phraseapp.internal.xml.XmlPrinterScanner
import phraseapp.repositories.operations.LocaleType
import phraseapp.repositories.operations.ResFolderType
import java.io.File

class PrinterHelper(
    val platform: Platform,
    val buildDir: String,
    val fileOperation: FileOperation = FileOperationImpl()
) {
    val tempStringFilePath: String = if (platform is Flutter) {
        "$buildDir${File.separator}string.xml"
    } else {
        "$buildDir${File.separator}${platform.defaultStringsFile}"
    }

    val languagePath: String = "$buildDir${File.separator}languages.json"

    /**
     * Print a strings file who will be used for the upload to Phraseapp.
     */
    fun printTempStringsFile(resource: Resource) {
        when (platform) {
            is Flutter -> printResource(tempStringFilePath, resource, true)
            else -> printResource(tempStringFilePath, resource)
        }
    }

    /**
     * Print all locales in language.json file.
     */
    fun printLocales(types: List<ResFolderType>) {
        fileOperation.print(languagePath, Gson().toJson(getLocales(types)))
    }

    /**
     * Build map with all countries in key and all languages for each country.
     */
    private fun getLocales(types: List<ResFolderType>): Map<String, List<String>> = types
        .filterIsInstance<LocaleType>()
        .map { it.country.uppercase() to it.language.lowercase() }
        .distinct()
        .associateWithList()

    /**
     * Print all resources in all paths.
     */
    fun printResources(configurations: Map<String, Map<ResFolderType, Resource>>) {
        configurations.forEach { configuration ->
            configuration.value.forEach { resource ->
                printResourceByType(configuration.key, resource.key, resource.value)
            }
        }
    }

    /**
     * Build the path from res folder path and its type and print the resource at this target path.
     */
    private fun printResourceByType(resFolder: String, type: ResFolderType, resource: Resource) {
        val path =
            "$resFolder${File.separator}${platform.getResPath(type)}${File.separator}${platform.getFilename(type)}"
        printResource(path, resource)
    }

    /**
     * Print resource on the target path.
     */
    private fun printResource(targetPath: String, resource: Resource, forceXMLPrinter: Boolean = false) {
        val content = if (forceXMLPrinter) {
            XmlPrinterScanner().start(resource)
        } else {
            when (platform.printer) {
                is ArbPrinterScanner -> (platform.printer as ArbPrinterScanner).start(resource)
                is XmlPrinterScanner -> (platform.printer as XmlPrinterScanner).start(resource)
                else -> TODO("not implemented")
            }
        }
        fileOperation.print(targetPath, content)
    }
}