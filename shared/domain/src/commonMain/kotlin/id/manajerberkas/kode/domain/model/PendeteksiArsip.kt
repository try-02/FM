package id.manajerberkas.kode.domain.model

object PendeteksiArsip {
    val ekstensiTahapPertama = setOf("zip", "jar", "tar", "gz", "tgz")
    val ekstensiDiimplementasikan = setOf("zip", "jar")

    fun adalahArsip(nama: String): Boolean {
        val ekstensi = nama.substringAfterLast('.', "").lowercase()
        return ekstensi in ekstensiTahapPertama
    }

    fun didukungUntukDibuka(nama: String): Boolean {
        val ekstensi = nama.substringAfterLast('.', "").lowercase()
        return ekstensi in ekstensiDiimplementasikan
    }
}