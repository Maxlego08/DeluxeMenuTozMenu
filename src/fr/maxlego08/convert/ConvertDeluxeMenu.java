package fr.maxlego08.convert;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.extendedclip.deluxemenus.menu.Menu;

import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.api.InventoryManager;
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

	/*Menu.getAllMenus().forEach(menu -> {

	menu.getMenuItems().forEach((i, b) -> {

		System.out.println(i);
		System.out.println(b);
		b.forEach((c, d) -> {
			System.out.println(c);
			System.out.println(d);
		});

	});

});*/
	
	public void convert(CommandSender sender) {

		if (this.isRunning) {
			message(sender, "§7Conversion in progress, please wait.");
			return;
		}
		
		this.isRunning = true;

		InventoryManager inventoryManager = this.plugin.getInventoryManager();

		Collection<Menu> menus = Menu.getAllMenus();
		message(sender, "§7Start of the conversion of §f" + menus.size() + " menu(s)§7.");
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

			message(sender, "§aConversion complete. §7Please check that your files have been converted correctly.");
			this.isRunning = false;
		});

	}

}
