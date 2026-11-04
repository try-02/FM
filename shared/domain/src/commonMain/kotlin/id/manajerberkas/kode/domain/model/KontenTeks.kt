package id.manajerberkas.kode.domain.model

data class KontenTeks(
    val teks: String,
    val encoding: String = "UTF-8"
)

data class HasilBacaTeks(
    val sukses: Boolean,
    val pesan: String? = null,
    val konten: KontenTeks? = null,
    val hanyaBaca: Boolean = false,
    val terlaluBesar: Boolean = false,
    val ukuranByte: Long? = null
)

data class HasilSimpanTeks(
    val sukses: Boolean,
    val pesan: String? = null
)