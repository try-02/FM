package id.manajerberkas.kode

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.presentation.layar.DaftarBerkasLayar
import id.manajerberkas.kode.presentation.layar.PencarianNamaLayar
import id.manajerberkas.kode.presentation.state.DaftarBerkasStateHolder
import id.manajerberkas.kode.presentation.state.DependensiEditor
import id.manajerberkas.kode.presentation.state.DependensiPenjelajah
import id.manajerberkas.kode.presentation.state.PencarianNamaStateHolder

@Composable
fun AplikasiUtamaAndroid(
    stateHolder: DaftarBerkasStateHolder,
    dependensiPenjelajah: DependensiPenjelajah,
    dependensiEditor: DependensiEditor,
    onBukaPrivat: () -> Unit,
    onBukaSaf: () -> Unit
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

    MaterialTheme {
        when {
            tujuanEditor != null -> {
                EditorAndroid(
                    lokasi = tujuanEditor.lokasi,
                    dependensi = dependensiEditor,
                    barisAwal = tujuanEditor.nomorBaris,
                    onTutup = { stateHolder.selesaiBukaEditor() }
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

            else -> {
                DaftarBerkasLayar(
                    state = state,
                    aksi = stateHolder,
                    onBukaPrivat = onBukaPrivat,
                    onBukaSaf = onBukaSaf
                )
            }
        }
    }
}