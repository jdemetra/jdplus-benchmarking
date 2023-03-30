package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.toolkit.base.api.timeseries.AbstractMultiTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.util.List;

public class TemporalDisaggregationDocument extends AbstractMultiTsDocument<TemporalDisaggregationSpec, TemporalDisaggregationResults> {

    private final ModellingContext context;

    public TemporalDisaggregationDocument() {
        super(TemporalDisaggregationSpec.CHOWLIN);
        context = ModellingContext.getActiveContext();
    }

    public TemporalDisaggregationDocument(ModellingContext context) {
        super(TemporalDisaggregationSpec.CHOWLIN);
        this.context = context;
    }
    
    public ModellingContext getContext(){
        return context;
    }

    @Override
    protected TemporalDisaggregationResults internalProcess(TemporalDisaggregationSpec spec, List<TsData> data) {
        if (data.isEmpty())
            return null;
        TsData[] indicators = new TsData[data.size()-1];
        for (int i=1; i<data.size(); ++i)
            indicators[i-1]=data.get(i);
        return TemporalDisaggregationProcessor.process(data.get(0), indicators, spec);
    }

}
