package ch.gma.contrat.entretien;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

public class TestPalindrome {

	@Test
	public void testPalindromeNonPassant1() {
		final String tester = "ab";
		Boolean resultat = Palindrome.isPalindrome(tester);
		assertFalse(resultat);
	}
	
	@Test
	public void testPalindrome1char() {
		final String tester = "a";
		Boolean resultat = Palindrome.isPalindrome(tester);
		assertTrue(resultat);
	}
	
	@Test
	public void testPalindromeBasique() {
		final String tester = "karine en irak";
		Boolean resultat = Palindrome.isPalindrome(tester);
		assertTrue(resultat);
	}
	
	@Test
	public void testPalindromeMinMaj() {
		final String tester = "aA";
		Boolean resultat = Palindrome.isPalindrome(tester);
		assertTrue(resultat);
	}
	
	
	@Test
	public void testPalindromePonctuation() {
		final String tester = ",a";
		Boolean resultat = Palindrome.isPalindrome2(tester);
		assertTrue(resultat);
	}
	
	@Test
	public void test() {
		assertTrue(Palindrome.isPalindrome2("abB;a"));
	}
		
}
