package uk.gov.companieshouse.orders.api.util;

import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;


/**
 * Encapsulates the Regex metacharacters used by the Java Regex API and a mechanism for escaping those metacharacters.
 *
 * <p>The metacharacters supported by this API are: &lt;([{\^-=$!|]})?*+.&gt;</p>
 *
 * <p>See <a href="https://docs.oracle.com/javase/tutorial/essential/regex/literals.html">Regular Expressions: String Literal</a></p>
 *
 * @see  Pattern
 */
@Component
public class RegexEscaper implements CharEscaper {

  /**
   * Pattern that defines Regex [special] metacharacters.
   */
  private static Pattern REGEX_METACHARACTERS = Pattern.compile("[\\<\\(\\[\\{\\\\^\\-\\=\\$\\!\\|\\]\\}\\)\\?\\*\\+\\.]");

  /**
   * Escapes any regex metacharacters in the supplied string.
   *
   * @param   source  string to be escaped
   *
   * @return  supplied string with escaped metacharacters.
   */
  public CharSequence escape(CharSequence source) {
    return Optional.of(source).map(s -> REGEX_METACHARACTERS.matcher(s).replaceAll("\\\\$0")).get();
  }
}
