package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
        shape = RoundedCornerShape(28.dp),
        title = {
            Text("Markah Cepat", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = aksi::tambahMarkahLokasiAktif,
                    enabled = state.lokasiAktif != null && !state.lokasiAktifSudahMarkah,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Tandai Folder Ini")
                }

                Spacer(Modifier.height(12.dp))

                if (state.daftarMarkah.isEmpty()) {
                    Text(
                        text = "Belum ada folder yang ditandai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.daftarMarkah) { markah ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (markah.jenis == JenisMarkah.FOLDER) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 10.dp)
                                    ) {
                                        Text(markah.lokasi.namaTampilan, fontWeight = FontWeight.Medium)
                                        Text(
                                            markah.lokasi.jalurTampilan ?: markah.lokasi.id,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = { aksi.bukaMarkah(markah) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Buka", style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(onClick = { aksi.hapusMarkah(markah) }) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = aksi::tutupDialogMarkah) {
                Text("Tutup")
            }
        }
    )
}