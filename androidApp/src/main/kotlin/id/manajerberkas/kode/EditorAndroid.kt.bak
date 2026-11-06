package id.manajerberkas.kode

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.presentation.layar.EditorLayar
import id.manajerberkas.kode.presentation.state.EditorStateHolder

@Composable
fun EditorAndroid(
    stateHolder: EditorStateHolder
) {
    val state by stateHolder.state.collectAsStateWithLifecycle()

    BackHandler {
        stateHolder.mintaTutup()
    }

    EditorLayar(
        state = state,
        aksi = stateHolder
    )
}