package com.example.daugia.core.ws;

import com.example.daugia.dto.response.BiddingDTO;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache giu toi đa lastN ban ghi tra gia theo tung ma phien.
 * Volatile, se mat khi server restart.
 */
public class HistoryCache {

    private final ConcurrentHashMap<String, Deque<BiddingDTO>> store = new ConcurrentHashMap<>();
    private final int maxPerAuction;

    public HistoryCache(int maxPerAuction) {
        this.maxPerAuction = Math.max(1, maxPerAuction);
    }

    public void append(String auctionId, BiddingDTO dto) {
        if (auctionId == null || dto == null) return;
        Deque<BiddingDTO> q = store.computeIfAbsent(auctionId, k -> new ArrayDeque<>(maxPerAuction));
        synchronized (q) {
            q.addFirst(dto); //Moi nhat vao dau
            while (q.size() > maxPerAuction) {
                q.removeLast();
            }
        }
    }

    public List<BiddingDTO> getLast(String auctionId, int limit) {
        Deque<BiddingDTO> q = store.get(auctionId);
        if (q == null) return List.of();
        int realLimit = limit > 0 ? Math.min(limit, maxPerAuction) : maxPerAuction;
        List<BiddingDTO> res = new ArrayList<>(realLimit);
        synchronized (q) {
            int i = 0;
            for (BiddingDTO b : q) {
                if (i++ >= realLimit) break;
                res.add(b);
            }
        }
        return res;
    }
}