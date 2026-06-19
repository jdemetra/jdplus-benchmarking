package jdplus.benchmarking.base.api.multivariate;

import jdplus.toolkit.base.api.timeseries.TsData;

@lombok.Getter
public class ModelData {
    private final TsData y;
    private final TsData[] x;

    public ModelData(TsData y, TsData[] x) {
        this.y = y;
        this.x = x;
    }
}
