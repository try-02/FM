package id.manajerberkas.kode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.presentation.layar.EditorLayar
import id.manajerberkas.kode.presentation.state.DependensiEditor
import id.manajerberkas.kode.presentation.state.EditorStateHolder

@Composable
fun EditorAndroid(
    lokasi: LokasiBerkas,
    dependensi: DependensiEditor,
    barisAwal: Int?,
    onTutup: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val stateHolder = remember(lokasi.id, barisAwal) {
        EditorStateHolder(
            lokasi = lokasi,
            dependensi = dependensi,
            scope = scope,
            onTutup = onTutup,
            barisAwal = barisAwal
        )
    }

    val state by stateHolder.state.collectAsStateWithLifecycle()

    EditorLayar(
        state = state,
        aksi = stateHolder
    )
}