package su.nightexpress.sunlight.moduleImpl.nametags.render;

/**
 * One contributor to a nameplate (team, rank, tag or glow). Holds raw
 * configured
 * text and a raw colour name; conversion to legacy codes happens in
 * {@link PrefixComposer} so composition stays a pure, testable function.
 */
public record Part(String prefix, String suffix, String color) {

    public static final Part EMPTY = new Part("", "", "");

    public Part {
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        color = color == null ? "" : color;
    }

    public static Part of(String prefix, String suffix, String color) {
        return new Part(nullToEmpty(prefix), nullToEmpty(suffix), nullToEmpty(color));
    }

    public boolean hasPrefix() {
        return !this.prefix.isEmpty();
    }

    public boolean hasSuffix() {
        return !this.suffix.isEmpty();
    }

    public boolean hasColor() {
        return !this.color.isBlank();
    }

    public boolean isEmpty() {
        return !this.hasPrefix() && !this.hasSuffix() && !this.hasColor();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
