package com.github.tylerspaeth.ui.view.signin;

import com.github.tylerspaeth.common.data.dao.UserDAO;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.common.enums.AccountTypeEnum;
import com.github.tylerspaeth.engine.request.IBConnectionRequest;
import com.github.tylerspaeth.engine.request.account.AccountSummaryRequest;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;
import com.github.tylerspaeth.ui.view.MainMenuView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SignInMenu extends AbstractMenuView {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignInMenu.class);

    public SignInMenu(AbstractView parent) {
        super(parent);

        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.findUsersByAccountType(AccountTypeEnum.INTERNAL);

        List<String> accountOptions = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();
        users.forEach(user -> {
            accountOptions.add(user.getExternalAccountID());
            optionBehaviors.add(() -> {
                LOGGER.info("User {} selected", user.getExternalAccountID());
                uiContext.activeUser = user;
                return new MainMenuView(this);
            });
        });

        // Log in to whatever IB Account is being used in IB Gateway
        accountOptions.add("IB Account");
        optionBehaviors.add(() -> {
            try {
                uiContext.engineCoordinator.submitRequest(new IBConnectionRequest());
                String accountID = uiContext.engineCoordinator.submitRequest(new AccountSummaryRequest()).accountID();
                User user = userDAO.findUserByExternalAccountID(accountID);
                if(user == null) {
                    user = new User();
                    user.setExternalAccountID(accountID);
                    user = userDAO.update(user);
                }
                uiContext.activeUser = user;
            } catch (Exception e) {
                LOGGER.error("Failed to connect to IB Account.");
            }
            return new MainMenuView(this);
        });
        setTopText("Select Account to Use:");
        setOptions(accountOptions, optionBehaviors);
    }
}
