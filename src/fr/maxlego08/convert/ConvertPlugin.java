package fr.maxlego08.convert;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import fr.maxlego08.convert.commands.CommandConvert;
import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.command.commands.CommandMenu;

/**
 * System to create your plugins very simply Projet:
 * https://github.com/Maxlego08/TemplatePlugin
 * 
 * @author Maxlego08
 *
 */
public class ConvertPlugin extends JavaPlugin {

	private MenuPlugin menuPlugin;
	private ConvertDeluxeMenu convert;;

	@Override
	public void onEnable() {

		this.menuPlugin = (MenuPlugin) Bukkit.getPluginManager().getPlugin("zMenu");
		this.convert = new ConvertDeluxeMenu(this.menuPlugin);

		CommandMenu commandMenu = this.menuPlugin.getCommandMenu();
		commandMenu.addSubCommand(new CommandConvert(this.menuPlugin, this.convert));

	}

	@Override
	public void onDisable() {

	}

}
