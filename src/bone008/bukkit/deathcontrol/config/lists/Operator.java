package bone008.bukkit.deathcontrol.config.lists;

import java.util.Arrays;
import java.util.List;

enum Operator {
	LESS("<"),
	LEQUAL("<="),
	GEQUAL(">="),
	GREATER(">"),
	EQUAL("=", "=="),
	UNEQUAL("!=", "<>");

	private final List<String> identifiers;

	private Operator(String... identifiers) {
		this.identifiers = Arrays.asList(identifiers);
	}

	public boolean invokeInt(int left, int right) {
		switch (this) {
		case LESS:
			return left < right;
		case LEQUAL:
			return left <= right;
		case GEQUAL:
			return left >= right;
		case GREATER:
			return left > right;
		case EQUAL:
			return left == right;
		case UNEQUAL:
			return left != right;
		default:
			throw new Error();
		}
	}

	public boolean invokeString(String left, String right) {
		switch (this) {
		case LESS:
			// "bcd" < "abcde"
			// but not "bcd" < "bcd"
			return right.contains(left) && !left.equals(right);
		case LEQUAL:
			return right.contains(left);
		case GEQUAL:
			return left.contains(right);
		case GREATER:
			// "abcde" > "bcd"
			// but not "bcd" > "bcd"
			return left.contains(right) && !left.equals(right);
		case EQUAL:
			return left.equals(right);
		case UNEQUAL:
			return !left.equals(right);
		default:
			throw new Error();
		}
	}

	public String getPrimaryIdentifier() {
		return identifiers.get(0);
	}

	public static Operator parse(String input) {
		for (Operator o : values()) {
			if (o.identifiers.contains(input))
				return o;
		}

		return null;
	}
}
