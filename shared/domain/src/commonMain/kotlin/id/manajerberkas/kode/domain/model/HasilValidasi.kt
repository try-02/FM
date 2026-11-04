package id.manajerberkas.kode.domain.model

data class HasilValidasi(
    val sukses: Boolean,
    val pesan: String? = null
)