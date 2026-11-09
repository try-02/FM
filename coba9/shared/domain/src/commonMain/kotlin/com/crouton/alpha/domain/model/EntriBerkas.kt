package com.crouton.alpha.domain.model

enum class JenisEntri { BERKAS, FOLDER, ARSIP, TIDAK_DIKENAL }

data class EntriBerkas(
    val lokasi: LokasiBerkas,
    val ukuranByte: Long,
    val waktuModifikasiEpoch: Long,
    val jenis: JenisEntri,
    val tersembunyi: Boolean = false,
    val lokasiArsip: String? = null
)
