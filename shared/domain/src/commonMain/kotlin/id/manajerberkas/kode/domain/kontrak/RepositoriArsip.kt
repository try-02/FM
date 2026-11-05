package id.manajerberkas.kode.domain.kontrak
import id.manajerberkas.kode.domain.model.HasilDaftarArsip
import id.manajerberkas.kode.domain.model.HasilEkstraksi
import id.manajerberkas.kode.domain.model.LokasiBerkas
interface RepositoriArsip {
    suspend fun daftarIsi(lokasiArsip: LokasiBerkas): HasilDaftarArsip
    suspend fun ekstrakItem(
        lokasiArsip: LokasiBerkas,
        jalurItem: String,
        lokasiTujuan: LokasiBerkas
    ): HasilEkstraksi
    suspend fun ekstrakSemua(
        lokasiArsip: LokasiBerkas,
        lokasiTujuan: LokasiBerkas
    ): HasilEkstraksi
}
