package com.crouton.alpha.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@Composable
fun EditorLayar(
    state: StateEditor,
    saatTeksBerubah: (String) -> Unit,
    saatSimpan: () -> Unit,
    saatKembali: () -> Unit,
    saatBuangPerubahan: () -> Unit,
    saatBatalTutup: () -> Unit,
    saatHapus: () -> Unit = {},
    tampilkanDialogHapus: Boolean = false,
    saatKonfirmasiHapus: () -> Unit = {},
    saatBatalHapus: () -> Unit = {}
) {
    Column {
        Text(text = state.namaBerkas + " - " + state.status.name + " - baris " + state.nomorBarisAktif)
        if (state.hanyaBaca) Text(text = "Hanya baca")
        TextField(
            value = state.teks,
            onValueChange = { if (!state.hanyaBaca) saatTeksBerubah(it) },
            readOnly = state.hanyaBaca
        )
        Button(onClick = { if (!state.hanyaBaca) saatSimpan() }) { Text(text = "Simpan") }
        Button(onClick = saatKembali) { Text(text = "Kembali") }
        Button(onClick = saatHapus) { Text(text = "Hapus") }
        if (tampilkanDialogHapus) {
            Text(text = "Hapus file ini secara permanen?")
            Button(onClick = saatKonfirmasiHapus) { Text(text = "Ya, Hapus") }
            Button(onClick = saatBatalHapus) { Text(text = "Batal") }
        }
        if (state.tampilkanDialogTutup) {
            Text(text = "Ada perubahan yang belum disimpan. Tutup file?")
            Button(onClick = saatSimpan) { Text(text = "Simpan") }
            Button(onClick = saatBuangPerubahan) { Text(text = "Buang Perubahan") }
            Button(onClick = saatBatalTutup) { Text(text = "Batal") }
        }
    }
}
