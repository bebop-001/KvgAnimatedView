@file:Suppress("SpellCheckingInspection")

package com.kana_tutor.utils

import android.os.Looper
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/*
 * Simple Observer modeled on LiveDats.
 *
 * Rather than dealng with life-cycle, assume
 * exceptiond are life-cycle related and just
 * remove the cb from the que.  Next time
 * the display cines yo ut should re-subscribe
 * and will be called with the current value.
 */
class Observed<T>(
    initialValue: T,
    @Suppress("MemberVisibilityCanBePrivate")
    // set this in code or using debugger to trace actions.
    var traceName: String = ""
) {
    companion object {
        private const val TAG = "Observed"
        private var id = 0
    }
    val observedId = id++
    private val observers =
        mutableSetOf<(T) -> Unit>()

    private fun invokeCB(cb: (T)->Unit, idx:Int):Boolean {
        var rv = true
        try {
            if (traceName.isNotEmpty())
                Log.d(TAG, "$traceName:$observedId: $idx invoke")
            if (Looper.myLooper() == Looper.getMainLooper())
                cb.invoke(value)
            else MainScope().launch{
                // runBlocking {cb.invoke(value) }
                cb.invoke(value)
            }
        }
        catch (e: Exception) {
            if (traceName.isNotEmpty())
                Log.d(
                    TAG, "$traceName:$observedId: " +
                        "invoke failed:" + e.toString())
            rv = false
        }
        return rv
    }
    private val lock = Object()
    fun observe(cb: (T) -> Unit) {
        synchronized(lock) {
            if (!observers.contains(cb)) {
                    invokeCB(cb, -1)
                    observers.add(cb)
            }
        }
    }
    private fun invoke() {
        if (traceName.isNotEmpty())
            Log.d(
                TAG,
            "$traceName:$observedId: " +
                "invoke: ${observers.size} observers"
            )
        synchronized(lock) {
            val badCb = mutableSetOf<(T) -> Unit>()
            observers.forEachIndexed { i, cb ->
                if (!invokeCB(cb, i))
                    badCb.add(cb)
            }
            // remove any cb's that exception'ed on us.
            // fail most often is life-cycle.  Remove all
            // at the same time to avoid corrupting the que.
            badCb.map { observers.remove(it) }
        }
    }
    // a value can be changed without replacing it as in adding
    // or removing a value of a mutable list or map.  If that happens,
    // Update allows the caller to notify subscribers of the
    // object of its change.
    fun update() = invoke()

    // Read-Write values.
    var value:T = initialValue
        set(setVal) {
            if (traceName.isNotEmpty())
                Log.d(TAG, "$traceName:$observedId: set")
            field = setVal
            invoke()
        }
    @Suppress("PropertyName")
    // Read-only values.
    val value_ro:T
        get() {
            if (traceName.isNotEmpty()) Log.d(TAG, "$traceName:$observedId: get")
            return synchronized(lock) { value }
        }
}

@Suppress("unused")
class ObservedSet<T>(
    initialValue:MutableSet<T>,
    traceString:String = ""
): MutableSet<T> {
    private val observed = Observed(initialValue, traceString)

    override val size:Int
        get() = observed.value_ro.size

    override fun clear() {
        observed.value.clear()
        observed.update()
    }

    override fun containsAll(elements: Collection<T>): Boolean =
        observed.value.containsAll(elements)
    fun containsAny(elements: Collection<T>): Boolean =
        observed.value.intersect(elements.toSet()).isNotEmpty()

    override fun addAll(elements: Collection<T>): Boolean {
        return if (observed.value.addAll(elements)) {
            observed.update()
            true
        }
        else false
    }

    val observedId = observed.observedId

    var value: Set<T>
        set(newVal) { observed.value = newVal.toMutableSet() }
        get() = observed.value
    @Suppress("PropertyName")
    val value_ro: Set<T>
        get() = observed.value_ro

    fun update() = observed.update()

    fun observe(cb: (Set<T>) -> Unit) =
        observed.observe(cb)

    override fun isEmpty() = observed.value_ro.isEmpty()
    override fun iterator(): MutableIterator<T> =
        observed.value.iterator()

    override fun retainAll(elements: Collection<T>): Boolean {
        return if (observed.value.retainAll(elements.toSet())) {
            observed.update()
            true
        }
        else false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return if (observed.value.retainAll(elements.toSet())) {
            observed.update()
            true
        }
        else false
    }

    fun isNotEmpty() = observed.value_ro.isNotEmpty()

    override fun contains(element: T) =
        observed.value_ro.contains(element)

    override fun add(element: T): Boolean {
        return if (observed.value.add(element)) {
            observed.update()
            true
        }
        else false
    }
    fun addAll(v: Set<T>) : Boolean{
        return if (observed.value.addAll(v)) {
            observed.update()
            true
        }
        else false
    }
    override fun remove(element: T): Boolean {
        return if (observed.value.remove(element)) {
            observed.update()
            true
        }
        else false
    }
    fun removeAll(v:Set<T>): Boolean {
        return if (observed.value.removeAll(v)) {
            observed.update()
            true
        }
        else false
    }
    fun retainAll(v:Set<T>): Boolean {
        return if (observed.value.retainAll(v)) {
            observed.update()
            true
        }
        else false
    }

    fun union(other: Set<T>): MutableSet<T> =
        observed.value_ro.union(other).toMutableSet()
    fun intersect(other: Set<T>) :MutableSet<T> =
        observed.value_ro.intersect(other).toMutableSet()
    fun subtract(other: Set<T>): MutableSet<T> =
        observed.value_ro.subtract(other).toMutableSet()
}

@Suppress("unused")
class ObservedPair <A,B> (
    initialValue: Pair<A,B>,
    traceString: String = ""
) {
    private val observed = Observed(initialValue, traceString)
    fun observe(cb: (Pair<A,B>) -> Unit) =
        observed.observe(cb)

    infix fun A.to(that: B) = Pair(this, that)
    val first:A
        get() = observed.value.first
    val second:B
        get() = observed.value.second
    fun toList() = observed.value.toList()

    operator fun component1():A = observed.value.first
    operator fun component2():B = observed.value.second

    var value:Pair<A,B>
        get() = observed.value
        set(newVal) { observed.value = newVal }
    val value_ro:Pair<A,B>
        get() = observed.value_ro
}