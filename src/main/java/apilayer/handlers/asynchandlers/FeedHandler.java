package apilayer.handlers.asynchandlers;

import com.google.gson.Gson;
import dblayer.InviteDAO;
import dblayer.PlaceDAO;
import dblayer.PlaydateDAO;
import lombok.extern.slf4j.Slf4j;
import model.Invite;
import model.Place;
import model.Playdate;
import model.User;
import presentable.FeedObject;
import spark.Request;
import spark.Response;
import utils.filters.TimeFilterable;

import java.util.*;
import java.util.stream.Collectors;

import static apilayer.handlers.asynchandlers.SparkHelper.*;

@Slf4j
public class FeedHandler {

    public static Object handleGetFeed(Request request, Response response) {
        Long start = System.currentTimeMillis();
        User user = getUserFromSession(request);
        int[] grid = getGridLocationFromRequest(request);
        long queryStart = System.currentTimeMillis();
        Optional<List<Playdate>> publicPlaydatesByLoc = PlaydateDAO.getInstance().getPublicPlaydatesByLoc(grid[0], grid[1], TimeFilterable.TimeFilter.ALL);
        log.info("Query took " + (System.currentTimeMillis() - queryStart));
        List<FeedObject> feedObjects = new ArrayList<>();
        publicPlaydatesByLoc.ifPresent(playdates -> feedObjects.addAll(playdates.stream()
                .filter(Playdate::playdateIsInFuture)
                .map(FeedObject::createFromPlayDate).collect(Collectors.toList())));
        List<Place> placeByLocation = PlaceDAO.getInstance().getPlaceByLocation(grid[0], grid[1]);
        feedObjects.addAll(placeByLocation.stream().map(FeedObject::createFromPlace).collect(Collectors.toList()));
        Collections.shuffle(feedObjects);
        Set<Playdate> allPlaydateWhoUserIsAttendingAlsoOwner = PlaydateDAO.getInstance().getAllPlaydateWhoUserIsAttendingAlsoOwner(user, TimeFilterable.TimeFilter.ALL);
        Set<FeedObject> attending = allPlaydateWhoUserIsAttendingAlsoOwner.stream()
                .filter(Playdate::playdateIsInFuture)
                .map(FeedObject::createFromPlayDate).collect(Collectors.toSet());
        attending.addAll(feedObjects);

        InviteDAO.getInstance().getInvitesOfUser(user).ifPresent(invites -> attending.addAll(invites.stream().filter(invite -> invite.getPlaydate().playdateIsInFuture())
                .map(FeedObject::createFromInvite).collect(Collectors.toSet())));


        log.info("Finished request in " + (System.currentTimeMillis() - start) + "ms");
        return new Gson().toJson(attending.stream().sorted((o1, o2) -> {
            if (o1.getObjectTypeId() == FeedObject.ObjectTypeId.INVITE) {
                if (o2.getObjectTypeId() == FeedObject.ObjectTypeId.INVITE) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return 0;
            }
        }).collect(Collectors.toList()));
    }

}
