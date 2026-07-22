package com.brokechango.hattitriki.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

private const val NO_BROWSER_HISTORY_ENTRY = -1

/**
 * Keeps Compose Navigation 3 in step with the browser session history.
 *
 * Mobile browsers own the native edge-swipe/back gesture. They report its completed result
 * through `popstate`, so recording an entry for each internal navigation is what makes that
 * gesture return to the previous Compose screen.
 */
@Composable
fun BrowserHistoryNavigationEffect(navigation: HattitrikiNavigationState) {
    DisposableEffect(navigation) {
        val controller = BrowserHistoryNavigationController(navigation)
        controller.start()
        navigation.setHistoryDelegate(controller)

        onDispose {
            navigation.setHistoryDelegate(null)
            controller.stop()
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private class BrowserHistoryNavigationController(
    private val navigation: HattitrikiNavigationState
) : HattitrikiNavigationHistoryDelegate {
    private data class Entry(
        val snapshot: HattitrikiNavigationSnapshot,
        val parentId: Int?
    )

    private val entries = mutableMapOf<Int, Entry>()
    private var currentEntryId = 0
    private var nextEntryId = 0
    private var isBackNavigationPending = false
    private val popStateListener: () -> JsAny? = {
        restoreBrowserEntry()
        null
    }

    fun start() {
        entries[currentEntryId] = Entry(
            snapshot = navigation.snapshot(),
            parentId = null
        )
        replaceBrowserHistoryEntry(currentEntryId)
        addBrowserPopStateListener(popStateListener)
    }

    fun stop() {
        removeBrowserPopStateListener(popStateListener)
    }

    override fun onBackRequested(): Boolean {
        if (isBackNavigationPending || entries[currentEntryId]?.parentId == null) return false

        isBackNavigationPending = goBackInBrowserHistory()
        return isBackNavigationPending
    }

    override fun onNavigationChanged(snapshot: HattitrikiNavigationSnapshot) {
        if (entries[currentEntryId]?.snapshot == snapshot) return

        val newEntryId = ++nextEntryId
        if (!pushBrowserHistoryEntry(newEntryId)) return

        entries[newEntryId] = Entry(
            snapshot = snapshot,
            parentId = currentEntryId
        )
        currentEntryId = newEntryId
    }

    private fun restoreBrowserEntry() {
        isBackNavigationPending = false

        val entryId = currentBrowserHistoryEntryId()
        val entry = entries[entryId] ?: return

        currentEntryId = entryId
        navigation.restore(entry.snapshot)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(entryId) => {
        try {
            const previousState = globalThis.history.state;
            const state = previousState && typeof previousState === 'object' ? previousState : {};
            globalThis.history.replaceState({ ...state, __hattitrikiNavigationEntryId: entryId }, '');
            return true;
        } catch (_) {
            return false;
        }
    }"""
)
private external fun replaceBrowserHistoryEntry(entryId: Int): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(entryId) => {
        try {
            const previousState = globalThis.history.state;
            const state = previousState && typeof previousState === 'object' ? previousState : {};
            globalThis.history.pushState({ ...state, __hattitrikiNavigationEntryId: entryId }, '');
            return true;
        } catch (_) {
            return false;
        }
    }"""
)
private external fun pushBrowserHistoryEntry(entryId: Int): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.history?.state?.__hattitrikiNavigationEntryId ?? -1")
private external fun currentBrowserHistoryEntryId(): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(callback) => globalThis.addEventListener('popstate', callback)")
private external fun addBrowserPopStateListener(callback: () -> JsAny?)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(callback) => globalThis.removeEventListener('popstate', callback)")
private external fun removeBrowserPopStateListener(callback: () -> JsAny?)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        try {
            globalThis.history.back();
            return true;
        } catch (_) {
            return false;
        }
    }"""
)
private external fun goBackInBrowserHistory(): Boolean
