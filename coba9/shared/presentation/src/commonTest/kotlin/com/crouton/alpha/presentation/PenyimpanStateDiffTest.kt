package com.crouton.alpha.presentation

import com.crouton.alpha.data.RepositoriBerkasInMemory
import com.crouton.alpha.domain.model.LokasiBerkas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PenyimpanStateDiffTest {
    @Test fun pilihDuaFileLaluBandingkan() = runTest {
        val repositori = RepositoriBerkasInMemory()
        val a = LokasiBerkas("a", "a.txt", "a", false)
        val b = LokasiBerkas("b", "b.txt", "b", false)
        repositori.letakkanKonten(a, "satu\ndua")
        repositori.letakkanKonten(b, "satu\ntiga")
        val penyimpan = PenyimpanStateDiff(repositori, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        assertEquals(false, penyimpan.pilihFile(a))
        assertEquals(true, penyimpan.pilihFile(b))
        penyimpan.bandingkan()
        val state = penyimpan.state.value
        assertEquals(2, state.jumlahPerubahan)
        assertTrue(state.barisDiff.isNotEmpty())
    }

    @Test fun navigasiPerubahanBerputar() = runTest {
        val repositori = RepositoriBerkasInMemory()
        val a = LokasiBerkas("a", "a.txt", "a", false)
        val b = LokasiBerkas("b", "b.txt", "b", false)
        repositori.letakkanKonten(a, "satu\ndua")
        repositori.letakkanKonten(b, "satu\ntiga")
        val penyimpan = PenyimpanStateDiff(repositori, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        penyimpan.pilihFile(a)
        penyimpan.pilihFile(b)
        penyimpan.bandingkan()
        penyimpan.perubahanBerikutnya()
        assertEquals(1, penyimpan.state.value.indeksPerubahanAktif)
    }
}
