package ru.peacecraft.core;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.peacecraft.core.commands.admin.IslandAdminCommand;
import ru.peacecraft.core.config.ConfigManager;
import ru.peacecraft.core.currencies.commands.ShopCommand;
import ru.peacecraft.core.currencies.listener.CurrencyDropListener;
import ru.peacecraft.core.currencies.listener.ShopGuiListener;
import ru.peacecraft.core.currencies.service.CurrencyService;
import ru.peacecraft.core.currencies.service.PlayerCurrencyService;
import ru.peacecraft.core.islands.config.IslandsConfig;
import ru.peacecraft.core.islands.listener.IslandDiscoveryListener;
import ru.peacecraft.core.islands.model.IslandData;
import ru.peacecraft.core.islands.service.IslandRegistry;
import ru.peacecraft.core.islands.service.IslandService;
import ru.peacecraft.core.islands.service.PlayerOnboardingService;
import ru.peacecraft.core.logpose.commands.LogPoseCommand;
import ru.peacecraft.core.logpose.listener.LogPoseInteractionListener;
import ru.peacecraft.core.logpose.service.LogPoseService;
import ru.peacecraft.core.message.MessageService;
import ru.peacecraft.core.persistence.listener.PlayerDataSessionListener;
import ru.peacecraft.core.persistence.repository.PlayerDataRepository;
import ru.peacecraft.core.persistence.service.PlayerDataService;
import ru.peacecraft.core.persistence.task.PlayerDataAutosaveTask;
import ru.peacecraft.core.travel.commands.FastTravelCommand;
import ru.peacecraft.core.travel.commands.TravelCommand;
import ru.peacecraft.core.travel.listener.TravelListener;
import ru.peacecraft.core.travel.service.TravelService;

public final class PeaceCraftPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageService messageService;

    private IslandRegistry islandRegistry;
    private IslandsConfig islandsConfig;
    private IslandService islandService;
    private PlayerOnboardingService playerOnboardingService;
    private IslandDiscoveryListener islandDiscoveryListener;

    private PlayerDataRepository playerDataRepository;
    private PlayerDataService playerDataService;
    private PlayerDataSessionListener playerDataSessionListener;
    private PlayerDataAutosaveTask playerDataAutosaveTask;

    private LogPoseService logPoseService;

    private TravelService travelService;

    private CurrencyService currencyService;
    private PlayerCurrencyService playerCurrencyService;

    @Override
    public void onEnable() {
        createPluginDataFolder();

        getLogger().info("Bootstrapping PeaceCraftCore...");

        bootstrapCore();
        bootstrapPersistence();
        bootstrapIslands();
        bootstrapLogPose();
        bootstrapTravel();
        bootstrapCurrencies();

        registerCommands();
        registerListeners();
        startBackgroundTasks();

        logLoadedIslands();
        getLogger().info(messageService.getPlain("startup.plugin-enabled"));
    }

    @Override
    public void onDisable() {
        if (playerDataAutosaveTask != null) {
            playerDataAutosaveTask.stop();
        }

        if (playerDataService != null) {
            int saved = playerDataService.saveAll();
            getLogger().info(() -> "Saved " + saved + " player data file(s) before shutdown.");
            playerDataService.shutdown();
        }

        if (messageService != null && messageService.isLoaded()) {
            getLogger().info(messageService.getPlain("startup.plugin-disabled"));
        } else {
            getLogger().info("PeaceCraftCore disabled.");
        }
    }

    private void bootstrapCore() {
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.messageService = new MessageService(this, this.configManager);
        this.messageService.load();
    }

    private void bootstrapPersistence() {
        this.playerDataRepository = new PlayerDataRepository(this);
        this.playerDataService = new PlayerDataService(this.playerDataRepository);
        this.playerDataSessionListener = new PlayerDataSessionListener(this, this.playerDataService);
        this.playerDataAutosaveTask = new PlayerDataAutosaveTask(
            this,
            this.playerDataService,
            this.configManager.getAutosaveIntervalSeconds()
        );
    }

    private void bootstrapIslands() {
        this.islandRegistry = new IslandRegistry();
        this.islandsConfig = new IslandsConfig(this, this.islandRegistry);
        this.islandsConfig.load();

        this.islandService = new IslandService(this.islandRegistry, this.configManager, this.playerDataService);
    }

    private void bootstrapLogPose() {
        this.logPoseService = new LogPoseService(this);

        this.playerOnboardingService = new PlayerOnboardingService(
            this.configManager,
            this.islandService,
            this.playerDataService,
            this.logPoseService,
            getLogger()
        );

        this.islandDiscoveryListener = new IslandDiscoveryListener(this, this.islandService, this.playerOnboardingService);
    }

    private void bootstrapTravel() {
        this.travelService = new TravelService(this);
    }

    private void bootstrapCurrencies() {
        this.currencyService = new CurrencyService(this);
        this.currencyService.load();

        this.playerCurrencyService = new PlayerCurrencyService(this);
    }

    private void startBackgroundTasks() {
        if (this.playerDataAutosaveTask != null) {
            this.playerDataAutosaveTask.start();
        }
    }

    private void registerCommands() {
        registerCommand("pc", new IslandAdminCommand(this));
        registerCommand("logpose", new LogPoseCommand(this, this.logPoseService));
        registerCommand("travel", new TravelCommand(this, this.travelService));
        registerCommand("fasttravel", new FastTravelCommand(this, this.travelService));
        registerCommand("shop", new ShopCommand(this));
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning(() -> "Command not found in plugin.yml: " + commandName);
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }

    private void registerListeners() {
        registerListener(this.playerDataSessionListener);
        registerListener(this.islandDiscoveryListener);
        registerListener(new LogPoseInteractionListener(this, this.logPoseService));
        registerListener(new TravelListener(this));
        registerListener(new CurrencyDropListener(this, this.playerCurrencyService));
        registerListener(new ShopGuiListener());
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info(() -> "Registered listener: " + listener.getClass().getSimpleName());
    }

    private void createPluginDataFolder() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder: " + getDataFolder().getAbsolutePath());
        }
    }

    private void logLoadedIslands() {
        getLogger().info(() -> "Loaded " + islandService.getLoadedIslandCount() + " islands.");

        for (IslandData islandData : islandService.getAllIslands()) {
            getLogger().info(() -> "- " + islandData.getId() + " (" + islandData.getDisplayName() + ")");
        }

        IslandData starterIsland = islandService.getStarterIsland();
        if (starterIsland == null) {
            throw new IllegalStateException("Starter island from config.yml was not found in islands.yml");
        }

        getLogger().info(() -> "Starter island: " + starterIsland.getId() + " (" + starterIsland.getDisplayName() + ")");
    }

    public ConfigManager getMainConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public IslandRegistry getIslandRegistry() {
        return islandRegistry;
    }

    public IslandsConfig getIslandsConfig() {
        return islandsConfig;
    }

    public IslandService getIslandService() {
        return islandService;
    }

    public PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepository;
    }

    public PlayerDataService getPlayerDataService() {
        return playerDataService;
    }

    public LogPoseService getLogPoseService() {
        return logPoseService;
    }

    public TravelService getTravelService() {
        return travelService;
    }

    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    public PlayerCurrencyService getPlayerCurrencyService() {
        return playerCurrencyService;
    }
}
