package id.manajerberkas.kode.presentation.state
import id.manajerberkas.kode.domain.model.KueriPencarianNama
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModePencarian
import id.manajerberkas.kode.domain.usecase.CariNamaBerkasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class PencarianNamaStateHolder(
    private val cariNamaBerkas: CariNamaBerkasUseCase,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(PencarianNamaState())
    val state: StateFlow<PencarianNamaState> = _state
    private var pekerjaan: Job? = null
    fun tetapkanLokasi(lokasi: LokasiBerkas?) {
        if (lokasi == null) return
        if (_state.value.lokasiPencarian?.id == lokasi.id) return
        pekerjaan?.cancel()
        _state.update {
            it.copy(
                lokasiPencarian = lokasi,
                hasil = emptyList(),
                sedangMencari = false,
                pernahMencari = false,
                pesan = null
            )
        }
    }
    fun ubahTeks(nilai: String) {
        _state.update { it.copy(teks = nilai) }
    }
    fun ubahMode(mode: ModePencarian) {
        _state.update { it.copy(mode = mode) }
    }
    fun togglePekaHuruf() {
        _state.update { it.copy(pekaHuruf = !it.pekaHuruf) }
    }
    fun ubahEkstensi(nilai: String) {
        _state.update { it.copy(ekstensi = nilai) }
    }
    fun ubahUkuranMin(nilai: String) {
        _state.update { it.copy(ukuranMinKb = nilai.filter { c -> c.isDigit() }) }
    }
    fun ubahUkuranMax(nilai: String) {
        _state.update { it.copy(ukuranMaxKb = nilai.filter { c -> c.isDigit() }) }
    }
    fun ubahJenisItem(hanyaFolder: Boolean?) {
        _state.update { it.copy(hanyaFolder = hanyaFolder) }
    }
    fun mulai() {
        val kondisi = _state.value
        val lokasi = kondisi.lokasiPencarian
        if (lokasi == null) {
            _state.update { it.copy(pesan = "Lokasi pencarian belum tersedia.") }
            return
        }
        if (kondisi.teks.isBlank()) {
            _state.update { it.copy(pesan = "Kata kunci pencarian kosong.") }
            return
        }
        val minByte = kondisi.ukuranMinKb.toLongOrNull()?.times(1024)
        val maxByte = kondisi.ukuranMaxKb.toLongOrNull()?.times(1024)
        if (minByte != null && maxByte != null && minByte > maxByte) {
            _state.update { it.copy(pesan = "Ukuran minimum lebih besar dari maksimum.") }
            return
        }
        val kueri = KueriPencarianNama(
            teks = kondisi.teks.trim(),
            mode = kondisi.mode,
            pekaHuruf = kondisi.pekaHuruf,
            ekstensi = kondisi.ekstensi.trim().ifBlank { null },
            ukuranMin = minByte,
            ukuranMax = maxByte,
            hanyaFolder = kondisi.hanyaFolder
        )
        pekerjaan?.cancel()
        pekerjaan = scope.launch {
            _state.update {
                it.copy(
                    sedangMencari = true,
                    hasil = emptyList(),
                    pesan = null,
                    pernahMencari = true
                )
            }
            cariNamaBerkas(lokasi, kueri)
                .catch { e ->
                    _state.update {
                        it.copy(
                            sedangMencari = false,
                            pesan = e.message ?: "Pencarian gagal."
                        )
                    }
                }
                .collect { hasil ->
                    _state.update {
                        it.copy(
                            hasil = hasil.daftar,
                            sedangMencari = !hasil.selesai,
                            pesan = hasil.pesan
                        )
                    }
                }
        }
    }
    fun batal() {
        pekerjaan?.cancel()
        _state.update {
            it.copy(
                sedangMencari = false,
                pesan = "Pencarian dibatalkan."
            )
        }
    }
    fun bersihkanHasil() {
        pekerjaan?.cancel()
        _state.update {
            it.copy(
                hasil = emptyList(),
                sedangMencari = false,
                pesan = null,
                pernahMencari = false
            )
        }
    }
}
