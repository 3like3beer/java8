package ch.gma.contrat.entretien;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestStream {
	@Test
	public void fact() {
		System.out.println(IntStream.range(1, 6).reduce((a, b) -> a * b).getAsInt());
	}

	@Test
	public void listPrimes() {
		IntStream.range(2, 100).filter(x -> !IntStream.range(2, x).filter(a -> x % a == 0).findAny().isPresent())
				.forEach(System.out::println);
//		IntStream.range(1, 6).filter(a -> a % 2 == 0).boxed().findAny();
	}

	@Test
	public void mapDivisor() {
		IntStream.range(2, 100)
				.mapToObj(x -> IntStream.range(2, x).filter(a -> x % a == 0).boxed().collect(Collectors.toList()))
				.forEach(System.out::println);
//		IntStream.range(1, 6).filter(a -> a % 2 == 0).boxed().findAny();
	}

	@Test
	public void test() {
		List<Person> persons = Arrays.asList(new Person("Max", 18), new Person("Peter", 23), new Person("Pamela", 23),
				new Person("David", 12));
		List<Person> filtered = persons.stream().filter(p -> p.name.startsWith("P")).collect(Collectors.toList());
		System.out.println(filtered);
		String phrase = persons.stream().filter(p -> p.age >= 18).map(p -> p.name)
				.collect(Collectors.joining(" and ", "In Germany ", " are of legal age."));

		System.out.println(phrase);
	}

	class Person {
		String name;
		int age;

		Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
