package id.manajerberkas.kode.data.repositori

import android.content.Context
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import id.manajerberkas.kode.data.adapter.LokasiSafTerurai
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.HasilDaftarBerkas
import id.manajerberkas.kode.domain.model.HasilOperasi
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.JenisItem
import id.manajerberkas.kode.domain.model.JenisPenyimpanan
import id.manajerberkas.kode.domain.model.KemampuanPenyimpanan
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MetadataBerkas
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import id.manajerberkas.kode.domain.model.HasilBacaTeks
import id.manajerberkas.kode.domain.model.HasilSimpanTeks
import id.manajerberkas.kode.domain.model.KontenTeks
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class RepositoriBerkasAndroid(
    private val context: Context
) : RepositoriBerkas {

    private val akarPrivat = context.filesDir

    private companion object {
        const val BATAS_EDITOR = 1_048_576L
    }

    override suspend fun daftarIsi(
        lokasi: LokasiBerkas
    ): HasilDaftarBerkas = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
                daftarIsiPrivat(lokasi)
            }

            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
                daftarIsiSaf(lokasi)
            }

            else -> {
                HasilDaftarBerkas(
                    sukses = false,
                    pesan = "Lokasi tidak dikenal."
                )
            }
        }
    }

    override suspend fun dapatkanKemampuan(
        lokasi: LokasiBerkas
    ): KemampuanPenyimpanan = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
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

            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
                kemampuanSaf(lokasi)
            }

            else -> {
                KemampuanPenyimpanan.kosong()
            }
        }
    }

    override suspend fun buatBerkas(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
                buatBerkasPrivat(lokasiInduk, nama)
            }

            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
                buatItemSaf(
                    lokasiInduk = lokasiInduk,
                    nama = nama,
                    mimeType = "application/octet-stream",
                    pesanSukses = "Berkas dibuat."
                )
            }

            else -> {
                HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi tidak dikenal."
                )
            }
        }
    }

    override suspend fun buatFolder(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
                buatFolderPrivat(lokasiInduk, nama)
            }

            lokasiInduk.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
                buatItemSaf(
                    lokasiInduk = lokasiInduk,
                    nama = nama,
                    mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                    pesanSukses = "Folder dibuat."
                )
            }

            else -> {
                HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi tidak dikenal."
                )
            }
        }
    }

    override suspend fun gantiNama(
        lokasi: LokasiBerkas,
        namaBaru: String
    ): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
                gantiNamaPrivat(lokasi, namaBaru)
            }

            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
                gantiNamaSaf(lokasi, namaBaru)
            }

            else -> {
                HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi tidak dikenal."
                )
            }
        }
    }

    override suspend fun hapus(
        lokasi: LokasiBerkas
    ): HasilOperasi = withContext(Dispatchers.IO) {
        when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
                hapusPrivat(lokasi)
            }

            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
                hapusSaf(lokasi)
            }

            else -> {
                HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi tidak dikenal."
                )
            }
        }
    }

    override suspend fun salinKe(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi = withContext(Dispatchers.IO) {
        if (
            sumber.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) &&
            lokasiIndukTujuan.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT)
        ) {
            salinPrivat(sumber, lokasiIndukTujuan)
        } else {
            HasilOperasi(
                sukses = false,
                pesan = "Salin antar lokasi ini belum didukung pada tahap ini."
            )
        }
    }

    override suspend fun pindahkanKe(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi = withContext(Dispatchers.IO) {
        if (
            sumber.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) &&
            lokasiIndukTujuan.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT)
        ) {
            pindahkanPrivat(sumber, lokasiIndukTujuan)
        } else {
            HasilOperasi(
                sukses = false,
                pesan = "Pindah antar lokasi ini belum didukung pada tahap ini."
            )
        }
    }

override suspend fun bacaTeks(
    lokasi: LokasiBerkas
): HasilBacaTeks = withContext(Dispatchers.IO) {
    when {
        lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
            bacaTeksPrivat(lokasi)
        }

        lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
            bacaTeksSaf(lokasi)
        }

        else -> {
            HasilBacaTeks(
                sukses = false,
                pesan = "Lokasi tidak dikenal."
            )
        }
    }
}

