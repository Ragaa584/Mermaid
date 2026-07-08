package com.mermaid.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MermaidListener implements Listener {

    private final NamespacedKey mermaidKey;

    public MermaidListener(NamespacedKey mermaidKey) {
        this.mermaidKey = mermaidKey;
    }

    // منع رمي القطعة على الأرض
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (MermaidItemUtil.isMermaidCharm(dropped, mermaidKey)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cمينفعش ترمي عقد حورية البحر!");
        }
    }

    // منع نقل القطعة لأي انفنتوري تاني (چست، شلكر، تريد مع فيلاجر، إلخ)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean movingMermaidItem =
                (current != null && MermaidItemUtil.isMermaidCharm(current, mermaidKey)) ||
                (cursor != null && MermaidItemUtil.isMermaidCharm(cursor, mermaidKey));

        if (!movingMermaidItem) return;

        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        // لو التاچ اللي ضغط عليه مش انفنتوري اللاعب نفسه (يعني چست/شلكر/تريد/فورنس..)
        boolean topIsPlayerInventory = topInventory instanceof PlayerInventory;

        if (!topIsPlayerInventory) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage("§cمينفعش تنقل عقد حورية البحر لانفنتوري تاني.");
            }
        }

        // منع الـ Shift-click اللي ممكن ينقلها تلقائي لچست مفتوح
        if (clickedInventory instanceof PlayerInventory && !topIsPlayerInventory && event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    // تنظيف أي بيانات مؤقتة لما اللاعب يخرج (تفادي تسريب ذاكرة)
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // ملحوظة: MermaidManager عنده Set/Map خاصين بيه؛ لو حابب ننظفهم من هنا
        // كان لازم نمرر مرجع الـ MermaidManager هنا. حاليًا الـ tick loop بيتعامل معاها تلقائيًا
        // لأنه بيشتغل بس على getOnlinePlayers()، فمفيش مشكلة حقيقية، بس سبنا الميثود دي كمكان
        // منطقي لو حبيت تضيف تنظيف إضافي مستقبلاً.
    }
}
