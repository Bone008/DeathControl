package bone008.bukkit.deathcontrol.config.lists;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import bone008.bukkit.deathcontrol.Operator;
import bone008.bukkit.deathcontrol.exceptions.ConditionFormatException;
import bone008.bukkit.deathcontrol.util.Util;

public class SpecialListItem extends ListItem {

	private static enum ItemProperty {
		ID(Integer.class),
		DATA(Integer.class),
		AMOUNT(Integer.class),
		ENCHANTMENT(ValueEnchantment.class),
		ENCHANTMENT_COUNT(Integer.class),
		TYPE(ValueItemType.class),
		NAME(String.class),
		LORE(String.class);

		public final Class<?> valueType;

		private ItemProperty(Class<?> valueType) {
			this.valueType = valueType;
		}

		public String toHumanString() {
			return name().replace('_', '-').toLowerCase();
		}

		public static ItemProperty parse(String input) {
			try {
				return valueOf(input.replace('-', '_').toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	private static class Condition {

		private final ItemProperty prop;
		private final Operator operator;
		private final Object value;

		public Condition(ItemProperty prop, Operator operator, Object value) {
			this.prop = prop;
			this.operator = operator;
			this.value = value;

			if (!prop.valueType.isInstance(value))
				throw new IllegalArgumentException("cannot mix property " + prop + " with value " + value.getClass().getSimpleName() + ", expected " + prop.valueType.getSimpleName());
		}

		public boolean matches(ItemStack itemStack) {
			switch (prop) {
			case ID:
				return operator.invokeInt(itemStack.getTypeId(), ((Integer) value).intValue());
			case DATA:
				return operator.invokeInt(itemStack.getDurability(), ((Integer) value).intValue());
			case AMOUNT:
				return operator.invokeInt(itemStack.getAmount(), ((Integer) value).intValue());
			case ENCHANTMENT_COUNT:
				return operator.invokeInt(itemStack.getEnchantments().size(), ((Integer) value).intValue());
			case ENCHANTMENT:
				return ((ValueEnchantment) value).invokeOperator(itemStack, operator);
			case TYPE:
				return ((ValueItemType) value).invokeOperator(itemStack, operator);
			case NAME: {
				ItemMeta meta = itemStack.getItemMeta();
				String cmpStr = "";
				if (meta != null && meta.hasDisplayName())
					cmpStr = meta.getDisplayName();

				return operator.invokeString(cmpStr, (String) value);
			}
			case LORE: {
				ItemMeta meta = itemStack.getItemMeta();
				String cmpStr = "";
				if (meta != null && meta.hasLore())
					cmpStr = Util.joinCollection("\n", meta.getLore());

				return operator.invokeString(cmpStr, (String) value);
			}
			default:
				throw new IllegalStateException();
			}
		}
	}

	// Note: whitespaces are included in groups 1 and 3!
	private static final Pattern REGEX_CONDITION = Pattern.compile("([^<=>!]+)([<=>!]+)([^<=>!]+)");

	private List<Condition> conditions = new LinkedList<Condition>();

	public SpecialListItem() {
	}

	public void parseCondition(String rawCondition) throws ConditionFormatException {
		Matcher matcher = REGEX_CONDITION.matcher(rawCondition);

		if (!matcher.matches())
			throw new ConditionFormatException("illegal format: " + rawCondition);

		String rawProperty = matcher.group(1).trim();
		String rawOperator = matcher.group(2);
		String rawValue = matcher.group(3).trim();

		ItemProperty property = ItemProperty.parse(rawProperty);
		if (property == null)
			throw new ConditionFormatException("illegal property: " + rawProperty);

		Operator operator = Operator.parse(rawOperator);
		if (operator == null)
			throw new ConditionFormatException("illegal operator: " + rawOperator);


		Object value = null;

		if (property.valueType == Integer.class)
			try {
				value = Integer.parseInt(rawValue);
			} catch (NumberFormatException e) {
				throw new ConditionFormatException("property value not a number: " + rawValue);
			}

		else if (property.valueType == String.class)
			value = (rawValue.equalsIgnoreCase("none") ? "" : rawValue.replace("\\n", "\n"));

		else if (property.valueType == ValueItemType.class) {
			value = ValueItemType.parseValue(rawValue).validateOperator(operator);

		}

		else if (property.valueType == ValueEnchantment.class) {
			value = ValueEnchantment.parseValue(rawValue).validateOperator(operator);
		}

		else
			throw new Error("ItemProperty without a value type detected; not supposed to happen");

		conditions.add(new Condition(property, operator, value));
	}

	@Override
	public boolean matches(ItemStack itemStack) {
		for (Condition cond : conditions) {
			if (!cond.matches(itemStack))
				return false;
		}

		return true;
	}

	@Override
	public CharSequence toHumanString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\n').append(ChatColor.ITALIC).append('{');

		boolean first = true;
		for (Iterator<Condition> it = conditions.iterator(); it.hasNext(); first = false) {
			if (!first)
				sb.append(", ");

			Condition cond = it.next();
			sb.append(cond.prop.toHumanString()).append(cond.operator.getPrimaryIdentifier()).append(cond.value);
		}

		return sb.append('}');
	}

	public static int compare(SpecialListItem o1, SpecialListItem o2) {
		// we can't really specify a unique order here
		return o1.conditions.size() - o2.conditions.size();
	}
}
