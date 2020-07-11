package com.connect.api.dto.entity;

import com.connect.api.service.utils.EncryptUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String username;

    private String password;

    private String email;

    private Integer elo;

    @Column(name = "verified_email")
    private boolean verifiedEmail;

    @Column(name = "lock_out_timestamp")
    private Timestamp lockOutTimestamp;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "consecutive_lock_out_count")
    private Integer consecutiveLockOuts;

    private byte[] image;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})
    private List<Comment> profileComments;

    @OneToMany(mappedBy = "commentor", cascade = {CascadeType.ALL})
    private List<Comment> postedComments;

    @OneToMany(mappedBy = "playerOne")
    private List<Game> gameDataSetOne;

    @OneToMany(mappedBy = "playerTwo")
    private List<Game> gameDataSetTwo;

    @Override
    public String toString() {
        return "UserData{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                //", \ngameDataSetOne=" + gameDataSetOne +
                //", \ngameDataSetTwo=" + gameDataSetTwo +
                '}';
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public List<Game> getAllGames() {
        List<Game> gameDataSet = new ArrayList<>();
        gameDataSet.addAll(gameDataSetOne);
        gameDataSet.addAll(gameDataSetTwo);
        return gameDataSet;
    }

    public void addProfileComment(Comment comment) {
        this.profileComments.add(comment);
    }

    public void addPostedComment(Comment comment) {
        this.postedComments.add(comment);
    }

    public boolean isAI() {
        return this.userId == 9999L;
    }

    public void setPassword(String password) {
        this.password = EncryptUtils.encrypt(password);
    }

}
