package id.manajerberkas.kode.domain.kontrak
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MarkahLokasi
interface RepositoriMarkah {
    suspend fun ambilSemua(): List<MarkahLokasi>
    suspend fun simpan(markah: MarkahLokasi)
    suspend fun hapus(idLokasi: String)
    suspend fun ada(idLokasi: String): Boolean
}
interface RepositoriBeranda {
    suspend fun ambil(): LokasiBerkas?
    suspend fun simpan(lokasi: LokasiBerkas)
    suspend fun hapus()
}
