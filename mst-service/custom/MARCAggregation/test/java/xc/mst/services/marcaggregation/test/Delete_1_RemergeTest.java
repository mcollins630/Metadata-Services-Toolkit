package xc.mst.services.marcaggregation.test;

import java.util.ArrayList;
import java.util.List;

/**
 * by deleting the 2nd record in the 'small' set (see 'small' folder), test that
 * 'delete' method gets called, and matchpoint data for this record gets
 * modified, record 2 used to match record 6, but after the 2nd record is deleted,
 * there is no more {2,6} match.
 *
 * Result:  one match set, {3,5}, all others should map directly to an output. (1,4,6)
 * Starting simple!
 *
 * @author John Brand
 *
 */
public class Delete_1_RemergeTest extends SmallTest {

    public List<String> getFolders() {
        List<String> fileStrs = new ArrayList<String>();
        fileStrs.add("delete_1_remerge");
        return fileStrs;
    }

    protected String getTestName() {
        return "Update_1_RemergeTest";
    }
    protected void setupMatcherExpectations() {
        //load expected number of records for each matcher.
        expectedMatchRecordIds.put("x028abMatcher", 0);
        expectedMatchRecords.put  ("x028abMatcher", 0);

        expectedMatchRecordIds.put("x245ahMatcher", 0);
        expectedMatchRecords.put  ("x245ahMatcher", 0);

        expectedMatchRecordIds.put("x240aMatcher", 0);
        expectedMatchRecords.put  ("x240aMatcher", 0);

        expectedMatchRecordIds.put("x260abcMatcher", 0);
        expectedMatchRecords.put  ("x260abcMatcher", 0);

        expectedMatchRecordIds.put("x130aMatcher", 0);
        expectedMatchRecords.put  ("x130aMatcher", 0);

        expectedMatchRecordIds.put("ISSNMatcher", 2);
        expectedMatchRecords.put  ("ISSNMatcher", 1);

        expectedMatchRecordIds.put("ISBNMatcher", 1);
        expectedMatchRecords.put  ("ISBNMatcher", 2);

        expectedMatchRecordIds.put("x024aMatcher", 0);
        expectedMatchRecords.put  ("x024aMatcher", 0);

        expectedMatchRecordIds.put("LccnMatcher", 5);
        expectedMatchRecords.put  ("LccnMatcher", 3);

        expectedMatchRecordIds.put("SystemControlNumberMatcher", 3);
        expectedMatchRecords.put  ("SystemControlNumberMatcher", 3);


        expectedResults.add(getExpectedMatchSet(new long[]{3,5}));
    }
    //no longer should get matchset 2,6 when all is done and working.

}
