package primalcat.tempusskins;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class Command implements CommandExecutor {


    private static String getSkinNameByIndex(List<String> skinNames, int index) {
        String skinName;
        try {
            skinName = skinNames.get(index);
            return skinName;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Использование данной команды возможно только в игре!");
            return true;
        }
        if (!sender.hasPermission("tempus.tskins")) {
            sender.sendMessage("§4Данное меню доступно только спонсорам!");
            return true;
        }

        Player player = (Player) sender;
        String nickname = player.getName();
        int invSize = 27;

        LinkedHashMap<String, String> playerSkins = Util.getPlayerSkins(nickname);
        List<String> skinNames = new ArrayList<>(playerSkins.keySet());

        Inventory skinsInv = Bukkit.createInventory(null, invSize, Component.text("§8Скины §7|§8 " + nickname));

        List<String> headLore = new ArrayList<>();
        headLore.add("§f");
        headLore.add("§6Установить скин: §fЛКМ");
        headLore.add("§6Удалить скин: §fШИФТ-ЛКМ");

        for (int i = 0; i < invSize; i++) {

            String skinName = getSkinNameByIndex(skinNames, i);

            if (skinName == null) break;
            String skinValue = Util.getSkinByName(nickname, skinName, Util.DataType.VALUE);
            // @TODO  player.getName replace null
            GameProfile profile = new GameProfile(UUID.randomUUID(), player.getName());
            profile.getProperties().put("textures", new Property("textures", skinValue));

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setDisplayName("§7"+skinName);
                skullMeta.setLore(headLore);
                skullMeta.getPersistentDataContainer().set(new NamespacedKey(TempusSkins.plugin, "skinName"), PersistentDataType.STRING, skinName);
                skullMeta.setCustomModelData(3);
                Field profileField;
                try {
                    profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);

                    playerHead.setItemMeta(skullMeta);

                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                playerHead.setItemMeta(skullMeta);
            }

            skinsInv.setItem(i, playerHead);


        }

        int empty = skinsInv.firstEmpty();

        if (empty != -1) {

            ItemStack addButton = new ItemStack(Material.PAPER);
            ItemMeta buttonMeta = addButton.getItemMeta();
            buttonMeta.setCustomModelData(39);
            buttonMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            buttonMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            buttonMeta.displayName(Component.text("§2+ §8| §fСоханить текущий"));
            addButton.setItemMeta(buttonMeta);

            skinsInv.setItem(empty, addButton);
        }

        player.openInventory(skinsInv);

        return true;
    }
}

