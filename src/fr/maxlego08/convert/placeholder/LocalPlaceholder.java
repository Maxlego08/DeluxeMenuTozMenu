package fr.maxlego08.convert.placeholder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.maxlego08.convert.ConvertPlugin;

public class LocalPlaceholder {
	
	private ConvertPlugin plugin;
	private final String prefix = "template";
	private final Pattern pattern = Pattern.compile("[%]([^%]+)[%]");

	/**
	 * Set plugin instance
	 * 
	 * @param plugin
	 */
	public void setPlugin(ConvertPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * static Singleton instance.
	 */
	private static volatile LocalPlaceholder instance;

	/**
	 * Private constructor for singleton.
	 */
	private LocalPlaceholder() {
	}

	/**
	 * Return a singleton instance of ZPlaceholderApi.
	 */
	public static LocalPlaceholder getInstance() {
		// Double lock for thread safety.
		if (instance == null) {
			synchronized (LocalPlaceholder.class) {
				if (instance == null) {
					instance = new LocalPlaceholder();
				}
			}
		}
		return instance;
	}

	/**
	 * 
	 * @param player
	 * @param displayName
	 * @return
	 */
	public String setPlaceholders(Player player, String placeholder) {

		if (placeholder == null || !placeholder.contains("%")) {
			return placeholder;
		}

		final String realPrefix = this.prefix + "_";

		Matcher matcher = this.pattern.matcher(placeholder);
		while (matcher.find()) {
			String stringPlaceholder = matcher.group(0);
			String regex = matcher.group(1).replace(realPrefix, "");
			String replace = this.onRequest(player, regex);
			if (replace != null) {
				placeholder = placeholder.replace(stringPlaceholder, replace);
			}
		}
		
		return placeholder;
	}

	/**
	 * 
	 * @param player
	 * @param lore
	 * @return
	 */
	public List<String> setPlaceholders(Player player, List<String> lore) {
		return lore == null ? null
				: lore.stream().map(e -> e = setPlaceholders(player, e)).collect(Collectors.toList());
	}

	/**
	 * Custom placeholder
	 * 
	 * @param player
	 * @param string
	 * @return
	 */
	public String onRequest(Player player, String string) {
		return null;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public ConvertPlugin getPlugin() {
		return plugin;
	}
	
}
