package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.KueriPencarianIsi
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModePencarianIsi
import id.manajerberkas.kode.domain.usecase.CariIsiBerkasUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PencarianIsiStateHolder(
    private val cariIsiBerkas: CariIsiBerkasUseCase,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(PencarianIsiState())
    val state: StateFlow<PencarianIsiState> = _state

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
                pesan = null,
                jumlahFileDiproses = 0,
                jumlahKecocokanTotal = 0
            )
        }
    }

    fun ubahTeks(nilai: String) {
        _state.update { it.copy(teks = nilai) }
    }

    fun ubahMode(mode: ModePencarianIsi) {
        _state.update { it.copy(mode = mode) }
    }

    fun togglePekaHuruf() {
        _state.update { it.copy(pekaHuruf = !it.pekaHuruf) }
    }

    fun toggleKataUtuh() {
        _state.update { it.copy(kataUtuh = !it.kataUtuh) }
    }

    fun toggleRegex() {
        _state.update { it.copy(gunakanRegex = !it.gunakanRegex) }
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

    fun mulai() {
        val kondisi = _state.value
        val lokasi = kondisi.lokasiPencarian

        if (lokasi == null) {
            _state.update { it.copy(pesan = "Lokasi pencarian belum tersedia.") }
            return
        }

        if (kondisi.teks.isBlank() && !kondisi.gunakanRegex) {
            _state.update { it.copy(pesan = "Kata kunci pencarian kosong.") }
            return
        }

        val minByte = kondisi.ukuranMinKb.toLongOrNull()?.times(1024)
        val maxByte = kondisi.ukuranMaxKb.toLongOrNull()?.times(1024)

        if (minByte != null && maxByte != null && minByte > maxByte) {
            _state.update { it.copy(pesan = "Ukuran minimum lebih besar dari maksimum.") }
            return
        }

        val kueri = KueriPencarianIsi(
            teks = kondisi.teks.trim(),
            mode = kondisi.mode,
            pekaHuruf = kondisi.pekaHuruf,
            kataUtuh = kondisi.kataUtuh,
            gunakanRegex = kondisi.gunakanRegex,
            ekstensi = kondisi.ekstensi.trim().ifBlank { null },
            ukuranMin = minByte,
            ukuranMax = maxByte
        )

        pekerjaan?.cancel()

        pekerjaan = scope.launch {
            _state.update {
                it.copy(
                    sedangMencari = true,
                    hasil = emptyList(),
                    pesan = null,
                    pernahMencari = true,
                    jumlahFileDiproses = 0,
                    jumlahKecocokanTotal = 0
                )
            }

            cariIsiBerkas(lokasi, kueri)
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
                            pesan = hasil.pesan,
                            jumlahFileDiproses = hasil.jumlahFileDiproses,
                            jumlahKecocokanTotal = hasil.jumlahKecocokanTotal
                        )
                    }
                }
        }
    }

    fun batal() {
        pekerjaan?.cancel()
        _state.update { it.copy(sedangMencari = false, pesan = "Pencarian dibatalkan.") }
    }

    fun bersihkanHasil() {
        pekerjaan?.cancel()
        _state.update {
            it.copy(
                hasil = emptyList(),
                sedangMencari = false,
                pesan = null,
                pernahMencari = false,
                jumlahFileDiproses = 0,
                jumlahKecocokanTotal = 0
            )
        }
    }
}