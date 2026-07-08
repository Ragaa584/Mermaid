package com.mermaid.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class MermaidPlugin extends JavaPlugin {

    private NamespacedKey mermaidKey;
    private MermaidManager mermaidManager;

    @Override
    public void onEnable() {
        // مفتاح مميز نستخدمه لوسم القطعة (NBT) عشان نفرق بينها وبين أي item عادي
        this.mermaidKey = new NamespacedKey(this, "mermaid_charm");

        this.mermaidManager = new MermaidManager(this, mermaidKey);

        // تسجيل الأوامر
        MermaidCommand commandExecutor = new MermaidCommand(this, mermaidKey);
        getCommand("mermaid").setExecutor(commandExecutor);
        getCommand("mermaid").setTabCompleter(commandExecutor);

        // تسجيل الأحداث (listener)
        getServer().getPluginManager().registerEvents(new MermaidListener(mermaidKey), this);

        // تشغيل الفحص الدوري (كل نص ثانية) بدل ما نعتمد على PlayerMoveEvent
        mermaidManager.startTask();

        getLogger().info("MermaidPlugin فعّال! ✔");
    }

    @Override
    public void onDisable() {
        if (mermaidManager != null) {
            mermaidManager.shutdown();
        }
        getLogger().info("MermaidPlugin اتقفل.");
    }

    public NamespacedKey getMermaidKey() {
        return mermaidKey;
    }
}
