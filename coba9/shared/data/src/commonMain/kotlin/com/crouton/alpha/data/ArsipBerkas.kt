package com.crouton.alpha.data

data class KemampuanArsip(
    val format: String,
    val dapatBaca: Boolean,
    val dapatEkstrak: Boolean,
    val dapatBuat: Boolean,
    val dapatUbahIsi: Boolean,
    val dapatEnkripsi: Boolean
)

object MatriksArsip {
    val kemampuan: List<KemampuanArsip> = listOf(
        KemampuanArsip("ZIP", true, true, true, true, true),
        KemampuanArsip("TAR", true, true, true, true, false),
        KemampuanArsip("GZIP", true, true, true, false, false),
        KemampuanArsip("TGZ", true, true, true, false, false),
        KemampuanArsip("JAR", true, true, true, true, false),
        KemampuanArsip("7z", false, false, false, false, false),
        KemampuanArsip("BZIP2", false, false, false, false, false),
        KemampuanArsip("XZ", false, false, false, false, false),
        KemampuanArsip("ZSTANDARD", false, false, false, false, false),
        KemampuanArsip("RAR", false, false, false, false, false)
    )

    fun cari(format: String): KemampuanArsip? = kemampuan.firstOrNull { it.format == format.uppercase() }
}

object KeamananArsip {
    val kedalamanMaksimum = 5
    val ukuranEkstrakMaksimumByte = 512L * 1024 * 1024

    fun adalahPathAman(pathDiArsip: String): Boolean {
        val bersih = pathDiArsip.replace('\\', '/')
        if (bersih.startsWith("/")) return false
        return bersih.split("/").none { it == ".." || it.isEmpty() && bersih.contains("//") }
    }

    fun dalamBatasKedalaman(pathDiArsip: String): Boolean {
        return pathDiArsip.count { it == '/' } < kedalamanMaksimum
    }
}
