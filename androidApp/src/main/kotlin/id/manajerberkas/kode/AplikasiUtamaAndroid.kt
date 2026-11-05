package id.manajerberkas.kode

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.usecase.BandingkanIsiBerkasUseCase
import id.manajerberkas.kode.domain.usecase.HitungPratinjauGantiNamaUseCase
import id.manajerberkas.kode.presentation.layar.ArsipLayar
import id.manajerberkas.kode.presentation.layar.DaftarBerkasLayar
import id.manajerberkas.kode.presentation.layar.GantiNamaMassalLayar
import id.manajerberkas.kode.presentation.layar.PencarianIsiLayar
import id.manajerberkas.kode.presentation.layar.PencarianNamaLayar
import id.manajerberkas.kode.presentation.state.ArsipStateHolder
import id.manajerberkas.kode.presentation.state.DaftarBerkasStateHolder
import id.manajerberkas.kode.presentation.state.DependensiEditor
import id.manajerberkas.kode.presentation.state.DependensiPenjelajah
import id.manajerberkas.kode.presentation.state.EditorStateHolder
import id.manajerberkas.kode.presentation.state.GantiNamaMassalStateHolder
import id.manajerberkas.kode.presentation.state.PencarianIsiStateHolder
import id.manajerberkas.kode.presentation.state.PencarianNamaStateHolder