override suspend fun simpanTeks(
    lokasi: LokasiBerkas,
    konten: KontenTeks
): HasilSimpanTeks = withContext(Dispatchers.IO) {
    when {
        lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
            simpanTeksPrivat(lokasi, konten)
        }

        lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
            simpanTeksSaf(lokasi, konten)
        }

        else -> {
            HasilSimpanTeks(
                sukses = false,
                pesan = "Lokasi tidak dikenal."
            )
        }
    }
}

override suspend fun dapatkanMetadata(
    lokasi: LokasiBerkas
): MetadataBerkas? = withContext(Dispatchers.IO) {
    when {
        lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> {
            metadataPrivat(lokasi)
        }

        lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_SAF) -> {
            metadataSaf(lokasi)
        }

        else -> null
    }
}

private fun bacaTeksPrivat(
    lokasi: LokasiBerkas
): HasilBacaTeks {
    return try {
        val file = berkasPrivat(jalurPrivatDariId(lokasi.id))

        if (!file.isFile) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "Lokasi bukan file."
            )
        }

        if (!file.canRead()) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File tidak dapat dibaca."
            )
        }

        if (file.length() > BATAS_EDITOR) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File terlalu besar untuk editor tahap ini.",
                terlaluBesar = true,
                ukuranByte = file.length()
            )
        }

        val bytes = file.inputStream().use { inputStream ->
            bacaByteSampaiBatas(inputStream)
        }

        if (bytes == null) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File terlalu besar untuk editor tahap ini.",
                terlaluBesar = true,
                ukuranByte = file.length()
            )
        }

        if (!adaBom(bytes) && terdeteksiBiner(bytes)) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File tidak dapat dibuka sebagai teks."
            )
        }

        val konten = decodeKonten(bytes)

        HasilBacaTeks(
            sukses = true,
            konten = konten,
            hanyaBaca = !file.canWrite(),
            ukuranByte = file.length()
        )
    } catch (e: Exception) {
        HasilBacaTeks(
            sukses = false,
            pesan = e.message ?: "Gagal membaca file."
        )
    }
}

private fun bacaTeksSaf(
    lokasi: LokasiBerkas
): HasilBacaTeks {
    return try {
        val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
            ?: return HasilBacaTeks(
                sukses = false,
                pesan = "Lokasi SAF tidak valid."
            )

        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            lokasiSaf.treeUri,
            lokasiSaf.documentId
        )

        val dokumen = DocumentFile.fromSingleUri(context, documentUri)
            ?: return HasilBacaTeks(
                sukses = false,
                pesan = "File SAF tidak dapat diakses."
            )

        if (!dokumen.isFile) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "Lokasi bukan file."
            )
        }

        if (!dokumen.canRead()) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File tidak dapat dibaca."
            )
        }

        if (dokumen.length() > BATAS_EDITOR) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File terlalu besar untuk editor tahap ini.",
                terlaluBesar = true,
                ukuranByte = dokumen.length()
            )
        }

        val bytes = context.contentResolver.openInputStream(documentUri)?.use { inputStream ->
            bacaByteSampaiBatas(inputStream)
        }

        if (bytes == null) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File terlalu besar untuk editor tahap ini.",
                terlaluBesar = true,
                ukuranByte = dokumen.length()
            )
        }

        if (!adaBom(bytes) && terdeteksiBiner(bytes)) {
            return HasilBacaTeks(
                sukses = false,
                pesan = "File tidak dapat dibuka sebagai teks."
            )
        }

        val konten = decodeKonten(bytes)

        HasilBacaTeks(
            sukses = true,
            konten = konten,
            hanyaBaca = !dokumen.canWrite(),
            ukuranByte = dokumen.length()
        )
    } catch (e: Exception) {
        HasilBacaTeks(
            sukses = false,
            pesan = e.message ?: "Gagal membaca file SAF."
        )
    }
}

