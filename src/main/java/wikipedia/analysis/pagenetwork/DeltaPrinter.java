package wikipedia.analysis.pagenetwork;

import java.util.List;
import java.util.Map;

import org.joda.time.DateMidnight;

import wikipedia.network.TimeFrameGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DeltaPrinter {

    public static void main(final String[] args) {
        List<DateMidnight> allTimeFrames = Lists.newArrayList(new DateMidnight(2010, 12, 1),
                                                          new DateMidnight(2011, 1, 1),
                                                          new DateMidnight(2011, 2, 1),
                                                          new DateMidnight(2011, 3, 1),
                                                          new DateMidnight(2011, 4, 1));

        final List<String> categories = CategoryLists.ENGLISH_MUSIC;
        Map<DateMidnight, TimeFrameGraph> dateGraphMap = Maps.newHashMap();

        for (DateMidnight dateTime : allTimeFrames) {
            dateGraphMap.put(dateTime, new NetworkBuilder(categories, "en", dateTime).getGraphAtDate());
        }
    }

}
