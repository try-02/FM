package id.manajerberkas.kode.data.repositori
import id.manajerberkas.kode.data.lokal.DatabaseAplikasi
import id.manajerberkas.kode.domain.kontrak.RepositoriRiwayatPencarian
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class RepositoriRiwayatPencarianSql(
    private val database: DatabaseAplikasi
) : RepositoriRiwayatPencarian {
    override suspend fun ambilSemua(
        batas: Int
    ): List<String> = withContext(Dispatchers.Default) {
        database.riwayatPencarianQueries
            .pilihSemua(batas.toLong())
            .executeAsList()
    }
    override suspend fun simpan(
        nilai: String,
        batas: Int
    ): Unit = withContext(Dispatchers.Default) {
        if (nilai.isBlank()) return@withContext
        database.riwayatPencarianQueries.hapusNilai(nilai)
        database.riwayatPencarianQueries.simpan(nilai)
        database.riwayatPencarianQueries.hapusKelebihan(batas.toLong())
    }
    override suspend fun hapusSemua(): Unit = withContext(Dispatchers.Default) {
        database.riwayatPencarianQueries.hapusSemua()
    }
}
