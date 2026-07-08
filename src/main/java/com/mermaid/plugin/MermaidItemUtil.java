package com.mermaid.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MermaidItemUtil {

    /**
     * بينشئ قطعة "عقد حورية البحر" المميزة (بنفسجية اللون + وسم NBT خاص).
     */
    public static ItemStack createMermaidCharm(NamespacedKey key) {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = item.getItemMeta();

        // اسم بنفسجي مميز (§5 = بنفسجي في كود ألوان ماين كرافت)
        meta.setDisplayName("§5§lعقد حورية البحر");

        meta.setLore(java.util.List.of(
                "§7تعويذة أسطورية قديمة",
                "§7تحوّلك لحورية بحر وانت جوا الماء",
                "",
                "§d✦ سرعة، قوة، تنفس تحت الماء ورؤية ليلية ✦"
        ));

        // نضيف تأثير "توهج" بصري بسيط (enchant وهمي مخفي) عشان يبان مميز في اليد
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        // الوسم السري اللي بيميز القطعة عن أي Nautilus Shell عادي
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * بيتحقق هل الـ ItemStack ده هو عقد الحورية الأصلي (مش أي نوتيلوس شل عادي).
     */
    public static boolean isMermaidCharm(ItemStack item, NamespacedKey key) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Boolean value = meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        return value != null && value;
    }
}
