package xly.doip.client;

/**
 * A specification of part of a sort order for {@link QueryParams}.
 */
public class SortField {
    private final String name;
    private final boolean reverse;

    /**
     * Constructs a SortField.
     *
     * @param name the name of the field to sort on
     * @param reverse if true, reverse the sort order
     */
    public SortField(String name, boolean reverse) {
        this.name = name;
        this.reverse = reverse;
    }

    public SortField(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
    }

    public boolean isReverse() {
        return reverse;
    }
}
