package pizzk.android.lanqin.utils


import java.io.*
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object IoUtils {

    fun string(ins: InputStream?): String {
        val br = BufferedReader(InputStreamReader(ins!!, Charset.defaultCharset()))
        val sbf = StringBuffer()
        try {
            var line: String
            while (true) {
                line = br.readLine() ?: break
                sbf.append(line)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sbf.toString()
    }

    fun closeQuietly(closeable: Closeable?) {
        closeable ?: return
        try {
            closeable.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveFile(file: File, ins: InputStream): File {
        val fos = FileOutputStream(file)
        if (!file.parentFile.exists()) {
            file.mkdirs()
        }
        try {
            val buffer = ByteArray(size = 4 * 1024)
            var length: Int
            while (true) {
                length = ins.read(buffer)
                if (length <= 0) break
                fos.write(buffer, 0, length)
            }
            fos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeQuietly(ins)
            closeQuietly(fos)
        }
        return file
    }

    /**文件压缩*/
    fun zip(paths: Array<String>, outs: File): Boolean {
        var zop: ZipOutputStream? = null
        return try {
            if (outs.exists()) outs.delete()
            if (!outs.parentFile.exists()) outs.parentFile.mkdirs()
            zop = ZipOutputStream(FileOutputStream(outs))
            paths.map { File(it) }.forEach { file ->
                val files: Array<File> = if (file.isFile) arrayOf(file) else file.listFiles()
                val dest: String = if (file.isFile) "" else file.name
                zip(files, dest, zop)
            }
            true
        } catch (e: Exception) {
            if (outs.exists()) outs.delete()
            e.printStackTrace()
            false
        } finally {
            closeQuietly(zop)
        }
    }

    private fun zip(files: Array<File>, dest: String, outs: ZipOutputStream) {
        val prefix: String = if (dest.isEmpty()) "" else "$dest${File.separator}"
        files.forEach { file ->
            val path = "$prefix${file.name}"
            if (file.isFile) {
                val entry = ZipEntry(path)
                val ins: BufferedInputStream = file.inputStream().buffered()
                outs.putNextEntry(entry)
                write(ins, outs)
                outs.closeEntry()
            } else {
                zip(file.listFiles(), path, outs)
            }
        }
    }

    fun write(ins: InputStream, outs: OutputStream, size: Int = DEFAULT_BUFFER_SIZE) {
        try {
            val buffer = ByteArray(size)
            val br: BufferedInputStream = ins.buffered()
            val bw: BufferedOutputStream = outs.buffered()
            var len = 0
            val read: () -> Boolean = {
                len = br.read(buffer)
                len >= 0
            }
            while (read()) {
                bw.write(buffer, 0, len)
            }
            bw.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeQuietly(ins)
        }
    }
}