package dbtest;

import dblayer.*;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.Test;
import testhelpers.HibernateTests;
import testutils.ModelCreators;
import utils.filters.TimeFilterable;

import static testutils.ModelCreators.*;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@Slf4j
public class PlaydateDAOTest extends HibernateTests {


    @Test
    public void testSavePlaydate() {
        User user = ModelCreators.createUser();
        Place place = ModelCreators.createPlace();
        Playdate playdate = ModelCreators.createPlaydate(user, place);
        boolean b = UserDAO.getInstance().saveUserOnLogin(user).isPresent();
        assertTrue(b);

        boolean b1 = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b1);

        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertTrue(playdateOptional.isPresent());
        assertNotNull(playdate.getId());

        remove(user);
        remove(place);
        remove(playdate);
    }

    @Test
    public void testSaveIllegalPlaydate() {
        User user = ModelCreators.createUser();
        Place place = ModelCreators.createPlace();
        Playdate playdate = ModelCreators.createPlaydate(user, place);
        playdate.setHeader(null);
        boolean b = UserDAO.getInstance().saveUserOnLogin(user).isPresent();
        assertTrue(b);

        boolean b1 = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b1);



        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertFalse(playdateOptional.isPresent());

        boolean b2 = PlaydateDAO.getInstance().deletePlaydate(playdate);
        assertFalse(b2);
        remove(user);
        remove(place);
    }


    @Test
    public void testDeletePlaydateWithInvite() {
        User user = createUser();
        User invited = createUser();
        Place place = createPlace();
        Playdate playdate = createPlaydate(user, place);
        save(user);
        save(invited);
        save(place);
        save(playdate);

        Invite invite = new Invite(playdate, invited);
        boolean b = InviteDAO.getInstance().addInviteToUserAndPlaydate(invite);
        assertTrue(b);

        try (Session session = HibernateUtil.getInstance().openSession()) {
            session.beginTransaction();
            long rows = session.createQuery("FROM Invite WHERE playdate = :playdate AND invited = :invited", Invite.class)
                    .setParameter("invited", invited).setParameter("playdate", playdate).list().size();
            session.getTransaction().commit();
            assertTrue(rows == 1);
        }

        PlaydateDAO.getInstance().deletePlaydate(playdate);

        try (Session session = HibernateUtil.getInstance().openSession()) {
            session.beginTransaction();
            long rows = session.createQuery("FROM Invite WHERE playdate = :playdate AND invited = :invited", Invite.class)
                    .setParameter("invited", invited).setParameter("playdate", playdate).list().size();
            session.getTransaction().commit();
            assertTrue(rows == 0);
        }
        log.info(user.toString());
        log.info(invite.toString());
        remove(place);
        remove(invited);
        remove(user);
    }



    @Test
    public void testSavePlaydateComment(){
        Place place = createPlace();
        User user = createUser();
        Playdate playdate = createPlaydate(user, place);
        Comment comment = new Comment();
        comment.setCommenter(user);
        comment.setComment("Testcomment");

        save(user);

        boolean b = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b);

        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertTrue(playdateOptional.isPresent());

        assertTrue(PlaydateDAO.getInstance().savePlaydateComment(comment, playdate).isPresent());

        remove(user);
        remove(playdate);
        remove(place);
    }

    @Test
    public void testUpdatePlaydate() {
        User user = ModelCreators.createUser();
        Place place = ModelCreators.createPlace();
        Playdate playdate = ModelCreators.createPlaydate(user, place);
        boolean b = UserDAO.getInstance().saveUserOnLogin(user).isPresent();
        assertTrue(b);

        boolean b1 = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b1);

        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertTrue(playdateOptional.isPresent());
        assertNotNull(playdate.getId());

        playdate.setDescription("test");
        boolean b2 = PlaydateDAO.getInstance().updatePlaydate(playdate);
        assertTrue(b2);

        remove(user);
        remove(place);
        remove(playdate);
    }

    @Test
    public void testUpdatePlaydateError() {
        User user = ModelCreators.createUser();
        Place place = ModelCreators.createPlace();
        Playdate playdate = ModelCreators.createPlaydate(user, place);
        boolean b = UserDAO.getInstance().saveUserOnLogin(user).isPresent();
        assertTrue(b);

        boolean b1 = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b1);

        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertTrue(playdateOptional.isPresent());
        assertNotNull(playdate.getId());

        Long playdateId = playdate.getId();

        playdate.setHeader(null);
        boolean b2 = PlaydateDAO.getInstance().updatePlaydate(playdate);
        assertFalse(b2);

        Optional<Playdate> playdateById = PlaydateDAO.getInstance().getPlaydateById(playdateId);
        assertTrue(playdateById.isPresent());
        assertNotNull(playdateById.get().getId());

        remove(user);
        remove(place);
        remove(playdateById.get());
    }

    @Test
    public void testGetPlaydateByOwner() {
        Place place = createPlace();
        User user = createUser();
        Playdate playdate = createPlaydate(user, place);

        boolean b = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b);
        assertNotNull(place.getId());


        boolean b1 = UserDAO.getInstance().saveUserOnLogin(user).isPresent();
        assertTrue(b1);
        assertNotNull(user.getId());

        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertTrue(playdateOptional.isPresent());
        assertNotNull(playdate.getId());

        Optional<List<Playdate>> playdateByOwnerId = PlaydateDAO.getInstance().getPlaydateByOwnerId(user.getId(), TimeFilterable.TimeFilter.ALL);
        assertTrue(playdateByOwnerId.isPresent());
        assertEquals(1, playdateByOwnerId.get().size());
        assertEquals(playdate.getId(), playdateByOwnerId.get().get(0).getId());

        remove(user);
        remove(place);
        remove(playdate);
    }


    @Test
    public void testGetPlaydateWhoUserIsAttending() {
        User user = createUser();
        User owner = createUser();

        Place place = createPlace();
        Playdate playdate = createPlaydate(owner, place);

        boolean b = UserDAO.getInstance().saveUserOnLogin(user).isPresent();
        assertTrue(b);
        assertNotNull(user.getId());

        boolean b1 = UserDAO.getInstance().saveUserOnLogin(owner).isPresent();
        assertTrue(b1);
        assertNotNull(owner.getId());

        boolean b2 = PlaceDAO.getInstance().storeOrUpdatePlace(place);
        assertTrue(b2);
        assertNotNull(place.getId());

        Optional<Playdate> playdateOptional = PlaydateDAO.getInstance().saveNewPlaydate(playdate);
        assertTrue(playdateOptional.isPresent());
        assertNotNull(playdate.getId());

        user.attendPlaydate(playdate);
        boolean b4 = playdate.addParticipant(user);
        assertTrue(b4);

        boolean b3 = PlaydateDAO.getInstance().addAttendance(user, playdate);
        assertTrue(b3);

        assertTrue(user.getAttendingPlaydates().size() > 0);

        remove(playdate);
        remove(place);
        remove(user);
        remove(owner);
    }


    @Test
    public void addInviteToUser() {
        User user = createUser();
        User owner = createUser();

        Place place = createPlace();

        Playdate playdate = createPlaydate(owner, place);

        save(user);
        save(owner);
        save(place);
        save(playdate);
        assertNotNull(playdate.getId());
        assertNotNull(user.getId());

        Invite invite = new Invite(playdate, user);

        save(invite);
        assertNotNull(invite.getId());

        remove(invite);
        remove(playdate);

        remove(place);
        remove(user);
        remove(owner);
    }


    @Test
    public void testGetAttending() {
        User user = createUser();
        User user1 = createUser();

        Place place = createPlace();
        Playdate playdate = createPlaydate(user, place);
        Playdate playdate1 = createPlaydate(user1, place);
        save(user);
        save(user1);
        save(place);
        save(playdate);
        save(playdate1);

        boolean b = PlaydateDAO.getInstance().addAttendance(user, playdate1);
        assertTrue(b);

        Hibernate.initialize(playdate1.getParticipants());

        assertEquals(playdate1.getParticipants().size(), 1);

        Optional<List<Playdate>> playdatesAttending = PlaydateDAO.getInstance().getPlaydatesAttending(user, TimeFilterable.TimeFilter.ALL);
        assertTrue(playdatesAttending.isPresent());
        assertEquals(1, playdatesAttending.get().size());

        Optional<List<Playdate>> playdateByOwnerId = PlaydateDAO.getInstance().getPlaydateByOwnerId(user.getId(), TimeFilterable.TimeFilter.ALL);
        assertTrue(playdateByOwnerId.isPresent());
        assertEquals(1, playdateByOwnerId.get().size());

        remove(playdate);
        remove(playdate1);
        remove(user);
        remove(user1);
        remove(place);
    }

    @Test
    public void testAddAttendance(){
        User user = createUser();
        User owner = createUser();
        Place place = createPlace();
        Playdate playdate = createPlaydate(owner, place);

        save(user);
        save(owner);
        save(place);
        save(playdate);

        assertTrue(PlaydateDAO.getInstance().addAttendance(user, playdate));

        remove(playdate);
        remove(place);
        remove(user);
        remove(owner);
    }
}
