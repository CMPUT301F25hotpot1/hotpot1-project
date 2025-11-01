package com.example.lottary.data;

import java.util.*;

/**
 * Pure-java sampling logic decoupled from Firestore for unit tests.
 * Inputs are lists of entrant IDs; output is a new sampled list.
 */
public class LotterySampler {

    /** Sample up to 'k' winners from 'waiting' that are not in taken (chosen/signedUp/cancelled). */
    public static List<String> sampleWinners(List<String> waiting, Set<String> taken, int k, long seed) {
        List<String> pool = new ArrayList<>();
        for (String id : waiting) if (!taken.contains(id)) pool.add(id);
        Collections.shuffle(pool, new Random(seed));
        if (k < 0) k = 0;
        if (k > pool.size()) k = pool.size();
        return new ArrayList<>(pool.subList(0, k));
    }

    /** Remaining seats given capacity and already signed up. */
    public static int capacityRemaining(int capacity, int signedUpSize) {
        if (capacity <= 0) return Integer.MAX_VALUE; // treat 0/negative as "no limit"
        return Math.max(0, capacity - signedUpSize);
    }
}
