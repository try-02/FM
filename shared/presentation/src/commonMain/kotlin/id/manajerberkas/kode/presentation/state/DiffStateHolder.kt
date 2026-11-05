package id.manajerberkas.kode.presentation.state
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModeDiff
import id.manajerberkas.kode.domain.model.ekstrakBlokUnified
import id.manajerberkas.kode.domain.model.ekstrakBlokBerdampingan
import id.manajerberkas.kode.domain.model.keBerdampingan
import id.manajerberkas.kode.domain.model.KontenTeks
import id.manajerberkas.kode.domain.usecase.BacaTeksUseCase
import id.manajerberkas.kode.domain.usecase.BandingkanIsiBerkasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class DiffStateHolder(
    private val lokasiA: LokasiBerkas,
    private val lokasiB: LokasiBerkas,
    private val bacaTeks: BacaTeksUseCase,
    private val bandingkanIsi: BandingkanIsiBerkasUseCase,
    private val scope: CoroutineScope
) : DiffAksi {
    private val _state = MutableStateFlow(DiffState(lokasiA = lokasiA, lokasiB = lokasiB))
    val state: StateFlow<DiffState> = _state
    private var pekerjaan: Job? = null
    init { bandingkan() }
    override fun bandingkan() {
        pekerjaan?.cancel()
        pekerjaan = scope.launch {
            _state.update { it.copy(memuat = true, pesan = null, hasil = null, indeksBlokAktif = -1) }
            val hasilA = bacaTeks(lokasiA)
            val hasilB = bacaTeks(lokasiB)
            if (!hasilA.sukses || hasilA.konten == null) {
                _state.update { it.copy(memuat = false, pesan = "Gagal membaca File A: ${hasilA.pesan ?: "Tidak diketahui"}") }
                return@launch
            }
            if (!hasilB.sukses || hasilB.konten == null) {
                _state.update { it.copy(memuat = false, pesan = "Gagal membaca File B: ${hasilB.pesan ?: "Tidak diketahui"}") }
                return@launch
            }
        // Ganti blok try-catch di dalam DiffStateHolder.kt:
        try {
            val kontenA = hasilA.konten
            val kontenB = hasilB.konten
            if (kontenA is KontenTeks && kontenB is KontenTeks) {
                val hasilDiff = bandingkanIsi(kontenA.teks, kontenB.teks)
                val barisBerdampingan = hasilDiff.keBerdampingan()
                val blokUnified = hasilDiff.baris.ekstrakBlokUnified()
                _state.update {
                    it.copy(
                        memuat = false,
                        hasil = hasilDiff,
                        barisBerdampingan = barisBerdampingan,
                        blokPerubahan = blokUnified,
                        mode = ModeDiff.UNIFIED,
                        pesan = if (hasilDiff.identik) "Kedua file identik." else "Ditemukan ${hasilDiff.jumlahPerubahan} perubahan."
                    )
                }
            } else {
                _state.update {
                    it.copy(memuat = false, pesan = "Berkas bukan merupakan format teks yang didukung.")
                }
            }
        } catch (e: Throwable) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            _state.update { it.copy(memuat = false, pesan = "Gagal membandingkan: ${e.message ?: "Memori tidak mencukupi"}") }
        }
        }
    }
    override fun ubahMode(mode: ModeDiff) {
        val kondisi = state.value
        val blokBaru = when (mode) {
            ModeDiff.UNIFIED -> kondisi.hasil?.baris?.ekstrakBlokUnified() ?: emptyList()
            ModeDiff.BERDAMPINGAN -> kondisi.barisBerdampingan.ekstrakBlokBerdampingan()
        }
        _state.update { it.copy(mode = mode, blokPerubahan = blokBaru, indeksBlokAktif = -1) }
    }
    override fun perubahanBerikutnya() {
        val kondisi = state.value
        if (kondisi.blokPerubahan.isEmpty()) return
        val selanjutnya = (kondisi.indeksBlokAktif + 1).coerceAtMost(kondisi.blokPerubahan.lastIndex)
        _state.update { it.copy(indeksBlokAktif = selanjutnya) }
    }
    override fun perubahanSebelumnya() {
        val kondisi = state.value
        if (kondisi.blokPerubahan.isEmpty()) return
        val sebelumnya = (kondisi.indeksBlokAktif - 1).coerceAtLeast(0)
        _state.update { it.copy(indeksBlokAktif = sebelumnya) }
    }
    override fun batal() { pekerjaan?.cancel() }
}
