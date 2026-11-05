package id.manajerberkas.kode.domain.kontrak
import id.manajerberkas.kode.domain.model.HasilPencarianNama
import id.manajerberkas.kode.domain.model.KueriPencarianNama
import id.manajerberkas.kode.domain.model.LokasiBerkas
import kotlinx.coroutines.flow.Flow
interface RepositoriPencarianNama {
    fun cari(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianNama
    ): Flow<HasilPencarianNama>
}
