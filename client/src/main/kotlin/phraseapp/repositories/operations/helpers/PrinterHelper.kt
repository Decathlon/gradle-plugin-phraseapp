package phraseapp.repositories.operations.helpers

import com.google.gson.Gson
import phraseapp.extensions.associateWithList
import phraseapp.internal.platforms.Flutter
import phraseapp.internal.platforms.Platform
import phraseapp.internal.printers.FileOperation
import phraseapp.internal.printers.FileOperationImpl
import phraseapp.internal.xml.ArbPrinterScanner
import phraseapp.internal.xml.Resource
import phraseapp.internal.xml.XmlPrinterScanner
import phraseapp.network.DEFAULT_OVERRIDE_DEFAULT_FILE
import phraseapp.repositories.operations.LanguageType
import phraseapp.repositories.operations.LocaleType
import phraseapp.repositories.operations.ResFolderType
import java.io.File

class PrinterHelper(
    val platform: Platform,
    val buildDir: String,
    val fileOperation: FileOperation = FileOperationImpl(),
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
    fun printResources(
        configurations: Map<String, Map<ResFolderType, Resource>>,
        overrideDefaultFile: Boolean = DEFAULT_OVERRIDE_DEFAULT_FILE,
    ) {
        configurations.forEach { configuration ->
            configuration.value.forEach { resource ->
                if ((!resource.key.isDefault || overrideDefaultFile)) {
                    printResourceByType(
                        resFolder = configuration.key,
                        type = resource.key,
                        resource = resource.value
                    )
                }
                copyDefaultFileIfNeeded(configuration, resource.key)
            }
        }
    }


    /**
     * Copy default file strings.xml to value-DEFAULT_LANGUAGE/strings.xml
     * if there is no variant, default file will be taken instead so we don't need to copy it.
     *
     * This method corrects this issue : https://github.com/Decathlon/gradle-plugin-phraseapp/issues/18
     * When a default language (ex: "en") has some variants (ex: en-GB) which have a translation which is different between them,
     * before this fix, there was no values-en file created, so, for an other variant (ex: en-IE) the translation will be pull from en-GB and not from "en".
     * See google "resource resolution order" doc for more details ->
     * https://developer.android.com/guide/topics/resources/multilingual-support?hl=fr#resource-resolution-examples
     *
     *
     */
    private fun copyDefaultFileIfNeeded(
        configuration: Map.Entry<String, Map<ResFolderType, Resource>>,
        type: ResFolderType
    ) {
        // Variant of default language ex: default = en, variant = en-IE or en-GB
        val hasVariant =
            configuration.value.filter { it.key.language.contains(type.language) && !it.key.isDefault }
                .isNotEmpty()

        if (type.isDefault && hasVariant) {
            duplicateFile(
                resFolder = configuration.key,
                type = type
            )
        }
    }

    private fun duplicateFile(
        resFolder: String,
        type: ResFolderType,
    ) {
        val defaultPath =
            "$resFolder${File.separator}${platform.getResPath(type)}${File.separator}${
                platform.getFilename(type)
            }"

        val duplicateFileType = LanguageType(type.language, false)
        val duplicatePath =
            "$resFolder${File.separator}${platform.getResPath(duplicateFileType)}${File.separator}${
                platform.getFilename(duplicateFileType)
            }"

        fileOperation.copy(defaultPath, duplicatePath)
    }

    /**
     * Build the path from res folder path and its type and print the resource at this target path.
     */
    private fun printResourceByType(resFolder: String, type: ResFolderType, resource: Resource) {
        val path =
            "$resFolder${File.separator}${platform.getResPath(type)}${File.separator}${
                platform.getFilename(
                    type
                )
            }"
        printResource(path, resource)
    }

    /**
     * Print resource on the target path.
     */
    private fun printResource(
        targetPath: String,
        resource: Resource,
        forceXMLPrinter: Boolean = false,
    ) {
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