package id.manajerberkas.kode.data.repositori

import android.content.Context
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import id.manajerberkas.kode.data.adapter.LokasiSafTerurai
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoriBerkasAndroid(
    private val context: Context
) : RepositoriBerkas {
    private val akarPrivat = context.filesDir
    private val akarEksternal: File
        get() = Environment.getExternalStorageDirectory()

    private companion object {
        const val BATAS_EDITOR = 1_048_576L // 1 MB
        const val UKURAN_PROBE_BINER = 8192
    }

    override suspend fun daftarIsi(lokasi: LokasiBerkas): HasilDaftarBerkas = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> daftarIsiFile(lokasi, akarPrivat, PabrikLokasiAndroid.SKEMA_PRIVAT)
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> daftarIsiFile(lokasi, akarEksternal, PabrikLokasiAndroid.SKEMA_EKSTERNAL)
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> daftarIsiSaf(lokasi)
            else -> HasilDaftarBerkas(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun dapatkanKemampuan(lokasi: LokasiBerkas): KemampuanPenyimpanan = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) || lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> {
                KemampuanPenyimpanan(
                    bisaBaca = true,
                    bisaTulis = true,
                    bisaBuatBerkas = true,
                    bisaBuatFolder = true,
                    bisaGantiNama = true,
                    bisaHapus = true,
                    bisaSalin = true,
                    bisaPindah = true,
                    bisaBagikan = false,
                    bisaKompres = false,
                    bisaEkstrak = false,
                    bisaBookmark = true,
                    dukunganAtomicReplace = true
                )
            }
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> kemampuanSaf(lokasi)
            else -> KemampuanPenyimpanan.kosong()
        }
    }

    override suspend fun buatBerkas(lokasiInduk: LokasiBerkas, nama: String): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> buatBerkasFile(lokasiInduk, nama, akarPrivat, PabrikLokasiAndroid.SKEMA_PRIVAT)
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> buatBerkasFile(lokasiInduk, nama, akarEksternal, PabrikLokasiAndroid.SKEMA_EKSTERNAL)
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> buatItemSaf(lokasiInduk, nama, "application/octet-stream", "Berkas dibuat.")
            else -> HasilOperasi(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun buatFolder(lokasiInduk: LokasiBerkas, nama: String): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> buatFolderFile(lokasiInduk, nama, akarPrivat, PabrikLokasiAndroid.SKEMA_PRIVAT)
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> buatFolderFile(lokasiInduk, nama, akarEksternal, PabrikLokasiAndroid.SKEMA_EKSTERNAL)
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> buatItemSaf(lokasiInduk, nama, DocumentsContract.Document.MIME_TYPE_DIR, "Folder dibuat.")
            else -> HasilOperasi(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun gantiNama(lokasi: LokasiBerkas, namaBaru: String): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> gantiNamaFile(lokasi, namaBaru, akarPrivat, PabrikLokasiAndroid.SKEMA_PRIVAT)
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> gantiNamaFile(lokasi, namaBaru, akarEksternal, PabrikLokasiAndroid.SKEMA_EKSTERNAL)
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> gantiNamaSaf(lokasi, namaBaru)
            else -> HasilOperasi(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun hapus(lokasi: LokasiBerkas): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> hapusFile(lokasi, akarPrivat, PabrikLokasiAndroid.SKEMA_PRIVAT)
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> hapusFile(lokasi, akarEksternal, PabrikLokasiAndroid.SKEMA_EKSTERNAL)
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> hapusSaf(lokasi)
            else -> HasilOperasi(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun salinKe(sumber: LokasiBerkas, lokasiIndukTujuan: LokasiBerkas): HasilOperasi = withContext(Dispatchers.IO) {
        val fileSumber = ambilBerkas(sumber)
        val fileTujuan = ambilBerkas(lokasiIndukTujuan)
        if (fileSumber != null && fileTujuan != null) {
            salinFile(fileSumber, fileTujuan)
        } else {
            HasilOperasi(false, "Salin antar format penyimpanan ini belum didukung.")
        }
    }

    override suspend fun pindahkanKe(sumber: LokasiBerkas, lokasiIndukTujuan: LokasiBerkas): HasilOperasi = withContext(Dispatchers.IO) {
        val fileSumber = ambilBerkas(sumber)
        val fileTujuan = ambilBerkas(lokasiIndukTujuan)
        if (fileSumber != null && fileTujuan != null) {
            pindahFile(fileSumber, fileTujuan)
        } else {
            HasilOperasi(false, "Pindah antar format penyimpanan ini belum didukung.")
        }
    }

    override suspend fun bacaTeks(lokasi: LokasiBerkas): HasilBacaTeks = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) || lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> {
                val file = ambilBerkas(lokasi) ?: return@withContext HasilBacaTeks(false, "File tidak ditemukan.")
                bacaTeksDariFile(file)
            }
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> bacaTeksSaf(lokasi)
            else -> HasilBacaTeks(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun simpanTeks(lokasi: LokasiBerkas, konten: KontenTeks): HasilSimpanTeks = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) || lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> {
                val file = ambilBerkas(lokasi) ?: return@withContext HasilSimpanTeks(false, "File tidak ditemukan.")
                simpanTeksKeFile(file, konten)
            }
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> simpanTeksSaf(lokasi, konten)
            else -> HasilSimpanTeks(false, "Lokasi tidak dikenal.")
        }
    }

    override suspend fun dapatkanMetadata(lokasi: LokasiBerkas): MetadataBerkas? = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) || lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> {
                val file = ambilBerkas(lokasi) ?: return@withContext null
                if (!file.exists()) null else MetadataBerkas(file.lastModified(), file.length(), file.canWrite())
            }
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> metadataSaf(lokasi)
            else -> null
        }
    }

    private fun ambilBerkas(lokasi: LokasiBerkas): File? {
        return try {
            when {
                lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
                    berkasDariRoot(lokasi.id.removePrefix(PabrikLokasiAndroid.SKEMA_PRIVAT).trim('/'), akarPrivat)
                }
                lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> {
                    berkasDariRoot(lokasi.id.removePrefix(PabrikLokasiAndroid.SKEMA_EKSTERNAL).trim('/'), akarEksternal)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun berkasDariRoot(jalurRelatif: String, akar: File): File {
        val target = if (jalurRelatif.isBlank()) akar else File(akar, jalurRelatif)
        val akarKanonik = akar.canonicalPath
        val targetKanonik = target.canonicalPath
        if (!(targetKanonik == akarKanonik || targetKanonik.startsWith(akarKanonik + File.separator))) {
            throw SecurityException("Akses di luar folder diizinkan.")
        }
        return target
    }

    private fun daftarIsiFile(lokasi: LokasiBerkas, akar: File, skema: String): HasilDaftarBerkas {
        return try {
            val jalurRelatif = lokasi.id.removePrefix(skema).trim('/')
            val folder = berkasDariRoot(jalurRelatif, akar)
            if (!folder.isDirectory) return HasilDaftarBerkas(false, "Lokasi bukan folder.")
            val berkasAnak = folder.listFiles() ?: return HasilDaftarBerkas(false, "Gagal mengakses isi folder (periksa izin penyimpanan).")
            val daftar = berkasAnak.map { anak ->
                val jalurAnak = if (jalurRelatif.isBlank()) anak.name else "$jalurRelatif/${anak.name}"
                val lokasiAnak = if (skema == PabrikLokasiAndroid.SKEMA_PRIVAT) PabrikLokasiAndroid.privat(jalurAnak) else PabrikLokasiAndroid.eksternal(jalurAnak)
                ItemBerkas(
                    nama = anak.name,
                    lokasi = lokasiAnak,
                    jenis = if (anak.isDirectory) JenisItem.FOLDER else JenisItem.BERKAS,
                    ukuranByte = if (anak.isFile) anak.length() else null,
                    waktuModifikasi = anak.lastModified()
                )
            }.sortedWith(compareByDescending<ItemBerkas> { it.jenis }.thenBy { it.nama.lowercase() })
            HasilDaftarBerkas(true, daftar = daftar)
        } catch (e: Exception) {
            HasilDaftarBerkas(false, e.message ?: "Gagal membaca direktori.")
        }
    }

    private fun buatBerkasFile(lokasiInduk: LokasiBerkas, nama: String, akar: File, skema: String): HasilOperasi {
        return try {
            val jalurInduk = lokasiInduk.id.removePrefix(skema).trim('/')
            val induk = berkasDariRoot(jalurInduk, akar)
            val berkasBaru = File(induk, nama)
            if (berkasBaru.exists()) return HasilOperasi(false, "Berkas sudah ada.")
            if (!berkasBaru.createNewFile()) return HasilOperasi(false, "Gagal membuat berkas.")
            val rel = berkasBaru.relativeTo(akar).path.replace(File.separatorChar, '/')
            val lokHasil = if (skema == PabrikLokasiAndroid.SKEMA_PRIVAT) PabrikLokasiAndroid.privat(rel) else PabrikLokasiAndroid.eksternal(rel)
            HasilOperasi(true, "Berkas dibuat.", lokasiHasil = lokHasil)
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal membuat berkas.")
        }
    }

    private fun buatFolderFile(lokasiInduk: LokasiBerkas, nama: String, akar: File, skema: String): HasilOperasi {
        return try {
            val jalurInduk = lokasiInduk.id.removePrefix(skema).trim('/')
            val induk = berkasDariRoot(jalurInduk, akar)
            val folderBaru = File(induk, nama)
            if (folderBaru.exists()) return HasilOperasi(false, "Folder sudah ada.")
            if (!folderBaru.mkdirs()) return HasilOperasi(false, "Gagal membuat folder.")
            val rel = folderBaru.relativeTo(akar).path.replace(File.separatorChar, '/')
            val lokHasil = if (skema == PabrikLokasiAndroid.SKEMA_PRIVAT) PabrikLokasiAndroid.privat(rel) else PabrikLokasiAndroid.eksternal(rel)
            HasilOperasi(true, "Folder dibuat.", lokasiHasil = lokHasil)
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal membuat folder.")
        }
    }

    private fun gantiNamaFile(lokasi: LokasiBerkas, namaBaru: String, akar: File, skema: String): HasilOperasi {
        return try {
            val jalur = lokasi.id.removePrefix(skema).trim('/')
            val file = berkasDariRoot(jalur, akar)
            val target = File(file.parentFile, namaBaru)
            if (target.exists()) return HasilOperasi(false, "Nama sudah digunakan.")
            if (!file.renameTo(target)) return HasilOperasi(false, "Gagal mengganti nama.")
            val rel = target.relativeTo(akar).path.replace(File.separatorChar, '/')
            val lokHasil = if (skema == PabrikLokasiAndroid.SKEMA_PRIVAT) PabrikLokasiAndroid.privat(rel) else PabrikLokasiAndroid.eksternal(rel)
            HasilOperasi(true, "Nama diganti.", lokasiHasil = lokHasil)
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal mengganti nama.")
        }
    }

    private fun hapusFile(lokasi: LokasiBerkas, akar: File, skema: String): HasilOperasi {
        return try {
            if (lokasi.id == skema) return HasilOperasi(false, "Root tidak dapat dihapus.")
            val jalur = lokasi.id.removePrefix(skema).trim('/')
            val file = berkasDariRoot(jalur, akar)
            val sukses = file.deleteRecursively()
            HasilOperasi(sukses, if (sukses) "Item dihapus." else "Gagal menghapus beberapa item.")
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal menghapus.")
        }
    }

    private fun salinFile(sumber: File, tujuan: File): HasilOperasi {
        return try {
            if (!tujuan.isDirectory) return HasilOperasi(false, "Tujuan bukan folder.")
            val target = File(tujuan, sumber.name)
            if (target.exists()) return HasilOperasi(false, "Item '${sumber.name}' sudah ada di folder tujuan.")

            val sumberKanonik = sumber.canonicalPath
            val targetKanonik = target.canonicalPath
            if (sumber.isDirectory && (targetKanonik == sumberKanonik || targetKanonik.startsWith(sumberKanonik + File.separator))) {
                return HasilOperasi(false, "Tidak dapat menyalin folder ke dalam dirinya sendiri.")
            }

            if (sumber.isDirectory) {
                sumber.copyRecursively(target, overwrite = false)
            } else {
                sumber.copyTo(target, overwrite = false)
            }
            HasilOperasi(true, "Item disalin.")
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal menyalin.")
        }
    }

    private fun pindahFile(sumber: File, tujuan: File): HasilOperasi {
        return try {
            if (!tujuan.isDirectory) return HasilOperasi(false, "Tujuan bukan folder.")
            val target = File(tujuan, sumber.name)
            if (target.exists()) return HasilOperasi(false, "Item '${sumber.name}' sudah ada di folder tujuan.")

            val sumberKanonik = sumber.canonicalPath
            val targetKanonik = target.canonicalPath
            if (sumber.isDirectory && (targetKanonik == sumberKanonik || targetKanonik.startsWith(sumberKanonik + File.separator))) {
                return HasilOperasi(false, "Tidak dapat memindahkan folder ke dalam dirinya sendiri.")
            }

            if (sumber.renameTo(target)) {
                HasilOperasi(true, "Item dipindahkan.")
            } else {
                val suksesSalin = if (sumber.isDirectory) {
                    sumber.copyRecursively(target, overwrite = false)
                } else {
                    sumber.copyTo(target, overwrite = false).exists()
                }
                if (suksesSalin) {
                    val terhapus = sumber.deleteRecursively()
                    if (terhapus) {
                        HasilOperasi(true, "Item dipindahkan.")
                    } else {
                        HasilOperasi(true, "Item disalin, namun berkas sumber belum terhapus sepenuhnya.")
                    }
                } else {
                    HasilOperasi(false, "Gagal memindahkan item.")
                }
            }
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal memindahkan.")
        }
    }

    private fun apakahBiner(bytes: ByteArray): Boolean {
        val batas = minOf(bytes.size, UKURAN_PROBE_BINER)
        for (i in 0 until batas) {
            if (bytes[i].toInt() == 0) return true
        }
        return false
    }

    private fun bacaTeksDariFile(file: File): HasilBacaTeks {
        return try {
            if (!file.isFile) return HasilBacaTeks(false, "Bukan file.")
            if (file.length() > BATAS_EDITOR) {
                return HasilBacaTeks(false, "File terlalu besar untuk editor (maksimal 1 MB).", terlaluBesar = true, ukuranByte = file.length())
            }
            val bytes = file.readBytes()
            if (apakahBiner(bytes)) {
                return HasilBacaTeks(false, "Berkas terdeteksi sebagai berkas biner dan tidak dapat diedit sebagai teks.")
            }
            val konten = decodeKonten(bytes)
            HasilBacaTeks(true, konten = konten, hanyaBaca = !file.canWrite(), ukuranByte = file.length())
        } catch (e: Exception) {
            HasilBacaTeks(false, e.message ?: "Gagal membaca teks.")
        }
    }

    private fun simpanTeksKeFile(file: File, konten: KontenTeks): HasilSimpanTeks {
        val parent = file.parentFile ?: return HasilSimpanTeks(false, "Direktori induk tidak ditemukan.")
        val charset = try { Charset.forName(konten.encoding) } catch (e: Exception) { Charsets.UTF_8 }
        val tempFile = try {
            File.createTempFile(".tmp_mbk_", ".tmp", parent)
        } catch (e: Exception) {
            File(parent, ".tmp_mbk_${System.currentTimeMillis()}_${file.name}")
        }
        return try {
            tempFile.outputStream().use { output ->
                output.write(konten.teks.toByteArray(charset))
                output.flush()
            }
            val targetPath = file.toPath()
            val tempPath = tempFile.toPath()
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            HasilSimpanTeks(true, "Perubahan disimpan.")
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            HasilSimpanTeks(false, e.message ?: "Gagal menyimpan teks secara atomik.")
        }
    }

    private fun daftarIsiSaf(lokasi: LokasiBerkas): HasilDaftarBerkas {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return HasilDaftarBerkas(false, "Lokasi SAF tidak valid.")
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val kolom = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )
            val daftar = mutableListOf<ItemBerkas>()
            context.contentResolver.query(childrenUri, kolom, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val namaIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                while (cursor.moveToNext()) {
                    val docId = cursor.getString(idIdx) ?: continue
                    val nama = cursor.getString(namaIdx) ?: continue
                    val mime = cursor.getString(mimeIdx) ?: continue
                    val isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR
                    daftar.add(
                        ItemBerkas(
                            nama = nama,
                            lokasi = LokasiBerkas(
                                id = PabrikLokasiAndroid.buatIdSaf(lokasiSaf.treeUri.toString(), docId),
                                namaTampilan = nama,
                                jalurTampilan = null,
                                jenisPenyimpanan = JenisPenyimpanan.SAF_ANDROID
                            ),
                            jenis = if (isDir) JenisItem.FOLDER else JenisItem.BERKAS,
                            ukuranByte = if (!isDir && !cursor.isNull(sizeIdx)) cursor.getLong(sizeIdx) else null,
                            waktuModifikasi = if (!cursor.isNull(modIdx)) cursor.getLong(modIdx) else null
                        )
                    )
                }
            }
            HasilDaftarBerkas(true, daftar = daftar.sortedWith(compareByDescending<ItemBerkas> { it.jenis }.thenBy { it.nama.lowercase() }))
        } catch (e: Exception) {
            HasilDaftarBerkas(false, e.message ?: "Gagal membaca SAF.")
        }
    }

    private fun kemampuanSaf(lokasi: LokasiBerkas): KemampuanPenyimpanan {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return KemampuanPenyimpanan.kosong()
            val uri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val doc = DocumentFile.fromSingleUri(context, uri) ?: return KemampuanPenyimpanan.kosong()
            val tulis = doc.canWrite()
            KemampuanPenyimpanan(
                bisaBaca = doc.canRead(),
                bisaTulis = tulis,
                bisaBuatBerkas = tulis,
                bisaBuatFolder = tulis,
                bisaGantiNama = tulis,
                bisaHapus = tulis,
                bisaSalin = false,
                bisaPindah = false,
                bisaBagikan = false,
                bisaKompres = false,
                bisaEkstrak = false,
                bisaBookmark = doc.canRead(),
                dukunganAtomicReplace = false
            )
        } catch (e: Exception) {
            KemampuanPenyimpanan.kosong()
        }
    }

    private fun buatItemSaf(lokasiInduk: LokasiBerkas, nama: String, mime: String, pesan: String): HasilOperasi {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasiInduk.id) ?: return HasilOperasi(false, "SAF tidak valid.")
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val hasil = DocumentsContract.createDocument(context.contentResolver, parentUri, mime, nama) ?: return HasilOperasi(false, "Gagal membuat item SAF.")
            val docId = DocumentsContract.getDocumentId(hasil)
            HasilOperasi(true, pesan, lokasiHasil = LokasiBerkas(PabrikLokasiAndroid.buatIdSaf(lokasiSaf.treeUri.toString(), docId), nama, null, JenisPenyimpanan.SAF_ANDROID))
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal membuat item SAF.")
        }
    }

    private fun gantiNamaSaf(lokasi: LokasiBerkas, namaBaru: String): HasilOperasi {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return HasilOperasi(false, "SAF tidak valid.")
            val docUri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val hasil = DocumentsContract.renameDocument(context.contentResolver, docUri, namaBaru)
                ?: return HasilOperasi(false, "Gagal mengganti nama di penyimpanan SAF.")
            val docId = try { DocumentsContract.getDocumentId(hasil) } catch (e: Exception) { lokasiSaf.documentId }
            HasilOperasi(true, "Nama diganti.", lokasiHasil = LokasiBerkas(PabrikLokasiAndroid.buatIdSaf(lokasiSaf.treeUri.toString(), docId), namaBaru, null, JenisPenyimpanan.SAF_ANDROID))
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal ganti nama SAF.")
        }
    }

    private fun hapusSaf(lokasi: LokasiBerkas): HasilOperasi {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return HasilOperasi(false, "SAF tidak valid.")
            val docUri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val sukses = DocumentsContract.deleteDocument(context.contentResolver, docUri)
            HasilOperasi(sukses, if (sukses) "Item dihapus." else "Gagal menghapus.")
        } catch (e: Exception) {
            HasilOperasi(false, e.message ?: "Gagal hapus SAF.")
        }
    }

    private fun bacaTeksSaf(lokasi: LokasiBerkas): HasilBacaTeks {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return HasilBacaTeks(false, "SAF tidak valid.")
            val docUri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)

            val doc = DocumentFile.fromSingleUri(context, docUri)
            val ukuranMetadata = doc?.length() ?: -1L
            if (ukuranMetadata > BATAS_EDITOR) {
                return HasilBacaTeks(false, "File SAF terlalu besar untuk editor (maksimal 1 MB).", terlaluBesar = true, ukuranByte = ukuranMetadata)
            }

            val buffer = ByteArrayOutputStream()
            val temp = ByteArray(8192)
            var totalRead = 0L

            context.contentResolver.openInputStream(docUri)?.use { input ->
                var read: Int
                while (input.read(temp).also { read = it } != -1) {
                    totalRead += read
                    if (totalRead > BATAS_EDITOR) {
                        return HasilBacaTeks(false, "File SAF melebihi batas 1 MB saat dibaca.", terlaluBesar = true, ukuranByte = totalRead)
                    }
                    buffer.write(temp, 0, read)
                }
            } ?: return HasilBacaTeks(false, "Gagal membuka stream berkas SAF.")

            val bytes = buffer.toByteArray()
            if (apakahBiner(bytes)) {
                return HasilBacaTeks(false, "Berkas SAF terdeteksi sebagai biner dan tidak dapat diedit sebagai teks.")
            }

            HasilBacaTeks(true, konten = decodeKonten(bytes), ukuranByte = bytes.size.toLong())
        } catch (e: Exception) {
            HasilBacaTeks(false, e.message ?: "Gagal membaca SAF.")
        }
    }

    private fun simpanTeksSaf(lokasi: LokasiBerkas, konten: KontenTeks): HasilSimpanTeks {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return HasilSimpanTeks(false, "SAF tidak valid.")
            val docUri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val charset = try { Charset.forName(konten.encoding) } catch (e: Exception) { Charsets.UTF_8 }
            context.contentResolver.openOutputStream(docUri, "wt")?.use { output ->
                output.write(konten.teks.toByteArray(charset))
                output.flush()
            } ?: return HasilSimpanTeks(false, "Gagal membuka output stream SAF.")
            HasilSimpanTeks(true, "Perubahan disimpan.")
        } catch (e: Exception) {
            HasilSimpanTeks(false, e.message ?: "Gagal simpan SAF.")
        }
    }

    private fun metadataSaf(lokasi: LokasiBerkas): MetadataBerkas? {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id) ?: return null
            val docUri = DocumentsContract.buildDocumentUriUsingTree(lokasiSaf.treeUri, lokasiSaf.documentId)
            val doc = DocumentFile.fromSingleUri(context, docUri) ?: return null
            MetadataBerkas(doc.lastModified(), doc.length(), doc.canWrite())
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeKonten(bytes: ByteArray): KontenTeks {
        return KontenTeks(teks = bytes.decodeToString(), encoding = "UTF-8")
    }
}