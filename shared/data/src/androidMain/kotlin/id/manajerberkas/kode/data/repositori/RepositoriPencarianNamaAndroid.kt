package id.manajerberkas.kode.data.repositori

import android.content.Context
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

    override fun cari(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianNama
    ): Flow<HasilPencarianNama> = flow {
        if (kueri.teks.isBlank()) {
            emit(
                HasilPencarianNama(
                    daftar = emptyList(),
                    selesai = true,
                    pesan = "Kueri pencarian kosong."
                )
            )

            return@flow
        }

        if (!lokasi.id.startsWith(PabrikLokasiAndroid.SKEMA_PRIVAT)) {
            emit(
                HasilPencarianNama(
                    daftar = emptyList(),
                    selesai = true,
                    pesan = "Pencarian SAF belum didukung pada tahap ini."
                )
            )

            return@flow
        }

        val jalurAwal = lokasi.id
            .removePrefix(PabrikLokasiAndroid.SKEMA_PRIVAT)
            .trim('/')

        val folderAwal = berkasPrivat(jalurAwal)

        if (!folderAwal.isDirectory) {
            emit(
                HasilPencarianNama(
                    daftar = emptyList(),
                    selesai = true,
                    pesan = "Lokasi pencarian bukan folder."
                )
            )

            return@flow
        }

        val hasil = mutableListOf<ItemBerkas>()

        cariPrivat(
            folder = folderAwal,
            jalurRelatif = jalurAwal,
            kueri = kueri,
            hasil = hasil
        ) { daftarSementara ->
            emit(
                HasilPencarianNama(
                    daftar = daftarSementara,
                    selesai = false,
                    pesan = null
                )
            )
        }

        emit(
            HasilPencarianNama(
                daftar = hasil.toList(),
                selesai = true,
                pesan = null
            )
        )
    }.flowOn(Dispatchers.IO)

    private suspend fun cariPrivat(
        folder: File,
        jalurRelatif: String,
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
                hasil.add(
                    ItemBerkas(
                        nama = anak.name,
                        lokasi = PabrikLokasiAndroid.privat(jalurAnak),
                        jenis = jenis,
                        ukuranByte = if (anak.isFile) anak.length() else null,
                        waktuModifikasi = anak.lastModified()
                    )
                )

                if (hasil.size % 50 == 0) {
                    kirim(hasil.toList())
                }
            }

            if (
                anak.isDirectory &&
                kueri.mode == ModePencarian.REKURSIF
            ) {
                cariPrivat(
                    folder = anak,
                    jalurRelatif = jalurAnak,
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
        if (kueri.hanyaFolder != null) {
            if (kueri.hanyaFolder && jenis != JenisItem.FOLDER) {
                return false
            }

            if (!kueri.hanyaFolder && jenis != JenisItem.BERKAS) {
                return false
            }
        }

        val cocokNama = if (kueri.pekaHuruf) {
            nama.contains(kueri.teks)
        } else {
            nama.contains(kueri.teks, ignoreCase = true)
        }

        if (!cocokNama) return false

        if (kueri.ekstensi != null && jenis == JenisItem.BERKAS) {
            val ekstensiFile = nama.substringAfterLast('.', "")
            val ekstensiDicari = kueri.ekstensi.removePrefix(".")

            if (!ekstensiFile.equals(ekstensiDicari, ignoreCase = true)) {
                return false
            }
        }

        if (jenis == JenisItem.BERKAS) {
            kueri.ukuranMin?.let { min ->
                if (ukuran < min) return false
            }

            kueri.ukuranMax?.let { max ->
                if (ukuran > max) return false
            }
        }

        return true
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
}