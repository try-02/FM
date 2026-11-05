package id.manajerberkas.kode.presentation.layar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.manajerberkas.kode.domain.model.HasilPratinjauGantiNama
import id.manajerberkas.kode.presentation.state.GantiNamaMassalState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GantiNamaMassalLayar(
    state: GantiNamaMassalState,
    onUbahPola: (String) -> Unit,
    onUbahNomorAwal: (String) -> Unit,
    onEksekusi: () -> Unit,
    onTutup: () -> Unit,
    onTutupSetelahEksekusi: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ganti Nama Massal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onTutup) {
                        Icon(Icons.Default.Close, contentDescription = "Batal")
                    }
                },
                actions = {
                    Button(
                        onClick = onEksekusi,
                        enabled = !state.sedangMemproses && state.jumlahValid > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.DriveFileRenameOutline, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Terapkan (${state.jumlahValid})")
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.pola,
                        onValueChange = onUbahPola,
                        label = { Text("Pola (contoh: {P}_{N}{S})") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.nomorAwal,
                        onValueChange = onUbahNomorAwal,
                        label = { Text("Nomor Mulai") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (state.sedangMemproses) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.pratinjau) { item ->
                    ItemPratinjauExpressive(item)
                }
            }
        }
    }
    state.hasilEksekusi?.let { hasil ->
        AlertDialog(
            onDismissRequest = onTutupSetelahEksekusi,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Operasi Selesai", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Berhasil diubah: ${hasil.count { it.sukses }} berkas")
                    Text("Gagal: ${hasil.count { !it.sukses }}")
                }
            },
            confirmButton = {
                Button(onClick = onTutupSetelahEksekusi, shape = RoundedCornerShape(12.dp)) {
                    Text("Selesai")
                }
            }
        )
    }
}
@Composable
private fun ItemPratinjauExpressive(item: HasilPratinjauGantiNama) {
    val warnaStatus = when {
        item.pesanError != null || item.konflik -> MaterialTheme.colorScheme.error
        item.namaBaru != null -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.namaLama, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "→ ${item.namaBaru ?: "(gagal)"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = warnaStatus
            )
        }
    }
}
