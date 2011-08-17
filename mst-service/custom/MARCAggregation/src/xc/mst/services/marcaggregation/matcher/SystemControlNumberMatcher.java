/**
  * Copyright (c) 2011 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  * @author Benjamin D. Anderson
  * 
  */
package xc.mst.services.marcaggregation.matcher;

import gnu.trove.TLongLongHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xc.mst.bo.record.InputRecord;
import xc.mst.bo.record.Record;

/**
 * The System control number corresponds to the 
 * <a href="http://www.loc.gov/marc/bibliographic/bd035.html">MARC 035 field</a>
 *
 * @author Benjamin D. Anderson
 *
 */
public class SystemControlNumberMatcher extends FieldMatcherService {

    protected Map<String, Long> prefixIds = new HashMap<String, Long>();
    protected TLongLongHashMap scn2outputIds = new TLongLongHashMap();
    
    protected long getPrefixId(String s) {
        // return the prefix String
        return 0l;
    }
      
    protected long getNumericId(String s) {
        // return the numeric portion
        return 0l;
    }
      
    protected long getMapId(String s) {
        return (getNumericId(s)*1000)+getPrefixId(s);
    }

    public List<Long> getMatchingOutputIds(InputRecord ir) {
        //String s = ir.getMARC().getDataFields().get(35).get('a');
        //return lccn2outputIds.get(getMapId(s));
        return null;
    }

    public void addRecordToMatcher(Record r) {
        // String s = r.getMARC().getDataFields().get(35).get('a');
        // lccn2outputIds.add(r.getId(), getMapId(s));
    }

    public List<Long> getPreviousMatchPoint(long inputId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void flush() {
        // TODO Auto-generated method stub
    }

    public void loadFromDB() {
        // TODO Auto-generated method stub
    }
    
    public void unload() {
        // TODO Auto-generated method stub
    }

}
