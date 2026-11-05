package id.manajerberkas.kode.domain.model
enum class ModePencarianIsi {
    FOLDER_SAAT_INI,
    REKURSIF
}
data class KueriPencarianIsi(
    val teks: String,
    val mode: ModePencarianIsi = ModePencarianIsi.REKURSIF,
    val pekaHuruf: Boolean = false,
    val kataUtuh: Boolean = false,
    val gunakanRegex: Boolean = false,
    val ekstensi: String? = null,
    val ukuranMin: Long? = null,
    val ukuranMax: Long? = null,
    val batasUkuranPerFile: Long = 5 * 1024 * 1024
)
data class KecocokanIsiFile(
    val lokasi: LokasiBerkas,
    val namaFile: String,
    val nomorBaris: Int,
    val potonganTeks: String
)
data class HasilPencarianIsi(
    val daftar: List<KecocokanIsiFile> = emptyList(),
    val selesai: Boolean = false,
    val pesan: String? = null,
    val jumlahFileDiproses: Int = 0,
    val jumlahKecocokanTotal: Int = 0
)
