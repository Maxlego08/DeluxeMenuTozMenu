package fr.maxlego08.convert;

import java.util.ArrayList;
import java.util.List;

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
