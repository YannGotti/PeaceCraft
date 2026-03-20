package ru.peacecraft.core.currencies.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.currencies.model.Currency;

public final class CurrencyService {

    private static final String FILE_NAME = "currencies.yml";

    private final PeaceCraftPlugin plugin;
    private final Map<String, Currency> currenciesById = new LinkedHashMap<>();
    private final Map<String, List<String>> currencyToIslands = new HashMap<>();

    private File currenciesFile;
    private FileConfiguration currenciesConfig;

    public CurrencyService(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        currenciesById.clear();
        currencyToIslands.clear();

        ensureBundledResourceExists();
        ensureFileExistsInDataFolder();

        this.currenciesFile = new File(plugin.getDataFolder(), FILE_NAME);
        this.currenciesConfig = YamlConfiguration.loadConfiguration(this.currenciesFile);

        loadCurrencies();
        plugin.getLogger().info(() -> "Loaded " + currenciesById.size() + " currencies.");
    }

    private void loadCurrencies() {
        ConfigurationSection currenciesSection = currenciesConfig.getConfigurationSection("currencies");
        if (currenciesSection == null) {
            plugin.getLogger().warning("No currencies found in currencies.yml");
            return;
        }

        for (String currencyId : currenciesSection.getKeys(false)) {
            ConfigurationSection currencyConfig = currenciesSection.getConfigurationSection(currencyId);
            if (currencyConfig == null) continue;

            Currency currency = new Currency(
                currencyConfig.getString("id", currencyId),
                currencyConfig.getString("name", currencyId),
                currencyConfig.getString("display-name", currencyId),
                currencyConfig.getString("symbol", "$"),
                currencyConfig.getInt("decimal-places", 0)
            );

            currenciesById.put(currencyId, currency);

            // Загружаем привязку к островам
            List<String> islands = currencyConfig.getStringList("islands");
            currencyToIslands.put(currencyId, islands);
        }
    }

    public void reload() {
        if (this.currenciesFile == null) {
            load();
            return;
        }

        currenciesById.clear();
        currencyToIslands.clear();

        this.currenciesConfig = YamlConfiguration.loadConfiguration(this.currenciesFile);
        loadCurrencies();
        plugin.getLogger().info(() -> "Reloaded " + currenciesById.size() + " currencies.");
    }

    public Currency getCurrency(String id) {
        return currenciesById.get(id);
    }

    public Collection<Currency> getAllCurrencies() {
        return Collections.unmodifiableCollection(currenciesById.values());
    }

    public boolean hasCurrency(String id) {
        return currenciesById.containsKey(id);
    }

    public List<String> getIslandsForCurrency(String currencyId) {
        return currencyToIslands.getOrDefault(currencyId, new ArrayList<>());
    }

    public String getCurrencyForIsland(String islandId) {
        for (Map.Entry<String, List<String>> entry : currencyToIslands.entrySet()) {
            if (entry.getValue().contains(islandId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void ensureBundledResourceExists() {
        if (plugin.getResource(FILE_NAME) == null) {
            throw new IllegalStateException("Embedded resource not found in jar: " + FILE_NAME);
        }
    }

    private void ensureFileExistsInDataFolder() {
        File targetFile = new File(plugin.getDataFolder(), FILE_NAME);
        if (!targetFile.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
    }
}
