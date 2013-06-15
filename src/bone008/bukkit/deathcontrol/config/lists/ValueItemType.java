package bone008.bukkit.deathcontrol.config.lists;

import static org.bukkit.Material.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.Operator;
import bone008.bukkit.deathcontrol.exceptions.ConditionFormatException;

public class ValueItemType {

	private static enum Types {
		WEAPON(WOOD_SWORD, STONE_SWORD, IRON_SWORD, GOLD_SWORD, DIAMOND_SWORD, BOW),
		SWORD(WOOD_SWORD, STONE_SWORD, IRON_SWORD, GOLD_SWORD, DIAMOND_SWORD),

		PICKAXE(WOOD_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLD_PICKAXE, DIAMOND_PICKAXE),
		SHOVEL(WOOD_SPADE, STONE_SPADE, IRON_SPADE, GOLD_SPADE, DIAMOND_SPADE),
		AXE(WOOD_AXE, STONE_AXE, IRON_AXE, GOLD_AXE, DIAMOND_AXE),
		HOE(WOOD_HOE, STONE_HOE, IRON_HOE, GOLD_HOE, DIAMOND_HOE),

		TOOL(SHEARS, FLINT_AND_STEEL, BUCKET, COMPASS, MAP, WATCH, FISHING_ROD, CARROT_STICK, WOOD_SWORD, STONE_SWORD, IRON_SWORD, GOLD_SWORD, DIAMOND_SWORD, WOOD_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLD_PICKAXE, DIAMOND_PICKAXE, WOOD_SPADE, STONE_SPADE, IRON_SPADE, GOLD_SPADE, DIAMOND_SPADE, WOOD_AXE, STONE_AXE, IRON_AXE, GOLD_AXE, DIAMOND_AXE, WOOD_HOE, STONE_HOE, IRON_HOE, GOLD_HOE, DIAMOND_HOE),


		HELMET(LEATHER_HELMET, IRON_HELMET, DIAMOND_HELMET, CHAINMAIL_HELMET, GOLD_HELMET),
		CHESTPLATE(LEATHER_CHESTPLATE, IRON_CHESTPLATE, DIAMOND_CHESTPLATE, CHAINMAIL_CHESTPLATE, GOLD_CHESTPLATE),
		PANTS(LEATHER_LEGGINGS, IRON_LEGGINGS, DIAMOND_LEGGINGS, CHAINMAIL_LEGGINGS, GOLD_LEGGINGS),
		BOOTS(LEATHER_BOOTS, IRON_BOOTS, DIAMOND_BOOTS, CHAINMAIL_BOOTS, GOLD_BOOTS),

		ARMOR(LEATHER_HELMET, IRON_HELMET, DIAMOND_HELMET, CHAINMAIL_HELMET, GOLD_HELMET, LEATHER_CHESTPLATE, IRON_CHESTPLATE, DIAMOND_CHESTPLATE, CHAINMAIL_CHESTPLATE, GOLD_CHESTPLATE, LEATHER_LEGGINGS, IRON_LEGGINGS, DIAMOND_LEGGINGS, CHAINMAIL_LEGGINGS, GOLD_LEGGINGS, LEATHER_BOOTS, IRON_BOOTS, DIAMOND_BOOTS, CHAINMAIL_BOOTS, GOLD_BOOTS);


		public final Set<Material> materials;

		private Types(Material... mats) {
			// great job, EnumSet builder methods, why would you think of taking in a varargs array ...
			materials = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(mats)));
		}

		public static Types parse(String input) {
			try {
				return valueOf(input.toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	public static ValueItemType parseValue(String rawValue) throws ConditionFormatException {
		String humanName;
		Set<Material> materials;

		Types parsedType = Types.parse(rawValue);
		if (parsedType == null) {
			Material mat = Material.matchMaterial(rawValue);
			if (mat == null)
				throw new ConditionFormatException("could not find material or type named '" + rawValue + "'");
			humanName = mat.toString().toLowerCase();
			materials = EnumSet.of(mat);
		}
		else {
			humanName = parsedType.toString().toLowerCase();
			materials = parsedType.materials;
		}

		return new ValueItemType(humanName, materials);
	}

	private final String humanName;
	private final Set<Material> materials;

	public ValueItemType(String humanName, Set<Material> materials) {
		this.humanName = humanName;
		this.materials = materials;
	}

	public ValueItemType validateOperator(Operator operator) throws ConditionFormatException {
		if (operator != Operator.EQUAL && operator != Operator.UNEQUAL)
			throw new ConditionFormatException("incompatible type for operator '" + operator.getPrimaryIdentifier() + "': item type");

		return this;
	}

	public boolean invokeOperator(ItemStack itemStack, Operator operator) {
		switch (operator) {
		case EQUAL:
			return materials.contains(itemStack.getType());
		case UNEQUAL:
			return !materials.contains(itemStack.getType());
		default:
			throw new IllegalArgumentException("invalid operator for item type condition: " + operator.getPrimaryIdentifier());
		}
	}

	@Override
	public String toString() {
		return humanName;
	}
}
