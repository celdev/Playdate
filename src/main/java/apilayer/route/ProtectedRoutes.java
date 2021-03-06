package apilayer.route;

import apilayer.Constants;
import apilayer.StaticFileTemplateHandlerImpl;
import apilayer.handlers.*;
import apilayer.handlers.asynchandlers.*;
import apilayer.handlers.templateHandlers.*;
import lombok.extern.slf4j.Slf4j;
import model.User;
import secrets.Secrets;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.*;

import static spark.Spark.*;
import static spark.Spark.delete;
import static spark.Spark.get;

@Slf4j
public class ProtectedRoutes {


    /** Initierar de routes som kräver att användaren är inloggad
     *
     *  Kollar först om användaren är inloggad, om användaren inte är inloggad så
     *  Skickas användaren tillbaka till startsidan
     *  om route som leder till att användaren ska redirectas till
     *  startsidan inte är av typen som hämtar en profilbild eller en platsbild från
     *  databasen så sparas den plats användaren var på i en session
     *  när användaren sedan loggat in så hämtas innehållet från session
     *  och användaren redirectas till sidan användaren var på tidigare
     *
     * */
    public static void initProtectedRoutes() {
        path(Paths.PROTECTED, () -> {
            //Kollar att användaren är inloggad innan varje request hanteras
            before("/*", (request, response) -> {
                if (!isLoggedIn(request)) {
                    if (shouldSaveContextPath(request.pathInfo())) {
                        String fullPath = request.pathInfo() + "?" + (request.queryString() != null && !request.queryString().isEmpty() ? request.queryString() : "");
                        request.session(true).attribute(Constants.ONLOGINREDIRECT, fullPath);
                    }
                    response.redirect("/index.html");
                    throw halt(400);
                }
            });


            /*      Övriga API-endpoints
            *           *   place-bilder
            *           *   profil-bild
            *           *   feed
            * */
            get(Paths.GETFEED, FeedHandler::handleGetFeed);
            get(Paths.APIIMAGE + "/:id", ImageHandler::handleGetAPIImage);
            get(Paths.GETPROFILEPICTURE + "/:id", ImageHandler::handleGetProfilePicture);

            get(Paths.REMOVEUSER, RemoveUserHandler::handleRemoveUser);

            /*  För att injicera data till en användare och logga in som en specifik användare
            * */
            get(Paths.ADDDATA, DataFillerHandler::injectData);
            get(Paths.FORCELOGIN, DataFillerHandler::forceLoginAsUser);

            /*
            get(Paths.GETFEED, (request, response) -> {
                Optional<PaginationWrapper<Place>> norrmalm = PlaceDAO.getInstance().getPlacesByGeoArea("Norrmalm", ParserHelpers.parseToInt(request.queryParams("offset")), 10);
                PaginationWrapper<FeedObject> paginationWrapper = new PaginationWrapper<>(
                        norrmalm.get().getCollection().stream().map(FeedObject::createFromPlace).collect(Collectors.toList()),
                        norrmalm.get().getPaginationOffset());
                return new Gson().toJson(paginationWrapper);
            });
            */

            /*      Kommentar-routes
            * */
            post(Paths.POSTPLACECOMMENT, CommentsHandler::handlePostPlaceComment);
            get(Paths.COMMENTSOFPLACE, CommentsHandler::handleGetCommentsOfPlace);
            //playdate
            post(Paths.POSTPLAYDATECOMMENT, CommentsHandler::handlePostPlaydateComment);
            get(Paths.COMMENTSOFPLAYDATE, CommentsHandler::handleGetCommentsOfPlaydate);
            delete(Paths.REMOVECOMMENT, CommentsHandler::handleRemoveComment);

            /* Report-routes */
            post(Paths.POSTREPORT, ReportHandler::createUserReport);


            /*      Sök-routes
            * */
            get(Paths.SEARCH_PLACE_BY_TERM, SearchHandlers::searchPlaces);


            /*      Place-routes
            * */
            get(Paths.GETPLACEBYLOCATION, PlaceHandler::handleGetPlaceByLoc);
            get(Paths.GETPLACEBYNAME, PlaceHandler::handleGetPlaceByName);
            get(Paths.GETPLACEBYGEONAME, PlaceHandler::handleGetPlaceByGeoArea);
            get(Paths.GETONEPLACEJSON, PlaceHandler::handleGetOnePlaceWithoutComments);


            /*      Profile-routes
            * */
            post(Paths.EDITPROFILE, ProfileHandlers::handleEditProfile);
            post(Paths.POSTNEWPROFILEPICTURE, UploadHandler::handleUploadProfilePicture);



            /*      Playdate-routes
            * */
            post(Paths.UPDATEPLAYDATE, PlaydateHandler::handleUpdatePlaydate);
            put(Paths.SHOWPLAYDATES, PlaydateHandler::removePlaydateAttendance);
            delete(Paths.DELETEPLAYDATE, PlaydateHandler::handleDeletePlaydate);
            post(Paths.CREATEPLAYDATE, PlaydateHandler::handleMakePlaydate);
            get(Paths.GETPLAYDATEOFPLACE, PlaydateHandler::handleGetPublicPlaydatesOfPlace);
            get(Paths.GETPLAYDATESOFMULTIPLEPLACE, SearchHandlers::searchPublicPlaydatesByMultiPlace);
            get(Paths.GETPOTENTIALFRIENDSTOINVITE, PlaydateHandler::handleGetFriendsToInvite);
            delete(Paths.OWNERKICKATTENDINGUSER, AttendanceInviteHandler::handleKickUserFromPlaydate);

            /*      FriendshipRequest-routes
            * */
            post(Paths.ACCEPTFRIENDSHIPREQUEST, FriendsHandler::handleAcceptFriendRequest);
            post(Paths.DECLINEFRIENDSHIPREQUEST, FriendsHandler::handleDeclineFriendshipRequest);
            post(Paths.SENDFRIENDSHIPREQUEST, FriendshipHandler::addFriendRequest);
            /*      Friendship-routes
            * */
            delete(Paths.REMOVEFRIENDSHIP, FriendshipHandler::handleRemoveFriend);
            delete(Paths.REMOVEFRIENDSHIPREQUEST, FriendshipHandler::handleRemoveFriendshipRequest);
            get(Paths.GETPOTENTIALFRIENDS, FriendsHandler::getPotentialFriends);
            get(Paths.GETSENTFRIENDREQUESTS, FriendsHandler::getSentFriendRequest);

            /*      INVITE-routes
            * */
            post(Paths.ACCEPTINVITE, AttendanceInviteHandler::handleAcceptInviteToPlaydate);
            delete(Paths.DECLINEINVITE, AttendanceInviteHandler::handleDeclineInviteToPlaydate);
            post(Paths.SENDINVITE, AttendanceInviteHandler::handleSendInviteToPlaydate);
            delete(Paths.OWNERREMOVEINVITE, AttendanceInviteHandler::handleRemoveInviteToPlaydate);

            post(Paths.ACCEPTINVITEBYPLAYDATE, AttendanceInviteHandler::handleAcceptInviteByPlaydate);
            delete(Paths.DECLINEINVITEBYPLAYDATE, AttendanceInviteHandler::handleDeclineInviteByPlaydate);
            post(Paths.USERJOINPLAYDATE, AttendanceInviteHandler::handleJoinPlaydate);
            delete(Paths.USERLEAVEPLAYDATE, AttendanceInviteHandler::handleUserLeavePlaydate);


            /*      EVENT-routes
            * */
            get(Paths.GETUSERATTENDINGPLAYDATES, EventHandler::getAllPlaydatesWhoUserIsAttendingOrOwnerFuture);
            get(Paths.GETPLAYDATESWHOUSERMAYWANTTOATTEND, EventHandler::getAllPlaydatesWhoUserIsNotAttendingButCanAttendThroughFriendFuture);
            get(Paths.GETPUBLICPLAYDATESBYLOCATION, EventHandler::getPublicPlaydatesCloseToUserFuture);


            get(Paths.MAKEUSERADMIN, (Request request, Response response) -> {
                User user = request.session().attribute(Constants.USER_SESSION_KEY);
                user.setAdmin(true);
                return "";
            });

            initProtectedStaticRoutes();
            AdminRoutes.initAdminRoutes();

        });
    }

