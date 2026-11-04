package id.manajerberkas.kode.presentation.layar

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.JenisAkhirBaris
import id.manajerberkas.kode.presentation.state.EditorAksi
import id.manajerberkas.kode.presentation.state.EditorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorLayar(
    state: EditorState,
    aksi: EditorAksi
) {
    var menuTerbuka by remember { mutableStateOf(false) }
    var cariPanelTerbuka by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.lokasi?.namaTampilan ?: "Editor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.hanyaBaca) "Hanya Baca" else if (state.adaPerubahan) "Belum Disimpan" else "Tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.adaPerubahan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = aksi::mintaTutup) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                },
                actions = {
                    IconButton(onClick = aksi::undo, enabled = state.bisaUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = aksi::redo, enabled = state.bisaRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = { cariPanelTerbuka = !cariPanelTerbuka }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari / Ganti")
                    }
                    IconButton(
                        onClick = aksi::simpan,
                        enabled = !state.hanyaBaca && state.adaPerubahan && !state.sedangMenyimpan
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.primary)
                    }

                    Box {
                        IconButton(onClick = { menuTerbuka = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Lainnya")
                        }
                        DropdownMenu(
                            expanded = menuTerbuka,
                            onDismissRequest = { menuTerbuka = false },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lompat ke Baris") },
                                leadingIcon = { Icon(Icons.Default.FormatListNumbered, null) },
                                onClick = { menuTerbuka = false; aksi.bukaDialogNavigasiBaris() }
                            )
                            DropdownMenuItem(
                                text = { Text("Statistik Berkas") },
                                leadingIcon = { Icon(Icons.Default.BarChart, null) },
                                onClick = { menuTerbuka = false; aksi.bukaPanelStatistik() }
                            )
                            DropdownMenuItem(
                                text = { Text(if (state.bungkusKata) "Bungkus Kata: ON" else "Bungkus Kata: OFF") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.WrapText, null) },
                                onClick = { menuTerbuka = false; aksi.toggleBungkusKata() }
                            )
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
            // Panel Cari & Ganti Expressive
            AnimatedVisibility(visible = cariPanelTerbuka) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.cari,
                                onValueChange = aksi::ubahCari,
                                placeholder = { Text("Cari teks") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.ganti,
                                onValueChange = aksi::ubahGanti,
                                placeholder = { Text("Ganti dengan") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                enabled = !state.hanyaBaca,
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = state.pekaHuruf,
                                onClick = aksi::togglePekaHuruf,
                                label = { Text("Aa") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            FilterChip(
                                selected = state.kataUtuh,
                                onClick = aksi::toggleKataUtuh,
                                label = { Text("\\b") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            FilterChip(
                                selected = state.gunakanRegex,
                                onClick = aksi::toggleRegex,
                                label = { Text(".*") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = aksi::cariSebelumnya) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Sebelumnya")
                            }
                            IconButton(onClick = aksi::cariBerikutnya) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Berikutnya")
                            }
                        }

                        if (!state.hanyaBaca) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = aksi::gantiSatu, shape = RoundedCornerShape(10.dp)) { Text("Ganti") }
                                OutlinedButton(onClick = aksi::gantiSemua, shape = RoundedCornerShape(10.dp)) { Text("Ganti Semua") }
                            }
                        }
                    }
                }
            }

            if (state.memuat || state.sedangMenyimpan) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                if (state.bungkusKata) {
                    TextField(
                        value = state.medan,
                        onValueChange = aksi::ubahTeks,
                        readOnly = state.hanyaBaca,
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace)
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
                            modifier = Modifier.fillMaxHeight().widthIn(min = 800.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }

            // Info Bar Bawah
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Baris ${if (state.barisAktif > 0) state.barisAktif else "-"}, Kolom ${if (state.kolomAktif > 0) state.kolomAktif else "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    state.pesan?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }

    // Dialog Konfirmasi & Tools
    if (state.konfirmasiTutup) {
        AlertDialog(
            onDismissRequest = aksi::batalTutup,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Simpan Perubahan?") },
            text = { Text("Berkas ini memiliki perubahan yang belum disimpan.") },
            confirmButton = {
                Button(onClick = aksi::simpanDanTutup, shape = RoundedCornerShape(12.dp)) { Text("Simpan & Tutup") }
            },
            dismissButton = {
                TextButton(onClick = aksi::tutupTanpaSimpan) { Text("Buang") }
            }
        )
    }

    if (state.panelStatistikTerbuka) {
        AlertDialog(
            onDismissRequest = aksi::tutupPanelStatistik,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Statistik Berkas") },
            text = {
                state.statistik?.let { stat ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Baris: ${stat.jumlahBaris} (Kode: ${stat.jumlahBarisKode})")
                        Text("Kata: ${stat.jumlahKata}")
                        Text("Karakter: ${stat.jumlahKarakter}")
                        Text("Akhir Baris: ${stat.akhirBaris}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = aksi::tutupPanelStatistik) { Text("Tutup") }
            }
        )
    }

    if (state.dialogNavigasiBaris) {
        var barisInput by remember { mutableStateOf(state.nomorBarisInput) }
        AlertDialog(
            onDismissRequest = aksi::tutupDialogNavigasiBaris,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Lompat ke Baris") },
            text = {
                OutlinedTextField(
                    value = barisInput,
                    onValueChange = { barisInput = it; aksi.ubahNomorBarisInput(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = aksi::konfirmasiNavigasiBaris, shape = RoundedCornerShape(12.dp)) { Text("Lompat") }
            },
            dismissButton = {
                TextButton(onClick = aksi::tutupDialogNavigasiBaris) { Text("Batal") }
            }
        )
    }
}