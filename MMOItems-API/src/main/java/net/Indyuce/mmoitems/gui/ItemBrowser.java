package net.Indyuce.mmoitems.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.NewItemEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.stat.BrowserDisplayIDX;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemBrowser extends PluginInventory {
    private final Map<String, ItemStack> cached = new LinkedHashMap<>();

    private final Type type;
    private boolean deleteMode;

    // Slots used to display items based on the item type explored
    private static final int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int[] slotsAlt = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public ItemBrowser(Player player) {
        this(player, null);
    }

    public ItemBrowser(Player player, Type type) {
        super(player);

        this.type = type;
    }


    @NotNull @Override public Inventory getInventory() {

        /*
         * ------------------------------
         *          TYPE BROWSER
         *
         *          Displays all possible item types if no type was previously selected by the player.
         *  ------------------------------
         */
        if (type == null) {

            int[] usedSlots = slots;
            int min = (page - 1) * usedSlots.length;
            int max = page * usedSlots.length;
            int n = 0;

            // Create inventory
            Inventory inv = Bukkit.createInventory(this, 54, "????????????");

            // Fetch the list of types
            List<Type> types = new ArrayList<>(MMOItems.plugin.getTypes().getAll());
            for (int j = min; j < Math.min(max, types.size()); j++) {

                // Current type to display into the GUI
                Type currentType = types.get(j);

                // Get number of items
                int items = MMOItems.plugin.getTemplates().getTemplates(currentType).size();

                // Display how many items are in the type
                ItemStack item = currentType.getItem();
                item.setAmount(Math.max(1, Math.min(64, items)));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + currentType.getName() + ChatColor.DARK_GRAY + " (????????????)");
                meta.addItemFlags(ItemFlag.values());
                List<String> lore = new ArrayList<>();
                lore.add(String.valueOf(ChatColor.GRAY) + ChatColor.ITALIC + "There " + (items == 1 ? "is" : "are") + " "
                        + (items < 1 ? String.valueOf(ChatColor.RED) + ChatColor.ITALIC + "no" : String.valueOf(ChatColor.GOLD) + ChatColor.ITALIC + items) + ChatColor.GRAY
                        + ChatColor.ITALIC + " item" + (items == 1 ? "" : "s") + " in that currentType.");
                meta.setLore(lore);
                item.setItemMeta(meta);

                // Set item
                inv.setItem(slots[n++], NBTItem.get(item).addTag(new ItemTag("typeId", currentType.getId())).toItem());
            }

            // Fill remainder slots with 'No Type' notice
            ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.setDisplayName(ChatColor.RED + "- ???????????? -");
            glass.setItemMeta(glassMeta);

            // Next Page
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(ChatColor.GREEN + "?????????");
            next.setItemMeta(nextMeta);

            // Previous Page
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta previousMeta = previous.getItemMeta();
            previousMeta.setDisplayName(ChatColor.GREEN + "?????????");
            previous.setItemMeta(previousMeta);

            // Fill
            while (n < slots.length) { inv.setItem(slots[n++], glass); }
            inv.setItem(18, page > 1 ? previous : null);
            inv.setItem(26, max >= MMOItems.plugin.getTypes().getAll().size() ? null : next);

            // Done
            return inv;
        }

        /*
         * ------------------------------
         *          ITEM BROWSER
         *
         *          Displays all the items of the chosen Type
         *  ------------------------------
         */
        Inventory inv = Bukkit.createInventory(this, 54, (deleteMode ? ("????????????: ") : ("????????????: ")) + type.getName());

        /*
         * Build cool Item Stacks for buttons and sh
         */
        ItemStack error = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
        ItemMeta errorMeta = error.getItemMeta();
        errorMeta.setDisplayName(ChatColor.RED + "- ?????? -");
        List<String> errorLore = new ArrayList<>();
        errorLore.add("\u00a7\u00a7oAn error occurred while ???????????????");
        errorLore.add("\u00a7\u00a7otrying to generate that item ?????????????????????");
        errorMeta.setLore(errorLore);
        error.setItemMeta(errorMeta);

        ItemStack noItem = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
        ItemMeta noItemMeta = noItem.getItemMeta();
        noItemMeta.setDisplayName(ChatColor.RED + "- ???????????? -");
        noItem.setItemMeta(noItemMeta);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "?????????");
        next.setItemMeta(nextMeta);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " ??????");
        back.setItemMeta(backMeta);

        ItemStack create = new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial());
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "????????????");
        create.setItemMeta(createMeta);

        ItemStack delete = new ItemStack(VersionMaterial.CAULDRON.toMaterial());
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + (deleteMode ? "????????????" : "????????????"));
        delete.setItemMeta(deleteMeta);

        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "?????????");
        previous.setItemMeta(previousMeta);

        if (type == Type.BLOCK) {
            ItemStack downloadPack = new ItemStack(Material.HOPPER);
            ItemMeta downloadMeta = downloadPack.getItemMeta();
            downloadMeta.setDisplayName(ChatColor.GREEN + "?????????????????????");
            downloadMeta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "Only seeing stone blocks?", "",
                    ChatColor.RED + "???????????????????????????, ?????????", ChatColor.RED + "????????????????????????.",
                    ChatColor.RED + "?????????????????????????????????????????????!"));
            downloadPack.setItemMeta(downloadMeta);
            inv.setItem(45, downloadPack); }

        // Get templates of this type
        HashMap<Double, ArrayList<MMOItemTemplate>> templates = BrowserDisplayIDX.select(MMOItems.plugin.getTemplates().getTemplates(type));

        /*
         *  -----------
         *    CALCULATE GUI BOUNDS AND PAGE SIZES
         *
         *      Each display index claims the entire column of items, such that there will be
         *      empty spaces added to fill the inventories.
         *
         *      In Four GUI mode, columns are four slots tall, else they are three slots tall.
         *  -----------
         */
        int[] usedSlots = type.isFourGUIMode() ? slotsAlt : slots;
        int min = (page - 1) * usedSlots.length;
        int max = page * usedSlots.length;
        int n = 0;

        int sc = type.isFourGUIMode() ? 4 : 3;
        int totalSpaceCount = 0;

        for (Map.Entry<Double, ArrayList<MMOItemTemplate>> indexTemplates : templates.entrySet()) {

            // Claim columns
            int totalSpaceAdd = indexTemplates.getValue().size();
            while (totalSpaceAdd > 0) { totalSpaceCount += sc; totalSpaceAdd -= sc; } }

        /*
         * Over the page-range currently in use...
         */
        for (int j = min; j < Math.min(max, totalSpaceCount); j++) {
            MMOItemTemplate template = BrowserDisplayIDX.getAt(j, templates);

            // No template here?
            if (template == null) {

                // Set Item
                inv.setItem(usedSlots[n], noItem);

                /*
                 *      Calculate next n from the slots.
                 *
                 *      #1 Adding 7 will give you the slot immediately under
                 *
                 *      #2 If it overflows, subtract 7sc (column space * 7)
                 *         and add one
                 */
                n += 7;
                if (n >= usedSlots.length) { n -= 7 * sc; n++; }
                continue;
            }

            // Build item -> any errors?
            ItemStack item = template.newBuilder(playerData.getRPG()).build().newBuilder().build();
            if (item == null || item.getType().isAir() || !item.getType().isItem() || item.getItemMeta() == null) {

                // Set Item
                cached.put(template.getId(), error);
                inv.setItem(usedSlots[n], error);

                /*
                 *      Calculate next n from the slots.
                 *
                 *      #1 Adding 7 will give you the slot immediately under
                 *
                 *      #2 If it overflows, subtract 7sc (column space * 7)
                 *         and add one
                 */
                n += 7;
                if (n >= usedSlots.length) { n -= 7 * sc; n++; }
                continue; }

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) { lore = new ArrayList<>(); }
            lore.add("");

            // Deleting lore?
            if (deleteMode) {
                lore.add(ChatColor.RED + AltChar.cross + " ???????????? " + AltChar.cross);
                meta.setDisplayName(ChatColor.RED + "??????: " + (meta.hasDisplayName() ? meta.getDisplayName() : MMOUtils.getDisplayName(item)));

            // Editing lore?
            } else {
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " ???????????????????????????");
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " ???????????????????????????"); }

            meta.setLore(lore);
            item.setItemMeta(meta);

            // Set item
            cached.put(template.getId(), item);
            inv.setItem(usedSlots[n], cached.get(template.getId()));

            /*
             *      Calculate next n from the slots.
             *
             *      #1 Adding 7 will give you the slot immediately under
             *
             *      #2 If it overflows, subtract 7sc (column space * 7)
             *         and add one
             */
            n += 7;
            if (n >= usedSlots.length) { n -= 7 * sc; n++; }
        }

        // Put the buttons
        if (!deleteMode) { inv.setItem(51, create); }
        inv.setItem(47, delete);
        inv.setItem(49, back);
        inv.setItem(18, page > 1 ? previous : null);
        inv.setItem(26, max >= totalSpaceCount ? null : next);
        for (int i : usedSlots) { if (SilentNumbers.isAir(inv.getItem(i))) { inv.setItem(i, noItem); } }
        return inv;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory())
            return;

        ItemStack item = event.getCurrentItem();
        if (MMOUtils.isMetaItem(item, false)) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "?????????")) {
                page++;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "?????????")) {
                page--;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " ??????"))
                new ItemBrowser(getPlayer()).open();

            else if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "????????????")) {
                deleteMode = false;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "????????????"))
                new NewItemEdition(this).enable("???????????????????????????????????????");

            else if (type != null && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "????????????")) {
                deleteMode = true;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "?????????????????????")) {
                MythicLib.plugin.getVersion().getWrapper().sendJson(getPlayer(),
                        "[{\"text\":\"????????????!\",\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"\",{\"text\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\",\"italic\":true,\"color\":\"white\"}]}}]");
                getPlayer().closeInventory();
            } else if (type == null && !item.getItemMeta().getDisplayName().equals(ChatColor.RED + "- ???????????? -"))
                new ItemBrowser(getPlayer(), MMOItems.plugin.getTypes().get(NBTItem.get(item).getString("typeId"))).open();
        }

        String id = NBTItem.get(item).getString("MMOITEMS_ITEM_ID");
        if (id.equals(""))
            return;

        if (deleteMode) {
            MMOItems.plugin.getTemplates().deleteTemplate(type, id);
            deleteMode = false;
            open();

        } else {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                getPlayer().getInventory().addItem(MMOItems.plugin.getItem(type, id, playerData));
                getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            }

            if (event.getAction() == InventoryAction.PICKUP_HALF)
                new ItemEdition(getPlayer(), MMOItems.plugin.getTemplates().getTemplate(type, id)).open();
        }
    }
}
