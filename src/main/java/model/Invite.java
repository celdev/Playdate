package model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringEscapeUtils;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Expose
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @Expose
    private Playdate playdate;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @Expose
    private User invited;

    public Invite(Playdate playdate, User invited) {
        this.playdate = playdate;
        this.invited = invited;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;


        Invite invite = (Invite) o;


        if (id != null && invite.id != null) {
            return id.equals(invite.id);
        }

        if (id != null ? !id.equals(invite.id) : invite.id != null) return false;
        if (!playdate.equals(invite.playdate)) return false;
        return invited.equals(invite.invited);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + playdate.hashCode();
        result = 31 * result + invited.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Invite{" +
                "id=" + id +
                ", invited=" + invited +
                '}';
    }
}
