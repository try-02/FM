package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.BlokPerubahan
import id.manajerberkas.kode.domain.model.BarisDiffBerdampingan
import id.manajerberkas.kode.domain.model.HasilDiff
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModeDiff

data class DiffState(
    val memuat: Boolean = false,
    val lokasiA: LokasiBerkas? = null,
    val lokasiB: LokasiBerkas? = null,
    val hasil: HasilDiff? = null,
    val barisBerdampingan: List<BarisDiffBerdampingan> = emptyList(),
    val mode: ModeDiff = ModeDiff.UNIFIED,
    val pesan: String? = null,
    val blokPerubahan: List<BlokPerubahan> = emptyList(),
    val indeksBlokAktif: Int = -1
) {
    val indeksBarisAktif: Int?
        get() = blokPerubahan.getOrNull(indeksBlokAktif)?.indeksAwal
}