package id.manajerberkas.kode.presentation.layar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.BarisDiff
import id.manajerberkas.kode.domain.model.BarisDiffBerdampingan
import id.manajerberkas.kode.domain.model.JenisOperasiDiff
import id.manajerberkas.kode.domain.model.ModeDiff
import id.manajerberkas.kode.presentation.state.DiffAksi
import id.manajerberkas.kode.presentation.state.DiffState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffLayar(
    state: DiffState,
    aksi: DiffAksi,
    onTutup: () -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.indeksBarisAktif) {
        state.indeksBarisAktif?.let { listState.animateScrollToItem(it) }
    }
    DisposableEffect(Unit) { onDispose { aksi.batal() } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perbandingan Berkas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onTutup) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                },
                actions = {
                    IconButton(onClick = aksi::perubahanSebelumnya, enabled = state.blokPerubahan.isNotEmpty()) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Sebelumnya")
                    }
                    IconButton(onClick = aksi::perubahanBerikutnya, enabled = state.blokPerubahan.isNotEmpty()) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Berikutnya")
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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("A: ${state.lokasiA?.namaTampilan ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("B: ${state.lokasiB?.namaTampilan ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.mode == ModeDiff.UNIFIED,
                            onClick = { aksi.ubahMode(ModeDiff.UNIFIED) },
                            label = { Text("Unified") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilterChip(
                            selected = state.mode == ModeDiff.BERDAMPINGAN,
                            onClick = { aksi.ubahMode(ModeDiff.BERDAMPINGAN) },
                            label = { Text("Berdampingan") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            if (state.memuat) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.hasil?.let {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    if (state.mode == ModeDiff.UNIFIED) {
                        itemsIndexed(state.hasil.baris) { index, baris ->
                            val isActiveBlock = state.blokPerubahan.getOrNull(state.indeksBlokAktif)?.let {
                                index in it.indeksAwal..it.indeksAkhir
                            } ?: false
                            BarisDiffUnifiedItemExpressive(baris = baris, isActive = isActiveBlock)
                        }
                    } else {
                        itemsIndexed(state.barisBerdampingan) { index, baris ->
                            val isActiveBlock = state.blokPerubahan.getOrNull(state.indeksBlokAktif)?.let {
                                index in it.indeksAwal..it.indeksAkhir
                            } ?: false
                            BarisDiffBerdampinganItemExpressive(baris = baris, isActiveBlock = isActiveBlock)
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun BarisDiffUnifiedItemExpressive(baris: BarisDiff, isActive: Boolean) {
    val bgColor = when {
        isActive -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        baris.jenis == JenisOperasiDiff.HAPUS -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        baris.jenis == JenisOperasiDiff.TAMBAH -> Color(0xFF2E7D32).copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${baris.nomorBarisA ?: " "}  ${baris.nomorBarisB ?: " "}  ",
            style = TextStyle(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = baris.isi,
            style = TextStyle(fontFamily = FontFamily.Monospace)
        )
    }
}
@Composable
private fun BarisDiffBerdampinganItemExpressive(baris: BarisDiffBerdampingan, isActiveBlock: Boolean) {
    val bgA = when {
        isActiveBlock -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        baris.jenisA == JenisOperasiDiff.HAPUS -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        else -> Color.Transparent
    }
    val bgB = when {
        isActiveBlock -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        baris.jenisB == JenisOperasiDiff.TAMBAH -> Color(0xFF2E7D32).copy(alpha = 0.25f)
        else -> Color.Transparent
    }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        // Kolom Sisi A (Berkas Asli)
        Row(
            modifier = Modifier
                .weight(1f)
                .background(bgA)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = (baris.nomorA?.toString() ?: "").padStart(4, ' ') + " ",
                style = TextStyle(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = baris.teksA ?: "",
                style = TextStyle(fontFamily = FontFamily.Monospace),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

        // Kolom Sisi B (Berkas Modifikasi)
        Row(
            modifier = Modifier
                .weight(1f)
                .background(bgB)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = (baris.nomorB?.toString() ?: "").padStart(4, ' ') + " ",
                style = TextStyle(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = baris.teksB ?: "",
                style = TextStyle(fontFamily = FontFamily.Monospace),
                maxLines = 1
            )
        }
    }
}
