package ua.ponarin.anki;

import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnkiEnglishWordsNormalizerTest {
    private static final int TRANSLATION_COLUMN_INDEX = 2;
    private static final int MEANING1_COLUMN_INDEX = 3;
    private static final String COLUMN_SEPARATOR = "\t";

    private AnkiEnglishWordsNormalizer normalizer;

    @BeforeEach
    public void setup() {
        normalizer = new AnkiEnglishWordsNormalizer();
    }

    @ParameterizedTest
    @MethodSource
    public void normalizeRecordsTest(Card card) {
        assertEquals(List.of(card.getExpectedRecord()), normalizer.normalizeRecords(List.of(card.getSourceRecord())));
    }

    private static Stream<Card> normalizeRecordsTest() {
        return Stream.of(
                Card.builder()
                        .translation("перевод")
                        .expectedTranslation("перевод")
                        .meaning("meaning")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .meaning("<a href=\"a\">meaning</a>")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .translation("перевод")
                        .expectedTranslation("перевод<br>перевод1")
                        .meaning("meaning<br>перевод1")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .expectedTranslation("перевод")
                        .meaning("meaning<br>перевод")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .translation("перевод")
                        .expectedTranslation("перевод<br>перевод1")
                        .meaning("перевод1")
                        .build(),
                Card.builder()
                        .translation("перевод")
                        .expectedTranslation("перевод<br>перевод1<br>перевод2")
                        .meaning("meaning<br>перевод1<br>meaning1<br>перевод2")
                        .expectedMeaning("meaning<br>meaning1")
                        .build(),
                Card.builder()
                        .translation("перевод")
                        .expectedTranslation("перевод<br>перевод1")
                        .meaning("meaning<br>перевод1<br>")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .translation("перевод")
                        .expectedTranslation("перевод<br>перевод1")
                        .meaning("meaning<br>перевод1")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .translation("  перевод   ")
                        .expectedTranslation("перевод<br>перевод1")
                        .meaning("  meaning<br>  перевод1   ")
                        .expectedMeaning("meaning")
                        .build(),
                Card.builder()
                        .translation("перевод<br><br>  <br> ")
                        .expectedTranslation("перевод<br>перевод1")
                        .meaning("meaning<br>перевод1 <br> <br>")
                        .expectedMeaning("meaning")
                        .build()
        );
    }

    @Builder
    private static class Card {
        @Builder.Default
        private String translation = "";
        @Builder.Default
        private String expectedTranslation = "";
        @Builder.Default
        private String meaning = "";
        @Builder.Default
        private String expectedMeaning = "";

        public String getSourceRecord() {
            return getRecord(translation, meaning);
        }

        public String getExpectedRecord() {
            return getRecord(expectedTranslation, expectedMeaning);
        }

        private String getRecord(String translation, String meaning) {
            var list = new ArrayList<>(Collections.nCopies(20, ""));
            list.set(TRANSLATION_COLUMN_INDEX, translation);
            list.set(MEANING1_COLUMN_INDEX, meaning);
            return String.join(COLUMN_SEPARATOR, list);
        }
    }
}
