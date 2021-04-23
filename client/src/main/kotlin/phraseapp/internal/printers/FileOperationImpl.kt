package phraseapp.internal.printers

import phraseapp.extensions.writeTo
import java.io.File

class FileOperationImpl : FileOperation {
    override fun print(path: String, content: String) {
        content.writeTo(path)
    }

    override fun delete(file: File) {
        if (file.parentFile.listFiles()?.size == 1 && file.exists()) {
            file.parentFile.deleteRecursively()
        } else {
            file.delete()
        }
    }
}