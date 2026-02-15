package com.github.tylerspaeth.ui.view.data;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Top level menu for the data manager.
 */
public class DataManagerMenu extends AbstractMenuView {

    public DataManagerMenu(AbstractView parent) {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText("Data Manager\n ");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        options.add("Create New Dataset");
        optionBehaviors.add(() -> new NewDatasetForm(this));

        options.add("View Existing Datasets");
        optionBehaviors.add(() -> {
            HorizontalMultiView horizontalMultiView = new HorizontalMultiView(this);
            DatasetsMenu datasetsMenu = new DatasetsMenu(horizontalMultiView);
            DatasetDetailView datasetDetailView = new DatasetDetailView(horizontalMultiView);
            horizontalMultiView.setViews(List.of(datasetsMenu, datasetDetailView));
            return horizontalMultiView;
        });

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }
}
