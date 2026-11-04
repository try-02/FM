package id.manajerberkas.kode.domain.model

data class HasilOperasi(
    val sukses: Boolean,
    val pesan: String? = null,
    val lokasiHasil: LokasiBerkas? = null
)