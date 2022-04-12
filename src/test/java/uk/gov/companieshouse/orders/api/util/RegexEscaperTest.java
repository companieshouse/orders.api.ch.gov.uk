package uk.gov.companieshouse.orders.api.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


/**
 * Responsible for unit testing {@link RegexEscaper#escape(CharSequence s)}.
 */
@ExtendWith(MockitoExtension.class)
class RegexEscaperTest {

    @InjectMocks
    private RegexEscaper regexEscaper;

    private static Stream<Arguments> translations() {
        return Stream.of(
                arguments("", ""),
                arguments("<", "\\<"),
                arguments("(", "\\("),
                arguments("[", "\\["),
                arguments("{", "\\{"),
                arguments("\\", "\\\\"),
                arguments("^", "\\^"),
                arguments("-", "\\-"),
                arguments("=", "\\="),
                arguments("$", "\\$"),
                arguments("!", "\\!"),
                arguments("|", "\\|"),
                arguments("]", "\\]"),
                arguments("}", "\\}"),
                arguments(")", "\\)"),
                arguments("?", "\\?"),
                arguments("*", "\\*"),
                arguments("+", "\\+"),
                arguments(".", "\\."),
                arguments("a", "a"),
                arguments("1", "1"),
                arguments(")*^.", "\\)\\*\\^\\."),
                arguments("demo@ch.gov.uk", "demo@ch\\.gov\\.uk"));
    }

    @DisplayName("Should throw null pointer exception if the supplied string is null")
    @Test
    void testEscapeNull() {
        assertThrows(NullPointerException.class, () -> regexEscaper.escape(null));
    }

    @DisplayName("Should correctly escape special characters")
    @ParameterizedTest
    @MethodSource("translations")
    void testEscapeCharacters(String source, String expected) {
        assertThat(regexEscaper.escape(source), is(expected));
    }
}
