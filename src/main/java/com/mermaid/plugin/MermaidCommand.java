package com.mermaid.plugin;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MermaidCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final NamespacedKey mermaidKey;

    public MermaidCommand(JavaPlugin plugin, NamespacedKey mermaidKey) {
        this.plugin = plugin;
        this.mermaidKey = mermaidKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("mermaid.give")) {
            sender.sendMessage("§cمعندكش صلاحية تستخدم الأمر ده.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /mermaid <give|remove> <player>");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage("§cاللاعب ده مش أونلاين.");
            return true;
        }

        switch (action) {
            case "give" -> {
                ItemStack charm = MermaidItemUtil.createMermaidCharm(mermaidKey);
                target.getInventory().addItem(charm);
                sender.sendMessage("§aتم إعطاء " + target.getName() + " عقد حورية البحر.");
                target.sendMessage("§dحصلت على §5عقد حورية البحر§d! جرب تنزل الميه وهو معاك.");
            }
            case "remove" -> {
                // بنشيل كل نسخة من العقد من انفنتوري اللاعب
                target.getInventory().forEach(item -> {
                    if (MermaidItemUtil.isMermaidCharm(item, mermaidKey)) {
                        item.setAmount(0);
                    }
                });
                sender.sendMessage("§aتم سحب عقد حورية البحر من " + target.getName() + ".");
            }
            default -> sender.sendMessage("§eUsage: /mermaid <give|remove> <player>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("give");
            options.add("remove");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                options.add(p.getName());
            }
        }
        return options;
    }
}
