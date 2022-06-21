package fr.maxlego08.convert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;

import com.extendedclip.deluxemenus.action.ClickAction;
import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.menu.Menu;
import com.extendedclip.deluxemenus.menu.MenuItem;
import com.extendedclip.deluxemenus.requirement.HasItemRequirement;
import com.extendedclip.deluxemenus.requirement.HasMoneyRequirement;
import com.extendedclip.deluxemenus.requirement.HasPermissionRequirement;
import com.extendedclip.deluxemenus.requirement.InputResultRequirement;
import com.extendedclip.deluxemenus.requirement.Requirement;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import com.extendedclip.deluxemenus.requirement.RequirementType;
import com.extendedclip.deluxemenus.requirement.wrappers.ItemWrapper;
import com.extendedclip.deluxemenus.utils.SkullUtils;

import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.api.enums.PlaceholderAction;
import fr.maxlego08.menu.api.enums.XSound;
import fr.maxlego08.menu.zcore.logger.Logger;
import fr.maxlego08.menu.zcore.utils.ZUtils;
import fr.maxlego08.menu.zcore.utils.nms.NMSUtils;

public class ConvertDeluxeMenu extends ZUtils {

	private final MenuPlugin plugin;
	private boolean isRunning = false;
	private final String placeholderArgumentFormat = "%%zmenu_argument_%s%%";

	private final List<Button> buttons = new ArrayList<>();

	/**
	 * @param plugin
	 */
	public ConvertDeluxeMenu(MenuPlugin plugin) {
		super();
		this.plugin = plugin;
	}

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

				this.buttons.clear();

				InventoryType inventoryType = menu.getInventoryType();

				if (inventoryType != null && inventoryType != InventoryType.PLAYER) {
					Logger.info(inventoryType + " not supported in " + menu.getName() + " menu, ship");
					continue;
				}

				String fileName = menu.getName();

