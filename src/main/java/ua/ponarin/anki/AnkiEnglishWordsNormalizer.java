package ua.ponarin.anki;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class AnkiEnglishWordsNormalizer {
    private static final Path SOURCE_FILE_PATH = Path.of("/Users/ihor/Documents/Personal/English Words (original).txt");
    private static final Path DESTINATION_FILE_PATH = Path.of("/Users/ihor/Documents/Personal/English Words (processed).txt");
    private static final String COLUMN_SEPARATOR = "\t";
    private static final String LINE_SEPARATOR = "<br>";
    private static final int TRANSLATION_COLUMN_INDEX = 2;
    private static final int MEANING1_COLUMN_INDEX = 3;
    private static final Function<String, List<String>> splitToColumns = record -> List.of(record.split(COLUMN_SEPARATOR, -1));
    private static final Function<String, List<String>> splitToTokens = record -> List.of(record.split(LINE_SEPARATOR));
    private static final Function<List<String>, String> combineTokens = tokens -> String.join(LINE_SEPARATOR, tokens);
    private static final Function<List<String>, String> combineColumns = columns -> String.join(COLUMN_SEPARATOR, columns);

    private static final UnaryOperator<List<List<String>>> moveTranslationsToMeanings = columns -> {
        var updatedColumns = new ArrayList<>(columns);
        var translations = columns.get(TRANSLATION_COLUMN_INDEX);
        var meanings = columns.get(MEANING1_COLUMN_INDEX);

        var separatedEntities = meanings.stream()
                .collect(Collectors.groupingBy(entry -> entry.matches("^[\\p{Alnum}(].*$")));

        var newTranslations = separatedEntities.getOrDefault(false, List.of());
        var updatedMeanings = separatedEntities.getOrDefault(true, List.of());
        translations.addAll(newTranslations);

        updatedColumns.set(TRANSLATION_COLUMN_INDEX, translations);
        updatedColumns.set(MEANING1_COLUMN_INDEX, updatedMeanings);

        return updatedColumns;
    };

    public static void main(String[] args) throws IOException {
        var ankiEnglishWordsNormalizer = new AnkiEnglishWordsNormalizer();
        var sourceRecords = Files.readAllLines(SOURCE_FILE_PATH);
        var invalidSourceRecords = ankiEnglishWordsNormalizer.validateRecords(sourceRecords);
        if (invalidSourceRecords.isEmpty()) {
            var processedRecords = ankiEnglishWordsNormalizer.normalizeRecords(sourceRecords);
            var invalidProcessedRecords = ankiEnglishWordsNormalizer.validateRecords(processedRecords);
            if (invalidProcessedRecords.isEmpty()) {
                Files.writeString(DESTINATION_FILE_PATH, String.join(System.lineSeparator(), processedRecords));
            } else {
                ankiEnglishWordsNormalizer.printInvalidRecords("Invalid processed records", invalidProcessedRecords);
            }
        } else {
            ankiEnglishWordsNormalizer.printInvalidRecords("Invalid source records", invalidSourceRecords);
            System.out.println("Source records contain records that didn't pass the validation process. Printing the records...");
        }

    }

    public List<String> normalizeRecords(List<String> records) {
        return records.stream()
                .map(splitToColumns)
                .map(columns -> columns.stream()
                        .map(column -> column.replaceAll("</div>", "<br>"))
                        .map(splitToTokens)
                        .map(tokens -> tokens.stream()
                                .map(token -> token.replaceAll("<(?!br).*?>", ""))
                                .map(String::trim)
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList()))
                .map(moveTranslationsToMeanings)
                .map(columns -> columns.stream()
                        .map(combineTokens)
                        .collect(Collectors.toList()))
                .map(combineColumns)
                .collect(Collectors.toList());
    }

    private List<List<String>> validateRecords(List<String> records) {
        return records.stream()
                .map(splitToColumns)
                .filter(columns -> columns.size() != 20)
                .collect(Collectors.toList());
    }

    private void printInvalidRecords(String title, List<List<String>> invalidRecords) {
        System.err.printf("%s. Total records count: %d%n", title, invalidRecords.size());
        invalidRecords.forEach(record -> System.err.printf("Source: '%s', number of columns: '%d', content: '%s'",
                record.get(0),
                record.size(),
                record));
    }
}