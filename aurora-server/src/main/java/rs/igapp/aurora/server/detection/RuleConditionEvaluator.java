package rs.igapp.aurora.server.detection;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import rs.igapp.aurora.domain.entity.LogEvent;
import rs.igapp.aurora.domain.entity.Rule;

/**
 * Interprets {@link Rule#getCondition()} against a {@link LogEvent}.
 *
 * <p>Supported syntax (clauses may be combined with {@code OR}, case-insensitive):
 * <ul>
 *   <li>{@code message CONTAINS 'literal'} — substring match on {@link LogEvent#getMessage()} (case-insensitive)</li>
 *   <li>{@code raw CONTAINS 'literal'} — substring match on {@link LogEvent#getRawData()} (if present)</li>
 *   <li>{@code message ~ 'regex'} — Java regex against message (dot does not match newline unless (?s) in pattern)</li>
 *   <li>{@code raw ~ 'regex'} — regex against raw data string</li>
 * </ul>
 * Inside single-quoted literals, {@code ''} is an escaped single quote (SQL style).
 *
 * <p>If a clause does not match any of the patterns above, it is treated as a plain substring of the message
 * (case-insensitive), after stripping optional surrounding single quotes.
 */
@Component
public class RuleConditionEvaluator {

    private static final Pattern MESSAGE_CONTAINS =
            Pattern.compile("(?is)^message\\s+contains\\s+'((?:''|[^'])*)'\\s*$");

    private static final Pattern RAW_CONTAINS =
            Pattern.compile("(?is)^raw\\s+contains\\s+'((?:''|[^'])*)'\\s*$");

    private static final Pattern MESSAGE_REGEX =
            Pattern.compile("(?is)^message\\s+~\\s*'((?:''|[^'])*)'\\s*$");

    private static final Pattern RAW_REGEX = Pattern.compile("(?is)^raw\\s+~\\s*'((?:''|[^'])*)'\\s*$");

    public boolean matches(Rule rule, LogEvent event) {
        if (rule.getCondition() == null || rule.getCondition().isBlank()) {
            return false;
        }
        String message = event.getMessage() != null ? event.getMessage() : "";
        String raw = event.getRawData() != null ? event.getRawData() : "";
        String condition = rule.getCondition().trim();
        String[] orParts = condition.split("(?i)\\s+OR\\s+");
        for (String part : orParts) {
            if (matchesClause(part.trim(), message, raw)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesClause(String clause, String message, String raw) {
        if (clause.isEmpty()) {
            return false;
        }
        Matcher mc = MESSAGE_CONTAINS.matcher(clause);
        if (mc.matches()) {
            String literal = unescapeSqlQuotes(mc.group(1));
            return containsIgnoreCase(message, literal);
        }
        Matcher rc = RAW_CONTAINS.matcher(clause);
        if (rc.matches()) {
            String literal = unescapeSqlQuotes(rc.group(1));
            return containsIgnoreCase(raw, literal);
        }
        Matcher mr = MESSAGE_REGEX.matcher(clause);
        if (mr.matches()) {
            String regex = unescapeSqlQuotes(mr.group(1));
            return matchesRegex(message, regex);
        }
        Matcher rr = RAW_REGEX.matcher(clause);
        if (rr.matches()) {
            String regex = unescapeSqlQuotes(rr.group(1));
            return matchesRegex(raw, regex);
        }
        String fallback = clause;
        if (fallback.length() >= 2
                && fallback.charAt(0) == '\''
                && fallback.charAt(fallback.length() - 1) == '\'') {
            fallback = unescapeSqlQuotes(fallback.substring(1, fallback.length() - 1));
        }
        return containsIgnoreCase(message, fallback);
    }

    private static String unescapeSqlQuotes(String s) {
        return s.replace("''", "'");
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static boolean matchesRegex(String text, String regex) {
        try {
            return Pattern.compile(regex).matcher(text).find();
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
