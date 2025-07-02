package io.collective;

import java.time.Clock;

public class SimpleAgedCache {
    private final Clock clock;
    private ExpirableEntry head;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        removeExpired();
        ExpirableEntry prev = null;
        ExpirableEntry curr = head;
        while (curr != null) {
            if (curr.key.equals(key)) {
                curr.value = value;
                curr.retentionInMillis = retentionInMillis;
                curr.createdAt = clock.millis();
                return;
            }
            prev = curr;
            curr = curr.next;
        }
        ExpirableEntry newEntry = new ExpirableEntry(key, value, retentionInMillis, clock.millis());
        newEntry.next = head;
        head = newEntry;
    }

    public boolean isEmpty() {
        removeExpired();
        return head == null;
    }

    public int size() {
        removeExpired();
        int count = 0;
        ExpirableEntry curr = head;
        while (curr != null) {
            count++;
            curr = curr.next;
        }
        return count;
    }

    public Object get(Object key) {
        removeExpired();
        ExpirableEntry curr = head;
        while (curr != null) {
            if (curr.key.equals(key)) {
                if (!curr.isExpired(clock.millis())) {
                    return curr.value;
                } else {
                    return null;
                }
            }
            curr = curr.next;
        }
        return null;
    }

    private void removeExpired() {
        ExpirableEntry curr = head;
        ExpirableEntry prev = null;
        long now = clock.millis();
        while (curr != null) {
            if (curr.isExpired(now)) {
                if (prev == null) {
                    head = curr.next;
                    curr = head;
                } else {
                    prev.next = curr.next;
                    curr = prev.next;
                }
            } else {
                prev = curr;
                curr = curr.next;
            }
        }
    }

    private static class ExpirableEntry {
        Object key;
        Object value;
        int retentionInMillis;
        long createdAt;
        ExpirableEntry next;

        ExpirableEntry(Object key, Object value, int retentionInMillis, long createdAt) {
            this.key = key;
            this.value = value;
            this.retentionInMillis = retentionInMillis;
            this.createdAt = createdAt;
        }

        boolean isExpired(long now) {
            return now - createdAt >= retentionInMillis;
        }
    }
}