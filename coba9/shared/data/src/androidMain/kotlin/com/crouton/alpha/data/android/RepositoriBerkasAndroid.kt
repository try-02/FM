package com.crouton.alpha.data.android

import com.crouton.alpha.domain.model.EntriBerkas
import com.crouton.alpha.domain.model.HasilDaftarBerkas
import com.crouton.alpha.domain.model.HasilSimpanBerkas
import com.crouton.alpha.domain.model.JenisEntri
import com.crouton.alpha.domain.model.KemampuanPenyimpanan
import com.crouton.alpha.domain.model.KontenBerkas
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.model.StatusSimpan
import com.crouton.alpha.domain.repository.RepositoriBerkas
import java.io.File
import java.io.FileInputStream

class RepositoriBerkasAndroid(private val direktoriDasar: File) : RepositoriBerkas {
    private fun berkasDari(lokasi: LokasiBerkas): File = File(direktoriDasar, lokasi.uriAtauPath)

    override suspend fun daftarIsi(lokasi: LokasiBerkas): HasilDaftarBerkas {
        val folder = berkasDari(lokasi)
        val anak = folder.listFiles() ?: emptyArray()
        val entri = anak.map { berkas ->
            EntriBerkas(
                LokasiBerkas(berkas.path, berkas.name, berkas.path, berkas.isDirectory),
                berkas.length(), berkas.lastModified(),
                if (berkas.isDirectory) JenisEntri.FOLDER else JenisEntri.BERKAS
            )
        }
        return HasilDaftarBerkas(entri, lokasi, kemampuan(lokasi))
    }

    override suspend fun baca(lokasi: LokasiBerkas): KontenBerkas {
        val berkas = berkasDari(lokasi)
        val bytes = FileInputStream(berkas).use { it.readBytes() }
        val teks = bytes.toString(Charsets.UTF_8)
        return KontenBerkas(teks, versiEksternal = berkas.lastModified())
    }

    override suspend fun simpan(lokasi: LokasiBerkas, konten: KontenBerkas): HasilSimpanBerkas {
        return try {
            val tujuan = berkasDari(lokasi)
            val sementara = File(tujuan.parent ?: direktoriDasar.path, tujuan.name + ".sementara")
            sementara.writeText(konten.teks)
            if (!sementara.renameTo(tujuan)) {
                tujuan.writeText(konten.teks)
                sementara.delete()
            }
            HasilSimpanBerkas(StatusSimpan.TERSIMPAN)
        } catch (e: Exception) {
            HasilSimpanBerkas(StatusSimpan.GAGAL, e.message ?: "Gagal menyimpan")
        }
    }

    override suspend fun salin(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean =
        runCatching { berkasDari(sumber).copyTo(berkasDari(tujuan), overwrite = false); true }.getOrDefault(false)

    override suspend fun pindah(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean =
        runCatching { berkasDari(sumber).renameTo(berkasDari(tujuan)) }.getOrDefault(false)

    override suspend fun hapus(lokasi: LokasiBerkas): Boolean = berkasDari(lokasi).delete()

    override suspend fun gantiNama(lokasi: LokasiBerkas, namaBaru: String): Boolean =
        runCatching { berkasDari(lokasi).renameTo(File(berkasDari(lokasi).parent, namaBaru)) }.getOrDefault(false)

    override suspend fun buatFolder(lokasi: LokasiBerkas, nama: String): Boolean =
        File(berkasDari(lokasi), nama).mkdir()

    override suspend fun buatBerkas(lokasi: LokasiBerkas, nama: String): Boolean =
        runCatching { File(berkasDari(lokasi), nama).createNewFile() }.getOrDefault(false)

    override suspend fun kemampuan(lokasi: LokasiBerkas): KemampuanPenyimpanan = KemampuanPenyimpanan.Penuh

    override suspend fun versiEksternal(lokasi: LokasiBerkas): Long = berkasDari(lokasi).lastModified()
}
