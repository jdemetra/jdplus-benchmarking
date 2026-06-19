package jdplus.benchmarking.base.r.util;

import jdplus.toolkit.base.api.timeseries.TsData;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DictionaryGroups {

    private final Map<String, TsData[]> dictionaryGroups = new LinkedHashMap();

    public static DictionaryGroups of(Map<String, TsData[]> rslt) {
        DictionaryGroups adic = new DictionaryGroups();
        adic.dictionaryGroups.putAll(rslt);
        return adic;
    }

    public void add(String name, TsData s) {
        TsData[] sName = this.dictionaryGroups.get(name);

        if (sName == null) {
            if (!(s == null)) {
                this.dictionaryGroups.put(name, new TsData[]{s});
            } else {
                this.dictionaryGroups.put(name, null);
            }
        } else {
            TsData[] sNameExt = Arrays.copyOf(sName, sName.length + 1);
            sNameExt[sName.length] = s;
            this.dictionaryGroups.put(name, sNameExt);
        }
    }

    public String[] names() {
        return (String[])this.dictionaryGroups.keySet().toArray(new String[this.dictionaryGroups.size()]);
    }

    public TsData[] get(String name) {
        return (TsData[])this.dictionaryGroups.get(name);
    }

    public Map<String, TsData[]> data() {
        return Collections.unmodifiableMap(this.dictionaryGroups);
    }
}
