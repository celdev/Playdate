package dblayer;

import apilayer.Constants;
import lombok.extern.slf4j.Slf4j;
import model.Comment;
import model.Place;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import secrets.Secrets;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class PlaceDAO {

    private static PlaceDAO instance;

    private PlaceDAO() {

    }

    public static PlaceDAO getInstance() {
        if (instance == null) {
            instance = new PlaceDAO();
        }
        return instance;
    }


    /** Returnerar en plats (om den finns) med stockholm-api ID:t som
     *  skickas in som parameter
     * */
    public Optional<Place> getPlaceBySthlmId(String sthlmId) {
        try (Session session = HibernateUtil.getInstance().openSession()) {
            return session.createQuery("FROM Place WHERE sthlmAPIid = :sthlmAPIid", Place.class)
                    .setParameter("sthlmAPIid", sthlmId)
                    .uniqueResultOptional();
        }
    }

    /** Returnerar en lista med alla platser för alla de ids som skickas in
     *  som parameter
     * */
    public Optional<List<Place>> getPlaceByMultiSthlmId(Set<String> ids) {
        try (Session session = HibernateUtil.getInstance().openSession()) {
            return Optional.of(session.createQuery("FROM Place WHERE sthlmAPIid in (:ids)", Place.class)
                    .setParameter("ids", ids)
                    .list());
        }
    }

    /** Returnerar results antal platser som har GeographicalArea
     *  som skickas in som parameter
     *  med start som offset
     *  Listan wrappas i en PaginationWrapper så att front-end
     *  vet vilket offset resultatet har
     * */
    public Optional<PaginationWrapper<Place>> getPlacesByGeoArea(String geoArea, int start, int results) {
        Session session = HibernateUtil.getInstance().openSession();
        Query<Place> placeQuery = session.
                createQuery("FROM Place WHERE geoArea LIKE :geoarea", Place.class);
        placeQuery.setParameter("geoarea", "%" + geoArea + "%")
                .setMaxResults(results).setFirstResult(start);
        List<Place> resultList = placeQuery.getResultList();
        return Optional.of(resultList.size() != 0 ? new PaginationWrapper<>(resultList, start) : new PaginationWrapper.PaginationWrapperForNull<Place>());
    }

    /** Returnerar alla platser vars namn innehåller
     *  @param name
     *  @param results  antal platser att returnera (max)
     *  @param start    vilket offset för resultatet
     * */
    public Optional<PaginationWrapper<Place>> getPlacesByName(String name, int start, int results) {
        Session session = HibernateUtil.getInstance().openSession();

        Query<Place> placeQuery = session.
                createQuery("FROM Place WHERE name LIKE :name", Place.class);
        placeQuery.setParameter("name", "%" + name + "%")
                .setMaxResults(results).setFirstResult(start);
        List<Place> resultList = placeQuery.getResultList();
        return Optional.of(resultList.size() != 0 ? new PaginationWrapper<>(resultList, start) : new PaginationWrapper.PaginationWrapperForNull<Place>());
    }



    /** Returnerar alla platser som ligger inom området
     *        _______
     *       |       |
     *       |   x   |
     *       |_______|
     *
     * där x är grid koordinaten med
     * @param locX
     * och
     * @param locY
     *
     * och avståndet från x till en vägg är definerad i Constants.GRID_SEARCH_AREA_SIZE
     *
     * */
    public List<Place> getPlaceByLocation(int locX, int locY) {
        try(Session session = HibernateUtil.getInstance().openSession()) {
            return session.createQuery("FROM Place WHERE geoX <= :xMax AND geoX >= :xMin AND geoY <= :yMax AND geoY >= :yMin", Place.class)
                    .setParameter("xMax", locX + Constants.GRID_SEARCH_AREA_SIZE)
                    .setParameter("xMin", locX - Constants.GRID_SEARCH_AREA_SIZE)
                    .setParameter("yMax", locY + Constants.GRID_SEARCH_AREA_SIZE)
                    .setParameter("yMin", locY - Constants.GRID_SEARCH_AREA_SIZE)
                    .list();
        }
    }

    /** Returnerar platsen med
     * @param placeId som skickas in
     *  (om den finns)
     * */
    public Optional<Place> getPlaceById(Long placeId) {
        try (Session session = HibernateUtil.getInstance().openSession()) {
            return session.byId(Place.class).loadOptional(placeId);
        }
    }

    /**
     * Försöker ta bort en plats med
     *
     * @param placeId om den finns och kunde tas bort så returneras true
     *                annars false
     */

    public boolean deletePlaceById(Long placeId) {

        Session session = null;
        Transaction tx = null;
        boolean ret = false;
        try {
            session = HibernateUtil.getInstance().openSession();
            tx = session.beginTransaction();
            Optional<Place> placeOptional = session.byId(Place.class).loadOptional(placeId);
            if (placeOptional.isPresent()) {
                session.remove(placeOptional.get());
            }
            tx.commit();
            ret = true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            ret = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return ret;
    }

    /** Sparar eller uppdaterar en plats i databasen
     *  Returnerar true om den kunde uppdateras/sparas
     *  false om det inte gick
     * */
    public boolean storeOrUpdatePlace(Place place) {
        Session session = null;
        Transaction tx = null;
        boolean ret = false;
        try {
            session = HibernateUtil.getInstance().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(place);
            tx.commit();
            ret = true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return ret;
    }

    /** Sparar en kommentar i databasen
     *  uppdaterar Place som kommentaren gäller
     * */

    @SuppressWarnings("Duplicates")
    public Optional<Set<Comment>> saveComment(Comment comment, Place place) {
        Set<Comment> ret = null;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getInstance().openSession();
            tx = session.beginTransaction();
            Hibernate.initialize(place.getComments());
            place.addComment(comment);
            session.save(comment);
            session.update(place);
            tx.commit();
            ret = place.getComments();
        } catch (Exception e) {
            log.error("error save comment", e);
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return Optional.ofNullable(ret);
    }

    public Optional<Set<Comment>> getCommentsForPlace(Place place) {
        try (Session session = HibernateUtil.getInstance().openSession()) {
            Hibernate.initialize(place.getComments());
            return Optional.ofNullable(place.getComments());
        }
    }
}
