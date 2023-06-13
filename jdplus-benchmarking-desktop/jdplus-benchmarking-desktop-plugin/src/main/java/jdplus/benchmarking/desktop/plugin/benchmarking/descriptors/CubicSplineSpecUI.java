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

import jdplus.benchmarking.base.api.benchmarking.univariate.CubicSplineSpec;
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
public class CubicSplineSpecUI implements IObjectDescriptor<CubicSplineSpec> {
    
    public static final String CUBICSPLINE = "CubicSpline";

    @Override
    public String toString() {
        return CUBICSPLINE;
    }

    CubicSplineSpec core;

    public CubicSplineSpecUI(CubicSplineSpec spec) {
        core = spec;
    }

    @Override
    public CubicSplineSpec getCore() {
        return core;
    }

    public Utility.AggregationType getType() {
        return Utility.convert(core.getAggregationType());
    }

    public void setType(Utility.AggregationType type) {
        core=core.toBuilder().aggregationType(Utility.convert(type)).build();
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        List<EnhancedPropertyDescriptor> props = new ArrayList<>();
        EnhancedPropertyDescriptor desc = typeDesc();
        if (desc != null) {
            props.add(desc);
        }
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

    @Override
    public String getDisplayName() {
        return CUBICSPLINE; //To change body of generated methods, choose Tools | Templates.
    }

    private static final int TYPE_ID = 0;
    private static final String TYPE_NAME = "Type",
            TYPE_DESC = "Type";
}
