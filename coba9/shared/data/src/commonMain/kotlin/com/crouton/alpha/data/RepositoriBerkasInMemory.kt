package com.crouton.alpha.data

import com.crouton.alpha.domain.model.EntriBerkas
import com.crouton.alpha.domain.model.HasilDaftarBerkas
import com.crouton.alpha.domain.model.HasilSimpanBerkas
import com.crouton.alpha.domain.model.JenisEntri
import com.crouton.alpha.domain.model.KemampuanPenyimpanan
import com.crouton.alpha.domain.model.KontenBerkas
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.model.StatusSimpan
import com.crouton.alpha.domain.repository.RepositoriBerkas

class RepositoriBerkasInMemory : RepositoriBerkas {
    private val isi = mutableMapOf<String, MutableList<EntriBerkas>>()
    private val konten = mutableMapOf<String, KontenBerkas>()
    private val versi = mutableMapOf<String, Long>()

    fun letakkan(folder: LokasiBerkas, entri: EntriBerkas) {
        isi.getOrPut(folder.pengenal) { mutableListOf() }.add(entri)
    }

    fun letakkanKonten(lokasi: LokasiBerkas, teks: String) {
        konten[lokasi.pengenal] = KontenBerkas(teks, versiEksternal = 1L)
        versi[lokasi.pengenal] = 1L
    }

    override suspend fun daftarIsi(lokasi: LokasiBerkas): HasilDaftarBerkas {
        return HasilDaftarBerkas(isi[lokasi.pengenal] ?: emptyList(), lokasi, KemampuanPenyimpanan.Penuh)
    }

    override suspend fun baca(lokasi: LokasiBerkas): KontenBerkas {
        return konten[lokasi.pengenal] ?: KontenBerkas("", versiEksternal = 0L)
    }

    override suspend fun simpan(lokasi: LokasiBerkas, kontenBaru: KontenBerkas): HasilSimpanBerkas {
        val versiBaru = (versi[lokasi.pengenal] ?: 0L) + 1
        konten[lokasi.pengenal] = kontenBaru.copy(versiEksternal = versiBaru)
        versi[lokasi.pengenal] = versiBaru
        return HasilSimpanBerkas(StatusSimpan.TERSIMPAN)
    }

    override suspend fun salin(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean {
        val k = konten[sumber.pengenal] ?: return false
        konten[tujuan.pengenal] = k
        return true
    }

    override suspend fun pindah(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean {
        val k = konten.remove(sumber.pengenal) ?: return false
        konten[tujuan.pengenal] = k
        return true
    }

    override suspend fun hapus(lokasi: LokasiBerkas): Boolean = konten.remove(lokasi.pengenal) != null

    override suspend fun gantiNama(lokasi: LokasiBerkas, namaBaru: String): Boolean {
        if (namaBaru.isBlank()) return false
        val k = konten.remove(lokasi.pengenal) ?: return false
        konten[lokasi.pengenal] = k
        return true
    }

    override suspend fun buatFolder(lokasi: LokasiBerkas, nama: String): Boolean {
        letakkan(lokasi, EntriBerkas(LokasiBerkas(nama, nama, nama, true), 0, 0, JenisEntri.FOLDER))
        return true
    }

    override suspend fun buatBerkas(lokasi: LokasiBerkas, nama: String): Boolean {
        letakkanKonten(LokasiBerkas(nama, nama, nama, false), "")
        return true
    }

    override suspend fun kemampuan(lokasi: LokasiBerkas): KemampuanPenyimpanan = KemampuanPenyimpanan.Penuh

    override suspend fun versiEksternal(lokasi: LokasiBerkas): Long = versi[lokasi.pengenal] ?: 0L
}
