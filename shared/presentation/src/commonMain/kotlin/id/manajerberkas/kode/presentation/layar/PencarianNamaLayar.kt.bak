package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.JenisItem
import id.manajerberkas.kode.domain.model.ModePencarian
import id.manajerberkas.kode.presentation.state.PencarianNamaState

@Composable
fun PencarianNamaLayar(
    state: PencarianNamaState,
    onUbahTeks: (String) -> Unit,
    onUbahMode: (ModePencarian) -> Unit,
    onTogglePekaHuruf: () -> Unit,
    onUbahEkstensi: (String) -> Unit,
    onUbahUkuranMin: (String) -> Unit,
    onUbahUkuranMax: (String) -> Unit,
    onUbahJenisItem: (Boolean?) -> Unit,
    onMulai: () -> Unit,
    onBatal: () -> Unit,
    onTutup: () -> Unit,
    onKlikHasil: (ItemBerkas) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(onClick = onTutup) {
                Text("Tutup")
            }

            Button(
                onClick = onMulai,
                enabled = !state.sedangMencari,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Cari")
            }

            Button(
                onClick = onBatal,
                enabled = state.sedangMencari,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Batal")
            }
        }

        Text(
            text = "Lokasi: ${state.lokasiPencarian?.namaTampilan ?: "-"}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        TextField(
            value = state.teks,
            onValueChange = onUbahTeks,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Nama file atau folder") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Button(
                onClick = { onUbahMode(ModePencarian.FOLDER_SAAT_INI) },
                enabled = state.mode != ModePencarian.FOLDER_SAAT_INI
            ) {
                Text("Folder ini")
            }

            Button(
                onClick = { onUbahMode(ModePencarian.REKURSIF) },
                enabled = state.mode != ModePencarian.REKURSIF,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Rekursif")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Checkbox(
                checked = state.pekaHuruf,
                onCheckedChange = { onTogglePekaHuruf() }
            )

            Text(
                text = "Peka huruf",
                modifier = Modifier.padding(end = 8.dp)
            )

            Checkbox(
                checked = state.hanyaFolder == true,
                onCheckedChange = {
                    onUbahJenisItem(if (state.hanyaFolder == true) null else true)
                }
            )

            Text(
                text = "Folder",
                modifier = Modifier.padding(end = 8.dp)
            )

            Checkbox(
                checked = state.hanyaFolder == false,
                onCheckedChange = {
                    onUbahJenisItem(if (state.hanyaFolder == false) null else false)
                }
            )

            Text("File")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            TextField(
                value = state.ekstensi,
                onValueChange = onUbahEkstensi,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ekstensi") }
            )

            TextField(
                value = state.ukuranMinKb,
                onValueChange = onUbahUkuranMin,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                placeholder = { Text("Min KB") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TextField(
                value = state.ukuranMaxKb,
                onValueChange = onUbahUkuranMax,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                placeholder = { Text("Maks KB") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        if (state.sedangMencari) {
            CircularProgressIndicator(
                modifier = Modifier.padding(8.dp)
            )
        }

        Text(
            text = if (state.pernahMencari) {
                "Hasil: ${state.hasil.size}"
            } else {
                "Belum ada pencarian."
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        state.pesan?.let { pesan ->
            Text(
                text = pesan,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.hasil) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onKlikHasil(item) }
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (item.jenis == JenisItem.FOLDER) "📁" else "📄",
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column {
                        Text(item.nama)

                        Text(
                            text = item.lokasi.jalurTampilan ?: item.lokasi.id,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}