private fun simpanTeksPrivat(
    lokasi: LokasiBerkas,
    konten: KontenTeks
): HasilSimpanTeks {
    return try {
        val file = berkasPrivat(jalurPrivatDariId(lokasi.id))

        if (!file.isFile) {
            return HasilSimpanTeks(
                sukses = false,
                pesan = "Lokasi bukan file."
            )
        }

        if (!file.canWrite()) {
            return HasilSimpanTeks(
                sukses = false,
                pesan = "Penyimpanan tidak dapat ditulis."
            )
        }

        val fileSementara = File(file.parentFile, "${file.name}.tmp")

        fileSementara.writeText(konten.teks)

        try {
            Files.move(
                fileSementara.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (e: Exception) {
            fileSementara.delete()
            throw e
        }

        HasilSimpanTeks(
            sukses = true,
            pesan = "Perubahan disimpan."
        )
    } catch (e: Exception) {
        HasilSimpanTeks(
            sukses = false,
            pesan = e.message ?: "Gagal menyimpan file."
        )
    }
}

private fun simpanTeksSaf(
    lokasi: LokasiBerkas,
    konten: KontenTeks
): HasilSimpanTeks {
    return try {
        val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
            ?: return HasilSimpanTeks(
                sukses = false,
                pesan = "Lokasi SAF tidak valid."
            )

        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            lokasiSaf.treeUri,
            lokasiSaf.documentId
        )

        val dokumen = DocumentFile.fromSingleUri(context, documentUri)
            ?: return HasilSimpanTeks(
                sukses = false,
                pesan = "File SAF tidak dapat diakses."
            )

        if (!dokumen.canWrite()) {
            return HasilSimpanTeks(
                sukses = false,
                pesan = "Penyimpanan tidak dapat ditulis."
            )
        }

        val outputStream = context.contentResolver.openOutputStream(
            documentUri,
            "wt"
        ) ?: return HasilSimpanTeks(
            sukses = false,
            pesan = "Gagal membuka file SAF untuk ditulis."
        )

        outputStream.use {
            it.write(konten.teks.encodeToByteArray())
        }

        HasilSimpanTeks(
            sukses = true,
            pesan = "Perubahan disimpan."
        )
    } catch (e: Exception) {
        HasilSimpanTeks(
            sukses = false,
            pesan = e.message ?: "Gagal menyimpan file SAF."
        )
    }
}

private fun bacaByteSampaiBatas(
    inputStream: InputStream
): ByteArray? {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8_192)
    var total = 0L

    while (true) {
        val dibaca = inputStream.read(buffer)

        if (dibaca == -1) break

        total += dibaca

        if (total > BATAS_EDITOR) return null

        output.write(buffer, 0, dibaca)
    }

    return output.toByteArray()
}

