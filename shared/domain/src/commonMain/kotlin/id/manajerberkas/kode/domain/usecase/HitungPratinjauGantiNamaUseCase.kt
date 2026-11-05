package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.model.HasilPratinjauGantiNama
import id.manajerberkas.kode.domain.model.ItemGantiNama
import id.manajerberkas.kode.domain.model.KueriGantiNamaMassal
class HitungPratinjauGantiNamaUseCase {
    operator fun invoke(
        items: List<ItemGantiNama>,
        kueri: KueriGantiNamaMassal
    ): List<HasilPratinjauGantiNama> {
        val hasil = mutableListOf<HasilPratinjauGantiNama>()
        for ((index, item) in items.withIndex()) {
            try {
                val namaBaru = terapkanPola(
                    pola = kueri.pola,
                    namaLama = item.namaLama,
                    nomorUrut = index,
                    nomorAwal = kueri.nomorAwal
                )
                if (namaBaru.isBlank()) {
                    hasil.add(
                        HasilPratinjauGantiNama(
                            lokasi = item.lokasi,
                            namaLama = item.namaLama,
                            namaBaru = null,
                            pesanError = "Nama baru kosong."
                        )
                    )
                } else if (namaBaru.contains(Regex("[\\\\/:*?\"<>|]"))) {
                    hasil.add(
                        HasilPratinjauGantiNama(
                            lokasi = item.lokasi,
                            namaLama = item.namaLama,
                            namaBaru = null,
                            pesanError = "Nama mengandung karakter tidak valid."
                        )
                    )
                } else {
                    hasil.add(
                        HasilPratinjauGantiNama(
                            lokasi = item.lokasi,
                            namaLama = item.namaLama,
                            namaBaru = namaBaru
                        )
                    )
                }
            } catch (e: Exception) {
                hasil.add(
                    HasilPratinjauGantiNama(
                        lokasi = item.lokasi,
                        namaLama = item.namaLama,
                        namaBaru = null,
                        pesanError = "Gagal memproses pola: ${e.message}"
                    )
                )
            }
        }
        val namaTerlihat = mutableSetOf<String>()
        return hasil.map { h ->
            if (h.namaBaru != null) {
                val namaLower = h.namaBaru.lowercase()
                if (namaTerlihat.contains(namaLower)) {
                    h.copy(konflik = true, pesanError = "Nama duplikat dalam batch.")
                } else {
                    namaTerlihat.add(namaLower)
                    h
                }
            } else {
                h
            }
        }
    }
    private fun terapkanPola(
        pola: String,
        namaLama: String,
        nomorUrut: Int,
        nomorAwal: Int
    ): String {
        val namaDasar = ambilNamaDasar(namaLama)
        val ekstensi = ambilEkstensi(namaLama)
        val nomor = nomorAwal + nomorUrut
        val regex = Regex("\\{(P|S|N|z[^}]*)\\}")
        return regex.replace(pola) { match ->
            val token = match.groupValues[1]
            when {
                token == "P" -> namaDasar
                token == "S" -> ekstensi
                token == "N" -> nomor.toString()
                token.startsWith("z") -> {
                    val isi = token.removePrefix("z")
                    val bagian = isi.split(":")
                    val mulaiStr = bagian.getOrNull(0).orEmpty()
                    val mulai = if (mulaiStr == "N" || mulaiStr.isBlank()) {
                        nomorAwal
                    } else {
                        mulaiStr.toIntOrNull() ?: nomorAwal
                    }
                    val lebar = if (bagian.size > 1) {
                        bagian[1].toIntOrNull() ?: 2
                    } else {
                        2
                    }
                    val nilai = mulai + nomorUrut
                    nilai.toString().padStart(lebar, '0')
                }
                else -> match.value
            }
        }
    }
    private fun ambilNamaDasar(nama: String): String {
        val indeksTitik = nama.lastIndexOf('.')
        return if (indeksTitik > 0) nama.substring(0, indeksTitik) else nama
    }
    private fun ambilEkstensi(nama: String): String {
        val indeksTitik = nama.lastIndexOf('.')
        return if (indeksTitik > 0) nama.substring(indeksTitik) else ""
    }
}
