package app.ashcon.architect.util;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to score and compare {@link String}s.
 *
 * @link https://github.com/rmm5t/liquidmetal
 * @author Steve Anton
 */
public interface LiquidMetal {

    double SCORE_NO_MATCH = 0.0;
    double SCORE_MATCH = 1.0;
    double SCORE_TRAILING = 0.8;
    double SCORE_TRAILING_BUT_STARTED = 0.9;
    double SCORE_BUFFER = 0.85;

    static <T> Optional<T> score(@Nullable String string, Stream<T> items, Function<T, String> mapper) {
        return Optional.ofNullable(score(string, items.collect(Collectors.toList()), mapper, 0.9));
    }

    static @Nullable <T> T score(@Nullable String string, Iterable<T> items, Function<T, String> mapper, double threshold) {
        if(string == null) {
            return null;
        }
        T best = null;
        for(T item : items) {
            if(threshold <= score(mapper.apply(item), string)) {
                if(best != null) {
                    return null;
                }
                best = item;
            }
        }
        return best;
    }

    static double score(String string, String abbreviation) {
        if (abbreviation.length() == 0) return SCORE_TRAILING;
        if (abbreviation.length() > string.length()) return SCORE_NO_MATCH;
        double[] scores = buildScoreArray(string, abbreviation);
        if (scores == null) {
            return 0;
        }
        double sum = 0.0;
        for (double score : scores) {
            sum += score;
        }
        return (sum / scores.length);
    }

    static double[] buildScoreArray(String string, String abbreviation) {
        double[] scores = new double[string.length()];
        String lower = string.toLowerCase();
        String chars = abbreviation.toLowerCase();
        int lastIndex = -1;
        boolean started = false;
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            int index = lower.indexOf(c, lastIndex + 1);
            if (index == -1) return null;
            if (index == 0) started = true;
            if (isNewWord(string, index)) {
                scores[index - 1] = 1.0;
                fillArray(scores, SCORE_BUFFER, lastIndex + 1, index - 1);
            } else if (isUpperCase(string, index)) {
                fillArray(scores, SCORE_BUFFER, lastIndex + 1, index);
            } else {
                fillArray(scores, SCORE_NO_MATCH, lastIndex + 1, index);
            }
            scores[index] = SCORE_MATCH;
            lastIndex = index;
        }
        double trailingScore = started ? SCORE_TRAILING_BUT_STARTED : SCORE_TRAILING;
        fillArray(scores, trailingScore, lastIndex + 1, scores.length);
        return scores;
    }

    static boolean isNewWord(String string, int index) {
        if (index == 0) return false;
        char c = string.charAt(index - 1);
        return (c == ' ' || c == '\t');
    }

    static void fillArray(double[] array, double value, int from, int to) {
        for (int i = from; i < to; i++) {
            array[i] = value;
        }
    }

    static boolean isUpperCase(String string, int index) {
        char c = string.charAt(index);
        return ('A' <= c && c <= 'Z');
    }

}