    private static boolean shouldSaveContextPath(String s) {
        return !s.contains(Paths.APIIMAGE) && !s.contains(Paths.GETPROFILEPICTURE);
    }

    private static void initProtectedStaticRoutes() {


        get(Paths.LANDING,
                new StaticFileTemplateHandlerImpl("feed.vm", 400, true)::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.EDITPROFILE,
                new StaticFileTemplateHandlerImpl("editprofile.vm", 400, true)::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.SHOWPLAYDATES,
                new GetUserPlaydateHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.CREATEPLAYDATE,
                new StaticFileTemplateHandlerImpl("createplaydate.vm", 400, true)::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.SHOWPLACE,
                new GetOnePlaceHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.SHOWUSER,
                new GetOneUserHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.EDITPLAYDATE,
                new EditPlaydateHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.GETMYFRIENDS,
                new GetMyFriendsHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.GETMYINVITES,
                new GetMyInvitesHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.GETONEPLAYDATE,
                new GetOnePlaydateHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());
        get(Paths.FINDPLACE, new StaticFileTemplateHandlerImpl("find-places.vm", 400, true){
            @Override
            public Optional<Map<String, Object>> createModelMap(Request request) {
                Map<String, Object> map = new HashMap<>();
                map.put("mapsapikey", Secrets.GOOGLE_MAPS_KEY);
                return Optional.of(map);
            }
        }::handleTemplateFileRequest, new VelocityTemplateEngine());

        get(Paths.SHOWPLAYDATESONMAP, new StaticFileTemplateHandlerImpl("find-playdates.vm",400, true)::handleTemplateFileRequest,new VelocityTemplateEngine());

        get(Paths.GETMYEVENTS, new GetMyEventsHandler()::handleTemplateFileRequest, new VelocityTemplateEngine());

    }

    /** Metoden returnerar true om användaren är inloggad
     *  loggar användarens ip på info
     * @param request request-objektet där session kan hämtas ifrån.
     * @return om användaren är inloggad
     * */
    private static boolean isLoggedIn(Request request) {
        return request.session().attribute(Constants.USER_SESSION_KEY) != null;
    }

}
