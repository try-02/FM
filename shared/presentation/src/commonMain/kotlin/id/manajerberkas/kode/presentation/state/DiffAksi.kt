package id.manajerberkas.kode.presentation.state
import id.manajerberkas.kode.domain.model.ModeDiff
interface DiffAksi {
    fun bandingkan()
    fun perubahanBerikutnya()
    fun perubahanSebelumnya()
    fun ubahMode(mode: ModeDiff)
    fun batal()
}
