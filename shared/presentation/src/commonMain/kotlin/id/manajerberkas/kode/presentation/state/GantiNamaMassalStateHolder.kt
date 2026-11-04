package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.ItemGantiNama
import id.manajerberkas.kode.domain.model.KueriGantiNamaMassal
import id.manajerberkas.kode.domain.usecase.EksekusiGantiNamaMassalUseCase
import id.manajerberkas.kode.domain.usecase.HitungPratinjauGantiNamaUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GantiNamaMassalStateHolder(
    private val items: List<ItemGantiNama>,
    private val hitungPratinjau: HitungPratinjauGantiNamaUseCase,
    private val eksekusiGantiNama: EksekusiGantiNamaMassalUseCase,
    private val scope: CoroutineScope,
    private val onSelesai: () -> Unit
) {
    private val _state = MutableStateFlow(GantiNamaMassalState(items = items))
    val state: StateFlow<GantiNamaMassalState> = _state

    private var pekerjaan: Job? = null

    init {
        hitungUlangPratinjau()
    }

    fun ubahPola(nilai: String) {
        _state.update { it.copy(pola = nilai) }
        hitungUlangPratinjau()
    }

    fun ubahNomorAwal(nilai: String) {
        _state.update { it.copy(nomorAwal = nilai.filter { c -> c.isDigit() }) }
        hitungUlangPratinjau()
    }

    private fun hitungUlangPratinjau() {
        val kondisi = _state.value
        val nomorAwal = kondisi.nomorAwal.toIntOrNull() ?: 1
        val kueri = KueriGantiNamaMassal(pola = kondisi.pola, nomorAwal = nomorAwal)

        val pratinjau = hitungPratinjau(kondisi.items, kueri)
        _state.update { it.copy(pratinjau = pratinjau, pesan = null) }
    }

    fun eksekusi() {
        val kondisi = _state.value
        if (kondisi.jumlahValid == 0) {
            _state.update { it.copy(pesan = "Tidak ada item valid untuk diproses.") }
            return
        }

        pekerjaan?.cancel()
        pekerjaan = scope.launch {
            _state.update { it.copy(sedangMemproses = true, pesan = null) }

            try {
                val hasil = eksekusiGantiNama(kondisi.pratinjau)
                val sukses = hasil.count { it.sukses }
                val gagal = hasil.size - sukses

                _state.update {
                    it.copy(
                        sedangMemproses = false,
                        hasilEksekusi = hasil,
                        pesan = "Selesai: $sukses berhasil, $gagal gagal."
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        sedangMemproses = false,
                        pesan = "Gagal mengeksekusi: ${e.message}"
                    )
                }
            }
        }
    }

    fun tutupSetelahEksekusi() {
        onSelesai()
    }
}