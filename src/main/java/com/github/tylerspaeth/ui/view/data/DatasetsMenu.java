package com.github.tylerspaeth.ui.view.data;

import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.DataManagerController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu to select a dataset to view in DatasetDetailView
 */
public class DatasetsMenu extends AbstractMenuView {

    private final DataManagerController dataManagerController;

    private final DatasetDetailView datasetDetailView;

    public DatasetsMenu(DatasetDetailView datasetDetailView) {
        dataManagerController = new DataManagerController();
        this.datasetDetailView = datasetDetailView;
    }

    @Override
    public void onEnter(UIContext uiContext) {

        setTopText("Select a dataset:\n ");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(HistoricalDataset historicalDataset : dataManagerController.getAllHistoricalDatasets()) {
            options.add(historicalDataset.toString());
            optionBehaviors.add(() -> {
                datasetDetailView.setHistoricalDataset(historicalDataset);
                return null;
            });
        }

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }

}
