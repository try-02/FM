package id.manajerberkas.kode.presentation.layar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.ItemArsip
import id.manajerberkas.kode.domain.model.JenisItemArsip
import id.manajerberkas.kode.presentation.state.ArsipState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArsipLayar(
    state: ArsipState,
    onKlikItem: (ItemArsip) -> Unit,
    onTekanLama: (ItemArsip) -> Unit,
    onKembali: () -> Unit,
    onEkstrakTerpilih: () -> Unit,
    onEkstrakSemua: () -> Unit,
    onBatalkanPilih: () -> Unit,
    onTutup: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.lokasiArsip?.namaTampilan ?: "Penjelajah Arsip",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.jalurAktif.isEmpty()) "/" else "/${state.jalurAktif}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (state.bisaKembali) {
                        IconButton(onClick = onKembali) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    } else {
                        IconButton(onClick = onTutup) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                },
                actions = {
                    if (state.modePilih) {
                        TextButton(onClick = onBatalkanPilih) { Text("Batal") }
                    } else {
                        IconButton(onClick = onTutup) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.modePilih) {
                        Button(
                            onClick = onEkstrakTerpilih,
                            enabled = state.itemTerpilih.isNotEmpty() && !state.sedangEkstrak,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FolderZip, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ekstrak (${state.itemTerpilih.size})")
                        }
                    } else {
                        Button(
                            onClick = onEkstrakSemua,
                            enabled = !state.sedangEkstrak && state.kemampuan?.bisaEkstrak == true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Unarchive, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ekstrak Semua Arsip")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.sedangEkstrak || state.memuat) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }

            state.pesan?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.daftarTampil) { item ->
                    val terpilih = state.itemTerpilih.contains(item.jalur)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (terpilih) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .combinedClickable(
                                onClick = { onKlikItem(item) },
                                onLongClick = { onTekanLama(item) }
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.modePilih) {
                                Checkbox(
                                    checked = terpilih,
                                    onCheckedChange = { onKlikItem(item) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (item.jenis == JenisItemArsip.FOLDER) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.jenis == JenisItemArsip.FOLDER) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(item.nama, fontWeight = FontWeight.Medium)
                                if (item.jenis == JenisItemArsip.BERKAS) {
                                    Text(
                                        formatUkuranArsip(item.ukuran),
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
}

private fun formatUkuranArsip(byte: Long): String {
    if (byte < 1024) return "$byte B"
    val kb = byte / 1024
    if (kb < 1024) return "$kb KiB"
    val mb = kb / 1024
    if (mb < 1024) return "$mb MiB"
    return "${mb / 1024} GiB"
}