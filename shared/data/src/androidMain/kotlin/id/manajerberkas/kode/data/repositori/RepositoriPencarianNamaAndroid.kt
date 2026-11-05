package id.manajerberkas.kode.data.repositori

import android.content.Context
import android.os.Environment
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.domain.kontrak.RepositoriPencarianNama
import id.manajerberkas.kode.domain.model.HasilPencarianNama
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.JenisItem
import id.manajerberkas.kode.domain.model.KueriPencarianNama
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModePencarian
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RepositoriPencarianNamaAndroid(
    private val context: Context
) : RepositoriPencarianNama {
    private val akarPrivat = context.filesDir
    private val akarEksternal: File
        get() = Environment.getExternalStorageDirectory()

    override fun cari(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianNama
    ): Flow<HasilPencarianNama> = flow {
        if (kueri.teks.isBlank()) {
            emit(HasilPencarianNama(daftar = emptyList(), selesai = true, pesan = "Kueri pencarian kosong."))
            return@flow
        }

        val (akar, skema) = when {
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT) -> akarPrivat to PabrikLokasiAndroid.SKEMA_PRIVAT
            lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_EKSTERNAL) -> akarEksternal to PabrikLokasiAndroid.SKEMA_EKSTERNAL
            else -> {
                emit(HasilPencarianNama(daftar = emptyList(), selesai = true, pesan = "Pencarian SAF belum didukung pada tahap ini."))
                return@flow
            }
        }

        val jalurAwal = lokasi.id.removePrefix(skema).trim('/')
        val folderAwal = berkasDariRoot(jalurAwal, akar)
        if (!folderAwal.isDirectory) {
            emit(HasilPencarianNama(daftar = emptyList(), selesai = true, pesan = "Lokasi pencarian bukan folder."))
            return@flow
        }

        val hasil = mutableListOf<ItemBerkas>()
        cariDiFolder(
            folder = folderAwal,
            jalurRelatif = jalurAwal,
            akar = akar,
            skema = skema,
            kueri = kueri,
            hasil = hasil
        ) { daftarSementara ->
            emit(HasilPencarianNama(daftar = daftarSementara, selesai = false, pesan = null))
        }
        emit(HasilPencarianNama(daftar = hasil.toList(), selesai = true, pesan = null))
    }.flowOn(Dispatchers.IO)

    private suspend fun cariDiFolder(
        folder: File,
        jalurRelatif: String,
        akar: File,
        skema: String,
        kueri: KueriPencarianNama,
        hasil: MutableList<ItemBerkas>,
        kirim: suspend (List<ItemBerkas>) -> Unit
    ) {
        currentCoroutineContext().ensureActive()
        val daftarAnak = folder.listFiles() ?: return
        for (anak in daftarAnak) {
            currentCoroutineContext().ensureActive()
            val jalurAnak = if (jalurRelatif.isBlank()) {
                anak.name
            } else {
                "${jalurRelatif.trimEnd('/')}/${anak.name}"
            }
            val jenis = if (anak.isDirectory) {
                JenisItem.FOLDER
            } else {
                JenisItem.BERKAS
            }
            if (cocokDenganKueri(anak.name, jenis, anak.length(), kueri)) {
                val lokasiAnak = if (skema == PabrikLokasiAndroid.SKEMA_PRIVAT) {
                    PabrikLokasiAndroid.privat(jalurAnak)
                } else {
                    PabrikLokasiAndroid.eksternal(jalurAnak)
                }
                hasil.add(
                    ItemBerkas(
                        nama = anak.name,
                        lokasi = lokasiAnak,
                        jenis = jenis,
                        ukuranByte = if (anak.isFile) anak.length() else null,
                        waktuModifikasi = anak.lastModified()
                    )
                )
                if (hasil.size % 50 == 0) {
                    kirim(hasil.toList())
                }
            }
            if (anak.isDirectory && kueri.mode == ModePencarian.REKURSIF) {
                cariDiFolder(
                    folder = anak,
                    jalurRelatif = jalurAnak,
                    akar = akar,
                    skema = skema,
                    kueri = kueri,
                    hasil = hasil,
                    kirim = kirim
                )
            }
        }
    }

    private fun cocokDenganKueri(
        nama: String,
        jenis: JenisItem,
        ukuran: Long,
        kueri: KueriPencarianNama
    ): Boolean {
        val hanyaFolder = kueri.hanyaFolder
        if (hanyaFolder != null) {
            if (hanyaFolder && jenis != JenisItem.FOLDER) return false
            if (!hanyaFolder && jenis != JenisItem.BERKAS) return false
        }
        val cocokNama = if (kueri.pekaHuruf) {
            nama.contains(kueri.teks)
        } else {
            nama.contains(kueri.teks, ignoreCase = true)
        }
        if (!cocokNama) return false
        val ekstensiDicari = kueri.ekstensi
        if (ekstensiDicari != null && jenis == JenisItem.BERKAS) {
            val ekstensiFile = nama.substringAfterLast('.', "")
            val target = ekstensiDicari.removePrefix(".")
            if (!ekstensiFile.equals(target, ignoreCase = true)) return false
        }
        if (jenis == JenisItem.BERKAS) {
            kueri.ukuranMin?.let { min -> if (ukuran < min) return false }
            kueri.ukuranMax?.let { max -> if (ukuran > max) return false }
        }
        return true
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