private fun terdeteksiBiner(bytes: ByteArray): Boolean {
    val batas = minOf(bytes.size, 8_192)

    for (i in 0 until batas) {
        if (bytes[i].toInt() == 0) {
            return true
        }
    }

    return false
}

    private fun daftarIsiPrivat(
        lokasi: LokasiBerkas
    ): HasilDaftarBerkas {
        return try {
            val jalurRelatif = jalurPrivatDariId(lokasi.id)
            val folder = berkasPrivat(jalurRelatif)

            if (!folder.isDirectory) {
                return HasilDaftarBerkas(
                    sukses = false,
                    pesan = "Lokasi bukan folder."
                )
            }

            val daftar = folder.listFiles()
                ?.map { anak ->
                    val jalurAnak = if (jalurRelatif.isBlank()) {
                        anak.name
                    } else {
                        "${jalurRelatif.trimEnd('/')}/${anak.name}"
                    }

                    ItemBerkas(
                        nama = anak.name,
                        lokasi = PabrikLokasiAndroid.privat(jalurAnak),
                        jenis = if (anak.isDirectory) {
                            JenisItem.FOLDER
                        } else {
                            JenisItem.BERKAS
                        },
                        ukuranByte = if (anak.isFile) anak.length() else null,
                        waktuModifikasi = anak.lastModified()
                    )
                }
                ?.sortedWith(
                    compareByDescending<ItemBerkas> { it.jenis }
                        .thenBy { it.nama.lowercase() }
                )
                ?: emptyList()

            HasilDaftarBerkas(
                sukses = true,
                daftar = daftar
            )
        } catch (e: Exception) {
            HasilDaftarBerkas(
                sukses = false,
                pesan = e.message ?: "Gagal membaca penyimpanan privat."
            )
        }
    }

    private fun daftarIsiSaf(
        lokasi: LokasiBerkas
    ): HasilDaftarBerkas {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
                ?: return HasilDaftarBerkas(
                    sukses = false,
                    pesan = "Lokasi SAF tidak valid."
                )

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                lokasiSaf.treeUri,
                lokasiSaf.documentId
            )

            val kolom = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )

            val daftar = mutableListOf<ItemBerkas>()

            context.contentResolver.query(
                childrenUri,
                kolom,
                null,
                null,
                null
            )?.use { cursor ->
                val indeksId = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID
                )

                val indeksNama = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                )

                val indeksMime = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                )

                val indeksUkuran = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_SIZE
                )

                val indeksModifikasi = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                )

                while (cursor.moveToNext()) {
                    val documentId = cursor.getString(indeksId) ?: continue
                    val nama = cursor.getString(indeksNama) ?: continue
                    val mime = cursor.getString(indeksMime) ?: continue

                    val jenis = if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        JenisItem.FOLDER
                    } else {
                        JenisItem.BERKAS
                    }

                    val ukuran = if (jenis == JenisItem.BERKAS && !cursor.isNull(indeksUkuran)) {
                        cursor.getLong(indeksUkuran)
                    } else {
                        null
                    }

                    val modifikasi = if (!cursor.isNull(indeksModifikasi)) {
                        cursor.getLong(indeksModifikasi)
                    } else {
                        null
                    }

                    daftar.add(
                        ItemBerkas(
                            nama = nama,
                            lokasi = LokasiBerkas(
                                id = PabrikLokasiAndroid.buatIdSaf(
                                    lokasiSaf.treeUri.toString(),
                                    documentId
                                ),
                                namaTampilan = nama,
                                jalurTampilan = null,
                                jenisPenyimpanan = JenisPenyimpanan.SAF_ANDROID
                            ),
                            jenis = jenis,
                            ukuranByte = ukuran,
                            waktuModifikasi = modifikasi
                        )
                    )
                }
            }

            HasilDaftarBerkas(
                sukses = true,
                daftar = daftar.sortedWith(
                    compareByDescending<ItemBerkas> { it.jenis }
                        .thenBy { it.nama.lowercase() }
                )
            )
        } catch (e: Exception) {
            HasilDaftarBerkas(
                sukses = false,
                pesan = e.message ?: "Gagal membaca penyimpanan SAF."
            )
        }
    }

    private fun kemampuanSaf(
        lokasi: LokasiBerkas
    ): KemampuanPenyimpanan {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
                ?: return KemampuanPenyimpanan.kosong()

            val dokumen = ambilDokumenSaf(lokasiSaf)
                ?: return KemampuanPenyimpanan.kosong()

            val bisaTulis = dokumen.canWrite()

            KemampuanPenyimpanan(
                bisaBaca = dokumen.canRead(),
                bisaTulis = bisaTulis,
                bisaBuatBerkas = bisaTulis,
                bisaBuatFolder = bisaTulis,
                bisaGantiNama = bisaTulis,
                bisaHapus = bisaTulis,
                bisaSalin = false,
                bisaPindah = false,
                bisaBagikan = false,
                bisaKompres = false,
                bisaEkstrak = false,
                bisaBookmark = dokumen.canRead(),
                dukunganAtomicReplace = false
            )
        } catch (e: Exception) {
            KemampuanPenyimpanan.kosong()
        }
    }

    private fun buatBerkasPrivat(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi {
        return try {
            val jalurInduk = jalurPrivatDariId(lokasiInduk.id)
            val induk = berkasPrivat(jalurInduk)

            if (!induk.isDirectory) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi induk bukan folder."
                )
            }

            val berkasBaru = File(induk, nama)

            if (berkasBaru.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Berkas sudah ada."
                )
            }

            val sukses = berkasBaru.createNewFile()

            if (!sukses) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Gagal membuat berkas."
                )
            }

            HasilOperasi(
                sukses = true,
                pesan = "Berkas dibuat.",
                lokasiHasil = PabrikLokasiAndroid.privat(relatifPrivat(berkasBaru))
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal membuat berkas."
            )
        }
    }

    private fun buatFolderPrivat(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi {
        return try {
            val jalurInduk = jalurPrivatDariId(lokasiInduk.id)
            val induk = berkasPrivat(jalurInduk)

            if (!induk.isDirectory) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi induk bukan folder."
                )
            }

            val folderBaru = File(induk, nama)

            if (folderBaru.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Folder sudah ada."
                )
            }

            val sukses = folderBaru.mkdirs()

            if (!sukses) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Gagal membuat folder."
                )
            }

            HasilOperasi(
                sukses = true,
                pesan = "Folder dibuat.",
                lokasiHasil = PabrikLokasiAndroid.privat(relatifPrivat(folderBaru))
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal membuat folder."
            )
        }
    }

    private fun buatItemSaf(
        lokasiInduk: LokasiBerkas,
        nama: String,
        mimeType: String,
        pesanSukses: String
    ): HasilOperasi {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasiInduk.id)
                ?: return HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi SAF tidak valid."
                )

            val parentUri = DocumentsContract.buildDocumentUriUsingTree(
                lokasiSaf.treeUri,
                lokasiSaf.documentId
            )

            val uriHasil = DocumentsContract.createDocument(
                context.contentResolver,
                parentUri,
                mimeType,
                nama
            ) ?: return HasilOperasi(
                sukses = false,
                pesan = "Gagal membuat item."
            )

            val documentId = DocumentsContract.getDocumentId(uriHasil)

            HasilOperasi(
                sukses = true,
                pesan = pesanSukses,
                lokasiHasil = LokasiBerkas(
                    id = PabrikLokasiAndroid.buatIdSaf(
                        lokasiSaf.treeUri.toString(),
                        documentId
                    ),
                    namaTampilan = nama,
                    jalurTampilan = null,
                    jenisPenyimpanan = JenisPenyimpanan.SAF_ANDROID
                )
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal membuat item pada SAF."
            )
        }
    }

    private fun gantiNamaPrivat(
        lokasi: LokasiBerkas,
        namaBaru: String
    ): HasilOperasi {
        return try {
            val file = berkasPrivat(jalurPrivatDariId(lokasi.id))

            if (!file.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Item tidak ditemukan."
                )
            }

            val target = File(file.parentFile, namaBaru)

            if (target.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Nama sudah digunakan."
                )
            }

            val sukses = file.renameTo(target)

            if (!sukses) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Gagal mengganti nama."
                )
            }

            HasilOperasi(
                sukses = true,
                pesan = "Nama diganti.",
                lokasiHasil = PabrikLokasiAndroid.privat(relatifPrivat(target))
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal mengganti nama."
            )
        }
    }

    private fun gantiNamaSaf(
        lokasi: LokasiBerkas,
        namaBaru: String
    ): HasilOperasi {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
                ?: return HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi SAF tidak valid."
                )

            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                lokasiSaf.treeUri,
                lokasiSaf.documentId
            )

            val uriHasil = DocumentsContract.renameDocument(
                context.contentResolver,
                documentUri,
                namaBaru
            ) ?: documentUri

            val documentId = try {
                DocumentsContract.getDocumentId(uriHasil)
            } catch (e: Exception) {
                lokasiSaf.documentId
            }

            HasilOperasi(
                sukses = true,
                pesan = "Nama diganti.",
                lokasiHasil = LokasiBerkas(
                    id = PabrikLokasiAndroid.buatIdSaf(
                        lokasiSaf.treeUri.toString(),
                        documentId
                    ),
                    namaTampilan = namaBaru,
                    jalurTampilan = null,
                    jenisPenyimpanan = JenisPenyimpanan.SAF_ANDROID
                )
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal mengganti nama pada SAF."
            )
        }
    }

    private fun hapusPrivat(
        lokasi: LokasiBerkas
    ): HasilOperasi {
        return try {
            if (lokasi.id == PabrikLokasiAndroid.SKEMA_PRIVAT) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Root privat tidak dapat dihapus."
                )
            }

            val file = berkasPrivat(jalurPrivatDariId(lokasi.id))

            if (!file.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Item tidak ditemukan."
                )
            }

            hapusRekursif(file)

            HasilOperasi(
                sukses = true,
                pesan = "Item dihapus."
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal menghapus item."
            )
        }
    }

    private fun hapusSaf(
        lokasi: LokasiBerkas
    ): HasilOperasi {
        return try {
            val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
                ?: return HasilOperasi(
                    sukses = false,
                    pesan = "Lokasi SAF tidak valid."
                )

            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                lokasiSaf.treeUri,
                lokasiSaf.documentId
            )

            val sukses = DocumentsContract.deleteDocument(
                context.contentResolver,
                documentUri
            )

            if (!sukses) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Gagal menghapus item pada SAF."
                )
            }

            HasilOperasi(
                sukses = true,
                pesan = "Item dihapus."
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal menghapus item pada SAF."
            )
        }
    }

    private fun salinPrivat(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi {
        return try {
            val fileSumber = berkasPrivat(jalurPrivatDariId(sumber.id))
            val indukTujuan = berkasPrivat(jalurPrivatDariId(lokasiIndukTujuan.id))

            if (!fileSumber.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Sumber tidak ditemukan."
                )
            }

            if (!indukTujuan.isDirectory) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Tujuan bukan folder."
                )
            }

            if (
                fileSumber.isDirectory &&
                indukTujuan.canonicalPath.startsWith(fileSumber.canonicalPath)
            ) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Tidak dapat menyalin folder ke dalam dirinya sendiri."
                )
            }

            val target = File(indukTujuan, fileSumber.name)

            if (target.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Nama sudah ada di tujuan."
                )
            }

            salinRekursif(fileSumber, target)

            HasilOperasi(
                sukses = true,
                pesan = "Item disalin.",
                lokasiHasil = PabrikLokasiAndroid.privat(relatifPrivat(target))
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal menyalin item."
            )
        }
    }

    private fun pindahkanPrivat(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi {
        return try {
            val fileSumber = berkasPrivat(jalurPrivatDariId(sumber.id))
            val indukTujuan = berkasPrivat(jalurPrivatDariId(lokasiIndukTujuan.id))

            if (!fileSumber.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Sumber tidak ditemukan."
                )
            }

            if (!indukTujuan.isDirectory) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Tujuan bukan folder."
                )
            }

            val target = File(indukTujuan, fileSumber.name)

            if (target.exists()) {
                return HasilOperasi(
                    sukses = false,
                    pesan = "Nama sudah ada di tujuan."
                )
            }

            val suksesRename = fileSumber.renameTo(target)

            if (suksesRename) {
                return HasilOperasi(
                    sukses = true,
                    pesan = "Item dipindahkan.",
                    lokasiHasil = PabrikLokasiAndroid.privat(relatifPrivat(target))
                )
            }

            salinRekursif(fileSumber, target)
            hapusRekursif(fileSumber)

            HasilOperasi(
                sukses = true,
                pesan = "Item dipindahkan.",
                lokasiHasil = PabrikLokasiAndroid.privat(relatifPrivat(target))
            )
        } catch (e: Exception) {
            HasilOperasi(
                sukses = false,
                pesan = e.message ?: "Gagal memindahkan item."
            )
        }
    }

    private fun ambilDokumenSaf(
        lokasiSaf: LokasiSafTerurai
    ): DocumentFile? {
        val root = DocumentFile.fromTreeUri(context, lokasiSaf.treeUri)
            ?: return null

        val treeDocumentId = try {
            DocumentsContract.getTreeDocumentId(lokasiSaf.treeUri)
        } catch (e: Exception) {
            return null
        }

        return if (lokasiSaf.documentId == treeDocumentId) {
            root
        } else {
            root.findDocumentById(lokasiSaf.documentId)
        }
    }

    private fun jalurPrivatDariId(id: String): String {
        return id
            .removePrefix(PabrikLokasiAndroid.SKEMA_PRIVAT)
            .trim('/')
    }

    private fun berkasPrivat(jalur: String): File {
        val file = if (jalur.isBlank()) {
            akarPrivat
        } else {
            File(akarPrivat, jalur)
        }

        val akarKanonik = akarPrivat.canonicalPath
        val fileKanonik = file.canonicalPath

        if (!fileKanonik.startsWith(akarKanonik)) {
            throw SecurityException("Path berada di luar penyimpanan privat aplikasi.")
        }

        return file
    }

    private fun relatifPrivat(file: File): String {
        val relatif = file.relativeTo(akarPrivat).path

        return if (relatif == ".") {
            ""
        } else {
            relatif.replace(File.separatorChar, '/')
        }
    }

    private fun hapusRekursif(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { anak ->
                hapusRekursif(anak)
            }
        }

        file.delete()
    }

    private fun salinRekursif(sumber: File, tujuan: File) {
        if (sumber.isDirectory) {
            tujuan.mkdirs()

            sumber.listFiles()?.forEach { anak ->
                salinRekursif(anak, File(tujuan, anak.name))
            }
        } else {
            sumber.inputStream().use { input ->
                tujuan.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

private fun adaBom(bytes: ByteArray): Boolean {
    if (bytes.size >= 3) {
        if (
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return true
        }
    }

    if (bytes.size >= 2) {
        if (
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xFE.toByte()
        ) {
            return true
        }

        if (
            bytes[0] == 0xFE.toByte() &&
            bytes[1] == 0xFF.toByte()
        ) {
            return true
        }
    }

    return false
}

private fun decodeKonten(bytes: ByteArray): KontenTeks {
    if (bytes.isEmpty()) {
        return KontenTeks(
            teks = "",
            encoding = "UTF-8"
        )
    }

    if (
        bytes.size >= 3 &&
        bytes[0] == 0xEF.toByte() &&
        bytes[1] == 0xBB.toByte() &&
        bytes[2] == 0xBF.toByte()
    ) {
        return KontenTeks(
            teks = String(
                bytes,
                3,
                bytes.size - 3,
                Charsets.UTF_8
            ),
            encoding = "UTF-8"
        )
    }

    if (
        bytes.size >= 2 &&
        bytes[0] == 0xFF.toByte() &&
        bytes[1] == 0xFE.toByte()
    ) {
        return KontenTeks(
            teks = String(
                bytes,
                2,
                bytes.size - 2,
                Charsets.UTF_16LE
            ),
            encoding = "UTF-16LE"
        )
    }

    if (
        bytes.size >= 2 &&
        bytes[0] == 0xFE.toByte() &&
        bytes[1] == 0xFF.toByte()
    ) {
        return KontenTeks(
            teks = String(
                bytes,
                2,
                bytes.size - 2,
                Charsets.UTF_16BE
            ),
            encoding = "UTF-16BE"
        )
    }

    return KontenTeks(
        teks = bytes.decodeToString(),
        encoding = "UTF-8"
    )
}

private fun metadataPrivat(
    lokasi: LokasiBerkas
): MetadataBerkas? {
    return try {
        val file = berkasPrivat(jalurPrivatDariId(lokasi.id))

        if (!file.exists()) return null

        MetadataBerkas(
            waktuModifikasi = file.lastModified(),
            ukuranByte = file.length(),
            bisaTulis = file.canWrite()
        )
    } catch (e: Exception) {
        null
    }
}

private fun metadataSaf(
    lokasi: LokasiBerkas
): MetadataBerkas? {
    return try {
        val lokasiSaf = PabrikLokasiAndroid.parseSaf(lokasi.id)
            ?: return null

        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            lokasiSaf.treeUri,
            lokasiSaf.documentId
        )

        val dokumen = DocumentFile.fromSingleUri(context, documentUri)
            ?: return null

        if (!dokumen.exists()) return null

        MetadataBerkas(
            waktuModifikasi = dokumen.lastModified(),
            ukuranByte = dokumen.length(),
            bisaTulis = dokumen.canWrite()
        )
    } catch (e: Exception) {
        null
    }
}
}