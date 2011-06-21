package wikipedia.analysis.pagenetwork;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Central place for all the category lists
 */
public final class CategoryLists {

    private CategoryLists() { }

    public static final List<String> ENGLISH_MUSIC = ImmutableList.of(
            "Category:American_female_singers",
            "Category:American_male_singers",
            "Category:American_singer-songwriters",
            "Category:American_pop_singer-songwriters",
            "Category:American_pop_singers",
            "Category:American_rock_singers",
            "Category:American_dance_musicians",
            // ------------------
            "Category:Canadian_singer-songwriters",
            "Category:Canadian_pop_singers",
            "Category:Canadian_female_singers",
            "Category:Canadian_male_singers",
            // ------------------
            "Category:English_rock_singers", "Category:English_pop_singers",
            "Category:English-language_singers", "Category:English_male_singers",
            "Category:English_female_singers",
            // ------------------
            "Category:Female_rock_singers",
            // ------------------
            "Category:1970s_singers", "Category:1980s_singers", "Category:1990s_singers",
            "Category:2000s_singers", "Category:2010s_singers",
            // ------------------
            "Category:1990s_rappers", "Category:2000s_rappers", "Category:2010s_rappers",
            // ------------------
            "Category:Grammy_Award_winners", "Category:World_Music_Awards_winners");

    public static final List<String> CLASSICAL_MUSIC = ImmutableList.of("Category:German_composers",
            "Category:German_classical_organists", "Category:Baroque composers", "Category:Classical composers of church music",
            "Category:Classical era composers", "Category:Opera composers", "Category:Italian composers", "Category:English composers",
            "Category:Polish_classical_pianists", "Category:Polish_composers", "Category:French_composers", "Category:German_composers",
            "Category:Romantic_composers", "Category:19th-century_composers", "Category:18th-century_composers", "Category:20th-century_composers");

}
