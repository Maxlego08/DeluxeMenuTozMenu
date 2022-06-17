package fr.maxlego08.convert.commands;

import fr.maxlego08.convert.ConvertDeluxeMenu;
import fr.maxlego08.menu.MenuPlugin;
import fr.maxlego08.menu.command.VCommand;
import fr.maxlego08.menu.zcore.enums.Message;
import fr.maxlego08.menu.zcore.enums.Permission;
import fr.maxlego08.menu.zcore.utils.commands.CommandType;

public class CommandConvert extends VCommand {

	private final ConvertDeluxeMenu convertDeluxeMenu;

	public CommandConvert(MenuPlugin plugin, ConvertDeluxeMenu convertDeluxeMenu) {
		super(plugin);
		this.convertDeluxeMenu = convertDeluxeMenu;
		this.setPermission(Permission.ZMENU_CONVERT);
		this.addSubCommand("convert");
		this.setDescription(Message.DESCRIPTION_CONVERT);
	}

	@Override
	protected CommandType perform(MenuPlugin plugin) {

		this.convertDeluxeMenu.convert(this.sender);

		return CommandType.SUCCESS;
	}

}
