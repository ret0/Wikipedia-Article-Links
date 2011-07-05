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

    public static final List<String> CLASSICAL_MUSIC = ImmutableList.of(
            "Category:German_composers",
            "Category:Italian composers",
            "Category:French_composers",
            "Category:Polish_composers",
            "Category:English composers",
            // ------------------
            "Category:Classical era composers",
            "Category:Romantic_composers",
            "Category:Baroque composers",
            "Category:Classical composers of church music",
            "Category:Opera composers",
            // ------------------
            "Category:17th-century_composers",
            "Category:18th-century_composers",
            "Category:19th-century_composers",
            "Category:20th-century_composers",
            // ------------------
            "Category:German_classical_organists",
            "Category:Polish_classical_pianists");


    public static final List<String> MUSIC_GROUPS = ImmutableList.of(
            "Category:2000s_music_groups",
            "Category:1990s_music_groups");

    public static final List<String> BORN_IN_THE_80IES = ImmutableList.of(
            "Category:1980_births",
            "Category:1981_births",
            "Category:1982_births",
            "Category:1983_births",
            "Category:1984_births",
            "Category:1985_births",
            "Category:1986_births",
            "Category:1987_births",
            "Category:1988_births",
            "Category:1989_births"
            );

//    public static final List<List<String>> getAllCategories() {
//        return ImmutableList.of(ENGLISH_MUSIC, CLASSICAL_MUSIC, MUSIC_GROUPS);
//    }

}
