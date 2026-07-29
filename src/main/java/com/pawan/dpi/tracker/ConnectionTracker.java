package com.pawan.dpi.tracker;

import com.pawan.dpi.model.FiveTuple;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionTracker {

    private final Map<FiveTuple, Flow> flowTable = new ConcurrentHashMap<>();

    public Flow getOrCreateFlow(FiveTuple fiveTuple) {

        return flowTable.computeIfAbsent(
                fiveTuple,
                key -> new Flow()
        );
    }

    public boolean contains(FiveTuple fiveTuple) {

        return flowTable.containsKey(fiveTuple);
    }

    public int getFlowCount() {

        return flowTable.size();
    }

    public Collection<Flow> getAllFlows() {

        return flowTable.values();
    }

    public void clear() {

        flowTable.clear();
    }
}