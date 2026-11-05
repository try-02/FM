package id.manajerberkas.kode

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.data.lokal.DatabaseAplikasi
import id.manajerberkas.kode.data.repositori.RepositoriBerandaSql
import id.manajerberkas.kode.data.repositori.RepositoriBerkasAndroid
import id.manajerberkas.kode.data.repositori.RepositoriMarkahSql
import id.manajerberkas.kode.data.repositori.RepositoriPencarianNamaAndroid
import id.manajerberkas.kode.data.repositori.RepositoriPreferensiEditorSql
import id.manajerberkas.kode.data.repositori.RepositoriRiwayatPencarianSql

// PERBAIKAN: Import Repository yang hilang
import id.manajerberkas.kode.data.repositori.RepositoriPencarianIsiAndroid
import id.manajerberkas.kode.data.repositori.RepositoriArsipZipAndroid

// PERBAIKAN: Import seluruh UseCase lengkap dari domain
import id.manajerberkas.kode.domain.usecase.AmbilBerandaUseCase
import id.manajerberkas.kode.domain.usecase.AmbilMarkahUseCase
import id.manajerberkas.kode.domain.usecase.AmbilPreferensiEditorUseCase
import id.manajerberkas.kode.domain.usecase.AmbilRiwayatPencarianUseCase
import id.manajerberkas.kode.domain.usecase.BacaTeksUseCase
import id.manajerberkas.kode.domain.usecase.BuatBerkasUseCase
import id.manajerberkas.kode.domain.usecase.BuatFolderUseCase
import id.manajerberkas.kode.domain.usecase.CariIsiBerkasUseCase
import id.manajerberkas.kode.domain.usecase.CariNamaBerkasUseCase
import id.manajerberkas.kode.domain.usecase.DaftarIsiArsipUseCase
import id.manajerberkas.kode.domain.usecase.DaftarIsiFolderUseCase
import id.manajerberkas.kode.domain.usecase.DapatkanKemampuanPenyimpananUseCase
import id.manajerberkas.kode.domain.usecase.DapatkanMetadataBerkasUseCase
import id.manajerberkas.kode.domain.usecase.EksekusiGantiNamaMassalUseCase
import id.manajerberkas.kode.domain.usecase.EkstrakItemArsipUseCase
import id.manajerberkas.kode.domain.usecase.EkstrakSemuaArsipUseCase
import id.manajerberkas.kode.domain.usecase.GantiNamaBerkasUseCase
import id.manajerberkas.kode.domain.usecase.HapusBerkasUseCase
import id.manajerberkas.kode.domain.usecase.HapusBerandaUseCase
import id.manajerberkas.kode.domain.usecase.HapusMarkahUseCase
import id.manajerberkas.kode.domain.usecase.HapusRiwayatPencarianUseCase
import id.manajerberkas.kode.domain.usecase.HitungStatistikTeksUseCase
import id.manajerberkas.kode.domain.usecase.PindahkanBerkasUseCase
import id.manajerberkas.kode.domain.usecase.SalinBerkasUseCase
import id.manajerberkas.kode.domain.usecase.SimpanBerandaUseCase
import id.manajerberkas.kode.domain.usecase.SimpanMarkahUseCase
import id.manajerberkas.kode.domain.usecase.SimpanPreferensiEditorUseCase
import id.manajerberkas.kode.domain.usecase.SimpanRiwayatPencarianUseCase
import id.manajerberkas.kode.domain.usecase.SimpanTeksUseCase

