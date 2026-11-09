package com.crouton.alpha.presentation

import com.crouton.alpha.data.RepositoriBerkasInMemory
import com.crouton.alpha.domain.model.EntriBerkas
import com.crouton.alpha.domain.model.JenisEntri
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.usecase.DaftarIsiUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class PenyimpanStateBerkasTest {
    private val dasar = LokasiBerkas("dasar", "Dasar", "dasar", true)

    @Test fun muatAwalMenampilkanDaftar() = runTest {
        val repositori = RepositoriBerkasInMemory()
        repositori.letakkan(dasar, EntriBerkas(LokasiBerkas("f1", "Dokumen", "f1", true), 0, 0, JenisEntri.FOLDER))
        val penyimpan = PenyimpanStateBerkas(DaftarIsiUseCase(repositori), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        penyimpan.muatAwal(dasar)
        val state = penyimpan.state.value
        assertEquals(1, state.daftar.size)
        assertEquals("Dokumen", state.daftar[0].nama)
        assertFalse(state.sedangMemuat)
    }

    @Test fun folderDiurutkanLebihDulu() = runTest {
        val repositori = RepositoriBerkasInMemory()
        repositori.letakkan(dasar, EntriBerkas(LokasiBerkas("b", "Berkas", "b", false), 10, 0, JenisEntri.BERKAS))
        repositori.letakkan(dasar, EntriBerkas(LokasiBerkas("a", "Folder", "a", true), 0, 0, JenisEntri.FOLDER))
        val penyimpan = PenyimpanStateBerkas(DaftarIsiUseCase(repositori), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        penyimpan.muatAwal(dasar)
        assertEquals("Folder", penyimpan.state.value.daftar[0].nama)
    }
}