@Composable
fun AplikasiUtamaAndroid(
    stateHolder: DaftarBerkasStateHolder,
    dependensiPenjelajah: DependensiPenjelajah,
    dependensiEditor: DependensiEditor,
    dapatkanEditorHolder: (LokasiBerkas, Int?) -> EditorStateHolder,
    onBukaPrivat: () -> Unit,
    onBukaSaf: () -> Unit,
    onBukaEksternal: () -> Unit = {}
) {
    val state by stateHolder.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pencarianStateHolder = remember {
        PencarianNamaStateHolder(
            cariNamaBerkas = dependensiPenjelajah.cariNamaBerkas,
            scope = scope
        )
    }
    val tujuanEditor = state.editorTujuan
    val tujuanDiff = state.tujuanDiff

    val penangananBackAktif = tujuanDiff != null ||
        state.pencarianTerbuka ||
        state.pencarianIsiTerbuka ||
        state.tujuanGantiNamaMassal != null ||
        state.tujuanArsip != null ||
        state.modePilihTujuan != null ||
        state.modePilih ||
        state.menuUntukItem != null ||
        state.dialogBuatBerkas ||
        state.dialogBuatFolder ||
        state.dialogGantiNama != null ||
        state.konfirmasiHapusTerpilih ||
        state.dialogMarkah ||
        state.bisaKembali

    BackHandler(enabled = penangananBackAktif && tujuanEditor == null) {
        when {
            tujuanDiff != null -> stateHolder.selesaiBandingkan()
            state.pencarianTerbuka -> {
                pencarianStateHolder.batal()
                stateHolder.tutupPencarian()
            }
            state.pencarianIsiTerbuka -> stateHolder.tutupPencarianIsi()
            state.tujuanGantiNamaMassal != null -> stateHolder.selesaiGantiNamaMassal()
            state.tujuanArsip != null -> stateHolder.selesaiBukaArsip()
            else -> stateHolder.kembali()
        }
    }

    MaterialTheme {
        when {
            tujuanDiff != null -> {
                val bandingkanIsiBerkasUseCase = remember { BandingkanIsiBerkasUseCase() }
                DiffAndroid(
                    lokasiA = tujuanDiff.lokasiA,
                    lokasiB = tujuanDiff.lokasiB,
                    bacaTeksUseCase = dependensiEditor.bacaTeks,
                    bandingkanIsiBerkasUseCase = bandingkanIsiBerkasUseCase,
                    onTutup = { stateHolder.selesaiBandingkan() }
                )
            }
            tujuanEditor != null -> {
                val editorHolder = remember(tujuanEditor.lokasi.id, tujuanEditor.nomorBaris) {
                    dapatkanEditorHolder(tujuanEditor.lokasi, tujuanEditor.nomorBaris)
                }
                EditorAndroid(
                    stateHolder = editorHolder
                )
            }
            state.pencarianTerbuka -> {
                LaunchedEffect(state.lokasiAktif?.id) {
                    pencarianStateHolder.tetapkanLokasi(state.lokasiAktif)
                }
                val statePencarian by pencarianStateHolder.state.collectAsStateWithLifecycle()
                PencarianNamaLayar(
                    state = statePencarian,
                    onUbahTeks = pencarianStateHolder::ubahTeks,
                    onUbahMode = pencarianStateHolder::ubahMode,
                    onTogglePekaHuruf = pencarianStateHolder::togglePekaHuruf,
                    onUbahEkstensi = pencarianStateHolder::ubahEkstensi,
                    onUbahUkuranMin = pencarianStateHolder::ubahUkuranMin,
                    onUbahUkuranMax = pencarianStateHolder::ubahUkuranMax,
                    onUbahJenisItem = pencarianStateHolder::ubahJenisItem,
                    onMulai = pencarianStateHolder::mulai,
                    onBatal = pencarianStateHolder::batal,
                    onTutup = {
                        pencarianStateHolder.batal()
                        stateHolder.tutupPencarian()
                    },
                    onKlikHasil = { item ->
                        stateHolder.bukaHasilPencarian(item)
                    }
                )
            }
            state.pencarianIsiTerbuka -> {
                val pencarianIsiStateHolder = remember {
                    PencarianIsiStateHolder(
                        cariIsiBerkas = dependensiPenjelajah.cariIsiBerkas,
                        scope = scope
                    )
                }
                LaunchedEffect(state.lokasiAktif?.id) {
                    pencarianIsiStateHolder.tetapkanLokasi(state.lokasiAktif)
                }
                val statePencarianIsi by pencarianIsiStateHolder.state.collectAsStateWithLifecycle()
                PencarianIsiLayar(
                    state = statePencarianIsi,
                    onUbahTeks = pencarianIsiStateHolder::ubahTeks,
                    onUbahMode = pencarianIsiStateHolder::ubahMode,
                    onTogglePekaHuruf = pencarianIsiStateHolder::togglePekaHuruf,
                    onToggleKataUtuh = pencarianIsiStateHolder::toggleKataUtuh,
                    onToggleRegex = pencarianIsiStateHolder::toggleRegex,
                    onUbahEkstensi = pencarianIsiStateHolder::ubahEkstensi,
                    onUbahUkuranMin = pencarianIsiStateHolder::ubahUkuranMin,
                    onUbahUkuranMax = pencarianIsiStateHolder::ubahUkuranMax,
                    onMulai = pencarianIsiStateHolder::mulai,
                    onBatal = pencarianIsiStateHolder::batal,
                    onTutup = {
                        pencarianIsiStateHolder.batal()
                        stateHolder.tutupPencarianIsi()
                    },
                    onKlikHasil = { item ->
                        stateHolder.bukaHasilPencarianIsi(item)
                    }
                )
            }
            state.tujuanGantiNamaMassal != null -> {
                val gantiNamaStateHolder = remember(state.tujuanGantiNamaMassal) {
                    GantiNamaMassalStateHolder(
                        items = state.tujuanGantiNamaMassal!!,
                        hitungPratinjau = HitungPratinjauGantiNamaUseCase(),
                        eksekusiGantiNama = dependensiPenjelajah.eksekusiGantiNamaMassal,
                        scope = scope,
                        onSelesai = { stateHolder.selesaiGantiNamaMassal() }
                    )
                }
                val stateGantiNama by gantiNamaStateHolder.state.collectAsStateWithLifecycle()
                GantiNamaMassalLayar(
                    state = stateGantiNama,
                    onUbahPola = gantiNamaStateHolder::ubahPola,
                    onUbahNomorAwal = gantiNamaStateHolder::ubahNomorAwal,
                    onEksekusi = gantiNamaStateHolder::eksekusi,
                    onTutup = { stateHolder.selesaiGantiNamaMassal() },
                    onTutupSetelahEksekusi = { stateHolder.selesaiGantiNamaMassal() }
                )
            }
            state.tujuanArsip != null -> {
                val arsipStateHolder = remember(state.tujuanArsip) {
                    ArsipStateHolder(
                        lokasiArsip = state.tujuanArsip!!,
                        lokasiTujuanEkstrak = state.lokasiAktif ?: state.tujuanArsip!!,
                        daftarIsiArsip = dependensiPenjelajah.daftarIsiArsip,
                        ekstrakItemArsip = dependensiPenjelajah.ekstrakItemArsip,
                        ekstrakSemuaArsip = dependensiPenjelajah.ekstrakSemuaArsip,
                        scope = scope,
                        onTutup = { stateHolder.selesaiBukaArsip() }
                    )
                }
                val stateArsip by arsipStateHolder.state.collectAsStateWithLifecycle()
                ArsipLayar(
                    state = stateArsip,
                    onKlikItem = arsipStateHolder::klikItem,
                    onTogglePilih = arsipStateHolder::togglePilih,
                    onTekanLama = arsipStateHolder::tekanLama,
                    onKembali = arsipStateHolder::kembali,
                    onEkstrakTerpilih = arsipStateHolder::ekstrakTerpilih,
                    onEkstrakSemua = arsipStateHolder::ekstrakSemua,
                    onBatalkanPilih = arsipStateHolder::batalkanPilih,
                    onTutup = arsipStateHolder::tutup
                )
            }
            else -> {
                DaftarBerkasLayar(
                    state = state,
                    aksi = stateHolder,
                    onBukaPrivat = onBukaPrivat,
                    onBukaSaf = onBukaSaf,
                    onBukaEksternal = onBukaEksternal
                )
            }
        }
    }
}