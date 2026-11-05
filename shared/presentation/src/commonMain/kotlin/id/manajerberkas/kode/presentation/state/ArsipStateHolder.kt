package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.ItemArsip
import id.manajerberkas.kode.domain.model.JenisItemArsip
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.usecase.DaftarIsiArsipUseCase
import id.manajerberkas.kode.domain.usecase.EkstrakItemArsipUseCase
import id.manajerberkas.kode.domain.usecase.EkstrakSemuaArsipUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArsipStateHolder(
    private val lokasiArsip: LokasiBerkas,
    private val lokasiTujuanEkstrak: LokasiBerkas,
    private val daftarIsiArsip: DaftarIsiArsipUseCase,
    private val ekstrakItemArsip: EkstrakItemArsipUseCase,
    private val ekstrakSemuaArsip: EkstrakSemuaArsipUseCase,
    private val scope: CoroutineScope,
    private val onTutup: () -> Unit
) {
    private val _state = MutableStateFlow(ArsipState(lokasiArsip = lokasiArsip))
    val state: StateFlow<ArsipState> = _state
    private var pekerjaanMuat: Job? = null
    private var pekerjaanEkstrak: Job? = null

    init {
        muatDaftar()
    }

    private fun muatDaftar() {
        pekerjaanMuat?.cancel()
        pekerjaanMuat = scope.launch {
            _state.update { it.copy(memuat = true, pesan = null) }
            val hasil = daftarIsiArsip(lokasiArsip)
            _state.update {
                it.copy(
                    memuat = false,
                    daftar = hasil.daftar,
                    kemampuan = hasil.kemampuan,
                    pesan = if (hasil.sukses) null else hasil.pesan
                )
            }
            perbaruiDaftarTampil()
        }
    }

    private fun perbaruiDaftarTampil() {
        val kondisi = _state.value
        val jalurAktif = kondisi.jalurAktif
        val tampil = kondisi.daftar.filter { item ->
            // Bersihkan slash di ujung untuk folder sebelum mencari parent
            val jalurBersih = item.jalur.trimEnd('/')
            val parent = jalurBersih.substringBeforeLast('/', "")
            val parentPath = if (parent.isEmpty()) "" else "$parent/"
            parentPath == jalurAktif
        }
        _state.update {
            it.copy(
                daftarTampil = tampil.sortedWith(
                    compareByDescending<ItemArsip> { it.jenis }
                        .thenBy { it.nama.lowercase() }
                ),
                bisaKembali = jalurAktif.isNotEmpty()
            )
        }
    }

    fun klikItem(item: ItemArsip) {
        if (item.jenis == JenisItemArsip.FOLDER) {
            _state.update { it.copy(jalurAktif = item.jalur) }
            perbaruiDaftarTampil()
        } else {
            _state.update { it.copy(pesan = "Pembukaan berkas di dalam arsip belum didukung. Silakan ekstrak berkas terlebih dahulu.") }
        }
    }

    fun kembali() {
        val kondisi = _state.value
        if (kondisi.jalurAktif.isEmpty()) {
            onTutup()
            return
        }
        val jalurBersih = kondisi.jalurAktif.trimEnd('/')
        val parent = jalurBersih.substringBeforeLast('/', "")
        val jalurBaru = if (parent.isEmpty()) "" else "$parent/"
        _state.update { it.copy(jalurAktif = jalurBaru) }
        perbaruiDaftarTampil()
    }

    fun togglePilih(item: ItemArsip) {
        val kondisi = _state.value
        val terpilih = kondisi.itemTerpilih.toMutableSet()
        if (terpilih.contains(item.jalur)) {
            terpilih.remove(item.jalur)
        } else {
            terpilih.add(item.jalur)
        }
        _state.update {
            it.copy(
                itemTerpilih = terpilih,
                modePilih = terpilih.isNotEmpty()
            )
        }
    }

    fun tekanLama(item: ItemArsip) {
        val kondisi = _state.value
        if (!kondisi.modePilih) {
            _state.update {
                it.copy(
                    modePilih = true,
                    itemTerpilih = setOf(item.jalur)
                )
            }
        } else {
            togglePilih(item)
        }
    }

    fun batalkanPilih() {
        _state.update {
            it.copy(
                modePilih = false,
                itemTerpilih = emptySet()
            )
        }
    }

    fun ekstrakTerpilih() {
        val kondisi = _state.value
        if (kondisi.itemTerpilih.isEmpty()) return
        pekerjaanEkstrak?.cancel()
        pekerjaanEkstrak = scope.launch {
            _state.update { it.copy(sedangEkstrak = true, pesan = null) }
            var totalBerhasil = 0
            var totalGagal = 0
            for (jalur in kondisi.itemTerpilih) {
                val hasil = ekstrakItemArsip(lokasiArsip, jalur, lokasiTujuanEkstrak)
                totalBerhasil += hasil.jumlahBerhasil
                totalGagal += hasil.jumlahGagal
            }
            _state.update {
                it.copy(
                    sedangEkstrak = false,
                    pesan = "Ekstraksi selesai: $totalBerhasil berhasil, $totalGagal gagal.",
                    modePilih = false,
                    itemTerpilih = emptySet()
                )
            }
        }
    }

    fun ekstrakSemua() {
        pekerjaanEkstrak?.cancel()
        pekerjaanEkstrak = scope.launch {
            _state.update { it.copy(sedangEkstrak = true, pesan = null) }
            val hasil = ekstrakSemuaArsip(lokasiArsip, lokasiTujuanEkstrak)
            _state.update {
                it.copy(
                    sedangEkstrak = false,
                    pesan = hasil.pesan ?: "Ekstraksi selesai."
                )
            }
        }
    }

    fun tutup() {
        onTutup()
    }
}