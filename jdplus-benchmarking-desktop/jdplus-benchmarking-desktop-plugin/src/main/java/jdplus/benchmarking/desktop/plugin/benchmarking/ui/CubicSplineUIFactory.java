/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.desktop.plugin.benchmarking.ui;

import jdplus.benchmarking.base.api.benchmarking.univariate.CubicSplineSpec;
import jdplus.benchmarking.desktop.plugin.benchmarking.descriptors.CubicSplineSpecUI;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.benchmarking.base.core.benchmarking.univariate.CubicSplineDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class CubicSplineUIFactory implements DocumentUIServices<CubicSplineSpec, CubicSplineDocument> {

    @Override
    public IProcDocumentView<CubicSplineDocument> getDocumentView(CubicSplineDocument document) {
        return CubicSplineViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<CubicSplineSpec> getSpecificationDescriptor(CubicSplineSpec spec) {
        return new CubicSplineSpecUI(spec);
    }

    @Override
    public Class<CubicSplineDocument> getDocumentType() {
        return CubicSplineDocument.class;
    }

    @Override
    public Class<CubicSplineSpec> getSpecType() {
        return CubicSplineSpec.class;
    }

    @Override
    public Color getColor() {
        return Color.BLUE;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/benchmarking/desktop/plugin/resource-monitor_16x16.png", false);
    }

    @Override
    public void showDocument(WorkspaceItem<CubicSplineDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            CubicSplineTopComponent view = new CubicSplineTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
