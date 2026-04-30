/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.api.multivariate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import jdplus.toolkit.base.api.design.Algorithm;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.Development;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class MultivariateChowLin {

    private final AtomicReference<MultivariateChowLin.Processor> PROCESSOR = new AtomicReference<>(MultivariateChowLinLoader.Processor.load());
    //private final MultivariateChowLinLoader.Processor PROCESSOR = new MultivariateChowLinLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }
    
    public MultivariateChowLinResults process(LinkedHashMap<String, ModelData> mData, Map<String, TsData> constraints, MultivariateChowLinSpec spec) {
        return PROCESSOR.get().process(mData, constraints, spec);
    }
    
    @Algorithm
    @SuppressWarnings("SingleFallbackNotExpected")
    @ServiceDefinition(quantifier = Quantifier.SINGLE)
    public interface Processor {

        MultivariateChowLinResults process(LinkedHashMap<String, ModelData> mData, Map<String, TsData> constraints, MultivariateChowLinSpec spec);

    }
}