import id.manajerberkas.kode.presentation.state.DaftarBerkasStateHolder
import id.manajerberkas.kode.presentation.state.DependensiEditor
import id.manajerberkas.kode.presentation.state.DependensiPenjelajah

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.enableEdgeToEdge(window)
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val repositoriBerkas = remember {
                RepositoriBerkasAndroid(context.applicationContext)
            }

            val repositoriPencarianNama = remember {
                RepositoriPencarianNamaAndroid(context.applicationContext)
            }

            val driver = remember {
                AndroidSqliteDriver(
                    schema = DatabaseAplikasi.Schema,
                    context = context.applicationContext,
                    name = "manajerberkas.db"
                )
            }

            val database = remember {
                DatabaseAplikasi(driver)
            }

            val repositoriMarkah = remember { RepositoriMarkahSql(database) }
            val repositoriBeranda = remember { RepositoriBerandaSql(database) }
            val repositoriRiwayat = remember { RepositoriRiwayatPencarianSql(database) }
            val repositoriPreferensi = remember { RepositoriPreferensiEditorSql(database) }

            val repositoriPencarianIsi = remember {
                RepositoriPencarianIsiAndroid(context.applicationContext)
            }

            val repositoriArsip = remember {
                RepositoriArsipZipAndroid(context.applicationContext)
            }

            val dependensiPenjelajah = remember {
                DependensiPenjelajah(
                    daftarIsiFolder = DaftarIsiFolderUseCase(repositoriBerkas),
                    dapatkanKemampuan = DapatkanKemampuanPenyimpananUseCase(repositoriBerkas),
                    buatBerkas = BuatBerkasUseCase(repositoriBerkas),
                    buatFolder = BuatFolderUseCase(repositoriBerkas),
                    gantiNama = GantiNamaBerkasUseCase(repositoriBerkas),
                    hapusBerkas = HapusBerkasUseCase(repositoriBerkas),
                    salinBerkas = SalinBerkasUseCase(repositoriBerkas),
                    pindahkanBerkas = PindahkanBerkasUseCase(repositoriBerkas),
                    ambilMarkah = AmbilMarkahUseCase(repositoriMarkah),
                    simpanMarkah = SimpanMarkahUseCase(repositoriMarkah),
                    hapusMarkah = HapusMarkahUseCase(repositoriMarkah),
                    ambilBeranda = AmbilBerandaUseCase(repositoriBeranda),
                    simpanBeranda = SimpanBerandaUseCase(repositoriBeranda),
                    hapusBeranda = HapusBerandaUseCase(repositoriBeranda),
                    cariNamaBerkas = CariNamaBerkasUseCase(repositoriPencarianNama),
                    cariIsiBerkas = CariIsiBerkasUseCase(repositoriPencarianIsi),
                    eksekusiGantiNamaMassal = EksekusiGantiNamaMassalUseCase(repositoriBerkas),
                    daftarIsiArsip = DaftarIsiArsipUseCase(repositoriArsip),
                    ekstrakItemArsip = EkstrakItemArsipUseCase(repositoriArsip),
                    ekstrakSemuaArsip = EkstrakSemuaArsipUseCase(repositoriArsip)
                )
            }

            val dependensiEditor = remember {
                DependensiEditor(
                    bacaTeks = BacaTeksUseCase(repositoriBerkas),
                    simpanTeks = SimpanTeksUseCase(repositoriBerkas),
                    hitungStatistik = HitungStatistikTeksUseCase(),
                    dapatkanMetadata = DapatkanMetadataBerkasUseCase(repositoriBerkas),
                    ambilRiwayat = AmbilRiwayatPencarianUseCase(repositoriRiwayat),
                    simpanRiwayat = SimpanRiwayatPencarianUseCase(repositoriRiwayat),
                    hapusRiwayat = HapusRiwayatPencarianUseCase(repositoriRiwayat),
                    ambilPreferensi = AmbilPreferensiEditorUseCase(repositoriPreferensi),
                    simpanPreferensi = SimpanPreferensiEditorUseCase(repositoriPreferensi)
                )
            }

            val stateHolder = remember {
                DaftarBerkasStateHolder(
                    dependensi = dependensiPenjelajah,
                    scope = scope
                )
            }

            val peluncurSaf = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri != null) {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        try {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (ignored: Exception) {
                        }
                    }

                    PabrikLokasiAndroid.safRoot(uri)?.let { lokasi ->
                        stateHolder.bukaAwal(lokasi)
                    }
                }
            }

            LaunchedEffect(Unit) {
                stateHolder.bukaAwalBeranda(PabrikLokasiAndroid.privatRoot())
            }

            AplikasiUtamaAndroid(
                stateHolder = stateHolder,
                dependensiPenjelajah = dependensiPenjelajah,
                dependensiEditor = dependensiEditor,
                onBukaPrivat = {
                    stateHolder.bukaAwal(PabrikLokasiAndroid.privatRoot())
                },
                onBukaSaf = {
                    peluncurSaf.launch(null)
                }
            )
        }
    }
}