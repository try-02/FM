package com.crouton.alpha.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
    saatBatalHapus: () -> Unit = {},
    saatCariBerikutnya: (String) -> Unit = {},
    saatCariSebelumnya: (String) -> Unit = {},
    saatGantiSatu: (String, String) -> Unit = { _, _ -> },
    saatGantiSemua: (String, String) -> Unit = { _, _ -> },
    saatMenujuBaris: (Int) -> Unit = {}
) {
    var kataCari by remember { mutableStateOf("") }
    var kataGanti by remember { mutableStateOf("") }
    var nomorBaris by remember { mutableStateOf("") }
    Column {
        Text(text = state.namaBerkas + " - " + state.status.name + " - baris " + state.nomorBarisAktif)
        if (state.hanyaBaca) Text(text = "Hanya baca")
        TextField(
            value = state.teks,
            onValueChange = { if (!state.hanyaBaca) saatTeksBerubah(it) },
            readOnly = state.hanyaBaca
        )
        TextField(value = kataCari, onValueChange = { kataCari = it }, label = { Text(text = "Cari") })
        TextField(value = kataGanti, onValueChange = { kataGanti = it }, label = { Text(text = "Ganti dengan") })
        Button(onClick = { saatCariBerikutnya(kataCari) }) { Text(text = "Berikutnya") }
        Button(onClick = { saatCariSebelumnya(kataCari) }) { Text(text = "Sebelumnya") }
        Button(onClick = { saatGantiSatu(kataCari, kataGanti) }) { Text(text = "Ganti") }
        Button(onClick = { saatGantiSemua(kataCari, kataGanti) }) { Text(text = "Ganti Semua") }
        TextField(value = nomorBaris, onValueChange = { nomorBaris = it }, label = { Text(text = "Lompat ke baris") })
        Button(onClick = { nomorBaris.toIntOrNull()?.let { saatMenujuBaris(it) } }) { Text(text = "Pergi") }
        if (state.pesanStatus.isNotEmpty()) Text(text = state.pesanStatus)
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
