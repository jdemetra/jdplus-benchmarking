/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.desktop.plugin.benchmarking.ui;

import jdplus.benchmarking.base.api.benchmarking.BenchmarkingDictionaries;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericChartUI;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
import jdplus.toolkit.base.api.util.Id;
import java.util.concurrent.atomic.AtomicReference;
import jdplus.benchmarking.base.core.benchmarking.univariate.CubicSplineDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class CubicSplineViewFactory extends ProcDocumentViewFactory<CubicSplineDocument> {


     private static final AtomicReference<IProcDocumentViewFactory<CubicSplineDocument>> INSTANCE = new AtomicReference();

    public CubicSplineViewFactory() {
        registerFromLookup(CubicSplineDocument.class);
    }

    public static IProcDocumentViewFactory<CubicSplineDocument> getDefault() {
        IProcDocumentViewFactory<CubicSplineDocument> fac = INSTANCE.get();
        if (fac == null) {
            fac = new CubicSplineViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    public static void setDefault(IProcDocumentViewFactory<CubicSplineDocument> factory) {
        INSTANCE.set(factory);
    }

    @Override
    public Id getPreferredView() {
        return BenchmarkingViewFactory.RESULTS_MAIN; //To change body of generated methods, choose Tools | Templates.
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1000)
    public static class InputDataFactory extends ProcDocumentItemFactory<CubicSplineDocument, CubicSplineDocument> {

        public InputDataFactory() {
            super(CubicSplineDocument.class, 
                    BenchmarkingViewFactory.INPUT_DATA, 
                    s-> s, 
                    new GenericTableUI(true, new String[]{ BenchmarkingDictionaries.ORIGINAL, BenchmarkingDictionaries.TARGET}));
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }
    
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1010)
    public static class BIRatioFactory extends ProcDocumentItemFactory<CubicSplineDocument, CubicSplineDocument> {

        public BIRatioFactory() {
            super(CubicSplineDocument.class, 
                    BenchmarkingViewFactory.INPUT_BI, 
                    s-> s, 
                    new GenericChartUI(true, new String[]{ BenchmarkingDictionaries.BIRATIO}));
        }

        @Override
        public int getPosition() {
            return 1010;
        }
    }
    
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
    public static class MainChartFactory extends ProcDocumentItemFactory<CubicSplineDocument, CubicSplineDocument> {

        public MainChartFactory() {
            super(CubicSplineDocument.class, 
                    BenchmarkingViewFactory.RESULTS_MAIN, 
                    s-> s, 
                    new GenericChartUI(true, new String[]{ BenchmarkingDictionaries.ORIGINAL, BenchmarkingDictionaries.BENCHMARKED}));
        }

        @Override
        public int getPosition() {
            return 2000;
        }
    }

}
