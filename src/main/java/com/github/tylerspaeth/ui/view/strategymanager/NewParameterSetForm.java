package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.ParameterSetController;
import com.github.tylerspaeth.ui.view.common.AbstractFormView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

import java.util.ArrayList;
import java.util.List;

/**
 * Form for creating a new parameter set.
 */
public class NewParameterSetForm extends AbstractFormView {

    private final ParameterSetController parameterSetController;

    private Strategy strategy;

    public NewParameterSetForm(AbstractView parent) {
        super(parent);
        parameterSetController = new ParameterSetController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText("Create new parameter set:\n");

        List<String> labels = new ArrayList<>();
        labels.add("Name: ");
        labels.add("Description: ");
        List<String> formFields = new ArrayList<>(List.of("", ""));
        setFormFields(labels, formFields);

        setSubmissionCallback(this::submitForm);
    }

    /**
     * Callback for form submission.
     * @param formFields The filled out fields of the form.
     * @return AbstractView that should be displayed next.
     */
    public AbstractView submitForm(List<String> formFields) {

        if(formFields.getFirst() == null || formFields.getFirst().isBlank() || strategy == null) {
            return null;
        }

        var result = parameterSetController.createNewParameterSetForStrategy(strategy, formFields.getFirst(), formFields.getLast());

        if(!result) {
            return null;
        }

        return parent;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

}
