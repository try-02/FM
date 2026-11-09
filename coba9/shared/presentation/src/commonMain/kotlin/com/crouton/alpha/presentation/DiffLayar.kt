package com.crouton.alpha.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DiffLayar(
    state: StateDiff,
    saatBerikutnya: () -> Unit = {},
    saatSebelumnya: () -> Unit = {},
    saatAlihMode: () -> Unit = {},
    saatKembali: () -> Unit = {}
) {
    MaterialTheme {
        Column {
            Text(
                text = if (state.modeUnified) "Mode Unified" else "Mode Berdampingan",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = "Terpilih: " + state.terpilih.joinToString(" vs "))
            Text(text = "Perubahan: " + state.jumlahPerubahan)
            Button(onClick = saatAlihMode) { Text(text = "Ganti Mode") }
            Button(onClick = saatSebelumnya) { Text(text = "Sebelumnya") }
            Button(onClick = saatBerikutnya) { Text(text = "Berikutnya") }
            if (state.sedangMemuat) {
                Text(text = "Menghitung diff...")
            } else {
                val awal = state.indeksPerubahanAktif.coerceAtLeast(0)
                val akhir = (awal + 20).coerceAtMost(state.barisDiff.size)
                for (i in awal until akhir) {
                    Text(text = state.barisDiff[i])
                }
            }
            if (state.pesan.isNotEmpty()) Text(text = state.pesan)
            Button(onClick = saatKembali) { Text(text = "Kembali") }
        }
    }
}
