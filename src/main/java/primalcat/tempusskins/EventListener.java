package primalcat.tempusskins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.MojangSkinDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.skinsrestorer.api.PropertyUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class EventListener implements Listener {

    @EventHandler
    public static void onSkinsGUI(InventoryClickEvent event) {
        String invTitle;

        try {
            invTitle = ((TextComponent) event.getView().title()).content();
        } catch (Exception e) {
            return;
        }

        if (!invTitle.contains("Скины")) return;
        onButtonClick(event);

    }

    private static void onButtonClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) event.getWhoClicked();
        String nickname = player.getName();
        String skinName;
        String skinValue;
        String skinSignature;

        switch (clickedItem.getType()) {
            case PLAYER_HEAD:
                ClickType clickType = event.getClick();

                ItemMeta itemMeta = clickedItem.getItemMeta();
                if (itemMeta == null) return;

                skinName = itemMeta.getPersistentDataContainer().get(new NamespacedKey(TempusSkins.plugin, "skinName"), PersistentDataType.STRING);

                if (clickType == ClickType.LEFT) {
                    skinValue = Util.getSkinByName(nickname, skinName, Util.DataType.VALUE);
                    skinSignature = Util.getSkinByName(nickname, skinName, Util.DataType.SIGNATURE);

                    SkinStorage skinStorage = TempusSkins.getAPI().getSkinStorage();
                    skinStorage.setCustomSkinData(skinName, SkinProperty.of(skinValue, skinSignature));

                    try{
                        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(skinName);
                        PlayerStorage playerStorage = TempusSkins.getAPI().getPlayerStorage();
                        playerStorage.setSkinIdOfPlayer(player.getUniqueId(), result.get().getIdentifier());

                        // Instantly apply skin to the player without requiring the player to rejoin
                        TempusSkins.getAPI().getSkinApplier(Player.class).applySkin(player);
//                        String skinURL = PropertyUtils.getSkinTextureUrl(property.get());
//                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin set " + nickname + " " + skinURL);
                    }catch (Exception e){
                        System.out.println(e);
                    }
                    break;
                }

                if (clickType == ClickType.SHIFT_LEFT) {
//                    System.out.println("Test " + nickname + " " + skinName);
                    Util.removeSkinByName(nickname, skinName);
                    player.sendMessage("§eСкин §6" + skinName + " §eуспешно удалён!");
                    player.closeInventory();
                    player.getInventory().removeItem(clickedItem);
//                    for (ItemStack item : player.getInventory()){
//                        System.out.println(item);
//                    }
                    break;
                }


            case PAPER:
                PlayerStorage playerStorage = TempusSkins.getAPI().getPlayerStorage();
                try {
                    Optional<SkinProperty> property = playerStorage.getSkinForPlayer(player.getUniqueId(), player.getName());

                    if (property.isPresent()) {
                        String textureUrl = PropertyUtils.getSkinTextureUrl(property.get());
//                        PropertyUtils.getSkinVariant(property.get());
                        MineSkinAPI mineSkinAPI = TempusSkins.getAPI().getMineSkinAPI();
                        MineSkinResponse response = mineSkinAPI.genSkin(textureUrl,  PropertyUtils.getSkinVariant(property.get()));
                        SkinProperty skinProperty = response.getProperty();

                        skinValue = skinProperty.getValue();
                        skinSignature = skinProperty.getSignature();
                    } else {
                        player.sendMessage(Component.text("§4Для сохранения скина вам нужно его установить!"));
                        return;
                    }
                } catch (Exception e) {
                    player.sendMessage(Component.text("§4Для сохранения скина вам нужно его установить!"));
                    return;
                }

                player.playSound(player, Sound.BLOCK_BONE_BLOCK_HIT, 1, 1);
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta headMeta = playerHead.getItemMeta();
                headMeta.displayName(Component.text(nickname));
                // @TODO player.getName was null so note this
                GameProfile profile = new GameProfile(UUID.randomUUID(), player.getName());
                profile.getProperties().put("textures", new Property("textures", skinValue));
                Field profileField;

                try {
                    profileField = headMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(headMeta, profile);
                    playerHead.setItemMeta(headMeta);
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }



                new AnvilGUI.Builder().onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        String newSkinName = stateSnapshot.getText();

                        Util.saveSkinByName(nickname, newSkinName, skinValue, skinSignature);
                        player.sendMessage("§2Скин с названием §e" + newSkinName + " §2успешно сохранён!");
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }
                    return Collections.emptyList();
                }).text(nickname).itemLeft(playerHead).title("§2Название скина:").plugin(TempusSkins.plugin).open(player);

        }
    }
}