package bone008.bukkit.deathcontrol.config.lists;

import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import bone008.bukkit.deathcontrol.exceptions.ConditionFormatException;
import bone008.bukkit.deathcontrol.util.Utilities;

public class ValueEnchantment {

	public static ValueEnchantment parseValue(String rawValue) throws ConditionFormatException {
		List<String> enchTokens = Utilities.tokenize(rawValue, "\\.", false);

		if (enchTokens.size() > 2)
			throw new ConditionFormatException("invalid enchantment format: " + rawValue);

		Enchantment enchantment = enchantmentByName(enchTokens.get(0));
		if (enchantment == null)
			throw new ConditionFormatException("unknown enchantment: " + enchTokens.get(0));

		int level = -1;
		if (enchTokens.size() == 2) {
			try {
				level = Integer.parseInt(enchTokens.get(1));
			} catch (NumberFormatException e) {
				throw new ConditionFormatException("enchantment level must be a number: " + enchTokens.get(1));
			}
		}

		return new ValueEnchantment(enchantment, level);
	}

	private static Enchantment enchantmentByName(String name) {
		// Reference: http://jd.bukkit.org/rb/apidocs/org/bukkit/enchantments/Enchantment.html#field_detail
		return Enchantment.getByName(name.toUpperCase().replace('-', '_'));
	}


	private final Enchantment enchantment;
	private final int valueLevel;

	public ValueEnchantment(Enchantment enchantment, int level) {
		this.enchantment = enchantment;
		this.valueLevel = level;
	}

	public ValueEnchantment validateOperator(Operator operator) throws ConditionFormatException {
		if (valueLevel < 1 && operator != Operator.EQUAL && operator != Operator.UNEQUAL)
			throw new ConditionFormatException("incompatible type for operator '" + operator.getPrimaryIdentifier() + "': unleveled enchantment");

		return this;
	}

	public boolean invokeOperator(ItemStack itemStack, Operator operator) {
		// currLevel is zero if not present
		int currLevel = itemStack.getEnchantmentLevel(enchantment);

		if (valueLevel > 0) { // value checks for enchantment level
			return operator.invokeInt(currLevel, valueLevel);
		}
		else {
			// return if "item has enchantment" equal to "operator looks for present enchantment"
			return (currLevel > 0) == (operator == Operator.EQUAL);
		}
	}

	@Override
	public String toString() {
		return enchantment.getName() + (valueLevel > 0 ? "." + valueLevel : "");
	}

}
