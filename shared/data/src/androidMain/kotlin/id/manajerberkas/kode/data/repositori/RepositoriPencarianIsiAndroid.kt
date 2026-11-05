package id.manajerberkas.kode.data.repositori

import android.content.Context
import android.os.Environment
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
    private val akarEksternal: File
        get() = Environment.getExternalStorageDirectory()
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
        } else if (kueri.kataUtuh) {
            // Kompilasi regex kata utuh sekali di awal, bukan per baris
            val pola = "\\b" + Regex.escape(kueri.teks) + "\\b"
            Regex(pola, if (kueri.pekaHuruf) emptySet() else setOf(RegexOption.IGNORE_CASE))
        } else null

        val (akar, skema) = when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> akarPrivat to PabrikLokasiAndroid.SKEMA_PRIVAT
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> akarEksternal to PabrikLokasiAndroid.SKEMA_EKSTERNAL
            else -> {
                emit(HasilPencarianIsi(selesai = true, pesan = "Pencarian isi SAF belum didukung pada tahap ini."))
                return@flow
            }
        }

        val jalurAwal = lokasi.id.removePrefix(skema).trim('/')
        val folderAwal = berkasDariRoot(jalurAwal, akar)
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
                    val ukuranMin = kueri.ukuranMin
                    val ukuranMax = kueri.ukuranMax
                    val ekstensiTarget = kueri.ekstensi
                    if (ukuranMin != null && anak.length() < ukuranMin) continue
                    if (ukuranMax != null && anak.length() > ukuranMax) continue
                    if (ekstensiTarget != null) {
                        val ekstensiFile = anak.name.substringAfterLast('.', "")
                        val target = ekstensiTarget.removePrefix(".")
                        if (!ekstensiFile.equals(target, ignoreCase = true)) continue
                    }
                    if (anak.length() > kueri.batasUkuranPerFile) {
                        jumlahFileDiproses++
                        continue
                    }
                    val kecocokan = cariDalamFile(anak, jalurAnak, skema, kueri, regex)
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
        skema: String,
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

        val lokasi = if (skema == PabrikLokasiAndroid.SKEMA_PRIVAT) {
            PabrikLokasiAndroid.privat(jalurRelatif)
        } else {
            PabrikLokasiAndroid.eksternal(jalurRelatif)
        }

        val namaFile = file.name
        var nomorBaris = 0
        try {
            file.bufferedReader().useLines { lines ->
                for (baris in lines) {
                    nomorBaris++
                    val cocok = if (regex != null) {
                        regex.containsMatchIn(baris)
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
        } catch (_: Exception) {
            return emptyList()
        }
        return hasil
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

    private fun berkasDariRoot(jalurRelatif: String, akar: File): File {
        val file = if (jalurRelatif.isBlank()) akar else File(akar, jalurRelatif)
        val akarKanonik = akar.canonicalPath
        val fileKanonik = file.canonicalPath
        if (!(fileKanonik == akarKanonik || fileKanonik.startsWith(akarKanonik + File.separator))) {
            throw SecurityException("Path berada di luar penyimpanan yang diizinkan.")
        }
        return file
    }
}