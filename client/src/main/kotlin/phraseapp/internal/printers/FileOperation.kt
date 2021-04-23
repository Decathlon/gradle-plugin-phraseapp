package phraseapp.internal.printers

import java.io.File

interface FileOperation {
    fun print(path: String, content: String)
    fun delete(file: File)
}