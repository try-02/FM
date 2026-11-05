package id.manajerberkas.kode.domain.model
enum class JenisPenyimpanan {
    PRIVAT_APLIKASI,
    SAF_ANDROID,
    DOKUMEN_IOS,
    ARSIP,
    TIDAK_DIKENAL,
    EKSTERNAL
}
data class LokasiBerkas(
    val id: String, 
    val namaTampilan: String,
    val jalurTampilan: String?, 
    val jenisPenyimpanan: JenisPenyimpanan
)
