package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.JenisAkhirBaris
import id.manajerberkas.kode.presentation.state.EditorAksi
import id.manajerberkas.kode.presentation.state.EditorState

@Composable
fun EditorLayar(
    state: EditorState,
    aksi: EditorAksi
) {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Button(onClick = aksi::mintaTutup) {
                    Text("Tutup")
                }

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = state.lokasi?.namaTampilan ?: "Editor"
                    )

                    Text(
                        text = buildString {
                            if (state.hanyaBaca) {
                                append("Hanya baca")
                            } else {
                                if (state.adaPerubahan) {
                                    append("Belum disimpan")
                                } else {
                                    append("Tersimpan")
                                }
                            }

                            if (state.sedangMenyimpan) {
                                append(" • Menyimpan")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = if (state.barisAktif > 0 && state.kolomAktif > 0) {
                            "Baris ${state.barisAktif}, Kolom ${state.kolomAktif}"
                        } else {
                            "Baris -, Kolom -"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = aksi::simpan,
                    enabled = !state.hanyaBaca && state.adaPerubahan && !state.sedangMenyimpan
                ) {
                    Text("Simpan")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = aksi::undo,
                    enabled = state.bisaUndo
                ) {
                    Text("Undo")
                }

                Button(
                    onClick = aksi::redo,
                    enabled = state.bisaRedo,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Redo")
                }

                Button(
                    onClick = aksi::bukaDialogNavigasiBaris,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Baris")
                }

                Button(
                    onClick = aksi::bukaPanelStatistik,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Statistik")
                }

                Button(
                    onClick = aksi::toggleBungkusKata,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        if (state.bungkusKata) {
                            "Bungkus ON"
                        } else {
                            "Bungkus OFF"
                        }
                    )
                }
            }

            state.pesan?.let { pesan ->
                Text(
                    text = pesan,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (state.memuat) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp)
                )
            }

            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.bungkusKata) {
                    TextField(
                        value = state.medan,
                        onValueChange = aksi::ubahTeks,
                        readOnly = state.hanyaBaca,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        TextField(
                            value = state.medan,
                            onValueChange = aksi::ubahTeks,
                            readOnly = state.hanyaBaca,
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(min = 1.dp),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = state.cari,
                        onValueChange = aksi::ubahCari,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Cari") }
                    )

                    TextField(
                        value = state.ganti,
                        onValueChange = aksi::ubahGanti,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        placeholder = { Text("Ganti") },
                        enabled = !state.hanyaBaca
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = state.pekaHuruf,
                        onCheckedChange = { aksi.togglePekaHuruf() }
                    )

                    Text(
                        text = "Peka huruf",
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Checkbox(
                        checked = state.kataUtuh,
                        onCheckedChange = { aksi.toggleKataUtuh() }
                    )

                    Text(
                        text = "Kata utuh",
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Checkbox(
                        checked = state.gunakanRegex,
                        onCheckedChange = { aksi.toggleRegex() }
                    )

                    Text("Regex")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(onClick = aksi::cariBerikutnya) {
                        Text("Berikutnya")
                    }

                    Button(
                        onClick = aksi::cariSebelumnya,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Sebelumnya")
                    }

                    Button(
                        onClick = aksi::bukaRiwayatCari,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Riwayat")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(
                        onClick = aksi::gantiSatu,
                        enabled = !state.hanyaBaca
                    ) {
                        Text("Ganti")
                    }

                    Button(
                        onClick = aksi::gantiSemua,
                        enabled = !state.hanyaBaca,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Ganti Semua")
                    }
                }
            }
        }
    }

    if (state.konfirmasiTutup) {
        AlertDialog(
            onDismissRequest = aksi::batalTutup,
            title = {
                Text("Perubahan belum disimpan")
            },
            text = {
                Column {
                    Text("Simpan perubahan sebelum menutup?")

                    Button(
                        onClick = aksi::simpanDanTutup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Simpan")
                    }

                    Button(
                        onClick = aksi::tutupTanpaSimpan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Buang perubahan")
                    }

                    Button(
                        onClick = aksi::batalTutup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Batal")
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (state.panelStatistikTerbuka) {
        DialogStatistik(state, aksi)
    }

    if (state.dialogNavigasiBaris) {
        DialogNavigasiBaris(state, aksi)
    }

    if (state.dialogRiwayatCari) {
        DialogRiwayatCari(state, aksi)
    }

if (state.konfirmasiKonflikEksternal) {
    AlertDialog(
        onDismissRequest = aksi::batalKonflikEksternal,
        title = {
            Text("File berubah di luar aplikasi")
        },
        text = {
            Column {
                Text(
                    "File di penyimpanan tampak berubah setelah dibuka. " +
                        "Menimpa dapat menghapus perubahan eksternal."
                )

                Button(
                    onClick = aksi::timpaKonflikEksternal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Tetap timpa")
                }

                Button(
                    onClick = aksi::muatUlangKonflikEksternal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Muat ulang dari file")
                }

                Button(
                    onClick = aksi::batalKonflikEksternal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Batal")
                }
            }
        },
        confirmButton = {}
    )
}
}

@Composable
private fun DialogStatistik(
    state: EditorState,
    aksi: EditorAksi
) {
    AlertDialog(
        onDismissRequest = aksi::tutupPanelStatistik,
        title = {
            Text("Statistik file")
        },
        text = {
            Column {
                if (state.sedangMenghitungStatistik) {
                    CircularProgressIndicator()
                } else {
                    state.statistik?.let { statistik ->
                        Text("Ukuran: ${formatUkuran(statistik.ukuranByte)}")
                        Text("Encoding: ${statistik.encoding ?: "-"}")
                        Text("Akhir baris: ${labelAkhirBaris(statistik.akhirBaris)}")
                        Text("Baris: ${statistik.jumlahBaris}")
                        Text("Baris kosong: ${statistik.jumlahBarisKosong}")
                        Text("Baris tidak kosong: ${statistik.jumlahBarisTidakKosong}")
                        Text("Kata: ${statistik.jumlahKata}")
                        Text("Karakter: ${statistik.jumlahKarakter}")
                        Text("Baris terpanjang: ${statistik.panjangBarisTerpanjang}")
                        Text("Baris kode: ${statistik.jumlahBarisKode}")
                    }
                }

                Button(
                    onClick = aksi::hitungStatistik,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Hitung ulang")
                }
            }
        },
        confirmButton = {
            Button(onClick = aksi::tutupPanelStatistik) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun DialogNavigasiBaris(
    state: EditorState,
    aksi: EditorAksi
) {
    AlertDialog(
        onDismissRequest = aksi::tutupDialogNavigasiBaris,
        title = {
            Text("Menuju nomor baris")
        },
        text = {
            TextField(
                value = state.nomorBarisInput,
                onValueChange = aksi::ubahNomorBarisInput,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        },
        confirmButton = {
            Button(onClick = aksi::konfirmasiNavigasiBaris) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = aksi::tutupDialogNavigasiBaris) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun DialogRiwayatCari(
    state: EditorState,
    aksi: EditorAksi
) {
    AlertDialog(
        onDismissRequest = aksi::tutupRiwayatCari,
        title = {
            Text("Riwayat pencarian")
        },
        text = {
            Column {
                if (state.riwayatCari.isEmpty()) {
                    Text("Belum ada riwayat pencarian.")
                } else {
                    state.riwayatCari.forEach { item ->
                        Button(
                            onClick = { aksi.pilihRiwayatCari(item) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(item)
                        }
                    }
                }

                Button(
                    onClick = aksi::hapusRiwayatCari,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Hapus riwayat")
                }
            }
        },
        confirmButton = {
            Button(onClick = aksi::tutupRiwayatCari) {
                Text("Tutup")
            }
        }
    )
}

private fun formatUkuran(byte: Long?): String {
    if (byte == null) return "-"

    if (byte < 1024) {
        return "$byte B"
    }

    val kb = byte / 1024

    if (kb < 1024) {
        return "$kb KiB"
    }

    val mb = kb / 1024

    if (mb < 1024) {
        return "$mb MiB"
    }

    val gb = mb / 1024

    return "$gb GiB"
}

private fun labelAkhirBaris(jenis: JenisAkhirBaris?): String {
    return when (jenis) {
        JenisAkhirBaris.LF -> "LF"
        JenisAkhirBaris.CRLF -> "CRLF"
        JenisAkhirBaris.CR -> "CR"
        JenisAkhirBaris.CAMPURAN -> "Campuran"
        null -> "-"
    }
}