package com.crouton.alpha.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DaftarBerkasLayar(state: StateLayarBerkas, saatItemDiklik: (StatusBerkasUi) -> Unit) {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Crouton Alpha",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "File | Terbaru | Bookmark | Pencarian | Pengaturan",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (state.lokasiAktif.isBlank()) "Lokasi: belum dipilih" else "Lokasi: " + state.lokasiAktif,
                style = MaterialTheme.typography.bodyMedium
            )
            if (state.sedangMemuat) {
                Text(text = "Memuat...", style = MaterialTheme.typography.bodyMedium)
            } else if (state.daftar.isEmpty()) {
                Text(
                    text = "Folder kosong atau belum ada lokasi yang ditambahkan.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 96.dp)) {
                    items(state.daftar) { item ->
                        Text(
                            text = item.nama + " (" + item.ukuranTampil + ")",
                            modifier = Modifier.clickable { saatItemDiklik(item) }
                        )
                    }
                }
            }
            if (state.pesan.isNotEmpty()) {
                Text(text = state.pesan, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
