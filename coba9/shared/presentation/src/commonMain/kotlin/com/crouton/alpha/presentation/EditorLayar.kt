package com.crouton.alpha.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@Composable
fun EditorLayar(state: StateEditor, saatTeksBerubah: (String) -> Unit, saatSimpan: () -> Unit) {
    Column {
        Text(text = state.namaBerkas + " - " + state.status.name + " - baris " + state.nomorBarisAktif)
        if (state.hanyaBaca) Text(text = "Hanya baca")
        TextField(value = state.teks, onValueChange = { if (!state.hanyaBaca) saatTeksBerubah(it) }, readOnly = state.hanyaBaca)
        Button(onClick = { if (!state.hanyaBaca) saatSimpan() }) { Text(text = "Simpan") }
    }
}
