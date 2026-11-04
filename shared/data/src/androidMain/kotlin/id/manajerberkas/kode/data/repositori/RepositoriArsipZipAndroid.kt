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

class RepositoriArsipZipAndroid(
    private val context: Context
) : RepositoriArsip {

    private val akarPrivat = context.filesDir
    private val maksKedalaman = 10

    override suspend fun daftarIsi(
        lokasiArsip: LokasiBerkas
    ): HasilDaftarArsip = withContext(Dispatchers.IO) {
        try {
            val file = berkasDariLokasi(lokasiArsip)
                ?: return@withContext HasilDaftarArsip(false, "File arsip tidak ditemukan.")

            if (!file.exists() || !file.isFile) {
                return@withContext HasilDaftarArsip(false, "File arsip tidak valid.")
            }

            val items = mutableListOf<ItemArsip>()
            val folderTerlihat = mutableSetOf<String>()

            ZipFile(file).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val jalur = entry.name

                    // Kumpulkan folder implisit
                    val bagian = jalur.split("/")
                    for (i in 1 until bagian.size) {
                        val folderPath = bagian.subList(0, i).joinToString("/") + "/"
                        if (folderPath != "/" && folderTerlihat.add(folderPath)) {
                            items.add(
                                ItemArsip(
                                    jalur = folderPath,
                                    nama = bagian[i - 1],
                                    jenis = JenisItemArsip.FOLDER,
                                    ukuran = 0,
                                    ukuranTerkompresi = null,
                                    waktuModifikasi = null
                                )
                            )
                        }
                    }

                    if (!entry.isDirectory) {
                        items.add(
                            ItemArsip(
                                jalur = jalur,
                                nama = jalur.substringAfterLast('/'),
                                jenis = JenisItemArsip.BERKAS,
                                ukuran = entry.size,
                                ukuranTerkompresi = entry.compressedSize,
                                waktuModifikasi = entry.time
                            )
                        )
                    }
                }
            }

            val kemampuan = KemampuanFormatArsip(
                format = "zip",
                bisaBaca = true,
                bisaEkstrak = true,
                bisaBuat = false,
                bisaUbahIsi = false,
                dukungEnkripsi = false
            )

            HasilDaftarArsip(
                sukses = true,
                daftar = items.sortedWith(
                    compareByDescending<ItemArsip> { it.jenis }
                        .thenBy { it.jalur.lowercase() }
                ),
                kemampuan = kemampuan
            )
        } catch (e: Exception) {
            HasilDaftarArsip(false, "Gagal membaca arsip: ${e.message}")
        }
    }

    override suspend fun ekstrakItem(
        lokasiArsip: LokasiBerkas,
        jalurItem: String,
        lokasiTujuan: LokasiBerkas
    ): HasilEkstraksi = withContext(Dispatchers.IO) {
        try {
            val fileArsip = berkasDariLokasi(lokasiArsip)
                ?: return@withContext HasilEkstraksi(false, "File arsip tidak ditemukan.")
            val folderTujuan = berkasDariLokasi(lokasiTujuan)
                ?: return@withContext HasilEkstraksi(false, "Folder tujuan tidak ditemukan.")

            if (!folderTujuan.isDirectory) {
                return@withContext HasilEkstraksi(false, "Tujuan bukan folder.")
            }

            var berhasil = 0
            var gagal = 0

            ZipFile(fileArsip).use { zip ->
                val entry = zip.getEntry(jalurItem)
                if (entry == null) {
                    return@withContext HasilEkstraksi(false, "Item tidak ditemukan dalam arsip.")
                }

                if (entry.isDirectory) {
                    val targetDir = File(folderTujuan, entry.name)
                    if (validasiZipSlip(targetDir, folderTujuan)) {
                        targetDir.mkdirs()
                        berhasil++
                    } else {
                        gagal++
                    }
                } else {
                    val targetFile = File(folderTujuan, entry.name)
                    if (validasiZipSlip(targetFile, folderTujuan)) {
                        targetFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        berhasil++
                    } else {
                        gagal++
                    }
                }
            }

            HasilEkstraksi(
                sukses = gagal == 0,
                pesan = if (gagal == 0) "Ekstraksi berhasil." else "Sebagian item gagal diekstrak.",
                jumlahBerhasil = berhasil,
                jumlahGagal = gagal
            )
        } catch (e: Exception) {
            HasilEkstraksi(false, "Gagal mengekstrak: ${e.message}")
        }
    }

    override suspend fun ekstrakSemua(
        lokasiArsip: LokasiBerkas,
        lokasiTujuan: LokasiBerkas
    ): HasilEkstraksi = withContext(Dispatchers.IO) {
        try {
            val fileArsip = berkasDariLokasi(lokasiArsip)
                ?: return@withContext HasilEkstraksi(false, "File arsip tidak ditemukan.")
            val folderTujuan = berkasDariLokasi(lokasiTujuan)
                ?: return@withContext HasilEkstraksi(false, "Folder tujuan tidak ditemukan.")

            if (!folderTujuan.isDirectory) {
                return@withContext HasilEkstraksi(false, "Tujuan bukan folder.")
            }

            var berhasil = 0
            var gagal = 0

            ZipFile(fileArsip).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val targetFile = File(folderTujuan, entry.name)

                    if (!validasiZipSlip(targetFile, folderTujuan)) {
                        gagal++
                        continue
                    }

                    try {
                        if (entry.isDirectory) {
                            targetFile.mkdirs()
                        } else {
                            targetFile.parentFile?.mkdirs()
                            zip.getInputStream(entry).use { input ->
                                targetFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        berhasil++
                    } catch (e: Exception) {
                        gagal++
                    }
                }
            }

            HasilEkstraksi(
                sukses = gagal == 0,
                pesan = if (gagal == 0) "Ekstraksi selesai." else "Sebagian item gagal diekstrak.",
                jumlahBerhasil = berhasil,
                jumlahGagal = gagal
            )
        } catch (e: Exception) {
            HasilEkstraksi(false, "Gagal mengekstrak arsip: ${e.message}")
        }
    }

    private fun validasiZipSlip(target: File, direktoriTujuan: File): Boolean {
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