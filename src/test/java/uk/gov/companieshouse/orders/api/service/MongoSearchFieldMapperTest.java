package uk.gov.companieshouse.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.util.CharEscaper;

@ExtendWith(MockitoExtension.class)
class MongoSearchFieldMapperTest {

    @Mock
    private CharEscaper charEscaper;

    @InjectMocks
    private MongoSearchFieldMapper mongoSearchFieldMapper;

    @DisplayName("Should correctly map exact match to any if source is blank")
    @Test
    void testExactMatchSourceIsBlank() {
        assertThat(mongoSearchFieldMapper.exactMatchOrAny(null), is(".*"));
        assertThat(mongoSearchFieldMapper.exactMatchOrAny(""), is(".*"));
    }

    @DisplayName("Should correctly map exact match if source is not blank")
    @Test
    void testExactMatch() {
        String source = "source";
        when(charEscaper.escape(source)).thenReturn("escaped source");
        assertThat(mongoSearchFieldMapper.exactMatchOrAny(source), is("^escaped source$"));
    }

    @DisplayName("Should correctly map partial match to any if source is blank")
    @Test
    void testPartialMatchSourceIsBlank() {
        assertThat(mongoSearchFieldMapper.partialMatchOrAny(null), is(".*"));
        assertThat(mongoSearchFieldMapper.partialMatchOrAny(""), is(".*"));
    }

    @DisplayName("Should correctly map partial match if source is not blank")
    @Test
    void testPartialMatch() {
        String source = "source";
        when(charEscaper.escape(source)).thenReturn("escaped source");
        assertThat(mongoSearchFieldMapper.partialMatchOrAny(source), is("escaped source"));
    }
}
