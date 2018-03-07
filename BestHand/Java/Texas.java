package enhohu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Texas {

	private static final Map<Character, Integer> POINTS = new HashMap<Character, Integer>(13) {

		private static final long serialVersionUID = 1L;
		{
			put('2', 2);
			put('3', 3);
			put('4', 4);
			put('5', 5);
			put('6', 6);
			put('7', 7);
			put('8', 8);
			put('9', 9);
			put('T', 10);
			put('J', 11);
			put('Q', 12);
			put('K', 13);
			put('A', 14);
		}
	};

	public static void main(String[] args) {

		System.out.println(Arrays.asList(bestHand(new String[] { "♥K", "♥J" })));
		System.out.println(Arrays.asList(bestHand(new String[] { "♣4", "♦A", "♥3", "♣2", "♦5" })));
		System.out.println(Arrays.asList(bestHand(new String[] { "♥3", "♠6", "♥8", "♥K", "♦7", "♦8" })));
		System.out.println(Arrays.asList(bestHand(new String[] { "♠5", "♠6", "♠T", "♥J", "♣T", "♠8", "♠A" })));
		System.out.println(Arrays.asList(bestHand(new String[] { "♦7", "♥7", "♦Q", "♠A", "♥Q" })));
		System.out.println(Arrays.asList(bestHand(new String[] { "♠6", "♦6", "♦7", "♥8", "♣T", "♠9", "♠A" })));
		System.out.println(Arrays.asList(bestHand(new String[] { "♠5", "♠6", "♠7", "♠8", "♠9" })));
	}

	private static String[] bestHand(final String[] cards) {

		String[] ret;
		if (cards.length == 2) {

			ret = new String[] { "high", "", "" };
			final char c = cards[0].charAt(0), c1 = cards[0].charAt(1), c2 = cards[1].charAt(1);
			if (c == cards[1].charAt(0)) {

				ret[2] = "" + c;
			}
			if (c1 == c2) {

				ret[0] = "1pair";
			}
			ret[1] = POINTS.get(c1) > POINTS.get(c2) ? "" + c1 + c2 : "" + c2 + c1;
		} else {

			ret = aboutFlush(cards);
			if (ret == null) {

				ret = other(cards);
			}
		}

		return ret;
	}

	private static String[] aboutFlush(final String[] cards) {

		final Entry<Character, List<Character>> max = Arrays.asList(cards).stream()
				.collect(Collectors.groupingBy(e -> (Character) e.charAt(0),
						Collectors.mapping(e -> (Character) e.charAt(1), Collectors.toList())))
				.entrySet().stream().filter(e -> e.getValue().size() >= 5).findFirst().orElse(null);

		if (max != null) {

			final String[] ret = straight(
					new String[] { "flush", max.getValue().stream().sorted((e1, e2) -> POINTS.get(e2) - POINTS.get(e1))
							.reduce("", (r, e) -> r + e, (e1, e2) -> e1), "" + max.getKey() },
					false);
			if (!"flush".equals(ret[0])) {

				ret[0] += "-flush";
			}

			return ret;
		}

		return null;
	}

	private static String[] other(final String[] cards) {

		final Map<Character, Long> data = Arrays.asList(cards).stream()
				.collect(Collectors.groupingBy(e -> (Character) e.charAt(1), Collectors.counting()));
		final Map<Long, Long> times = data.values().stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		String[] ret = new String[] { "", "", "" };
		ret[1] = data.entrySet().stream().map(e -> new char[] { e.getKey(), (char) e.getValue().intValue() })
				.sorted((e1, e2) -> {

					if (e1[1] == e2[1]) {

						return POINTS.get(e2[0]) - POINTS.get(e1[0]);
					} else {

						return e2[1] - e1[1];
					}
				}).reduce("", (r, e) -> {

					if (e[1] == 1) {

						return r + e[0];
					} else {

						final String[] arr = new String[e[1]];
						Arrays.fill(arr, "" + e[0]);

						return r + String.join("", arr);
					}
				}, (e1, e2) -> e1);

		if (times.containsKey(4L)) {

			ret[0] = "4kind";
		} else if (times.containsKey(3L) && times.containsKey(2L)) {

			ret[0] = "4house";
		} else {

			ret = straight(ret, true);
			if ("".equals(ret[0])) {

				if (times.containsKey(3L)) {

					ret[0] = "3kind";
				} else if (times.containsKey(2L)) {

					ret[0] = times.get(2L) == 2L ? "2pair" : "1pair";
				} else {

					ret[0] = "high";
				}
			} else {

				ret[0] = "straight";
			}
		}

		return ret;
	}

	private static String[] straight(final String[] ret, final boolean notFlush) {

		if (ret[1].startsWith("AKQJT")) {

			ret[0] = "r";
			ret[1] = "AKQJT";
		} else if (ret[1].startsWith("A") && (notFlush && ret[1].contains("5") && ret[1].contains("4")
				&& ret[1].contains("3") && ret[1].contains("2") || !notFlush && ret[1].contains("5432"))) {

			ret[0] = "s";
			if (ret[1].contains("6")) {

				ret[1] = ret[1].contains("7") ? "76543" : "65432";
			} else {

				ret[1] = "5432A";
			}
		} else {

			String unit = ret[1].replaceAll("(.)\\1+", "$1");
			if (unit.length() > 4) {

				if (notFlush) {

					final String[] arr = unit.split("");
					Arrays.sort(arr, (e1, e2) -> POINTS.get(e2.charAt(0)) - POINTS.get(e1.charAt(0)));
					unit = String.join("", arr);
				}

				for (int i = 0; i < unit.length() - 4; i++) {

					if (POINTS.get(unit.charAt(i)) - POINTS.get(unit.charAt(i + 4)) == 4) {

						ret[0] = "s";
						ret[1] = unit.substring(i, i + 5);

						break;
					}
				}
			}

			if (ret[1].length() > 5) {

				ret[1] = ret[1].substring(0, 5);
			}
		}

		return ret;
	}
}