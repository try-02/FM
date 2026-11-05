package id.manajerberkas.kode.data.repositori

import android.content.Context
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.domain.kontrak.RepositoriArsip
import id.manajerberkas.kode.domain.model.HasilDaftarArsip
import id.manajerberkas.kode.domain.model.HasilEkstraksi
import id.manajerberkas.kode.domain.model.ItemArsip
import id.manajerberkas.kode.domain.model.JenisItemArsip
import id.manajerberkas.kode.domain.model.KemampuanFormatArsip
import id.manajerberkas.kode.domain.model.LokasiBerkas
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

class RepositoriArsipAndroid(
    private val context: Context
) : RepositoriArsip {

    private val akarPrivat = context.filesDir

    override suspend fun daftarIsi(lokasiArsip: LokasiBerkas): HasilDaftarArsip = withContext(Dispatchers.IO) {
        val file = berkasDariLokasi(lokasiArsip) 
            ?: return@withContext HasilDaftarArsip(false, "File arsip tidak ditemukan.")
        if (!file.exists() || !file.isFile) return@withContext HasilDaftarArsip(false, "File arsip tidak valid.")

        val namaLower = file.name.lowercase()
        val adalahZip = namaLower.endsWith(".zip") || namaLower.endsWith(".jar")
        val adalahTar = namaLower.endsWith(".tar") || namaLower.endsWith(".tgz") || namaLower.endsWith(".tar.gz")
        val adalahGzipTunggal = namaLower.endsWith(".gz") && !adalahTar

        try {
            val items = mutableListOf<ItemArsip>()
            val folderTerlihat = mutableSetOf<String>()
            val format = when {
                adalahZip -> "zip"
                adalahTar -> "tar"
                adalahGzipTunggal -> "gzip"
                else -> "unknown"
            }

            if (adalahZip) {
                ZipFile(file).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        prosesEntryZip(entry.name, entry.isDirectory, entry.size, entry.compressedSize, entry.time, items, folderTerlihat)
                    }
                }
            } else if (adalahTar) {
                // Membaca isi arsip TAR / TAR.GZ
                bukaTarStream(file).use { stream ->
                    var entry = stream.nextTarEntry
                    while (entry != null) {
                        prosesEntryTar(entry.name, entry.isDirectory, entry.size, entry.lastModifiedDate?.time, items, folderTerlihat)
                        entry = stream.nextTarEntry
                    }
                }
            } else if (adalahGzipTunggal) {
                // GZIP tunggal hanya berisi 1 berkas (nama asli tanpa ekstensi .gz)
                val namaAsli = file.nameWithoutExtension
                items.add(ItemArsip(namaAsli, namaAsli, JenisItemArsip.BERKAS, file.length(), null, null))
            } else {
                return@withContext HasilDaftarArsip(false, "Format tidak didukung.")
            }

            val kemampuan = KemampuanFormatArsip(
                format = format,
                bisaBaca = true,
                bisaEkstrak = true,
                bisaBuat = false,
                bisaUbahIsi = false,
                dukungEnkripsi = false
            )

            HasilDaftarArsip(
                sukses = true,
                daftar = items.sortedWith(compareByDescending<ItemArsip> { it.jenis }.thenBy { it.jalur.lowercase() }),
                kemampuan = kemampuan
            )
        } catch (e: Exception) {
            HasilDaftarArsip(false, "Gagal membaca arsip: ${e.message}")
        }
    }

    override suspend fun ekstrakItem(lokasiArsip: LokasiBerkas, jalurItem: String, lokasiTujuan: LokasiBerkas): HasilEkstraksi = withContext(Dispatchers.IO) {
        val fileArsip = berkasDariLokasi(lokasiArsip) ?: return@withContext HasilEkstraksi(false, "File arsip tidak ditemukan.")
        val folderTujuan = berkasDariLokasi(lokasiTujuan) ?: return@withContext HasilEkstraksi(false, "Folder tujuan tidak ditemukan.")
        if (!folderTujuan.isDirectory) return@withContext HasilEkstraksi(false, "Tujuan bukan folder.")

        val namaLower = fileArsip.name.lowercase()
        val adalahZip = namaLower.endsWith(".zip") || namaLower.endsWith(".jar")
        val adalahTar = namaLower.endsWith(".tar") || namaLower.endsWith(".tgz") || namaLower.endsWith(".tar.gz")
        val adalahGzipTunggal = namaLower.endsWith(".gz") && !adalahTar

        try {
            if (adalahZip) {
                ZipFile(fileArsip).use { zip ->
                    val entry = zip.getEntry(jalurItem) ?: return@withContext HasilEkstraksi(false, "Item tidak ditemukan.")
                    val target = File(folderTujuan, entry.name)
                    if (!validasiPathTraversal(target, folderTujuan)) return@withContext HasilEkstraksi(false, "Path traversal terdeteksi.")
                    
                    if (entry.isDirectory) {
                        target.mkdirs()
                    } else {
                        target.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
                    }
                    return@withContext HasilEkstraksi(true, "Berhasil.", 1, 0)
                }
            } else if (adalahTar) {
                bukaTarStream(fileArsip).use { stream ->
                    var entry = stream.nextTarEntry
                    while (entry != null) {
                        val entryPath = entry.name.trimStart('/')
                        if (entryPath == jalurItem.trimStart('/') || entry.name == jalurItem) {
                            val target = File(folderTujuan, entryPath)
                            if (!validasiPathTraversal(target, folderTujuan)) return@withContext HasilEkstraksi(false, "Path traversal terdeteksi.")
                            if (entry.isDirectory) {
                                target.mkdirs()
                            } else {
                                target.parentFile?.mkdirs()
                                target.outputStream().use { output -> stream.copyTo(output) }
                            }
                            return@withContext HasilEkstraksi(true, "Berhasil.", 1, 0)
                        }
                        entry = stream.nextTarEntry
                    }
                    return@withContext HasilEkstraksi(false, "Item tidak ditemukan dalam arsip.")
                }
            } else if (adalahGzipTunggal) {
                val namaAsli = fileArsip.nameWithoutExtension
                if (jalurItem != namaAsli) return@withContext HasilEkstraksi(false, "Item tidak ditemukan.")
                val target = File(folderTujuan, namaAsli)
                if (!validasiPathTraversal(target, folderTujuan)) return@withContext HasilEkstraksi(false, "Path traversal terdeteksi.")
                target.parentFile?.mkdirs()
                GzipCompressorInputStream(fileArsip.inputStream()).use { input -> 
                    target.outputStream().use { output -> input.copyTo(output) } 
                }
                return@withContext HasilEkstraksi(true, "Berhasil.", 1, 0)
            }
            return@withContext HasilEkstraksi(false, "Format tidak didukung.")
        } catch (e: Exception) {
            HasilEkstraksi(false, "Gagal mengekstrak: ${e.message}")
        }
    }

    override suspend fun ekstrakSemua(lokasiArsip: LokasiBerkas, lokasiTujuan: LokasiBerkas): HasilEkstraksi = withContext(Dispatchers.IO) {
        val fileArsip = berkasDariLokasi(lokasiArsip) ?: return@withContext HasilEkstraksi(false, "File arsip tidak ditemukan.")
        val folderTujuan = berkasDariLokasi(lokasiTujuan) ?: return@withContext HasilEkstraksi(false, "Folder tujuan tidak ditemukan.")
        if (!folderTujuan.isDirectory) return@withContext HasilEkstraksi(false, "Tujuan bukan folder.")

        val namaLower = fileArsip.name.lowercase()
        val adalahZip = namaLower.endsWith(".zip") || namaLower.endsWith(".jar")
        val adalahTar = namaLower.endsWith(".tar") || namaLower.endsWith(".tgz") || namaLower.endsWith(".tar.gz")
        val adalahGzipTunggal = namaLower.endsWith(".gz") && !adalahTar

        var berhasil = 0
        var gagal = 0

        try {
            if (adalahZip) {
                ZipFile(fileArsip).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        val target = File(folderTujuan, entry.name)
                        if (!validasiPathTraversal(target, folderTujuan)) { gagal++; continue }
                        try {
                            if (entry.isDirectory) {
                                target.mkdirs()
                            } else {
                                target.parentFile?.mkdirs()
                                zip.getInputStream(entry).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
                            }
                            berhasil++
                        } catch (e: Exception) { gagal++ }
                    }
                }
            } else if (adalahTar) {
                bukaTarStream(fileArsip).use { stream ->
                    var entry = stream.nextTarEntry
                    while (entry != null) {
                        val entryPath = entry.name.trimStart('/')
                        val target = File(folderTujuan, entryPath)
                        if (!validasiPathTraversal(target, folderTujuan)) { 
                            gagal++
                            entry = stream.nextTarEntry
                            continue 
                        }
                        try {
                            if (entry.isDirectory) {
                                target.mkdirs()
                            } else {
                                target.parentFile?.mkdirs()
                                target.outputStream().use { output -> stream.copyTo(output) }
                            }
                            berhasil++
                        } catch (e: Exception) { gagal++ }
                        entry = stream.nextTarEntry
                    }
                }
            } else if (adalahGzipTunggal) {
                val namaAsli = fileArsip.nameWithoutExtension
                val target = File(folderTujuan, namaAsli)
                if (validasiPathTraversal(target, folderTujuan)) {
                    try {
                        target.parentFile?.mkdirs()
                        GzipCompressorInputStream(fileArsip.inputStream()).use { input -> 
                            target.outputStream().use { output -> input.copyTo(output) } 
                        }
                        berhasil++
                    } catch (e: Exception) { gagal++ }
                } else {
                    gagal++
                }
            } else {
                return@withContext HasilEkstraksi(false, "Format tidak didukung.")
            }

            HasilEkstraksi(gagal == 0, if (gagal == 0) "Ekstraksi selesai." else "Sebagian item gagal.", berhasil, gagal)
        } catch (e: Exception) {
            HasilEkstraksi(false, "Gagal mengekstrak arsip: ${e.message}")
        }
    }

    // Mengembalikan TarArchiveInputStream secara spesifik
    private fun bukaTarStream(file: File): TarArchiveInputStream {
        val fis = file.inputStream()
        val namaLower = file.name.lowercase()
        val perluGzip = namaLower.endsWith(".tgz") || namaLower.endsWith(".tar.gz")
        
        return if (perluGzip) {
            TarArchiveInputStream(GzipCompressorInputStream(fis))
        } else {
            TarArchiveInputStream(fis)
        }
    }

    private fun prosesEntryZip(jalur: String, isDir: Boolean, size: Long, compSize: Long, time: Long, items: MutableList<ItemArsip>, folderTerlihat: MutableSet<String>) {
        val bagian = jalur.split("/")
        for (i in 1 until bagian.size) {
            val folderPath = bagian.subList(0, i).joinToString("/") + "/"
            if (folderPath != "/" && folderTerlihat.add(folderPath)) {
                items.add(ItemArsip(folderPath, bagian[i - 1], JenisItemArsip.FOLDER, 0, null, null))
            }
        }
        if (!isDir) {
            items.add(ItemArsip(jalur, jalur.substringAfterLast('/'), JenisItemArsip.BERKAS, size, compSize, time))
        }
    }

    private fun prosesEntryTar(jalur: String, isDir: Boolean, size: Long, time: Long?, items: MutableList<ItemArsip>, folderTerlihat: MutableSet<String>) {
        val jalurBersih = jalur.trimStart('/')
        val bagian = jalurBersih.split("/")
        for (i in 1 until bagian.size) {
            val folderPath = bagian.subList(0, i).joinToString("/") + "/"
            if (folderPath != "/" && folderTerlihat.add(folderPath)) {
                items.add(ItemArsip(folderPath, bagian[i - 1], JenisItemArsip.FOLDER, 0, null, null))
            }
        }
        if (!isDir) {
            items.add(ItemArsip(jalurBersih, jalurBersih.substringAfterLast('/'), JenisItemArsip.BERKAS, size, null, time))
        }
    }

    private fun validasiPathTraversal(target: File, direktoriTujuan: File): Boolean {
        val canonicalTarget = target.canonicalPath
        val canonicalDir = direktoriTujuan.canonicalPath + File.separator
        return canonicalTarget.startsWith(canonicalDir) || canonicalTarget == direktoriTujuan.canonicalPath
    }

    private fun berkasDariLokasi(lokasi: LokasiBerkas): File? {
        if (!lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT)) return null
        val jalur = lokasi.id.removePrefix(PabrikLokasiAndroid.SKEMA_PRIVAT).trim('/')
        val file = if (jalur.isBlank()) akarPrivat else File(akarPrivat, jalur)
        val akarKanonik = akarPrivat.canonicalPath
        val fileKanonik = file.canonicalPath
        return if (fileKanonik.startsWith(akarKanonik)) file else null
    }
}