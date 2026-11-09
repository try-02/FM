package com.crouton.alpha.domain.model

data class KontenBerkas(
    val teks: String,
    val encodingTerdeteksi: String = "UTF-8",
    val akhirBaris: String = "LF",
    val adalahBiner: Boolean = false,
    val versiEksternal: Long = 0L
)

data class HasilDaftarBerkas(
    val isi: List<EntriBerkas>,
    val lokasi: LokasiBerkas,
    val kemampuan: KemampuanPenyimpanan
)

enum class StatusSimpan { TERSIMPAN, GAGAL, KONFLIK_EKSTERNAL, HANYA_BACA }

data class HasilSimpanBerkas(
    val status: StatusSimpan,
    val pesan: String = ""
)
