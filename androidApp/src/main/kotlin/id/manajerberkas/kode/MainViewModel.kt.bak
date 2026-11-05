package id.manajerberkas.kode

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.manajerberkas.kode.data.adapter.PabrikLokasiAndroid
import id.manajerberkas.kode.data.lokal.DatabaseAplikasi
import id.manajerberkas.kode.data.repositori.*
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.usecase.*
import id.manajerberkas.kode.presentation.state.DaftarBerkasStateHolder
import id.manajerberkas.kode.presentation.state.DependensiEditor
import id.manajerberkas.kode.presentation.state.DependensiPenjelajah
import id.manajerberkas.kode.presentation.state.EditorStateHolder
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences("preferensi_manajer_berkas", Context.MODE_PRIVATE)

    private val driver = AndroidSqliteDriver(
        schema = DatabaseAplikasi.Schema,
        context = application.applicationContext,
        name = "manajerberkas.db"
    )
    private val database = DatabaseAplikasi(driver)

    private val repositoriBerkas = RepositoriBerkasAndroid(application.applicationContext)
    private val repositoriPencarianNama = RepositoriPencarianNamaAndroid(application.applicationContext)
    private val repositoriPencarianIsi = RepositoriPencarianIsiAndroid(application.applicationContext)
    private val repositoriArsip = RepositoriArsipAndroid(application.applicationContext)

    private val repositoriMarkah = RepositoriMarkahSql(database)
    private val repositoriBeranda = RepositoriBerandaSql(database)
    private val repositoriRiwayat = RepositoriRiwayatPencarianSql(database)
    private val repositoriPreferensi = RepositoriPreferensiEditorSql(database)

    val dependensiPenjelajah = DependensiPenjelajah(
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

    val dependensiEditor = DependensiEditor(
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

    val stateHolder = DaftarBerkasStateHolder(
        dependensi = dependensiPenjelajah,
        scope = viewModelScope
    )

    val pencarianIsiStateHolder = PencarianIsiStateHolder(
        cariIsiBerkas = dependensiPenjelajah.cariIsiBerkas,
        scope = viewModelScope
    )

    // Cache state editor aktif agar tidak hilang saat rotasi layar / recreate Activity
    private var editorStateHolderAktif: EditorStateHolder? = null

    fun dapatkanEditorStateHolder(lokasi: LokasiBerkas, barisAwal: Int?): EditorStateHolder {
        val existing = editorStateHolderAktif
        if (existing != null && existing.state.value.lokasi?.id == lokasi.id) {
            return existing
        }
        val holder = EditorStateHolder(
            lokasi = lokasi,
            dependensi = dependensiEditor,
            scope = viewModelScope,
            onTutup = {
                tutupEditor()
            },
            barisAwal = barisAwal
        )
        editorStateHolderAktif = holder
        return holder
    }

    fun tutupEditor() {
        editorStateHolderAktif = null
        stateHolder.selesaiBukaEditor()
    }

    fun inisialisasiAwalJikaPerlu(mintaIzinSemuaBerkas: () -> Unit) {
        // Hanya buka beranda jika belum ada lokasi aktif (fresh start, bukan saat rotasi layar)
        if (stateHolder.state.value.lokasiAktif != null) return

        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    stateHolder.bukaAwalBeranda(PabrikLokasiAndroid.eksternalRoot())
                } else {
                    // Coba periksa apakah sebelumnya sudah ada SAF yang disimpan
                    val safUriString = preferences.getString("saf_terakhir_uri", null)
                    val safUri = safUriString?.let { Uri.parse(it) }
                    val lokasiSaf = safUri?.let { PabrikLokasiAndroid.safRoot(it) }

                    if (lokasiSaf != null) {
                        stateHolder.bukaAwalBeranda(lokasiSaf)
                    } else {
                        stateHolder.bukaAwalBeranda(PabrikLokasiAndroid.privatRoot())
                    }
                    mintaIzinSemuaBerkas()
                }
            } else {
                stateHolder.bukaAwalBeranda(PabrikLokasiAndroid.eksternalRoot())
            }
        }
    }

    fun simpanSafTerpilih(uri: Uri) {
        preferences.edit()
            .putString("saf_terakhir_uri", uri.toString())
            .putBoolean("pertama_kali_buka", false)
            .apply()
        PabrikLokasiAndroid.safRoot(uri)?.let { lokasi ->
            stateHolder.bukaAwal(lokasi)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            driver.close()
        } catch (_: Exception) {}
    }
}