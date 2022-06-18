package fr.maxlego08.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;

public class Button {

	private final String path;
	private final Material material;
	private final String displayName;
	private final List<Integer> slots = new ArrayList<Integer>();

	/**
	 * @param path
	 * @param material
	 * @param displayName
	 */
	public Button(String path, Material material, String displayName) {
		super();
		this.path = path;
		this.material = material;
		this.displayName = displayName;
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

	public List<String> toRange() {
		List<String> strings = new ArrayList<>();

		if (this.slots.size() <= 4) {
			return this.slots.stream().map(String::valueOf).collect(Collectors.toList());
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
				strings.add(start + "-" + oldValue);
			}
		}

		return strings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((material == null) ? 0 : material.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		if (material != other.material)
			return false;
		return true;
	}

}
