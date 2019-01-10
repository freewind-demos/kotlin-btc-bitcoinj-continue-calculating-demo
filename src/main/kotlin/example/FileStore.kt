package example

import java.io.File

class FileStore(dataDir: File) {

    // Content in addingFile always considered as 'non-complete'
    private val addingFile = File(dataDir, "data.adding.txt")

    // Content in dataFile or deletingFile always considered as "complete'
    private val dataFile = File(dataDir, "data.txt")
    private val deletingFile = File(dataDir, "data.deleting.txt")

    fun read(): String? {
        if (dataFile.exists()) {
            return dataFile.readText()
        } else if (deletingFile.exists()) {
            return deletingFile.readText()
        }
        return null
    }

    fun write(content: String) {
        if (addingFile.exists()) {
            addingFile.delete()
        }
        addingFile.createNewFile()
        addingFile.writeText(content)

        if (dataFile.exists()) {
            if (deletingFile.exists()) {
                deletingFile.delete()
            }
            dataFile.renameTo(deletingFile)
        }

        addingFile.renameTo(dataFile)

        if (deletingFile.exists()) {
            deletingFile.delete()
        }
    }

    fun clear() {
        // Note: the orders is important
        val files = listOf(addingFile, deletingFile, dataFile)
        files.filter { it.exists() }.forEach { it.delete() }
    }

}
