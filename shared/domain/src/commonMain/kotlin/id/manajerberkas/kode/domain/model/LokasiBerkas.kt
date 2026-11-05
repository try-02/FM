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
    val id: String, // Identifier unik lintas platform (URI string / Document ID)
    val namaTampilan: String,
    val jalurTampilan: String?, // Path POSIX jika tersedia, null untuk URI/Sandbox
    val jenisPenyimpanan: JenisPenyimpanan
)