package fr.maxlego08.convert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;

import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuItem;

import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.zcore.logger.Logger;
import fr.maxlego08.menu.zcore.utils.ZUtils;

public class ConvertDeluxeMenu extends ZUtils {

	private final MenuPlugin plugin;
	private boolean isRunning = false;

	/**
	 * @param plugin
	 */
	public ConvertDeluxeMenu(MenuPlugin plugin) {
		super();
		this.plugin = plugin;
	}

	/*
	 * Menu.getAllMenus().forEach(menu -> {
	 * 
	 * menu.getMenuItems().forEach((i, b) -> {
	 * 
	 * System.out.println(i); System.out.println(b); b.forEach((c, d) -> {
	 * System.out.println(c); System.out.println(d); });
	 * 
	 * });
	 * 
	 * });
	 */

	public void convert(CommandSender sender) {

		if (this.isRunning) {
			message(sender, "§7Conversion in progress, please wait.");
			return;
		}

		this.isRunning = true;

		File folderInventories = new File(this.plugin.getDataFolder(), "inventories/convert");
		if (!folderInventories.exists()) {
			folderInventories.mkdir();
		}

		File folderCommands = new File(this.plugin.getDataFolder(), "commands/convert");
		if (!folderCommands.exists()) {
			folderCommands.mkdir();
		}

		Collection<Menu> menus = Menu.getAllMenus();
		message(sender, "§7Start of the conversion of §f" + menus.size() + " menu(s)§7.");
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

			for (Menu menu : menus) {

				System.out.println("---");

				InventoryType inventoryType = menu.getInventoryType();

				if (inventoryType != null && inventoryType != InventoryType.PLAYER) {
					Logger.info(inventoryType + " not supported in " + menu.getName() + " menu, ship");
					continue;
				}

				String fileName = menu.getName();
				File file = new File(folderInventories, fileName + ".yml");
				if (file.exists()) {
					Logger.info("inventories/convert/" + fileName + ".yml already exist, skip");
					continue;
				}

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

				YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

				configuration.set("name", menu.getMenuTitle());
				configuration.set("size", menu.getSize());
				configuration.set("updateInterval", menu.getUpdateInterval());
				configuration.set("items", "[]");

				try {
					this.loadItems(menu, configuration);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					configuration.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			message(sender, "§aConversion complete. §7Please check that your files have been converted correctly.");
			this.isRunning = false;
		});

	}

	/**
	 * Allows you to load items
	 * 
	 * @param menu
	 * @param configuration
	 */
	private void loadItems(Menu menu, YamlConfiguration configuration) {

		Map<Integer, TreeMap<Integer, MenuItem>> items = menu.getMenuItems();

		items.forEach((value, treeMap) -> {
			treeMap.forEach((value2, item) -> {
				this.saveButton(item, configuration, "items." + value + ".");
			});
		});

	}

	private void saveButton(MenuItem item, YamlConfiguration configuration, String path) {

		configuration.set(path + "type", "NONE");
		configuration.set(path + "slot", item.getSlot());
		this.saveItem(item, configuration, path + "item.");

	}

	private void saveItem(MenuItem item, YamlConfiguration configuration, String path) {

		if (item.getMaterial() != null) {
			configuration.set(path + "material", item.getMaterial().name());
		}

		if (item.isPlaceholderMaterial() && item.getPlaceholderMaterial() != null) {
			configuration.set(path + "material", item.getPlaceholderMaterial());
		}

		if (item.getAmount() > 1) {
			configuration.set(path + "amount", item.getAmount());
		}

		if (item.getDynamicAmount() != null) {
			configuration.set(path + "amount", item.getDynamicAmount());
		}

		if (item.getData() > 0) {
			configuration.set(path + "data", item.getData());
		}

		if (item.getDisplayName() != null) {
			configuration.set(path + "name", item.getDisplayName());
		}

		if (item.getLore() != null) {
			configuration.set(path + "lore", item.getLore());
		}

		this.saveEnchantments(item, configuration, path);
		this.saveFlags(item, configuration, path);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private void saveEnchantments(MenuItem item, YamlConfiguration configuration, String path) {

		try {

			Field field = null;
			for (Field f : item.getClass().getDeclaredFields()) {
				if (f.getName().equalsIgnoreCase("enchantments")) {
					field = f;
					break;
				}
			}

			if (field == null) {
				return;
			}

			field = item.getClass().getField("enchantments");
			field.setAccessible(true);
			Map<Enchantment, Integer> enchantments = (Map<Enchantment, Integer>) field.get(item);
			List<String> list = enchantments.entrySet().stream().map(entry -> {
				return entry.getKey().getName() + "," + entry.getValue();
			}).collect(Collectors.toList());
			if (list.size() > 0) {
				configuration.set(path + "enchants", list);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void saveFlags(MenuItem item, YamlConfiguration configuration, String path) {

		List<ItemFlag> flags = item.itemFlags() != null ? item.itemFlags() : new ArrayList<ItemFlag>();

		if (item.hideAttributes() && !flags.contains(ItemFlag.HIDE_ATTRIBUTES)) {
			flags.add(ItemFlag.HIDE_ATTRIBUTES);
		}

		if (item.hideEnchants() && !flags.contains(ItemFlag.HIDE_ENCHANTS)) {
			flags.add(ItemFlag.HIDE_ENCHANTS);
		}

		if (item.hidePotionEffects() && !flags.contains(ItemFlag.HIDE_POTION_EFFECTS)) {
			flags.add(ItemFlag.HIDE_POTION_EFFECTS);
		}

		if (item.hideUnbreakable() && !flags.contains(ItemFlag.HIDE_UNBREAKABLE)) {
			flags.add(ItemFlag.HIDE_UNBREAKABLE);
		}

		configuration.set(path + "flags", flags.stream().map(ItemFlag::name).collect(Collectors.toList()));
	}

}
