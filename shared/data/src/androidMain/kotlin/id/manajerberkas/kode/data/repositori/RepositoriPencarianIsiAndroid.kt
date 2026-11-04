package id.manajerberkas.kode.data.repositori

import android.content.Context
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.domain.kontrak.RepositoriPencarianIsi
import id.manajerberkas.kode.domain.model.HasilPencarianIsi
import id.manajerberkas.kode.domain.model.KecocokanIsiFile
import id.manajerberkas.kode.domain.model.KueriPencarianIsi
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModePencarianIsi
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RepositoriPencarianIsiAndroid(
    private val context: Context
) : RepositoriPencarianIsi {

    private val akarPrivat = context.filesDir
    private val maksHasilPerFile = 1000

    override fun cari(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianIsi
    ): Flow<HasilPencarianIsi> = flow {
        if (kueri.teks.isBlank() && !kueri.gunakanRegex) {
            emit(HasilPencarianIsi(selesai = true, pesan = "Kueri pencarian kosong."))
            return@flow
        }

        val regex = if (kueri.gunakanRegex) {
            try {
                val opsi = if (kueri.pekaHuruf) emptySet() else setOf(RegexOption.IGNORE_CASE)
                Regex(kueri.teks, opsi)
            } catch (e: Exception) {
                emit(HasilPencarianIsi(selesai = true, pesan = "Ekspresi reguler tidak valid: ${e.message}"))
                return@flow
            }
        } else null

        if (!lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT)) {
            emit(HasilPencarianIsi(selesai = true, pesan = "Pencarian isi SAF belum didukung pada tahap ini."))
            return@flow
        }

        val jalurAwal = lokasi.id.removePrefix(PabrikLokasiAndroid.SKEMA_PRIVAT).trim('/')
        val folderAwal = berkasPrivat(jalurAwal)
        if (!folderAwal.isDirectory) {
            emit(HasilPencarianIsi(selesai = true, pesan = "Lokasi pencarian bukan folder."))
            return@flow
        }

        val hasil = mutableListOf<KecocokanIsiFile>()
        var jumlahFileDiproses = 0
        var jumlahKecocokanTotal = 0

        suspend fun emitParsial(selesai: Boolean, pesan: String? = null) {
            emit(
                HasilPencarianIsi(
                    daftar = hasil.toList(),
                    selesai = selesai,
                    pesan = pesan,
                    jumlahFileDiproses = jumlahFileDiproses,
                    jumlahKecocokanTotal = jumlahKecocokanTotal
                )
            )
        }

        suspend fun prosesFolder(folder: File, jalurRelatif: String) {
            currentCoroutineContext().ensureActive()
            val daftarAnak = folder.listFiles() ?: return
            for (anak in daftarAnak) {
                currentCoroutineContext().ensureActive()
                val jalurAnak = if (jalurRelatif.isBlank()) anak.name else "${jalurRelatif.trimEnd('/')}/${anak.name}"

                if (anak.isDirectory) {
                    if (kueri.mode == ModePencarianIsi.REKURSIF) {
                        prosesFolder(anak, jalurAnak)
                    }
                } else if (anak.isFile) {
                    if (kueri.ukuranMin != null && anak.length() < kueri.ukuranMin) continue
                    if (kueri.ukuranMax != null && anak.length() > kueri.ukuranMax) continue
                    if (kueri.ekstensi != null) {
                        val ekstensiFile = anak.name.substringAfterLast('.', "")
                        val target = kueri.ekstensi.removePrefix(".")
                        if (!ekstensiFile.equals(target, ignoreCase = true)) continue
                    }
                    if (anak.length() > kueri.batasUkuranPerFile) {
                        jumlahFileDiproses++
                        continue
                    }

                    val kecocokan = cariDalamFile(anak, jalurAnak, kueri, regex)
                    jumlahFileDiproses++
                    if (kecocokan.isNotEmpty()) {
                        hasil.addAll(kecocokan)
                        jumlahKecocokanTotal += kecocokan.size
                        emitParsial(selesai = false)
                    }
                }
            }
        }

        prosesFolder(folderAwal, jalurAwal)
        emitParsial(selesai = true)
    }.flowOn(Dispatchers.IO)

    private fun cariDalamFile(
        file: File,
        jalurRelatif: String,
        kueri: KueriPencarianIsi,
        regex: Regex?
    ): List<KecocokanIsiFile> {
        val hasil = mutableListOf<KecocokanIsiFile>()

        file.inputStream().use { input ->
            val header = ByteArray(8192)
            val dibaca = input.read(header)
            if (dibaca > 0) {
                for (i in 0 until dibaca) {
                    if (header[i].toInt() == 0) return emptyList()
                }
            }
        }

        val lokasi = PabrikLokasiAndroid.privat(jalurRelatif)
        val namaFile = file.name
        var nomorBaris = 0

        try {
            file.bufferedReader().useLines { lines ->
                for (baris in lines) {
                    nomorBaris++
                    val cocok = if (regex != null) {
                        regex.containsMatchIn(baris)
                    } else if (kueri.kataUtuh) {
                        cocokKataUtuh(baris, kueri.teks, kueri.pekaHuruf)
                    } else {
                        if (kueri.pekaHuruf) baris.contains(kueri.teks)
                        else baris.contains(kueri.teks, ignoreCase = true)
                    }

                    if (cocok) {
                        val potongan = buatPotongan(baris, kueri.teks, regex)
                        hasil.add(
                            KecocokanIsiFile(
                                lokasi = lokasi,
                                namaFile = namaFile,
                                nomorBaris = nomorBaris,
                                potonganTeks = potongan
                            )
                        )
                        if (hasil.size >= maksHasilPerFile) return@useLines
                    }
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }

        return hasil
    }

    private fun cocokKataUtuh(teks: String, kueri: String, pekaHuruf: Boolean): Boolean {
        val pola = "\\b" + Regex.escape(kueri) + "\\b"
        val regex = Regex(pola, if (pekaHuruf) emptySet() else setOf(RegexOption.IGNORE_CASE))
        return regex.containsMatchIn(teks)
    }

    private fun buatPotongan(baris: String, kueri: String, regex: Regex?): String {
        val maks = 200
        if (baris.length <= maks) return baris

        val indeks = if (regex != null) {
            regex.find(baris)?.range?.first ?: 0
        } else {
            val idx = baris.indexOf(kueri, ignoreCase = true)
            if (idx < 0) 0 else idx
        }

        val mulai = (indeks - maks / 2).coerceAtLeast(0)
        val akhir = (mulai + maks).coerceAtMost(baris.length)
        return baris.substring(mulai, akhir)
    }

    private fun berkasPrivat(jalur: String): File {
        val file = if (jalur.isBlank()) akarPrivat else File(akarPrivat, jalur)
        val akarKanonik = akarPrivat.canonicalPath
        val fileKanonik = file.canonicalPath
        if (!fileKanonik.startsWith(akarKanonik)) {
            throw SecurityException("Path berada di luar penyimpanan privat aplikasi.")
        }
        return file
    }
}