package id.manajerberkas.kode.data.adapter

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import id.manajerberkas.kode.domain.model.JenisPenyimpanan
import id.manajerberkas.kode.domain.model.LokasiBerkas

object PabrikLokasiAndroid {

    const val SKEMA_PRIVAT = "privat:"
    const val SKEMA_SAF = "saf:"
    const val SKEMA_EKSTERNAL = "eksternal:"

    fun privatRoot(): LokasiBerkas {
        return LokasiBerkas(
            id = SKEMA_PRIVAT,
            namaTampilan = "Penyimpanan Privat",
            jalurTampilan = "/",
            jenisPenyimpanan = JenisPenyimpanan.PRIVAT_APLIKASI
        )
    }

    fun privat(jalur: String): LokasiBerkas {
        val jalurBersih = jalur.trim('/')
        val nama = if (jalurBersih.isBlank()) "Penyimpanan Privat" else jalurBersih.substringAfterLast('/')

        return LokasiBerkas(
            id = SKEMA_PRIVAT + jalurBersih,
            namaTampilan = nama,
            jalurTampilan = "/$jalurBersih",
            jenisPenyimpanan = JenisPenyimpanan.PRIVAT_APLIKASI
        )
    }

    // --- DUKUNGAN PENYIMPANAN UTAMA (/storage/emulated/0) ---
    fun eksternalRoot(): LokasiBerkas {
        val path = Environment.getExternalStorageDirectory().absolutePath
        return LokasiBerkas(
            id = SKEMA_EKSTERNAL,
            namaTampilan = "Penyimpanan Utama",
            jalurTampilan = path,
            jenisPenyimpanan = JenisPenyimpanan.EKSTERNAL
        )
    }

    fun eksternal(jalur: String): LokasiBerkas {
        val jalurBersih = jalur.trim('/')
        val nama = if (jalurBersih.isBlank()) "Penyimpanan Utama" else jalurBersih.substringAfterLast('/')
        val rootPath = Environment.getExternalStorageDirectory().absolutePath

        return LokasiBerkas(
            id = SKEMA_EKSTERNAL + jalurBersih,
            namaTampilan = nama,
            jalurTampilan = "$rootPath/$jalurBersih",
            jenisPenyimpanan = JenisPenyimpanan.EKSTERNAL
        )
    }

    fun safRoot(treeUri: Uri): LokasiBerkas? {
        return try {
            val documentId = DocumentsContract.getTreeDocumentId(treeUri)

            LokasiBerkas(
                id = buatIdSaf(treeUri.toString(), documentId),
                namaTampilan = Uri.decode(treeUri.lastPathSegment ?: "Dokumen SAF"),
                jalurTampilan = null,
                jenisPenyimpanan = JenisPenyimpanan.SAF_ANDROID
            )
        } catch (e: Exception) {
            null
        }
    }

    fun buatIdSaf(treeUri: String, documentId: String): String {
        return SKEMA_SAF + Uri.encode(treeUri) + "|" + Uri.encode(documentId)
    }

    fun parseSaf(id: String): LokasiSafTerurai? {
        if (!id.startsWith(SKEMA_SAF)) return null

        val tanpaSkema = id.removePrefix(SKEMA_SAF)
        val bagian = tanpaSkema.split("|")

        if (bagian.size != 2) return null

        return try {
            val treeUri = Uri.parse(Uri.decode(bagian[0]))
            val documentId = Uri.decode(bagian[1])

            LokasiSafTerurai(
                treeUri = treeUri,
                documentId = documentId
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class LokasiSafTerurai(
    val treeUri: Uri,
    val documentId: String
)