package id.manajerberkas.kode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.presentation.layar.DaftarBerkasLayar
import id.manajerberkas.kode.presentation.state.DaftarBerkasStateHolder

@Composable
fun AplikasiManajerBerkasAndroid(
    stateHolder: DaftarBerkasStateHolder,
    onBukaPrivat: () -> Unit,
    onBukaSaf: () -> Unit
) {
    val state by stateHolder.state.collectAsStateWithLifecycle()

    DaftarBerkasLayar(
        state = state,
        aksi = stateHolder,
        onBukaPrivat = onBukaPrivat,
        onBukaSaf = onBukaSaf
    )
}