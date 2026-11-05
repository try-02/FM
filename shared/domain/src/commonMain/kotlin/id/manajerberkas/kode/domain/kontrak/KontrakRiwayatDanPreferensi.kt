package id.manajerberkas.kode.domain.kontrak
interface RepositoriRiwayatPencarian {
    suspend fun ambilSemua(batas: Int): List<String>
    suspend fun simpan(nilai: String, batas: Int)
    suspend fun hapusSemua()
}
interface RepositoriPreferensiEditor {
    suspend fun ambilBoolean(
        kunci: String,
        default: Boolean
    ): Boolean
    suspend fun simpanBoolean(
        kunci: String,
        nilai: Boolean
    )
}
