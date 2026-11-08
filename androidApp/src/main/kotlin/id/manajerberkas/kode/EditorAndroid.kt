package id.manajerberkas.kode

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.manajerberkas.kode.presentation.layar.EditorLayar
import id.manajerberkas.kode.presentation.state.EditorAksi
import id.manajerberkas.kode.presentation.state.EditorState
import id.manajerberkas.kode.presentation.state.EditorStateHolder

@Composable
fun EditorAndroid(
    stateHolder: EditorStateHolder
) {
    val state by stateHolder.state.collectAsStateWithLifecycle()

    BackHandler {
        stateHolder.mintaTutup()
    }

    EditorLayar(
        state = state,
        aksi = stateHolder,
        editorArea = { modifier ->
            EditorTeksAndroidNative(
                state = state,
                aksi = stateHolder,
                modifier = modifier
            )
        }
    )
}

@Composable
private fun EditorTeksAndroidNative(
    state: EditorState,
    aksi: EditorAksi,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val textColor = colorScheme.onSurface.toArgb()
    val selectionColor = colorScheme.primary.copy(alpha = 0.35f).toArgb()

    // Bukan snapshot state: tulis ke flag ini tiap ketikan tidak boleh
    // menjadwalkan rekomposisi (sebelumnya 2 rekomposisi per ketikan).
    val isUpdatingInternally = remember { booleanArrayOf(false) }
    // Referensi String terakhir yang dikirim ke holder. String yang sama
    // berarti teks berasal dari ketikan pengguna sendiri, jadi update()
    // tidak perlu menyalin+membandingkan seluruh teks (O(n)) per ketikan.
    val teksTerakhirDikirim = remember { arrayOf<String?>(null) }
    var editTextRef by remember { mutableStateOf<EditText?>(null) }

    // Memperbarui posisi kursor saat ada aksi navigasi/lompat baris/cari dari luar
    LaunchedEffect(state.medan.selection) {
        val et = editTextRef ?: return@LaunchedEffect
        val targetSel = state.medan.selection
        if (!isUpdatingInternally[0] && targetSel.end <= et.text.length &&
            (et.selectionStart != targetSel.start || et.selectionEnd != targetSel.end)) {
            try {
                et.setSelection(
                    targetSel.start.coerceIn(0, et.text.length),
                    targetSel.end.coerceIn(0, et.text.length)
                )
            } catch (_: Exception) {}
        }
    }

    if (state.bungkusKata) {
        AndroidView<EditText>(
            modifier = modifier,
            factory = { context ->
                buatEditText(context, textColor, selectionColor, hanyaBaca = state.hanyaBaca, bungkusKata = true).apply {
                    setText(state.medan.text)
                    teksTerakhirDikirim[0] = state.medan.text
                    if (state.medan.selection.end <= text.length) {
                        setSelection(state.medan.selection.start, state.medan.selection.end)
                    }
                    pasangTextWatcher { newText, sel ->
                        isUpdatingInternally[0] = true
                        teksTerakhirDikirim[0] = newText
                        aksi.ubahTeks(TextFieldValue(newText, sel))
                        isUpdatingInternally[0] = false
                    }
                    editTextRef = this
                }
            },
            update = { editText ->
                editText.setTextColor(textColor)
                editText.setHighlightColor(selectionColor)
                editText.isFocusable = !state.hanyaBaca
                editText.isFocusableInTouchMode = !state.hanyaBaca

                // Hanya sinkronkan bila diubah operasi luar (Undo/Redo/Ganti):
                // referensi berbeda dari teks terakhir yang dikirim saat mengetik.
                if (!isUpdatingInternally[0] && state.medan.text !== teksTerakhirDikirim[0]) {
                    teksTerakhirDikirim[0] = state.medan.text
                    val selStart = state.medan.selection.start.coerceIn(0, state.medan.text.length)
                    val selEnd = state.medan.selection.end.coerceIn(0, state.medan.text.length)
                    editText.setText(state.medan.text)
                    if (selEnd <= editText.text.length) {
                        editText.setSelection(selStart, selEnd)
                    }
                }
            }
        )
    } else {
        val hScroll = rememberScrollState()
        Box(modifier = modifier.horizontalScroll(hScroll)) {
            AndroidView<EditText>(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 2500.dp),
                factory = { context ->
                    buatEditText(context, textColor, selectionColor, hanyaBaca = state.hanyaBaca, bungkusKata = false).apply {
                        setText(state.medan.text)
                        teksTerakhirDikirim[0] = state.medan.text
                        if (state.medan.selection.end <= text.length) {
                            setSelection(state.medan.selection.start, state.medan.selection.end)
                        }
                        pasangTextWatcher { newText, sel ->
                            isUpdatingInternally[0] = true
                            teksTerakhirDikirim[0] = newText
                            aksi.ubahTeks(TextFieldValue(newText, sel))
                            isUpdatingInternally[0] = false
                        }
                        editTextRef = this
                    }
                },
                update = { editText ->
                    editText.setTextColor(textColor)
                    editText.setHighlightColor(selectionColor)
                    editText.isFocusable = !state.hanyaBaca
                    editText.isFocusableInTouchMode = !state.hanyaBaca

                    if (!isUpdatingInternally[0] && state.medan.text !== teksTerakhirDikirim[0]) {
                        teksTerakhirDikirim[0] = state.medan.text
                        val selStart = state.medan.selection.start.coerceIn(0, state.medan.text.length)
                        val selEnd = state.medan.selection.end.coerceIn(0, state.medan.text.length)
                        editText.setText(state.medan.text)
                        if (selEnd <= editText.text.length) {
                            editText.setSelection(selStart, selEnd)
                        }
                    }
                }
            )
        }
    }
}

private fun buatEditText(
    context: Context,
    textColor: Int,
    selectionColor: Int,
    hanyaBaca: Boolean,
    bungkusKata: Boolean
): EditText {
    return EditText(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        typeface = Typeface.MONOSPACE
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
        gravity = Gravity.TOP or Gravity.START
        background = null
        includeFontPadding = false
        setPadding(20, 20, 20, 20)
        setTextColor(textColor)
        setHighlightColor(selectionColor)
        isFocusable = !hanyaBaca
        isFocusableInTouchMode = !hanyaBaca
        isCursorVisible = !hanyaBaca
        inputType = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_FLAG_MULTI_LINE or
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN

        setHorizontallyScrolling(!bungkusKata)
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = !bungkusKata
    }
}

private fun EditText.pasangTextWatcher(
    onTextChange: (String, TextRange) -> Unit
) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val text = s?.toString() ?: ""
            val start = selectionStart.coerceIn(0, text.length)
            val end = selectionEnd.coerceIn(0, text.length)
            onTextChange(text, TextRange(start, end))
        }
    })
}