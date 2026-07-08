package com.mermaid.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MermaidManager {

    private final JavaPlugin plugin;
    private final NamespacedKey mermaidKey;

    // اللاعبين اللي دلوقتي في "وضع الحورية"
    private final Set<UUID> activeMermaids = new HashSet<>();

    // نخزن الجزمة الأصلية بتاعت اللاعب عشان نرجعهالوا بعد ما يطلع من الوضع
    private final Map<UUID, ItemStack> savedBoots = new HashMap<>();

    private BukkitTask task;

    private static final int DURATION_TICKS = 40; // نجدد الإيفكتات كل 2 ثانية (40 تيك) عشان تفضل مستمرة

    public MermaidManager(JavaPlugin plugin, NamespacedKey mermaidKey) {
        this.plugin = plugin;
        this.mermaidKey = mermaidKey;
    }

    public void startTask() {
        // بنفحص كل 10 تيكات (نص ثانية) بدل ما نستخدم PlayerMoveEvent عشان نقلل الحمل على السيرفر
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 10L);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        // نرجع كل حاجة لطبيعتها لو البلجن اتقفل والسيرفر لسه شغال
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (activeMermaids.contains(p.getUniqueId())) {
                deactivate(p);
            }
        }
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean inWater = player.isInWater() || player.getLocation().getBlock().isLiquid();
            boolean hasCharm = hasMermaidCharm(player);
            boolean isActive = activeMermaids.contains(player.getUniqueId());

            if (inWater && hasCharm) {
                activate(player);
            } else if (isActive) {
                deactivate(player);
            }
        }
    }

    private boolean hasMermaidCharm(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (MermaidItemUtil.isMermaidCharm(item, mermaidKey)) {
                return true;
            }
        }
        return false;
    }

    private void activate(Player player) {
        boolean firstTime = !activeMermaids.contains(player.getUniqueId());
        activeMermaids.add(player.getUniqueId());

        // الإيفكتات (بنجددها باستمرار طول ما هو في الوضع ده)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION_TICKS, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, DURATION_TICKS, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, DURATION_TICKS, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, DURATION_TICKS, 0, true, false));

        if (firstTime) {
            player.sendMessage("§d✦ اتحولتي لحورية بحر! ✦");
            equipTail(player);
        }
    }

    private void deactivate(Player player) {
        activeMermaids.remove(player.getUniqueId());

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.STRENGTH);

        player.sendMessage("§7رجعتي طبيعية.");
        restoreBoots(player);
    }

    /**
     * ملحوظة مهمة: مافيش API رسمي في Paper/Spigot يغيّر شكل جسم اللاعب لديل سمكة حقيقي.
     * الحل هنا "تجميلي" بسيط: بنحط جزمة جلد بنفسجية بدل جزمة اللاعب وقت ما يكون في وضع الحورية،
     * كإشارة بصرية بسيطة. لو عايز شكل ديل حقيقي 3D لازم Resource Pack مخصص (Blockbench)
     * أو مكتبة خارجية زي ModelEngine.
     */
    private void equipTail(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack currentBoots = inv.getBoots();

        // نخزن الجزمة الأصلية عشان نرجعهالوا بعدين (ممكن تكون null لو مفيش جزمة أصلاً)
        savedBoots.put(player.getUniqueId(), currentBoots);

        ItemStack tailBoots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) tailBoots.getItemMeta();
        meta.setColor(Color.PURPLE);
        meta.setDisplayName("§5ذيل حورية البحر");
        tailBoots.setItemMeta(meta);

        inv.setBoots(tailBoots);
    }

    private void restoreBoots(Player player) {
        ItemStack original = savedBoots.remove(player.getUniqueId());
        player.getInventory().setBoots(original); // ممكن تكون null، وده طبيعي لو ماكانش لابس جزمة أصلاً
    }

    public void handleQuit(Player player) {
        if (activeMermaids.remove(player.getUniqueId())) {
            savedBoots.remove(player.getUniqueId());
        }
    }
}
