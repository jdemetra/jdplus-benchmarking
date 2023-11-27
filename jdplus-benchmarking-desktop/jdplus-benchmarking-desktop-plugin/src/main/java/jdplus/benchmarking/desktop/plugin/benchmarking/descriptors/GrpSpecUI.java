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
package jdplus.benchmarking.desktop.plugin.benchmarking.descriptors;

import jdplus.benchmarking.base.api.benchmarking.univariate.GrpSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class GrpSpecUI implements IObjectDescriptor<GrpSpec> {
    
    public static final String GRP = "Grp";

    @Override
    public String toString() {
        return GRP;
    }

    GrpSpec core;

    public GrpSpecUI(GrpSpec spec) {
        core = spec;
    }

    @Override
    public GrpSpec getCore() {
        return core;
    }

    public Utility.AggregationType getType() {
        return Utility.convert(core.getAggregationType());
    }

    public void setType(Utility.AggregationType type) {
        core=core.toBuilder().aggregationType(Utility.convert(type)).build();
    }

    public double getPrecision() {
        return core.getPrecision();
    }

    public void setPrecision(double p) {
        core=core.toBuilder().precision(p).build();
    }

    public boolean isDenton() {
        return core.isDentonInitialization();
    }

    public void setDenton(boolean denton) {
        core=core.toBuilder().dentonInitialization(denton).build();
    }

    public int getMaxIter() {
        return core.getMaxIter();
    }

    public void setMaxIter(int niter) {
        core=core.toBuilder().maxIter(niter).build();
    }

//    public int getFrequency() {
//        return core.getDefaultPeriod();
//    }
//
//    public void setFrequency(int period) {
//        core=core.toBuilder().defaultPeriod(period).build();
//    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        List<EnhancedPropertyDescriptor> props = new ArrayList<>();
        EnhancedPropertyDescriptor desc = typeDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = dentonDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = precisionDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = iterDesc();
        if (desc != null) {
            props.add(desc);
        }
//        desc = freqDesc();
//        if (desc != null) {
//            props.add(desc);
//        }
        return props;
    }

    private EnhancedPropertyDescriptor typeDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Type", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TYPE_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(TYPE_NAME);
            desc.setShortDescription(TYPE_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor precisionDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Precision", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PRECISION_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(PRECISION_NAME);
            desc.setShortDescription(PRECISION_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor iterDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("MaxIter", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ITER_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(ITER_NAME);
            desc.setShortDescription(ITER_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor dentonDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Denton", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, INIT_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(INIT_NAME);
            desc.setShortDescription(INIT_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    private EnhancedPropertyDescriptor freqDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("Frequency", this.getClass());
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DEFFREQ_ID);
//            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
//            desc.setDisplayName(DEFFREQ_NAME);
//            desc.setShortDescription(DEFFREQ_DESC);
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }

    @Override
    public String getDisplayName() {
        return GRP; //To change body of generated methods, choose Tools | Templates.
    }

    private static final int TYPE_ID = 0, PRECISION_ID = 10, ITER_ID = 20, INIT_ID = 30;
    private static final String TYPE_NAME = "Type", PRECISION_NAME = "Precision",
            ITER_NAME = "iterations", INIT_NAME = "Denton initialization",
            TYPE_DESC = "Type", PRECISION_DESC = "Precision",
            ITER_DESC = "Max number of iterations", INIT_DESC = "Denton initialisation";
}
