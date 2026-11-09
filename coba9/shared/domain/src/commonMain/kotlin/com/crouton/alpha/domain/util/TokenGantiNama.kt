package com.crouton.alpha.domain.util

object TokenGantiNama {
    fun namaDasar(namaBerkas: String): String {
        val titikTerakhir = namaBerkas.lastIndexOf('.')
        return if (titikTerakhir > 0) namaBerkas.substring(0, titikTerakhir) else namaBerkas
    }

    fun ekstensi(namaBerkas: String): String {
        val titikTerakhir = namaBerkas.lastIndexOf('.')
        return if (titikTerakhir > 0) namaBerkas.substring(titikTerakhir) else ""
    }

    fun terapkanPola(pola: String, namaBerkas: String, nomorUrut: Int): String {
        val dasar = namaDasar(namaBerkas)
        val ext = ekstensi(namaBerkas)
        var hasil = pola
        hasil = hasil.replace("{P}", dasar)
        hasil = hasil.replace("{S}", ext)
        hasil = hasil.replace(Regex("\\{z\\d+:\\d+\\}")) { cocok ->
            val Lebar = cocok.value.substring(3, cocok.value.length - 1).split(":")[1].toInt()
            nomorUrut.toString().padStart(Lebar, '0')
        }
        hasil = hasil.replace("{zN}", nomorUrut.toString().padStart(2, '0'))
        hasil = hasil.replace("{N}", nomorUrut.toString())
        return hasil
    }

    fun deteksiKonflik(namaBaru: List<String>): List<String> {
        return namaBaru.groupBy { it }.filterValues { it.size > 1 }.keys.toList()
    }
}
