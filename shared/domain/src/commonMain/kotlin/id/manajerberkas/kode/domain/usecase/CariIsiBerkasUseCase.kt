package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriPencarianIsi
import id.manajerberkas.kode.domain.model.HasilPencarianIsi
import id.manajerberkas.kode.domain.model.KueriPencarianIsi
import id.manajerberkas.kode.domain.model.LokasiBerkas
import kotlinx.coroutines.flow.Flow

class CariIsiBerkasUseCase(
    private val repositori: RepositoriPencarianIsi
) {
    operator fun invoke(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianIsi
    ): Flow<HasilPencarianIsi> {
        return repositori.cari(lokasi, kueri)
    }
}