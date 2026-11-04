package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.JenisItem
import id.manajerberkas.kode.domain.model.ModePencarian
import id.manajerberkas.kode.presentation.state.PencarianNamaState

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Berkas", fontWeight = FontWeight.Bold) },
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
                        placeholder = { Text("Ketik nama file / folder...") },
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.mode == ModePencarian.FOLDER_SAAT_INI,
                            onClick = { onUbahMode(ModePencarian.FOLDER_SAAT_INI) },
                            label = { Text("Folder Ini") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = state.mode == ModePencarian.REKURSIF,
                            onClick = { onUbahMode(ModePencarian.REKURSIF) },
                            label = { Text("Semua Subfolder") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = state.pekaHuruf,
                            onClick = onTogglePekaHuruf,
                            label = { Text("Aa") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.hanyaFolder == true,
                            onClick = { onUbahJenisItem(if (state.hanyaFolder == true) null else true) },
                            label = { Text("Hanya Folder") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        FilterChip(
                            selected = state.hanyaFolder == false,
                            onClick = { onUbahJenisItem(if (state.hanyaFolder == false) null else false) },
                            label = { Text("Hanya Berkas") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            if (state.sedangMencari) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }

            Text(
                text = if (state.pernahMencari) "Ditemukan ${state.hasil.size} hasil" else "Siap mencari.",
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
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.jenis == JenisItem.FOLDER) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(item.nama, fontWeight = FontWeight.Medium)
                                Text(
                                    item.lokasi.jalurTampilan ?: item.lokasi.id,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}