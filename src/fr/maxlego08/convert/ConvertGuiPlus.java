package fr.maxlego08.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.kinglol12345.GUIPlus.gui.GUI;
import de.kinglol12345.GUIPlus.gui.item.GItem;
import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.zcore.logger.Logger;
import fr.maxlego08.menu.zcore.logger.Logger.LogType;
import fr.maxlego08.menu.zcore.utils.ZUtils;
import fr.maxlego08.menu.zcore.utils.loader.ItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;

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
			
			Logger.info("§aConversion complete. §7Please check that your files have been converted correctly.", LogType.SUCCESS);
			Logger.info("§7Dont forget to run §f/zmenu reload§7.", LogType.SUCCESS);
			this.isRunning = false;
		});
	}

	public void loadItems(GUI gui, YamlConfiguration configuration, File file) {

		for (Entry<Integer, GItem> entry : gui.getItems().entrySet()) {

			int slot = entry.getKey();
			GItem gItem = entry.getValue();

			ItemStack itemStack = gItem.getItemStack();
			Loader<ItemStack> loader = new ItemStackLoader();

			String path = "items." + slot + ".";

			if (!alreadyExist(itemStack, path, slot)) {

				configuration.set(path + "type", "NONE");
				configuration.set(path + "slot", slot);
				loader.save(itemStack, configuration, path + "item.", file);

				this.saveButton(gItem, configuration, path, itemStack);
			}

		}

		for (Button button : this.buttons) {

			String path = button.getPath();
			if (button.getSlots().size() > 1) {
				configuration.set(path + "slot", null);
				configuration.set(path + "type", "NONE_SLOT");
				configuration.set(path + "slots", button.toRange());
			}
		}
	}

	private void saveButton(GItem gItem, YamlConfiguration configuration, String path, ItemStack itemStack) {
		System.out.println("TO DO");
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
