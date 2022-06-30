package fr.maxlego08.convert.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;

import fr.maxlego08.convert.ConvertDeluxeMenu;
import fr.maxlego08.convert.ConvertGuiPlus;
import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.command.VCommand;
import fr.maxlego08.menu.zcore.enums.Message;
import fr.maxlego08.menu.zcore.enums.Permission;
import fr.maxlego08.menu.zcore.utils.commands.CommandType;

public class CommandConvert extends VCommand {

	public CommandConvert(MenuPlugin plugin) {
		super(plugin);
		this.setPermission(Permission.ZMENU_CONVERT);
		this.addSubCommand("convert");
		this.addRequireArg("plugin", (a, b) -> Arrays.asList("deluxemenu", "guiplus"));
		this.setDescription(Message.DESCRIPTION_CONVERT);
	}

	@Override
	protected CommandType perform(MenuPlugin plugin) {

		MenuPlugin menuPlugin = (MenuPlugin) Bukkit.getPluginManager().getPlugin("zMenu");
		String type = this.argAsString(0);
		if (type.equalsIgnoreCase("deluxemenu")) {

			if (!Bukkit.getPluginManager().isPluginEnabled("DeluxeMenus")){
				message(this.sender, "§The DeluxeMenu plugin is not active on your server.");
				return CommandType.CONTINUE;
			}
			
			ConvertDeluxeMenu convertDeluxeMenu = new ConvertDeluxeMenu(menuPlugin);
			convertDeluxeMenu.convert(this.sender);
			
		} else if (type.equalsIgnoreCase("guiplus")) {

			if (!Bukkit.getPluginManager().isPluginEnabled("GUIPlus")){
				message(this.sender, "§The GUIPlus plugin is not active on your server.");
				return CommandType.CONTINUE;
			}
			
			ConvertGuiPlus convertGuiPlus = new ConvertGuiPlus(menuPlugin);
			convertGuiPlus.convert(this.sender);
			
		} else {
			return CommandType.SYNTAX_ERROR;
		}

		return CommandType.SUCCESS;
	}

}
