package com.crouton.alpha.data.ios

import com.crouton.alpha.domain.model.EntriBerkas
import com.crouton.alpha.domain.model.HasilDaftarBerkas
import com.crouton.alpha.domain.model.HasilSimpanBerkas
import com.crouton.alpha.domain.model.JenisEntri
import com.crouton.alpha.domain.model.KemampuanPenyimpanan
import com.crouton.alpha.domain.model.KontenBerkas
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.model.StatusSimpan
import com.crouton.alpha.domain.repository.RepositoriBerkas
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFileEncoding
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFileAtomically

class RepositoriBerkasIos : RepositoriBerkas {
    private val manajer = NSFileManager.defaultManager

    override suspend fun daftarIsi(lokasi: LokasiBerkas): HasilDaftarBerkas {
        val isi = (manajer.contentsOfDirectoryAtPath(lokasi.uriAtauPath) as? List<*>)?.map { it.toString() } ?: emptyList()
        val entri = isi.map { nama ->
            val path = lokasi.uriAtauPath + "/" + nama
            val adalahFolder = manajer.fileExistsAtPath(path)
            EntriBerkas(LokasiBerkas(path, nama, path, adalahFolder), 0, 0, if (adalahFolder) JenisEntri.FOLDER else JenisEntri.BERKAS)
        }
        return HasilDaftarBerkas(entri, lokasi, kemampuan(lokasi))
    }

    override suspend fun baca(lokasi: LokasiBerkas): KontenBerkas {
        val teks = NSString.stringWithContentsOfFileEncoding(lokasi.uriAtauPath, NSUTF8StringEncoding) ?: ""
        return KontenBerkas(teks, versiEksternal = 0L)
    }

    override suspend fun simpan(lokasi: LokasiBerkas, konten: KontenBerkas): HasilSimpanBerkas {
        return try {
            val berhasil = (konten.teks as NSString).writeToFileAtomically(lokasi.uriAtauPath, true, NSUTF8StringEncoding, null)
            if (berhasil) HasilSimpanBerkas(StatusSimpan.TERSIMPAN) else HasilSimpanBerkas(StatusSimpan.GAGAL, "Gagal menulis")
        } catch (e: Exception) {
            HasilSimpanBerkas(StatusSimpan.GAGAL, e.message ?: "Gagal menyimpan")
        }
    }

    override suspend fun salin(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean = runCatching {
        manajer.copyItemAtPath(sumber.uriAtauPath, tujuan.uriAtauPath, null); true
    }.getOrDefault(false)

    override suspend fun pindah(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean = runCatching {
        manajer.moveItemAtPath(sumber.uriAtauPath, tujuan.uriAtauPath, null); true
    }.getOrDefault(false)

    override suspend fun hapus(lokasi: LokasiBerkas): Boolean = runCatching {
        manajer.removeItemAtPath(lokasi.uriAtauPath, null); true
    }.getOrDefault(false)

    override suspend fun gantiNama(lokasi: LokasiBerkas, namaBaru: String): Boolean {
        val tujuan = lokasi.uriAtauPath.substringBeforeLast("/") + "/" + namaBaru
        return runCatching { manajer.moveItemAtPath(lokasi.uriAtauPath, tujuan, null); true }.getOrDefault(false)
    }

    override suspend fun buatFolder(lokasi: LokasiBerkas, nama: String): Boolean = runCatching {
        manajer.createDirectoryAtPathWithIntermediateDirectoriesAttributes(lokasi.uriAtauPath + "/" + nama, true, null, null); true
    }.getOrDefault(false)

    override suspend fun buatBerkas(lokasi: LokasiBerkas, nama: String): Boolean = runCatching {
        ("" as NSString).writeToFileAtomically(lokasi.uriAtauPath + "/" + nama, true, NSUTF8StringEncoding, null)
    }.getOrDefault(false)

    override suspend fun kemampuan(lokasi: LokasiBerkas): KemampuanPenyimpanan = KemampuanPenyimpanan.Penuh

    override suspend fun versiEksternal(lokasi: LokasiBerkas): Long = 0L
}
