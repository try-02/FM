package id.manajerberkas.kode

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.enableEdgeToEdge(window)
        super.onCreate(savedInstanceState)

        setContent {
            val peluncurSaf = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri != null) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) {}
                    }
                    viewModel.simpanSafTerpilih(uri)
                }
            }

            val peluncurIzinSemuaBerkas = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    viewModel.stateHolder.bukaAwal(PabrikLokasiAndroid.eksternalRoot())
                }
            }

            LaunchedEffect(Unit) {
                viewModel.inisialisasiAwalJikaPerlu {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    try {
                        peluncurIzinSemuaBerkas.launch(intent)
                    } catch (_: Exception) {
                        peluncurIzinSemuaBerkas.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    }
                }
            }

            AplikasiUtamaAndroid(
                stateHolder = viewModel.stateHolder,
                dependensiPenjelajah = viewModel.dependensiPenjelajah,
                dependensiEditor = viewModel.dependensiEditor,
                dapatkanEditorHolder = viewModel::dapatkanEditorStateHolder,
                onBukaPrivat = {
                    viewModel.stateHolder.bukaAwal(PabrikLokasiAndroid.privatRoot())
                },
                onBukaSaf = {
                    peluncurSaf.launch(null)
                },
                onBukaEksternal = {
                    viewModel.stateHolder.bukaAwal(PabrikLokasiAndroid.eksternalRoot())
                }
            )
        }
    }
}