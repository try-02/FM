package com.crouton.alpha.domain.repository

import com.crouton.alpha.domain.model.HasilDaftarBerkas
import com.crouton.alpha.domain.model.HasilSimpanBerkas
import com.crouton.alpha.domain.model.KemampuanPenyimpanan
import com.crouton.alpha.domain.model.KontenBerkas
import com.crouton.alpha.domain.model.LokasiBerkas

interface RepositoriBerkas {
    suspend fun daftarIsi(lokasi: LokasiBerkas): HasilDaftarBerkas
    suspend fun baca(lokasi: LokasiBerkas): KontenBerkas
    suspend fun simpan(lokasi: LokasiBerkas, konten: KontenBerkas): HasilSimpanBerkas
    suspend fun salin(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean
    suspend fun pindah(sumber: LokasiBerkas, tujuan: LokasiBerkas): Boolean
    suspend fun hapus(lokasi: LokasiBerkas): Boolean
    suspend fun gantiNama(lokasi: LokasiBerkas, namaBaru: String): Boolean
    suspend fun buatFolder(lokasi: LokasiBerkas, nama: String): Boolean
    suspend fun buatBerkas(lokasi: LokasiBerkas, nama: String): Boolean
    suspend fun kemampuan(lokasi: LokasiBerkas): KemampuanPenyimpanan
    suspend fun versiEksternal(lokasi: LokasiBerkas): Long
}
