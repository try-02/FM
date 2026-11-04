package id.manajerberkas.kode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.usecase.BacaTeksUseCase
import id.manajerberkas.kode.domain.usecase.BandingkanIsiBerkasUseCase
import id.manajerberkas.kode.presentation.layar.DiffLayar
import id.manajerberkas.kode.presentation.state.DiffStateHolder

@Composable
fun DiffAndroid(
    lokasiA: LokasiBerkas,
    lokasiB: LokasiBerkas,
    bacaTeksUseCase: BacaTeksUseCase,
    bandingkanIsiBerkasUseCase: BandingkanIsiBerkasUseCase,
    onTutup: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val stateHolder = remember(lokasiA.id, lokasiB.id) {
        DiffStateHolder(
            lokasiA = lokasiA,
            lokasiB = lokasiB,
            bacaTeks = bacaTeksUseCase,
            bandingkanIsi = bandingkanIsiBerkasUseCase,
            scope = scope
        )
    }

    val state by stateHolder.state.collectAsStateWithLifecycle()

    DiffLayar(
        state = state,
        aksi = stateHolder,
        onTutup = onTutup
    )
}