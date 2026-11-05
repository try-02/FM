package id.manajerberkas.kode.domain.kontrak
import id.manajerberkas.kode.domain.model.HasilPencarianIsi
import id.manajerberkas.kode.domain.model.KueriPencarianIsi
import id.manajerberkas.kode.domain.model.LokasiBerkas
import kotlinx.coroutines.flow.Flow
interface RepositoriPencarianIsi {
    fun cari(
        lokasi: LokasiBerkas,
        kueri: KueriPencarianIsi
    ): Flow<HasilPencarianIsi>
}
