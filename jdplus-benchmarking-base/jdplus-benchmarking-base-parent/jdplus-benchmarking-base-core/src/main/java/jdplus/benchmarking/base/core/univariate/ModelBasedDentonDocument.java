package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.ModelBasedDentonSpec;
import jdplus.toolkit.base.api.timeseries.AbstractMultiTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.util.List;

public class ModelBasedDentonDocument extends AbstractMultiTsDocument<ModelBasedDentonSpec, ModelBasedDentonResults> {

    private final ModellingContext context;

    public ModelBasedDentonDocument() {
        super(ModelBasedDentonSpec.DEFAULT);
        context = ModellingContext.getActiveContext();
    }

    public ModelBasedDentonDocument(ModellingContext context) {
        super(ModelBasedDentonSpec.DEFAULT);
        this.context = context;
    }
    
    public ModellingContext getContext(){
        return context;
    }

    @Override
    protected ModelBasedDentonResults internalProcess(ModelBasedDentonSpec spec, List<TsData> data) {
        if (data.size() != 2)
            return null;
        TsData low = data.get(0);
        TsData high = data.get(1);
        if (low == null || high == null)
            return null;
        return ModelBasedDentonProcessor.process(low, high, spec);
    }

}
