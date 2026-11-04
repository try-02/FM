package id.manajerberkas.kode.domain.model

object ValidasiNamaBerkas {

    private val karakterTerlarang = listOf(
        "/",
        "\\",
        ":",
        "*",
        "?",
        "\"",
        "<",
        ">",
        "|"
    )

    fun periksa(nama: String): HasilValidasi {
        if (nama.isBlank()) {
            return HasilValidasi(
                sukses = false,
                pesan = "Nama tidak boleh kosong."
            )
        }

        if (nama == "." || nama == "..") {
            return HasilValidasi(
                sukses = false,
                pesan = "Nama tidak valid."
            )
        }

        if (nama.length > 255) {
            return HasilValidasi(
                sukses = false,
                pesan = "Nama terlalu panjang."
            )
        }

        if (nama.any { it.isISOControl() }) {
            return HasilValidasi(
                sukses = false,
                pesan = "Nama mengandung karakter kontrol."
            )
        }

        karakterTerlarang.forEach { karakter ->
            if (nama.contains(karakter)) {
                return HasilValidasi(
                    sukses = false,
                    pesan = "Nama mengandung karakter tidak diizinkan: $karakter"
                )
            }
        }

        return HasilValidasi(sukses = true)
    }
}