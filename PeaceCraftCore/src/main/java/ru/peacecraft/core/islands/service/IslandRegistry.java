package ru.peacecraft.core.islands.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import ru.peacecraft.core.islands.model.IslandData;

public final class IslandRegistry {

    private final Map<String, IslandData> islandsById = new LinkedHashMap<>();

    public void clear() {
        islandsById.clear();
    }

    public void register(IslandData islandData) {
        String islandId = islandData.getId();

        if (islandId == null || islandId.isBlank()) {
            throw new IllegalArgumentException("Island id cannot be null or blank.");
        }

        if (islandsById.containsKey(islandId)) {
            throw new IllegalArgumentException("Duplicate island id detected: " + islandId);
        }

        islandsById.put(islandId, islandData);
    }

    public IslandData getById(String islandId) {
        return islandsById.get(islandId);
    }

    public boolean contains(String islandId) {
        return islandsById.containsKey(islandId);
    }

    public Collection<IslandData> getAll() {
        return Collections.unmodifiableCollection(islandsById.values());
    }

    public int size() {
        return islandsById.size();
    }

    public boolean isEmpty() {
        return islandsById.isEmpty();
    }
}
