package ch.gma.contrat.entretien;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Palindrome {

  public static void main(String[] args) {
    String mot = "aba";
    System.out.println(isPalindrome(mot));
  }

  public static boolean isPalindrome2(String mot) {
	  Optional<String> reverse = sanitizeToStream(mot).reduce((a,b) -> b +a);
	  return reverse.get().equalsIgnoreCase(sanitizeToStream(mot).collect(Collectors.joining()));
  }

private static Stream<String> sanitizeToStream(String mot) {
	return toStream(mot).filter(a-> !a.matches("[ ,;:]"));
}

private static Stream<String> toStream(String mot) {
	return mot.codePoints().mapToObj(s -> String.valueOf((char) s));
}
  
  
  private static String removeSpaces(String mot) {
    return mot.replace(" ", "");
  }
  
  public static String removeSpacesStream(String mot) {
	    return  toStream(mot)
	    		.filter(s -> !s.equalsIgnoreCase(" "))
	    		.collect(Collectors.joining());
  }
	  
	  
  public static boolean isPalindrome(String mot) {
    boolean isPalindrome = true;
    String result = removeSpaces(mot).toLowerCase();
    
    toStream(mot)
    	.reduce((b,c) -> b+ c);
    
   

    for (int i = 0; i < result.length() - 1; i++) {

      if (!result.substring(i, i + 1)
        .equals(result.substring(result.length() - i - 1, result.length() - i))) {
        return false;
      }
    }
    return isPalindrome;
  }
}
