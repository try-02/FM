package com.crouton.alpha.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DaftarBerkasLayar(state: StateLayarBerkas, saatItemDiklik: (String) -> Unit) {
    Text(text = state.lokasiAktif)
    if (state.sedangMemuat) {
        Text(text = "Memuat...")
    } else {
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 96.dp)) {
            items(state.daftar) { item ->
                Text(text = item.nama + " (" + item.ukuranTampil + ")", modifier = Modifier.clickable { saatItemDiklik(item.nama) })
            }
        }
    }
    if (state.pesan.isNotEmpty()) Text(text = state.pesan)
}
