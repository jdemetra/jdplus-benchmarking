package jdplus.benchmarking.base.r;

import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.r.util.Dictionary;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class arrDictionary {

//    public static final String R = "r";
//    public static final String RPREFIX = "r@";
    private final Map<String, TsData[]> arrDictionary = new LinkedHashMap();

    public static arrDictionary of(Map<String, TsData[]> rslt) {
        arrDictionary adic = new arrDictionary();
        adic.arrDictionary.putAll(rslt);
        return adic;
    }

    public void add(String name, TsData s) {
        TsData[] sName = this.arrDictionary.get(name);

        if (sName == null) {
            this.arrDictionary.put(name, new TsData[]{s});
        } else {
            TsData[] sNameExt = Arrays.copyOf(sName, sName.length + 1);
            sNameExt[sName.length] = s;
            this.arrDictionary.put(name, sNameExt);
        }
    }

    public String[] names() {
        return (String[])this.arrDictionary.keySet().toArray(new String[this.arrDictionary.size()]);
    }

    public TsData[] get(String name) {
        return (TsData[])this.arrDictionary.get(name);
    }

    public Map<String, TsData[]> data() {
        return Collections.unmodifiableMap(this.arrDictionary);
    }
}