				try {
					this.createCommand(menu, new File(folderCommands, fileName + ".yml"), fileName);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

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

				if (menu.getMenuTitle() != null) {
					configuration.set("name", colorReverse(menu.getMenuTitle()));
				}
				configuration.set("size", menu.getSize());
				try {
					int updateInterval = getField(menu, "updateInterval");
					if (updateInterval != 0) {
						configuration.set("updateInterval", updateInterval);
					}
				} catch (Exception e1) {
				}
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
	 * Allows you to create a command
	 * 
	 * @param menu
	 * @param file
	 * @param name
	 * @throws IOException
	 */
	private void createCommand(Menu menu, File file, String name) throws IOException {

		if (!menu.registersCommand()){
			return;
		}
		
		if (file.exists()) {
			Logger.info("commands/convert/" + name + ".yml already exist, skip");
			return;
		}

		file.createNewFile();

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

		String path = "commands." + name + ".";

		configuration.set(path + "command", menu.getName());
		configuration.set(path + "inventory", name);
		if (menu.getAliases().size() > 0) {
			configuration.set(path + "aliases", menu.getAliases());
		}
		if (menu.getPermission() != null) {
			configuration.set(path + "permission", menu.getPermission());
		}

		try {
			List<String> args = getField(menu, "args");
			if (args != null) {
				configuration.set(path + "arguments", args);
			}
		} catch (Exception e) {
		}

		configuration.save(file);

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

				if (!alreadyExist(item, path)) {
					this.saveButton(item, configuration, path);
				}

			} else {

				List<MenuItem> sortItems = treeMap.values().stream()
						.sorted(Comparator.comparingInt(MenuItem::getPriority)).collect(Collectors.toList());

				for (MenuItem item : sortItems) {
					this.saveButton(item, configuration, path);
					path += "else.";
				}
			}
		});

		for (Button button : this.buttons) {

			String path = button.getPath();
			if (button.getSlots().size() > 1) {
				configuration.set(path + "slot", null);
				configuration.set(path + "type", "NONE_SLOT");
				configuration.set(path + "slots", button.toRange());
			}

		}

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

		if (item.updatePlaceholders()) {
			configuration.set(path + "update", true);
		}

		this.saveViewRequirement(item, configuration, path);

		if (!this.saveClickRequirement(item.getClickRequirements(), "click_requirement", configuration, path,
				item.getClickHandler(), ClickType.RIGHT, ClickType.LEFT)) {
			this.saveClick(item.getClickHandler(), configuration, path, ClickType.UNKNOWN);
		}

		if (!this.saveClickRequirement(item.getRightClickRequirements(), "right_click_requirement", configuration, path,
				item.getRightClickHandler(), ClickType.RIGHT)) {
			this.saveClick(item.getRightClickHandler(), configuration, path, ClickType.RIGHT);
		}

		if (!this.saveClickRequirement(item.getLeftClickRequirements(), "left_click_requirement", configuration, path,
				item.getLeftClickHandler(), ClickType.LEFT)) {
			this.saveClick(item.getLeftClickHandler(), configuration, path, ClickType.LEFT);
		}

		this.saveClickRequirement(item.getShiftLeftClickRequirements(), "shift_left_click_requirement", configuration,
				path, item.getShiftLeftClickHandler(), ClickType.SHIFT_LEFT);

		this.saveClickRequirement(item.getShiftRightClickRequirements(), "shift_right_click_requirement", configuration,
				path, item.getShiftRightClickHandler(), ClickType.SHIFT_RIGHT);

		this.saveClickRequirement(item.getMiddleClickRequirements(), "middle_click_requirement", configuration, path,
				item.getMiddleClickHandler(), ClickType.MIDDLE);

		this.saveItem(item, configuration, path + "item.");
	}

	/**
	 * Allows you to save a click with a requirement
	 * 
	 * @param requirements
	 * @param configuration
	 * @param path
	 * @param clickHandler
	 * @return boolean
	 */
	private boolean saveClickRequirement(RequirementList requirements, String name, YamlConfiguration configuration,
			String path, ClickHandler allow, ClickType... clickTypes) {

		if (requirements == null || allow == null) {
			return false;
		}

		ClickHandler deny = requirements.getDenyHandler();

		if (configuration.getString(path + "type").equals("NONE")) {
			configuration.set(path + "type", "PERFORM_COMMAND");
		}

		String currentPath = path + "actions." + name + ".";

		List<String> clicks = Arrays.asList(clickTypes).stream().map(ClickType::name).collect(Collectors.toList());
		configuration.set(currentPath + "clicks", clicks);

		this.saveClickHandler(allow, configuration, currentPath + "allow.");
		if (deny != null) {
			this.saveClickHandler(deny, configuration, currentPath + "deny.");
		}

		int index = 1;
		String permissionPath = currentPath + "permissions.";

		for (Requirement requirement : requirements.getRequirements()) {

			if (requirement instanceof HasPermissionRequirement) {
				try {

					String permission = this.getField(requirement, "perm");
					boolean isReverse = this.getField(requirement, "invert");

					String cPath = permissionPath + "permission_" + (index++) + ".";
					configuration.set(cPath + "permission", (isReverse ? "!" : "") + permission);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requirement instanceof InputResultRequirement) {
				try {

					String input = getField(requirement, "input");
					String result = getField(requirement, "result");
					RequirementType type = getField(requirement, "type");

					PlaceholderAction action = convertAction(type);

					String cPath = permissionPath + "placeholder_" + (index++) + ".";

					configuration.set(cPath + "action", action.name());
					configuration.set(cPath + "placeHolder", input);
					configuration.set(cPath + "value", result);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requirement instanceof HasMoneyRequirement) {

				try {

					boolean invert = getField(requirement, "invert");
					String placeholder = getField(requirement, "placeholder");
					long amount = getField(requirement, "amount");

					if (placeholder == null) {
						placeholder = "%vault_eco_balance%";
					}

					String cPath = permissionPath + "placeholder_" + (index++) + ".";

					PlaceholderAction action = invert ? PlaceholderAction.LOWER : PlaceholderAction.SUPERIOR_OR_EQUAL;
					configuration.set(cPath + "action", action.name());
					configuration.set(cPath + "placeHolder", placeholder);
					configuration.set(cPath + "value", amount);

				} catch (Exception e) {
				}

			} else if (requirement instanceof HasItemRequirement) {

				try {

					ItemWrapper itemWrapper = getField(requirement, "wrapper");

					String cPath = permissionPath + "item_" + (index++) + ".";

					configuration.set(cPath + "material", itemWrapper.getMaterial());
					configuration.set(cPath + "amount", itemWrapper.getAmount());

				} catch (Exception e) {
				}

			}
		}

		return true;
	}

	private void saveClickHandler(ClickHandler clickHandler, YamlConfiguration configuration, String path) {
		List<String> messages = new ArrayList<>();
		List<String> commands = new ArrayList<>();
		List<String> consoleCommands = new ArrayList<>();

		try {
			List<ClickAction> actions = getField(clickHandler, "val$actions");
			for (ClickAction action : actions) {
				String value = action.getExecutable();
				switch (action.getType()) {
				case CONSOLE:
					consoleCommands.add(value);
					break;
				case MESSAGE:
					messages.add(value);
					break;
				case PLAYER:
					commands.add(value);
					break;
				case PLAY_SOUND:
					Optional<XSound> optional = XSound.matchXSound(value);
					if (optional.isPresent()) {
						configuration.set(path + "sound", optional.get().name());
						configuration.set(path + "pitch", 1);
						configuration.set(path + "volume", 1);
					}
					break;
				case TAKE_MONEY:
					consoleCommands.add("eco take %player% " + value);
					break;
				case GIVE_MONEY:
					consoleCommands.add("eco give %player% " + value);
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
		}

		if (messages.size() > 0) {
			configuration.set(path + "messages", changeColor(messages));
		}

		if (commands.size() > 0) {
			configuration.set(path + "playerCommands", commands);
		}

		if (consoleCommands.size() > 0) {
			configuration.set(path + "consoleCommands", consoleCommands);
		}

	}

	/**
	 * Permet de sauvegarder les actions du click
	 * 
	 * @param item
	 * @param configuration
	 * @param path
	 */
	private void saveClick(ClickHandler clickHandler, YamlConfiguration configuration, String path,
			ClickType clickType) {
		if (clickHandler != null) {

			List<String> messages = new ArrayList<>();
			List<String> commands = new ArrayList<>();
			List<String> consoleCommands = new ArrayList<>();

			try {
				List<ClickAction> actions = getField(clickHandler, "val$actions");
				for (ClickAction action : actions) {
					String value = action.getExecutable();
					switch (action.getType()) {
					case BROADCAST:
						break;
					case BROADCAST_JSON:
						break;
					case BROADCAST_SOUND:
						break;
					case BROADCAST_WORLD_SOUND:
						break;
					case CHAT:
						break;
					case CLOSE:
						configuration.set(path + "closeInventory", true);
						break;
					case CONNECT:
						break;
					case CONSOLE:
						consoleCommands.add(value);
						break;
					case GIVE_EXP:
						break;
					case GIVE_MONEY:
						consoleCommands.add("eco give %player% " + value);
						break;
					case GIVE_PERM:
						break;
					case JSON_BROADCAST:
						break;
					case JSON_MESSAGE:
						break;
					case MESSAGE:
						messages.add(value);
						break;
					case META:
						break;
					case OPEN_GUI_MENU:
					case OPEN_MENU:
						configuration.set(path + "type", "INVENTORY");
						configuration.set(path + "inventory", value);
						configuration.set(path + "plugin", "zMenu");
						break;
					case PLACEHOLDER:
						break;
					case PLAYER:
						commands.add(value);
						break;
					case PLAYER_COMMAND_EVENT:
						break;
					case PLAY_SOUND:
						Optional<XSound> optional = XSound.matchXSound(value);
						if (optional.isPresent()) {
							configuration.set(path + "sound", optional.get().name());
							configuration.set(path + "pitch", 1);
							configuration.set(path + "volume", 1);
						}
						break;
					case REFRESH:
						configuration.set(path + "refreshOnClick", true);
						break;
					case TAKE_EXP:
						break;
					case TAKE_MONEY:
						consoleCommands.add("eco take %player% " + value);
						break;
					case TAKE_PERM:
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (configuration.getString(path + "type").equalsIgnoreCase("INVENTORY")) {
				configuration.set(path + "closeInventory", null);
			}

			if (messages.size() > 0) {
				configuration.set(path + "messages", changeColor(messages));
			}

			if (commands.size() > 0) {
				configuration.set(path + "commands", commands);
				configuration.set(path + "type", "PERFORM_COMMAND");
			}

			if (consoleCommands.size() > 0) {
				configuration.set(path + "type", "PERFORM_COMMAND");
				if (clickType == ClickType.UNKNOWN) {
					configuration.set(path + "consoleCommands", consoleCommands);
				} else if (clickType == ClickType.RIGHT) {
					configuration.set(path + "consoleRightCommands", consoleCommands);
				} else if (clickType == ClickType.LEFT) {
					configuration.set(path + "consoleLeftCommands", consoleCommands);
				}
			}
		}
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

		if (item.isHead() && item.getHeadOwner() != null) {
			if (item.isBaseHead()) {

				configuration.set(path + "material", playerHead().getType().name());
				if (!NMSUtils.isNewVersion()) {
					configuration.set(path + "data", "3");
				}
				configuration.set(path + "url", item.getHeadOwner());

			} else if (item.isHeadDbHead()) {

				configuration.set(path + "material", item.getHeadOwner().replace("-", ":"));

			} else if (item.isTextureHead()) {

				String url = SkullUtils.getEncoded(item.getHeadOwner());
				configuration.set(path + "material", playerHead().getType().name());
				if (!NMSUtils.isNewVersion()) {
					configuration.set(path + "data", "3");
				}
				configuration.set(path + "url", url);

			} else {

				configuration.set(path + "playerHead", item.getHeadOwner());
				configuration.set(path + "material", playerHead().getType().name());
				if (!NMSUtils.isNewVersion()) {
					configuration.set(path + "data", "3");
				}

			}
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
			configuration.set(path + "name", changeColor(item.getDisplayName()));
		}

		if (item.getLore() != null) {
			configuration.set(path + "lore", changeColor(item.getLore()));
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
					boolean isReverse = this.getField(requirement, "invert");
					configuration.set(path + "permission", (isReverse ? "!" : "") + permission);
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
			} else if (requirement instanceof HasMoneyRequirement) {

				try {

					boolean invert = getField(requirement, "invert");
					String placeholder = getField(requirement, "placeholder");
					long amount = getField(requirement, "amount");

					if (placeholder == null) {
						placeholder = "%vault_eco_balance%";
					}

					PlaceholderAction action = invert ? PlaceholderAction.LOWER : PlaceholderAction.SUPERIOR_OR_EQUAL;
					configuration.set(path + "action", action.name());
					configuration.set(path + "placeHolder", placeholder);
					configuration.set(path + "value", amount);

				} catch (Exception e) {
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

	/**
	 * Transform {@link RequirementType} to {@link PlaceholderAction}
	 * 
	 * @param type
	 * @return {@link PlaceholderAction}
	 */
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

	/**
	 * Check if button already exist
	 * 
	 * @param item
	 * @param path
	 * @return boolean
	 */
	private boolean alreadyExist(MenuItem item, String path) {

		if (item.getMaterial() != null && item.getDisplayName() != null) {

			Button button = new Button(path, item.getMaterial(), item.getDisplayName(), item.getLore());

			Optional<Button> optional = this.buttons.stream().filter(e -> e.equals(button)).findFirst();

			if (optional.isPresent()) {

				Button existButton = optional.get();
				existButton.add(item.getSlot());

				return true;
			}

			button.add(item.getSlot());
			this.buttons.add(button);
		}

		return false;
	}

	/**
	 * Replace hex color &#<color> to #<color>
	 * 
	 * @param strings
	 * @return strings
	 */
	private List<String> changeColor(List<String> strings) {
		return strings.stream().map(this::changeColor).collect(Collectors.toList());
	}

	/**
	 * Replace hex color &#<color> to #<color>
	 * 
	 * @param string
	 * @return string
	 */
	private String changeColor(String string) {

		if (NMSUtils.isHexColor()) {
			Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
			Matcher matcher = pattern.matcher(string);
			while (matcher.find()) {
				String group = matcher.group();
				string = string.replace(group, group.substring(1, group.length()));
				matcher = pattern.matcher(string);
			}
		}

		return convertArgument(string);
	}

	/**
	 * Replace command argument to placeholder
	 * 
	 * @param string
	 * @return string
	 */
	private String convertArgument(String string) {

		Pattern pattern = Pattern.compile("[{]([^}]+)[}]");
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			String group = matcher.group(0);
			String argument = matcher.group(1);
			string = string.replace(group, String.format(this.placeholderArgumentFormat, argument));
			matcher = pattern.matcher(string);
		}

		return string;

	}

}
