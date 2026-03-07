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

    @Override
    public void onEnter(UIContext uiContext) {
        setTopText("Data Manager\n ");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        options.add("Create New Dataset");
        optionBehaviors.add(NewDatasetForm::new);

        options.add("View Existing Datasets");
        optionBehaviors.add(() -> {
            HorizontalMultiView horizontalMultiView = new HorizontalMultiView();
            DatasetDetailView datasetDetailView = new DatasetDetailView();
            DatasetsMenu datasetsMenu = new DatasetsMenu(datasetDetailView);
            horizontalMultiView.setViews(List.of(datasetsMenu, datasetDetailView));
            return horizontalMultiView;
        });

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }
}
