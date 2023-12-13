package fr.maxlego08.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.kinglol12345.GUIPlus.gui.GUI;
import de.kinglol12345.GUIPlus.gui.actions.Action;
import de.kinglol12345.GUIPlus.gui.item.GItem;
import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.zcore.logger.Logger;
import fr.maxlego08.menu.zcore.logger.Logger.LogType;
import fr.maxlego08.menu.zcore.utils.ZUtils;
import fr.maxlego08.menu.zcore.utils.loader.ItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import fr.maxlego08.menu.zcore.utils.xseries.XSound;

public class ConvertGuiPlus extends ZUtils {

	private final MenuPlugin plugin;
	private boolean isRunning = false;
	private final List<Button> buttons = new ArrayList<>();

	/**
	 * @param plugin
	 * @param isRunning
	 */
	public ConvertGuiPlus(MenuPlugin plugin) {
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

		Map<String, GUI> guis = GUI.getLoadedGUIS();

		message(sender, "§7Start of the conversion of §f" + guis.size() + " menu(s)§7.");
		Logger.info("§7Start of the conversion of §f" + guis.size() + " menu(s)§7.");

		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

			for (Entry<String, GUI> entry : guis.entrySet()) {

				GUI gui = entry.getValue();
				String fileName = entry.getKey();

				File file = new File(folderInventories, fileName + ".yml");
				if (file.exists()) {
					Logger.info("inventories/convert/" + fileName + ".yml already exist, skip", LogType.ERROR);
					continue;
				}

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

				YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

				if (gui.getTitle() != null) {
					configuration.set("name", colorReverse(gui.getTitle()));
				}

				savePermissionOpen(gui, configuration, file);

				configuration.set("size", gui.getSize());
				configuration.set("items", "[]");

				try {
					this.loadItems(gui, configuration, file);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					configuration.save(file);
					Logger.info("Saved file: " + file.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			message(sender, "§aConversion complete. §7Please check that your files have been converted correctly.");
			message(sender, "§7Dont forget to run §f/zmenu reload§7.");

			Logger.info("§aConversion complete. §7Please check that your files have been converted correctly.",
					LogType.SUCCESS);
			Logger.info("§7Dont forget to run §f/zmenu reload§7.", LogType.SUCCESS);
			this.isRunning = false;
		});
	}

	private void savePermissionOpen(GUI gui, YamlConfiguration configuration, File file) {

		if (gui.getPermission() != null) {

			List<Map<String, Object>> requirements = new ArrayList<>();
			List<Map<String, Object>> denyActions = new ArrayList<>();

			Map<String, Object> map = new HashMap<>();
			map.put("permission", gui.getPermission());
			map.put("type", "permission");
			requirements.add(map);

			configuration.set("open_requirement.requirements", requirements);

			Map<String, Object> mapDenyAction = new HashMap<>();
			mapDenyAction.put("type", "message");
			mapDenyAction.put("messages", Arrays.asList("&cYou have no permission to open this inventory."));
			denyActions.add(mapDenyAction);

			configuration.set("open_requirement.deny", denyActions);
		}

	}

	public void loadItems(GUI gui, YamlConfiguration configuration, File file) {

		for (Entry<Integer, GItem> entry : gui.getItems().entrySet()) {

			int slot = entry.getKey();
			GItem gItem = entry.getValue();

			ItemStack itemStack = gItem.getItemStack();
			Loader<ItemStack> loader = new ItemStackLoader();

			String path = "items." + slot + ".";

			if (!alreadyExist(itemStack, path, slot)) {

				// configuration.set(path + "type", "NONE");
				configuration.set(path + "slot", slot);
				loader.save(itemStack, configuration, path + "item.", file);

				this.saveButton(gItem, configuration, path, itemStack);
			}

		}

		for (Button button : this.buttons) {

			String path = button.getPath();
			if (button.getSlots().size() > 1) {
				configuration.set(path + "slot", null);
				// configuration.set(path + "type", "NONE_SLOT");
				configuration.set(path + "slots", button.toRange());
			}
		}
	}

	private void saveButton(GItem gItem, YamlConfiguration configuration, String path, ItemStack itemStack) {

		if (gItem.getShowPermission() != null) {
			configuration.set(path + "permission", gItem.getShowPermission());
		}

		saveAction(gItem, configuration, path, itemStack, ClickType.LEFT, gItem.getOnLeftClick());
		saveAction(gItem, configuration, path, itemStack, ClickType.SHIFT_LEFT, gItem.getOnShiftLeftClick());
		saveAction(gItem, configuration, path, itemStack, ClickType.RIGHT, gItem.getOnRightClick());
		saveAction(gItem, configuration, path, itemStack, ClickType.SHIFT_RIGHT, gItem.getOnShiftRightClick());
		saveAction(gItem, configuration, path, itemStack, ClickType.MIDDLE, gItem.getOnMiddleClick());

	}

	private void saveAction(GItem gItem, YamlConfiguration configuration, String path, ItemStack itemStack,
			ClickType clickType, Action action) {

		List<Map<String, Object>> actions = toAction(action.getCommands());
		if (actions.isEmpty()) {
			return;
		}

		String currentPath = path + "click_requirement." + clickType.name().toLowerCase() + ".";
		configuration.set(currentPath + "clicks", Arrays.asList(clickType).stream().map(ClickType::name).collect(Collectors.toList()));
		configuration.set(currentPath + "success", actions);
		
		
	}

	private List<Map<String, Object>> toAction(List<String> guiCommands) {
		List<Map<String, Object>> maps = new ArrayList<>();

		List<String> commands = new ArrayList<>();
		List<String> messages = new ArrayList<>();
		List<String> consoleCommands = new ArrayList<>();

		guiCommands.forEach(command -> {

			command = command.replace("<player>", "%player%");

			if (command.startsWith("<server>")) {

				command = command.replace("<server>", "");
				consoleCommands.add(command);
				
			} else if (command.startsWith("<op>")) {
				
				command = command.replace("<op>", "");
				consoleCommands.add(command);

			} else if (command.startsWith("<msg>")) {

				command = command.replace("<msg>", "");
				messages.add(command);

			} else if (command.startsWith("<connect>")) {

				String serverName = command.replace("<connect>", "");
				Map<String, Object> map = new HashMap<>();
				map.put("type", "connect");
				map.put("server", serverName);
				maps.add(map);

			} else if (command.startsWith("<sound>")) {

				String soundName = command.replace("<sound>", "");
				XSound.matchXSound(soundName).ifPresent(sound -> {
					Map<String, Object> map = new HashMap<>();
					map.put("type", "sound");
					map.put("sound", sound.name());
					map.put("volume", 1f);
					map.put("pitch", 1f);
					maps.add(map);
				});

			} else if (command.startsWith("<back>")) {
				
				Map<String, Object> map = new HashMap<>();
				map.put("type", "back");
				maps.add(map);
				
			} else {

				if (command.startsWith("<")) {
					Logger.info("Command " + command + " not supported for the conversion", LogType.WARNING);
				} else {
					commands.add(command);
				}

			}
		});

		if (messages.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "message");
			map.put("messages", messages);
			maps.add(map);
		}

		if (commands.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "player_command");
			map.put("commands", commands);
			maps.add(map);
		}

		if (consoleCommands.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "console_command");
			map.put("commands", consoleCommands);
			maps.add(map);
		}

		return maps;
	}

	/**
	 * Check if button already exist
	 * 
	 * @param item
	 * @param path
	 * @return boolean
	 */
	private boolean alreadyExist(ItemStack itemStack, String path, int slot) {

		ItemMeta itemMeta = itemStack.getItemMeta();
		Button button = new Button(path, itemStack.getType(), itemMeta.getDisplayName(), itemMeta.getLore());

		Optional<Button> optional = this.buttons.stream().filter(e -> e.equals(button)).findFirst();

		if (optional.isPresent()) {

			Button existButton = optional.get();
			existButton.add(slot);

			return true;
		}

		button.add(slot);
		this.buttons.add(button);

		return false;
	}

}
