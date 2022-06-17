package fr.maxlego08.convert;

import fr.maxlego08.convert.command.CommandManager;
import fr.maxlego08.convert.inventory.ZInventoryManager;
import fr.maxlego08.convert.listener.AdapterListener;
import fr.maxlego08.convert.save.Config;
import fr.maxlego08.convert.save.MessageLoader;
import fr.maxlego08.convert.zcore.ZPlugin;

/**
 * System to create your plugins very simply Projet:
 * https://github.com/Maxlego08/TemplatePlugin
 * 
 * @author Maxlego08
 *
 */
public class ConvertPlugin extends ZPlugin {

	@Override
	public void onEnable() {

		this.preEnable();

		this.commandManager = new CommandManager(this);
		this.inventoryManager = new ZInventoryManager(this);

		/* Add Listener */

		this.addListener(new AdapterListener(this));
		this.addListener(inventoryManager);

		/* Add Saver */
		this.addSave(Config.getInstance());
		this.addSave(new MessageLoader(this));
		// addSave(new CooldownBuilder());

		this.getSavers().forEach(saver -> saver.load(this.getPersist()));
		
		this.postEnable();
	}

	@Override
	public void onDisable() {

		this.preDisable();

		this.getSavers().forEach(saver -> saver.save(this.getPersist()));

		this.postDisable();

	}

}
