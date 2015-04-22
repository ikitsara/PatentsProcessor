package lodi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawLocation {

    /**
     * Return the canonical String for this locale. The result is used in fuzzy string
     * matching. The arguments to this function may be null or empty strings, in which
     * case they do not appear in the final concatenated string.
     *
     * @param city Locale city
     * @param state Locale state
     * @param country Locale country
     * @return The concatenated representation of this locale
     */
    public static String concatenateLocation(final String city, final String state, final String country) {
        StringJoiner j = new StringJoiner(", ");

        if (city != null && !city.isEmpty())
            j.add(city);

        if (state != null && !state.isEmpty())
            j.add(state);

        if (country != null && !country.isEmpty())
            j.add(country);

        return j.toString();
    }

    /**
     * Apply various filters to clean the text of this raw location to make it suitable
     * for matching.
     *
     * @param text The raw location text
     * @return The cleaned location text
     */
    public static String cleanRawLocation(String text) {
        text = eolPattern.matcher(text).replaceAll("");
        text = separatorPattern.matcher(text).replaceAll(", ");

        text = manualReplacements.apply(text);
        text = quickFix(text);

        return text;
    }

    protected final static Pattern eolPattern = Pattern.compile("[\r\n]");
    protected final static Pattern separatorPattern = Pattern.compile("\\|", Pattern.UNICODE_CHARACTER_CLASS);
    protected final static Pattern curlyPattern = Pattern.compile("\\{.*\\((.*)\\).*\\}", Pattern.UNICODE_CHARACTER_CLASS);

    protected final static PatternReplacements manualReplacements = initManualReplacements();
    protected final static PatternReplacements removePatterns = initRemovePatterns();
    protected final static PatternReplacements countryPatterns = initCountryPatterns();

    /**
     * Perform additional text fixes that can't be expressed as a simple pattern
     * replacement. Currently, this consists of replacing the contents of a curly-braced
     * expression with the contents of the parenthesized expression that it contains. So
     * '{blah blah (X)}' is replaced by X.
     *
     * @param text In put text to apply fixes to
     * @return Fixed text
     */
    protected static String quickFix(String text) {
        Matcher m = curlyPattern.matcher(text);

        if (m.matches())
            return m.replaceAll(m.group(1));
        else
            return text;
    }

    private static PatternReplacements initManualReplacements() {
        return initPatternReplacements("/manual_replacement_library.txt");
    }

    private static PatternReplacements initRemovePatterns() {
        return initPatternReplacements("/remove_patterns.txt");
    }

    private static PatternReplacements initCountryPatterns() {
        return initPatternReplacements("/country_patterns.txt");
    }

    private static PatternReplacements initPatternReplacements(String resource) {
        URL url = RawLocation.class.getResource(resource);

        PatternReplacements pr = null;  

        try (BufferedReader in = 
                new BufferedReader(new InputStreamReader(url.openStream())))
        {
            pr = PatternReplacements.loadFromFile(in);
        }
        catch(java.io.IOException e) {
            System.out.println("There was a problem reading '" + resource + "'");
        }

        return pr;
    }
    
    // fields taken from the database

    public final String city;
    public final String state;
    public final String country;

    // the cleaned field used for matching

    public final String cleanedLocation;

    public RawLocation(final String city, final String state, final String country) {
        this.city = city;
        this.state = city;
        this.country = city;

        String s = concatenateLocation(city, state, country);
        s = cleanRawLocation(s);

        this.cleanedLocation = s;
    }

}