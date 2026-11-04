package id.manajerberkas.kode.domain.model

data class KemampuanPenyimpanan(
    val bisaBaca: Boolean,
    val bisaTulis: Boolean,
    val bisaBuatBerkas: Boolean,
    val bisaBuatFolder: Boolean,
    val bisaGantiNama: Boolean,
    val bisaHapus: Boolean,
    val bisaSalin: Boolean,
    val bisaPindah: Boolean,
    val bisaBagikan: Boolean,
    val bisaKompres: Boolean,
    val bisaEkstrak: Boolean,
    val bisaBookmark: Boolean,
    val dukunganAtomicReplace: Boolean
) {
    companion object {
        fun kosong(): KemampuanPenyimpanan {
            return KemampuanPenyimpanan(
                bisaBaca = false,
                bisaTulis = false,
                bisaBuatBerkas = false,
                bisaBuatFolder = false,
                bisaGantiNama = false,
                bisaHapus = false,
                bisaSalin = false,
                bisaPindah = false,
                bisaBagikan = false,
                bisaKompres = false,
                bisaEkstrak = false,
                bisaBookmark = false,
                dukunganAtomicReplace = false
            )
        }
    }
}