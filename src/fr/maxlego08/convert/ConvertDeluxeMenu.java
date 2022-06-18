package fr.maxlego08.convert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import com.extendedclip.deluxemenus.requirement.HasPermissionRequirement;
import com.extendedclip.deluxemenus.requirement.InputResultRequirement;
import com.extendedclip.deluxemenus.requirement.Requirement;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.requirement.RequirementType;

import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.api.enums.PlaceholderAction;
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
			message(sender, "§7Dont forget to run §f/zmenu reload§7.");
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

			String path = "items." + value + ".";

			if (treeMap.size() == 1) {
				MenuItem item = treeMap.firstEntry().getValue();
				System.out.println(value + " - Slot: " + item.getSlot());
				this.saveButton(item, configuration, path);
			} else {
				treeMap.forEach((value2, item) -> {
					System.out.println(
							value + " - " + value2 + " - Slot: " + item.getSlot() + " Priority: " + item.getPriority());
					// this.saveButton(item, configuration, "items." + value +
					// ".");
				});

				List<MenuItem> sortItems = treeMap.values().stream()
						.sorted(Comparator.comparingInt(MenuItem::getPriority)).collect(Collectors.toList());

				for (MenuItem item : sortItems) {
					System.out.println(value + " - X - Slot: " + item.getSlot() + " Priority: " + item.getPriority());
					this.saveButton(item, configuration, path);
					path += "else.";
				}
			}
		});

	}

	/**
	 * Allows you to save a button
	 * 
	 * @param item
	 * @param configuration
	 * @param path
	 */
	private void saveButton(MenuItem item, YamlConfiguration configuration, String path) {

		configuration.set(path + "type", "NONE");
		configuration.set(path + "slot", item.getSlot());

		this.saveViewRequirement(item, configuration, path);
		this.saveItem(item, configuration, path + "item.");

	}

	/**
	 * Allows you to save an item
	 * 
	 * @param item
	 * @param configuration
	 * @param path
	 */
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

	@SuppressWarnings({ "deprecation" })
	/**
	 * Allows to save enchantments
	 * 
	 * @param item
	 * @param configuration
	 * @param path
	 */
	private void saveEnchantments(MenuItem item, YamlConfiguration configuration, String path) {

		try {
			Map<Enchantment, Integer> enchantments = getField(item, "enchantments");
			if (enchantments != null) {
				List<String> list = enchantments.entrySet().stream().map(entry -> {
					return entry.getKey().getName() + "," + entry.getValue();
				}).collect(Collectors.toList());
				if (list.size() > 0) {
					configuration.set(path + "enchants", list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Allows to save the ItemFlags
	 * 
	 * @param item
	 * @param configuration
	 * @param path
	 */
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

		if (flags.size() > 0) {
			configuration.set(path + "flags", flags.stream().map(ItemFlag::name).collect(Collectors.toList()));
		}
	}

	/**
	 * Allows you to save view requirements
	 * 
	 * @param item
	 * @param configuration
	 * @param path
	 */
	private void saveViewRequirement(MenuItem item, YamlConfiguration configuration, String path) {

		RequirementList requirements = item.getViewRequirements();
		if (requirements == null) {
			return;
		}

		for (Requirement requirement : requirements.getRequirements()) {

			if (requirement instanceof HasPermissionRequirement) {

				try {
					String permission = this.getField(requirement, "perm");
					configuration.set(path + "permission", permission);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (requirement instanceof InputResultRequirement) {

				try {

					String input = getField(requirement, "input");
					String result = getField(requirement, "result");
					RequirementType type = getField(requirement, "type");

					PlaceholderAction action = convertAction(type);
					configuration.set(path + "action", action.name());
					configuration.set(path + "placeHolder", input);
					configuration.set(path + "value", result);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}

	}

	@SuppressWarnings("unchecked")
	private <T> T getField(Object object, String fieldName) throws Exception {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return (T) field.get(object);
	}

	private PlaceholderAction convertAction(RequirementType type) {
		switch (type) {
		case DOES_NOT_HAVE_EXP:
		case DOES_NOT_HAVE_ITEM:
		case DOES_NOT_HAVE_META:
		case DOES_NOT_HAVE_MONEY:
		case DOES_NOT_HAVE_PERMISSION:
		case REGEX_DOES_NOT_MATCH:
		case REGEX_MATCHES:
		case HAS_EXP:
		case HAS_ITEM:
		case HAS_META:
		case HAS_MONEY:
		case HAS_PERMISSION:
		case IS_NEAR:
		case IS_NOT_NEAR:
		case JAVASCRIPT:
		case STRING_DOES_NOT_EQUAL:
		case STRING_DOES_NOT_EQUAL_IGNORECASE:
			break;
		case EQUAL_TO:
			return PlaceholderAction.EQUAL_TO;
		case GREATER_THAN:
			return PlaceholderAction.SUPERIOR;
		case GREATER_THAN_EQUAL_TO:
			return PlaceholderAction.SUPERIOR_OR_EQUAL;
		case LESS_THAN:
			return PlaceholderAction.LOWER;
		case LESS_THAN_EQUAL_TO:
			return PlaceholderAction.LOWER_OR_EQUAL;
		case STRING_CONTAINS:
			return PlaceholderAction.CONTAINS_STRING;
		case STRING_DOES_NOT_CONTAIN:
			break;
		case STRING_EQUALS:
			return PlaceholderAction.EQUALS_STRING;
		case STRING_EQUALS_IGNORECASE:
			return PlaceholderAction.EQUALSIGNORECASE_STRING;
		default:
			break;
		}
		return null;
	}

}
