package su.nightexpress.sunlight.moduleImpl.chat.rule;

import su.nightexpress.nightcore.util.LowerCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordFilter {

    private static final Pattern WORD_SPLITTER = Pattern.compile("(?U)\\b\\w+\\b");

    private final Set<String> exactWords;
    private final List<RuleHandler> ruleHandlers;

    public WordFilter(Set<String> rules) {
        this.exactWords = new HashSet<>();
        this.ruleHandlers = new ArrayList<>();

        this.ruleHandlers.add(new RuleHandler(RuleValidator.forSequence('-'), RuleResult.ALLOW));
        this.ruleHandlers.add(new RuleHandler(RuleValidator.forSequence('+'), RuleResult.BLOCK));
        this.ruleHandlers.add(new RuleHandler(RuleValidator.forPrefix('-'), RuleResult.ALLOW));
        this.ruleHandlers.add(new RuleHandler(RuleValidator.forSuffix('-'), RuleResult.ALLOW));
        this.ruleHandlers.add(new RuleHandler(RuleValidator.forPrefix('+'), RuleResult.BLOCK));
        this.ruleHandlers.add(new RuleHandler(RuleValidator.forSuffix('+'), RuleResult.BLOCK));

        for (String rule : rules) {
            if (rule == null || rule.isBlank())
                continue;

            RuleHandler handler = this.ruleHandlers.stream().filter(ruleHandler -> ruleHandler.canHandle(rule))
                    .findFirst().orElse(null);
            if (handler == null) {
                String normalized = rule.trim().toLowerCase(java.util.Locale.ROOT);
                if (!normalized.isBlank()) this.exactWords.add(normalized);
                continue;
            }

            String cleaned = handler.clean(rule);
            if (cleaned == null || cleaned.isBlank()) continue;
            handler.addWord(cleaned);
        }
    }

    public boolean matches(String input) {
        if (input.isBlank())
            return false;

        Matcher matcher = WORD_SPLITTER.matcher(input);

        while (matcher.find()) {
            String word = matcher.group();
            if (this.isBadWord(word)) {
                return true;
            }
        }

        return false;
    }

    public String censor(String input, char censorChar) {
        if (input.isBlank())
            return input;

        StringBuilder builder = new StringBuilder(input);
        Matcher matcher = WORD_SPLITTER.matcher(input);

        while (matcher.find()) {
            String word = matcher.group();
            if (this.isBadWord(word)) {
                for (int index = matcher.start(); index < matcher.end(); index++) {
                    builder.setCharAt(index, censorChar);
                }
            }
        }

        return builder.toString();
    }

    public boolean isBadWord(String word) {
        String lower = LowerCase.USER_LOCALE.apply(word);
        if (this.exactWords.contains(lower))
            return true;

        for (RuleHandler node : this.ruleHandlers) {
            RuleResult result = node.scan(lower);
            if (result == RuleResult.ALLOW)
                return false;
            if (result == RuleResult.BLOCK)
                return true;
        }

        return false;
    }
}
