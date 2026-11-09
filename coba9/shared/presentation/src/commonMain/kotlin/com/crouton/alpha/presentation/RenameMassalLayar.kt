package com.crouton.alpha.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@Composable
fun RenameMassalLayar(state: StateRename, saatPolaBerubah: (String) -> Unit) {
    Column {
        TextField(value = state.pola, onValueChange = saatPolaBerubah)
        state.pratinjau.forEach { baris -> Text(text = baris) }
        if (state.adaKonflik) Text(text = "Ada konflik nama")
        if (state.pesan.isNotEmpty()) Text(text = state.pesan)
    }
}
