package fr.maxlego08.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Material;

public class Button {

	private final String path;
	private final Material material;
	private final String displayName;
	private final List<String> lore;
	private final List<Integer> slots = new ArrayList<Integer>();

	/**
	 * @param path
	 * @param material
	 * @param displayName
	 * @param lore
	 */
	public Button(String path, Material material, String displayName, List<String> lore) {
		super();
		this.path = path;
		this.material = material;
		this.displayName = displayName;
		this.lore = lore == null ? new ArrayList<>() : lore;
	}

	/**
	 * @return the lore
	 */
	public List<String> getLore() {
		return lore;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the material
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the slots
	 */
	public List<Integer> getSlots() {
		return slots;
	}

	public void add(int slot) {
		this.slots.add(slot);
	}

	public List<?> toRange() {
		List<String> strings = new ArrayList<>();

		if (this.slots.size() <= 4) {
			return this.slots.stream().sorted().collect(Collectors.toList());
		}

		int start;
		int oldValue = start = this.slots.get(0);
		for (int index = 0; index != this.slots.size(); index++) {

			int slot = this.slots.get(index);
			int diff = slot - oldValue;
			if (diff >= 2) {
				strings.add(start + "-" + oldValue);
				start = slot;
			}
			oldValue = slot;

			if (index == this.slots.size() - 1) {
				strings.add(Math.min(start, oldValue) + "-" + Math.max(start, oldValue));
			}
		}

		// To be sure that there are no worries we will do a reverse and we will
		// see if all the slots are present, if not then we will put a classic
		// list.
		List<Integer> slots = new ArrayList<>();
		if (strings.size() > 0) {
			for (String line : strings) {
				if (line.contains("-")) {
					try {
						String[] values = line.split("-");
						int from = Integer.valueOf(values[0]);
						int to = Integer.valueOf(values[1]) + 1;
						slots.addAll(IntStream.range(Math.min(from, to), Math.max(from, to)).boxed()
								.collect(Collectors.toList()));
					} catch (Exception ignored) {
						ignored.printStackTrace();
					}
				}

			}
		}

		if (!slots.equals(this.slots)) {
			return this.slots.stream().sorted().collect(Collectors.toList());
		}

		return strings;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((lore == null) ? 0 : lore.hashCode());
		result = prime * result + ((material == null) ? 0 : material.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Button other = (Button) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (lore == null) {
			if (other.lore != null)
				return false;
		} else if (!lore.equals(other.lore))
			return false;
		if (material != other.material)
			return false;
		return true;
	}

	

}
