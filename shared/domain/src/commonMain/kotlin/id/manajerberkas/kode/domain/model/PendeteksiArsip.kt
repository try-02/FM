package id.manajerberkas.kode.domain.model

object PendeteksiArsip {
    val ekstensiTahapPertama = setOf("zip", "jar", "tar", "gz", "tgz")
    val ekstensiDiimplementasikan = setOf("zip", "jar", "tar", "gz", "tgz")

    fun adalahArsip(nama: String): Boolean {
        val ekstensi = nama.substringAfterLast('.', "").lowercase()
        // Handle .tar.gz
        if (nama.lowercase().endsWith(".tar.gz")) return true
        return ekstensi in ekstensiTahapPertama
    }

    fun didukungUntukDibuka(nama: String): Boolean {
        val ekstensi = nama.substringAfterLast('.', "").lowercase()
        if (nama.lowercase().endsWith(".tar.gz")) return true
        return ekstensi in ekstensiDiimplementasikan
    }
}