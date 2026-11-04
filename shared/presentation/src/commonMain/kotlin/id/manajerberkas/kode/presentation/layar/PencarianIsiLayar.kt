package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.KecocokanIsiFile
import id.manajerberkas.kode.domain.model.ModePencarianIsi
import id.manajerberkas.kode.presentation.state.PencarianIsiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PencarianIsiLayar(
    state: PencarianIsiState,
    onUbahTeks: (String) -> Unit,
    onUbahMode: (ModePencarianIsi) -> Unit,
    onTogglePekaHuruf: () -> Unit,
    onToggleKataUtuh: () -> Unit,
    onToggleRegex: () -> Unit,
    onUbahEkstensi: (String) -> Unit,
    onUbahUkuranMin: (String) -> Unit,
    onUbahUkuranMax: (String) -> Unit,
    onMulai: () -> Unit,
    onBatal: () -> Unit,
    onTutup: () -> Unit,
    onKlikHasil: (KecocokanIsiFile) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Isi Teks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onTutup) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                },
                actions = {
                    if (state.sedangMencari) {
                        TextButton(onClick = onBatal) { Text("Batal") }
                    } else {
                        Button(
                            onClick = onMulai,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Search, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Cari")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.teks,
                        onValueChange = onUbahTeks,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Teks yang dicari di dalam berkas...") }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.mode == ModePencarianIsi.FOLDER_SAAT_INI,
                            onClick = { onUbahMode(ModePencarianIsi.FOLDER_SAAT_INI) },
                            label = { Text("Folder Ini") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = state.mode == ModePencarianIsi.REKURSIF,
                            onClick = { onUbahMode(ModePencarianIsi.REKURSIF) },
                            label = { Text("Rekursif") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = state.pekaHuruf,
                            onClick = onTogglePekaHuruf,
                            label = { Text("Aa") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = state.gunakanRegex,
                            onClick = onToggleRegex,
                            label = { Text(".*") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            if (state.sedangMencari) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }

            Text(
                text = if (state.pernahMencari) "Kecocokan: ${state.jumlahKecocokanTotal} di ${state.hasil.size} file" else "Siap mencari isi.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.hasil) { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onKlikHasil(item) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.namaFile, fontWeight = FontWeight.Bold)
                                Text("Baris ${item.nomorBaris}", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                item.potonganTeks,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}