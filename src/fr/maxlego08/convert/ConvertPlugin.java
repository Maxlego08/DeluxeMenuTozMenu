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

	@Override
	public void onEnable() {

		this.menuPlugin = (MenuPlugin) Bukkit.getPluginManager().getPlugin("zMenu");

		CommandMenu commandMenu = this.menuPlugin.getCommandMenu();
		commandMenu.addSubCommand(new CommandConvert(this.menuPlugin));

	}

	@Override
	public void onDisable() {

	}

}
