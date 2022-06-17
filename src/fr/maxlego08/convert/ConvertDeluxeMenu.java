package fr.maxlego08.convert;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;

import com.extendedclip.deluxemenus.menu.Menu;

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

				this.loadItems(menu, configuration);

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

	}

}
