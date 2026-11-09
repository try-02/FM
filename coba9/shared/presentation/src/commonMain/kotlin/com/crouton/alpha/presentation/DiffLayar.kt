package com.crouton.alpha.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DiffLayar(state: StateDiff) {
    Column {
        Text(text = if (state.modeUnified) "Mode Unified" else "Mode Berdampingan")
        Text(text = "Perubahan: " + state.jumlahPerubahan)
        Text(text = "Perubahan aktif: " + (state.indeksPerubahanAktif + 1))
        if (state.sedangMemuat) Text(text = "Menghitung diff...")
        if (state.pesan.isNotEmpty()) Text(text = state.pesan)
    }
}
