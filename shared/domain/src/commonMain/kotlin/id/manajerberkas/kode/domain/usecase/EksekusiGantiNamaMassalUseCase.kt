package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.HasilOperasi
import id.manajerberkas.kode.domain.model.HasilPratinjauGantiNama

class EksekusiGantiNamaMassalUseCase(
    private val repositori: RepositoriBerkas
) {
    suspend operator fun invoke(
        pratinjau: List<HasilPratinjauGantiNama>
    ): List<HasilOperasi> {
        val hasil = mutableListOf<HasilOperasi>()

        for (item in pratinjau) {
            if (!item.valid || item.namaBaru == null) {
                hasil.add(
                    HasilOperasi(
                        sukses = false,
                        pesan = item.pesanError ?: "Item tidak valid."
                    )
                )
                continue
            }

            val operasi = repositori.gantiNama(item.lokasi, item.namaBaru)
            hasil.add(operasi)
        }

        return hasil
    }
}