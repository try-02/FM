package id.manajerberkas.kode.domain.model
enum class JenisMarkah {
    BERKAS,
    FOLDER,
    ARSIP,
    LOKASI_KERJA
}
data class MarkahLokasi(
    val lokasi: LokasiBerkas,
    val jenis: JenisMarkah
)
