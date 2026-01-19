package com.github.tylerspaeth.ui.view.data;

import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.DataManagerController;
import com.github.tylerspaeth.ui.view.common.AbstractFormView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Form to create a new dataset.
 */
public class NewDatasetForm extends AbstractFormView {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewDatasetForm.class);

    private final DataManagerController dataManagerController;

    public NewDatasetForm(AbstractView parent) {
        super(parent);
        dataManagerController = new DataManagerController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText("Create new Dataset");

        List<String> labels = new ArrayList<>();
        labels.add("Name: ");
        labels.add("Source: ");
        labels.add("Ticker Symbol: ");
        labels.add("Time Interval: ");
        labels.add("Interval Unit: ");
        labels.add("Source File Location: ");
        labels.add("Source File Metadata Rows: ");
        labels.add("Source File Column Order: ");
        labels.add("Source File Date Format: ");
        List<String> formFields = labels.stream().map(_ -> "").collect(Collectors.toList());
        setFormFields(labels, formFields);

        setSubmissionCallback(this::submitForm);
    }

    /**
     * Callback for form submission.
     * @param formFields The filled out fields of the form.
     * @return AbstractView that should be displayed next.
     */
    public AbstractView submitForm(List<String> formFields) {

        // Validate required fields
        for(int i = 0; i < formFields.size(); i++) {
            if(i == 1) continue;
            if(formFields.get(i) == null || formFields.get(i).isBlank()) {
                LOGGER.warn("Field at index {} is missing.", i);
                return null;
            }
        }

        HistoricalDataset newDataset = null;
        try {
            newDataset = dataManagerController.tryToCreateDataset(formFields.get(0), formFields.get(1), formFields.get(2),
                    Integer.parseInt(formFields.get(3)), IntervalUnitEnum.valueOf(formFields.get(4)), formFields.get(5),
                    Integer.parseInt(formFields.get(6)), formFields.get(7), formFields.get(8));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unable to create dataset as invalid fields was provided.", e);
        }

        // The new dataset will be null if a new one was not created
        if(newDataset == null) {
            return null;
        }

        return parent;
    }

}
