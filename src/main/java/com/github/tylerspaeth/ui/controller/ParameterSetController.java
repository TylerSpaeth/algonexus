package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.StrategyParameterDAO;
import com.github.tylerspaeth.common.data.entity.StrategyParameter;

/**
 * Controller for ParameterSetMenu and ParameterUpdateView
 */
public class ParameterSetController {

    private final StrategyParameterDAO strategyParameterDAO;

    public ParameterSetController() {
        strategyParameterDAO = new StrategyParameterDAO();
    }

    public void updateStrategyParameter(StrategyParameter strategyParameter) {
        strategyParameterDAO.update(strategyParameter);
    }

}
