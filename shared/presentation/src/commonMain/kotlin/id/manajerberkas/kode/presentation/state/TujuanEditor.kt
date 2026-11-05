package id.manajerberkas.kode.presentation.state
import id.manajerberkas.kode.domain.model.LokasiBerkas
data class TujuanEditor(
    val lokasi: LokasiBerkas,
    val nomorBaris: Int? = null
)
