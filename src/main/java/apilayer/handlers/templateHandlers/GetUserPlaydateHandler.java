package apilayer.handlers.templateHandlers;

import apilayer.Constants;
import apilayer.StaticFileTemplateHandlerImpl;
import dblayer.PlaydateDAO;
import lombok.extern.slf4j.Slf4j;
import model.Playdate;
import model.User;
import spark.Request;
import utils.filters.TimeFilterable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class GetUserPlaydateHandler extends StaticFileTemplateHandlerImpl {

    public GetUserPlaydateHandler() throws IllegalArgumentException {
        super("my-playdates.vm", 400, true);
    }

    @Override
    public Optional<Map<String, Object>> createModelMap(Request request) {
        Map<String, Object> map = new HashMap<>();
        User user = request.session().attribute(Constants.USER_SESSION_KEY);

        Optional<List<Playdate>> playdatesAttending = PlaydateDAO.getInstance().getPlaydatesAttending(user, TimeFilterable.TimeFilter.ALL);
        Optional<List<Playdate>> playdateByOwnerId = PlaydateDAO.getInstance().getPlaydateByOwnerId(user.getId(), TimeFilterable.TimeFilter.ALL);

        if (!playdateByOwnerId.isPresent() || !playdatesAttending.isPresent()) {
            return Optional.of(map);
        }
        map.put("playdatesAttending", playdatesAttending.get());
        map.put("playdatesOwner", playdateByOwnerId.get());
        return Optional.of(map);
    }
}
