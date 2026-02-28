package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.BacktestController;
import com.github.tylerspaeth.ui.view.common.AbstractFormView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu with options for running a strategy with a given parameter set and dataset.
 */
public class ParameterSetRunForm extends AbstractFormView {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSetRunForm.class);

    private final BacktestController backtestController;

    private final StrategyParameterSet strategyParameterSet;

    public ParameterSetRunForm(AbstractView parent, StrategyParameterSet strategyParameterSet) {
        super(parent);
        this.strategyParameterSet = strategyParameterSet;
        this.backtestController = new BacktestController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        if(strategyParameterSet == null) {
            return;
        }

        setTopText("Parameter Set: " + strategyParameterSet);

        setFormFields(new ArrayList<>(List.of("Enter Starting Account Balance")), new ArrayList<>(List.of("")));
        setSubmissionCallback(this::runBacktest);
        setSubmitButtonText("Run Backtest");
    }

    /**
     * Submission callback for running the backtest.
     * @param formFields Fields the contain the starting account balance.
     * @return View to display upon submission.
     */
    private AbstractView runBacktest(List<String> formFields) {
        try {
            LOGGER.info("Running backtest for StrategyParameterSet {}", strategyParameterSet.getStrategyParameterSetID());
            backtestController.runBacktest(uiContext.engineCoordinator, uiContext.activeUser, strategyParameterSet, Float.parseFloat(formFields.getFirst()));
        } catch (Exception e) {
            LOGGER.error("Failed to run backtest for StrategyParameterSet {}", strategyParameterSet, e);
            return null;
        }
        return parent;
    }

}
