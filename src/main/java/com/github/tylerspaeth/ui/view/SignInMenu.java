package com.github.tylerspaeth.ui.view;

import com.github.tylerspaeth.common.data.dao.UserDAO;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.common.enums.AccountTypeEnum;
import com.github.tylerspaeth.engine.request.IBConnectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SignInMenu extends AbstractMenuView {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignInMenu.class);

    public SignInMenu() {

        UserDAO userDAO = new UserDAO();
        List<String> accountOptions = userDAO.findUsersByAccountType(AccountTypeEnum.INTERNAL).stream().map(User::getExternalAccountID).collect(Collectors.toList());

        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();
        accountOptions.forEach(accountOption -> {
            optionBehaviors.add(() -> {
                LOGGER.info("User {} selected", accountOption);
                return null;
            });
        });

        // Log in to whatever IB Account is being used in IB Gateway
        accountOptions.add("IB Account");
        optionBehaviors.add(() -> {
            try {
                uiContext.engineCoordinator.submitRequest(new IBConnectionRequest());
            } catch (Exception e) {
                LOGGER.error("Failed to connect to IB Account.");
            }
            return null;
        });
        setTopText("Select Account to Use:");
        setOptions(accountOptions, optionBehaviors);
    }
}
