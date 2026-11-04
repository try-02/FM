package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.JenisMarkah
import id.manajerberkas.kode.presentation.state.DaftarBerkasAksi
import id.manajerberkas.kode.presentation.state.DaftarBerkasState

@Composable
fun DialogMarkah(
    state: DaftarBerkasState,
    aksi: DaftarBerkasAksi
) {
    AlertDialog(
        onDismissRequest = aksi::tutupDialogMarkah,
        title = { Text("Markah lokasi") },
        text = {
            Column {
                Button(
                    onClick = aksi::tambahMarkahLokasiAktif,
                    enabled = state.lokasiAktif != null && !state.lokasiAktifSudahMarkah,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tambahkan folder saat ini")
                }

                if (state.daftarMarkah.isEmpty()) {
                    Text(
                        text = "Belum ada markah.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        items(state.daftarMarkah) { markah ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (markah.jenis == JenisMarkah.BERKAS) {
                                        "📄 ${markah.lokasi.namaTampilan}"
                                    } else {
                                        "📁 ${markah.lokasi.namaTampilan}"
                                    }
                                )

                                Text(
                                    text = markah.lokasi.jalurTampilan ?: markah.lokasi.id,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Row(
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Button(
                                        onClick = { aksi.bukaMarkah(markah) }
                                    ) {
                                        Text("Buka")
                                    }

                                    Button(
                                        onClick = { aksi.hapusMarkah(markah) },
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text("Hapus")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = aksi::tutupDialogMarkah) {
                Text("Tutup")
            }
        }
    )
}