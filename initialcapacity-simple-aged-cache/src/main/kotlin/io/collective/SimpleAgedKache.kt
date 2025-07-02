package io.collective

import java.time.Clock

class SimpleAgedKache(private val clock: Clock = Clock.systemDefaultZone()) {
    private var head: ExpirableEntry? = null

    fun put(key: Any, value: Any, retentionInMillis: Int) {
        removeExpired()
        var prev: ExpirableEntry? = null
        var curr = head
        while (curr != null) {
            if (curr.key == key) {
                curr.value = value
                curr.retentionInMillis = retentionInMillis
                curr.createdAt = clock.millis()
                return
            }
            prev = curr
            curr = curr.next
        }
        val newEntry = ExpirableEntry(key, value, retentionInMillis, clock.millis())
        newEntry.next = head
        head = newEntry
    }

    fun isEmpty(): Boolean {
        removeExpired()
        return head == null
    }

    fun size(): Int {
        removeExpired()
        var count = 0
        var curr = head
        while (curr != null) {
            count++
            curr = curr.next
        }
        return count
    }

    fun get(key: Any): Any? {
        removeExpired()
        var curr = head
        while (curr != null) {
            if (curr.key == key) {
                return if (!curr.isExpired(clock.millis())) curr.value else null
            }
            curr = curr.next
        }
        return null
    }

    private fun removeExpired() {
        var curr = head
        var prev: ExpirableEntry? = null
        val now = clock.millis()
        while (curr != null) {
            if (curr.isExpired(now)) {
                if (prev == null) {
                    head = curr.next
                    curr = head
                } else {
                    prev.next = curr.next
                    curr = prev.next
                }
            } else {
                prev = curr
                curr = curr.next
            }
        }
    }

    private class ExpirableEntry(
        val key: Any,
        var value: Any,
        var retentionInMillis: Int,
        var createdAt: Long
    ) {
        var next: ExpirableEntry? = null
        fun isExpired(now: Long): Boolean = now - createdAt >= retentionInMillis
    }
}