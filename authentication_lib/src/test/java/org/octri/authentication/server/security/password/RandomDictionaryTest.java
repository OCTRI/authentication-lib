package org.octri.authentication.server.security.password;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for RandomDictionary.
 *
 * @author lawhead
 *
 */
public class RandomDictionaryTest {

	List<String> words = new ArrayList<String>(Arrays.asList("foo", "bar", "baz", "four", "eagle", "wizard", "bread"));
	List<String> threeLetterWords = new ArrayList<String>(Arrays.asList("foo", "bar", "baz"));

	RandomDictionary dict = new RandomDictionary(words);

	@Test
	public void testRandomWord() {
		String word = dict.getRandom(3, 3);
		assertEquals(word.length(), 3);

		assertTrue(word == "foo" || word == "bar" || word == "baz");

		List<String> words = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			word = dict.getRandom(4, 6);
			words.add(word);
			assertTrue(word.length() >= 4 && word.length() <= 6);
			assertFalse(threeLetterWords.contains(word));
		}
		Set<String> set = new HashSet<>(words);

		assertTrue(words.size() > set.size());
		assertTrue(set.size() > 1);
	}

	@Test
	public void testWordsOfLength() {
		assertEquals(dict.wordsOfLength(3), Integer.valueOf(3));

		assertEquals(dict.wordsOfLength(12), Integer.valueOf(0));

		assertEquals(dict.wordsInRange(3, 4), Integer.valueOf(4));
		assertEquals(dict.wordsInRange(3, 12), Integer.valueOf(words.size()));
	}
}
