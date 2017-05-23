package apilayer.route;

import apilayer.Constants;
import apilayer.StaticFileTemplateHandlerImpl;
import apilayer.handlers.Paths;
import apilayer.handlers.adminhandlers.AdminPlaceHandler;
import apilayer.handlers.adminhandlers.AdminSthlmHandler;
import apilayer.handlers.adminhandlers.AdminStockholmAsyncHandler;
import apilayer.handlers.adminhandlers.DashboardHandler;
import lombok.extern.slf4j.Slf4j;
import model.User;
import spark.template.velocity.VelocityTemplateEngine;


import static spark.Spark.*;

@Slf4j
public class AdminRoutes {

    public static void initAdminRoutes() {
        path(Paths.ADMIN.ADMIN, () ->{
            before("/*", (request, response) -> {
                if (!((User) request.session().attribute((Constants.USER_SESSION_KEY))).isAdmin()) {
                    throw halt(500);
                }
            });

            get(Paths.ADMIN.ADMIN_INDEX, new DashboardHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());

            get(Paths.ADMIN.ADMIN_PLACE, new AdminPlaceHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
            get(Paths.ADMIN.ADMIN_STHLM, new AdminSthlmHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());

            post(Paths.ADMIN.RUN_GET_API, AdminStockholmAsyncHandler::reloadAPI);
            get(Paths.ADMIN.GET_RUNNER_STATUS, AdminStockholmAsyncHandler::getStatusOfRunner);

        });
    }

}
