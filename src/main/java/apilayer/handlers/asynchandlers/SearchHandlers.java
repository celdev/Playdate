package apilayer.handlers.asynchandlers;

import com.google.gson.Gson;
import dblayer.SearchDAO;
import lombok.extern.slf4j.Slf4j;
import model.Place;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class SearchHandlers {


    public static Object searchPlaces(Request request, Response response) {
        String term = request.queryParams("searchTerm");
        if (term.length() < 3) {
            return "";
        }
        try {
            List<Place> placeByTermThroughCache = SearchDAO.getInstance().getPlaceByTermThroughCache(term);
            placeByTermThroughCache.forEach(place -> place.setComments(null));
            return new Gson().toJson(placeByTermThroughCache);
        } catch (ExecutionException e) {
            log.error("error searching by term through cache", e);
            response.status(500);
            return "";
        }
    }


}