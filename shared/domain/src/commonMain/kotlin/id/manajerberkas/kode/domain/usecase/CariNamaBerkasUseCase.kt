package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.kontrak.RepositoriPencarianNama
import id.manajerberkas.kode.domain.model.HasilPencarianNama
import id.manajerberkas.kode.domain.model.KueriPencarianNama
import id.manajerberkas.kode.domain.model.LokasiBerkas
import kotlinx.coroutines.flow.Flow
class CariNamaBerkasUseCase(
    private val repositori: RepositoriPencarianNama
) {
    operator fun invoke(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianNama
    ): Flow<HasilPencarianNama> {
        return repositori.cari(lokasi, kueri)
    }
}
