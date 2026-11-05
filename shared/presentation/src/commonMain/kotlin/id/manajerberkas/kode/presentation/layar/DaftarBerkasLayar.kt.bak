package id.manajerberkas.kode.presentation.layar

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.JenisItem
import id.manajerberkas.kode.presentation.state.DaftarBerkasAksi
import id.manajerberkas.kode.presentation.state.DaftarBerkasState
import id.manajerberkas.kode.presentation.state.ModePilihTujuan

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DaftarBerkasLayar(
    state: DaftarBerkasState,
    aksi: DaftarBerkasAksi,
    onBukaPrivat: () -> Unit,
    onBukaSaf: () -> Unit
) {
    val idTerpilih = remember(state.itemTerpilih) {
        state.itemTerpilih.map { it.lokasi.id }.toSet()
    }
    var menuOpsiTerbuka by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            when {
                state.modePilihTujuan != null -> {
                    TopAppBar(
                        title = {
                            Text(
                                if (state.modePilihTujuan == ModePilihTujuan.SALIN) "Pilih Folder Salin" else "Pilih Folder Pindah",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = aksi::batalPilihTujuan) {
                                Icon(Icons.Default.Close, contentDescription = "Batal")
                            }
                        },
                        actions = {
                            Button(
                                onClick = aksi::konfirmasiTujuan,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Pilih Sini")
                            }
                        }
                    )
                }
                state.modePilih -> {
                    TopAppBar(
                        title = {
                            Text(
                                "${state.itemTerpilih.size} dipilih",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = aksi::batalkanPilih) {
                                Icon(Icons.Default.Close, contentDescription = "Batal")
                            }
                        },
                        actions = {
                            IconButton(onClick = aksi::pilihSemua) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Pilih Semua")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                else -> {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = state.lokasiAktif?.namaTampilan ?: "Penyimpanan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = state.lokasiAktif?.jalurTampilan ?: "/",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            if (state.bisaKembali) {
                                IconButton(onClick = aksi::kembali) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                                }
                            } else {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = aksi::bukaPencarian) {
                                Icon(Icons.Default.Search, contentDescription = "Cari")
                            }
                            Box {
                                IconButton(onClick = { menuOpsiTerbuka = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = menuOpsiTerbuka,
                                    onDismissRequest = { menuOpsiTerbuka = false },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Pencarian Isi Teks") },
                                        leadingIcon = { Icon(Icons.Default.FindInPage, null) },
                                        onClick = { menuOpsiTerbuka = false; aksi.bukaPencarianIsi() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Markah Tersimpan") },
                                        leadingIcon = { Icon(Icons.Default.Bookmarks, null) },
                                        onClick = { menuOpsiTerbuka = false; aksi.bukaDialogMarkah() }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Jadikan Beranda") },
                                        leadingIcon = { Icon(Icons.Default.Home, null) },
                                        enabled = state.lokasiAktif != null && !state.lokasiAktifAdalahBeranda,
                                        onClick = { menuOpsiTerbuka = false; aksi.jadikanBeranda() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Hapus Beranda") },
                                        leadingIcon = { Icon(Icons.Default.HomeWork, null) },
                                        enabled = state.beranda != null,
                                        onClick = { menuOpsiTerbuka = false; aksi.hapusBeranda() }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (state.modePilih) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = aksi::mulaiSalin,
                            enabled = state.kemampuan?.bisaSalin == true,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Salin")
                        }
                        FilledTonalButton(
                            onClick = aksi::mulaiPindah,
                            enabled = state.kemampuan?.bisaPindah == true,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.DriveFileMove, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pindah")
                        }
                        FilledTonalButton(
                            onClick = aksi::mintaHapusTerpilih,
                            enabled = state.kemampuan?.bisaHapus == true,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Hapus")
                        }
                        if (state.itemTerpilih.size == 2 && state.itemTerpilih.all { it.jenis == JenisItem.BERKAS }) {
                            Button(
                                onClick = aksi::mulaiBandingkan,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Difference, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Diff")
                            }
                        }
                        if (state.itemTerpilih.isNotEmpty()) {
                            OutlinedButton(
                                onClick = aksi::mulaiGantiNamaMassal,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Ganti Nama")
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!state.modePilih && state.modePilihTujuan == null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.kemampuan?.bisaBuatFolder == true) {
                        SmallFloatingActionButton(
                            onClick = aksi::bukaDialogBuatFolder,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Folder Baru")
                        }
                    }
                    if (state.kemampuan?.bisaBuatBerkas == true) {
                        ExtendedFloatingActionButton(
                            onClick = aksi::bukaDialogBuatBerkas,
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(20.dp),
                            icon = { Icon(Icons.Default.Add, null) },
                            text = { Text("File Baru", fontWeight = FontWeight.Bold) }
                        )
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
            // Chip Lokasi Cepat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isPrivat = state.lokasiAktif?.id?.contains("privat", ignoreCase = true) == true
                FilterChip(
                    selected = isPrivat,
                    onClick = onBukaPrivat,
                    label = { Text("Privat") },
                    leadingIcon = { Icon(Icons.Default.FolderSpecial, null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(14.dp)
                )
                FilterChip(
                    selected = !isPrivat && state.lokasiAktif != null,
                    onClick = onBukaSaf,
                    label = { Text("SAF Eksternal") },
                    leadingIcon = { Icon(Icons.Default.SdCard, null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(14.dp)
                )
            }

            AnimatedVisibility(visible = state.sedangOperasi || state.memuat) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            state.pesan?.let { pesan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(pesan, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }

            if (state.daftar.isEmpty() && !state.memuat) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.FolderOpen, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Folder kosong", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.daftar, key = { it.lokasi.id }) { item ->
                        val terpilih = idTerpilih.contains(item.lokasi.id)
                        ItemBerkasCardExpressive(
                            item = item,
                            terpilih = terpilih,
                            modePilih = state.modePilih || state.modePilihTujuan != null,
                            onClick = { aksi.klikItem(item) },
                            onLongClick = { aksi.tekanLama(item) }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tambah/Ganti Nama & Konfirmasi
    if (state.dialogBuatBerkas) {
        DialogNamaExpressive("Buat File", "", aksi::tutupDialogBuatBerkas, aksi::buatBerkas)
    }
    if (state.dialogBuatFolder) {
        DialogNamaExpressive("Buat Folder", "", aksi::tutupDialogBuatFolder, aksi::buatFolder)
    }
    state.dialogGantiNama?.let { item ->
        key(item.lokasi.id) {
            DialogNamaExpressive("Ganti Nama", item.nama, aksi::tutupDialogGantiNama, aksi::gantiNama)
        }
    }
    if (state.dialogMarkah) {
        DialogMarkah(state, aksi)
    }
    if (state.konfirmasiHapusTerpilih) {
        AlertDialog(
            onDismissRequest = aksi::batalHapus,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Hapus ${state.itemTerpilih.size} item?", fontWeight = FontWeight.Bold) },
            text = { Text("Item yang dihapus tidak dapat dipulihkan.") },
            confirmButton = {
                Button(
                    onClick = aksi::konfirmasiHapus,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = aksi::batalHapus) { Text("Batal") }
            }
        )
    }
    state.menuUntukItem?.let { item ->
        DialogMenuKonteksExpressive(state, aksi, item)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemBerkasCardExpressive(
    item: ItemBerkas,
    terpilih: Boolean,
    modePilih: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        color = if (terpilih) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (terpilih) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (modePilih) {
                Checkbox(
                    checked = terpilih,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            val isFolder = item.jenis == JenisItem.FOLDER
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isFolder) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFolder) Icons.Default.Folder else dapatkanIkonBerkas(item.nama),
                    contentDescription = null,
                    tint = if (isFolder) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = item.nama,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.lokasi.jalurTampilan ?: item.lokasi.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun dapatkanIkonBerkas(nama: String): ImageVector {
    val n = nama.lowercase()
    return when {
        n.endsWith(".zip") || n.endsWith(".tar") || n.endsWith(".gz") || n.endsWith(".7z") -> Icons.Default.FolderZip
        n.endsWith(".kt") || n.endsWith(".java") || n.endsWith(".py") || n.endsWith(".js") || n.endsWith(".json") || n.endsWith(".xml") -> Icons.Default.Code
        n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".webp") -> Icons.Default.Image
        n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".m4a") -> Icons.Default.AudioFile
        n.endsWith(".mp4") || n.endsWith(".mkv") -> Icons.Default.VideoFile
        n.endsWith(".pdf") -> Icons.Default.PictureAsPdf
        else -> Icons.Default.Description
    }
}

@Composable
private fun DialogNamaExpressive(
    judul: String,
    nilaiAwal: String,
    onTutup: () -> Unit,
    onKonfirmasi: (String) -> Unit
) {
    var teks by remember { mutableStateOf(nilaiAwal) }
    AlertDialog(
        onDismissRequest = onTutup,
        shape = RoundedCornerShape(28.dp),
        title = { Text(judul, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = teks,
                onValueChange = { teks = it },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (teks.isNotBlank()) onKonfirmasi(teks.trim()) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onTutup) { Text("Batal") }
        }
    )
}

@Composable
private fun DialogMenuKonteksExpressive(
    state: DaftarBerkasState,
    aksi: DaftarBerkasAksi,
    item: ItemBerkas
) {
    val satuItem = state.itemTerpilih.size <= 1
    AlertDialog(
        onDismissRequest = aksi::tutupMenu,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (state.itemTerpilih.size > 1) "${state.itemTerpilih.size} item" else item.nama, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = { aksi.bukaItemDariMenu(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Buka") }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = aksi::mulaiSalin,
                        enabled = state.kemampuan?.bisaSalin == true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Salin") }
                    FilledTonalButton(
                        onClick = aksi::mulaiPindah,
                        enabled = state.kemampuan?.bisaPindah == true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Pindah") }
                }

                if (satuItem && state.kemampuan?.bisaGantiNama == true) {
                    OutlinedButton(
                        onClick = aksi::bukaDialogGantiNama,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Ganti Nama") }
                }

                OutlinedButton(
                    onClick = { aksi.tambahMarkahItem(item) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Tambah ke Markah") }

                Button(
                    onClick = aksi::mintaHapusTerpilih,
                    enabled = state.kemampuan?.bisaHapus == true,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Hapus") }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = aksi::tutupMenu) { Text("Tutup") }
        }
    )
}