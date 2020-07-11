package com.connect.api.service;

import com.connect.api.dto.entity.Comment;
import com.connect.api.dto.entity.User;
import com.connect.api.dto.request.CommentRequest;
import com.connect.api.dto.request.CreateUserRequest;
import com.connect.api.dto.response.*;
import com.connect.api.repository.UserRepository;
import com.connect.api.service.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class UserService {

    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    private final UserRepository userRepository;

    private final GameService gameService;

    private final EventService eventService;

    public UserService(UserRepository userRepository, GameService gameService, EventService eventService) {
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.eventService = eventService;
    }

    public LoginResponse addNewUser(CreateUserRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();

        log.info("Request to create a new user with - username: {}, password: {}, email: {}", username, password, email);

        if (username == null || password == null || email == null) {
            return new LoginResponse(false, null, "All fields are mandatory");
        }

        Long userId = userRepository.getUserWithUsername(username);
        Long emailUserId = userRepository.getUserWithEmail(email);

        if (userId != null) {
            log.info("User already exists with - username: {}", username);
            return new LoginResponse(false, null, "Username is already taken");
        } else if (emailUserId != null) {
            log.info("Email already in use - email: {}", email);
            return new LoginResponse(false, null, "Email is already linked to a user");
        }

        if (!isPasswordValid(password)) {
            log.info("Password doesn't meet requirements - password: {}", password);
            return new LoginResponse(false, null, "Password must be at least 10 characters long and contain a number");
        }

        User newUser = new User(username, password, email);
        newUser.setElo(1200);
        userRepository.save(newUser);

        log.info("Creating new user with - username: {}", username);
        return new LoginResponse(true, newUser.getUserId(), null);
    }

    public LoginResponse verifyLogin(String username, String password) {

        log.info("Request to verify user login with - username: {}, password: {}", username, password);

        Long userId = userRepository.getUserWithUsername(username);

        if (userId == null) {
            log.info("No existing user for - username: {}", username);
            return new LoginResponse(false, null, "No account with specified username");
        }

        Optional<User> queryResult = userRepository.findById(userId);

        if (!queryResult.isPresent()) {
            log.info("No existing user for - username: {}", username);
            return new LoginResponse(false, null, "No account with specified username");
        }

        User user = queryResult.get();

        boolean passwordMatches = EncryptUtils.checkPassword(password, user.getPassword());
        log.info("Password matches = [{}] for - username: {}", passwordMatches, username);

        if (user.getLockOutTimestamp() != null) {
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            int lockOutTime = getLockOutTime(user.getConsecutiveLockOuts());
            double milliDiff = (double)(Math.abs(currentTimestamp.getTime() - user.getLockOutTimestamp().getTime()));
            double lockOutMinutesRemaining = lockOutTime - (milliDiff / (1000.0 * 60.0));
            if (lockOutMinutesRemaining > 0) {
                String errorMessage = String.format("Account is locked out. Try again in %d minutes", (int) Math.ceil(lockOutMinutesRemaining));
                return new LoginResponse(false, null, errorMessage);
            } else {
                user.setLockOutTimestamp(null);
                user.setRetryCount(0);
            }
        }

        if (!passwordMatches) {
            int retryCount = user.getRetryCount();
            log.info("Retry Count: {} - username: {}", retryCount, username);
            if (retryCount >= 2) {
                int consecutiveLockOuts = user.getConsecutiveLockOuts() + 1;
                user.setLockOutTimestamp(new Timestamp(System.currentTimeMillis()));
                user.setRetryCount(0);
                user.setConsecutiveLockOuts(consecutiveLockOuts);
                String errorMessage = String.format("Account is now locked. Try again in %d minutes", getLockOutTime(consecutiveLockOuts));
                return new LoginResponse(false, null, errorMessage);
            } else {
                user.setRetryCount(retryCount + 1);
            }
        } else {
            user.setConsecutiveLockOuts(0);
            user.setRetryCount(0);
        }

        return new LoginResponse(passwordMatches, user.getUserId(), passwordMatches ? null : "Incorrect username and password combination");
    }

    private int getLockOutTime(int consecutiveLockOuts) {
        return (int) Math.ceil(Math.exp(consecutiveLockOuts));
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.length() >= 10 && password.matches(".*\\d+.*");
    }

    public UserProfileResponse getUserProfile(Long userId) {
        Optional<User> queryResult = userRepository.findById(userId);

        UserProfileResponse profile = new UserProfileResponse();

        if (!queryResult.isPresent()) {
            profile.setSuccess(false);
            profile.setErrorMessage("No user with specified userId");
            return profile;
        }

        User user = queryResult.get();

        List<PastGamesResponse> pastGames = new ArrayList<>();
        profile.setPastGameCount(gameService.getPastGameCount(user));
        profile.setIsOnline(eventService.isUserOnline(userId));
        /*
        int gameCount = 0;
        for (Game game: user.getAllGames()) {

            if (gameCount > 10) {
                break;
            }

            gameCount++;
            PastGamesResponse pastGame = new PastGamesResponse();
            List<List<List<Integer>>> gameHistory = new ArrayList<>();
            for (State state: game.getStateHistory()) {
                gameHistory.add(BoardUtils.fromBoardString(state.getState()));
            }

            pastGame.setOutcome(null);
            pastGame.setGameId(game.getGameId());

            pastGame.setGameHistory(gameHistory);

            User opponent = Objects.equals(game.getPlayerOne().getUserId(), user.getUserId()) ?
                    game.getPlayerTwo() : game.getPlayerOne();

            if (opponent != null) {
                pastGame.setOpponent(opponent.getUsername());
                pastGame.setOpponentElo(opponent.getElo());

                if (game.getOutcome() != null) {
                    pastGame.setOutcome(true);
                    if ((opponent == game.getPlayerOne() && game.getOutcome() == 1) || (opponent == game.getPlayerTwo() && game.getOutcome() == 2) ) {
                        pastGame.setOutcome(false);
                    }
                }

            }

            pastGames.add(pastGame);
        }
        */


        List<CommentResponse> comments = new ArrayList<>();
        for (Comment comment: user.getProfileComments()) {
            CommentResponse commentResponse = new CommentResponse();
            commentResponse.setComment(comment.getComment());
            commentResponse.setCommentor(comment.getCommentor().getUsername());
            commentResponse.setDate(formatter.format(comment.getDate()));
            comments.add(commentResponse);
        }
        profile.setComments(comments);

        log.info("Successfully created profile response for userId: {}", user.getUserId());

        profile.setElo(user.getElo());
        profile.setEmail(user.getEmail());
        profile.setUsername(user.getUsername());
        profile.setPastGames(pastGames);
        profile.setSuccess(true);
        profile.setIsVerified(user.isVerifiedEmail());
        profile.setUserId(userId);
        profile.setImage(user.getImage());
        return profile;
    }

    public void saveProfileImage(Long userId, MultipartFile file) throws IOException {

        Optional<User> queryResult = userRepository.findById(userId);

        if (!queryResult.isPresent()) {
            return;
        }

        User user = queryResult.get();
        user.setImage(file.getBytes());
        userRepository.save(user);
    }

    public String getUsername(Long userId) {
        return userRepository.getUsername(userId);
    }

    public String getEmail(Long userId) {
        return userRepository.getEmail(userId);
    }

    public void addComment(CommentRequest commentRequest) {

        Optional<User> userResult = userRepository.findById(commentRequest.getUserId());
        Optional<User> commentorResult = userRepository.findById(commentRequest.getCommentorId());

        log.info("{}, {}", userResult, commentorResult);
        if (userResult.isPresent() && commentorResult.isPresent()) {
            User user = userResult.get();
            User commentor = commentorResult.get();

            Comment comment = new Comment();
            comment.setComment(commentRequest.getComment());
            comment.setCommentor(commentor);
            comment.setUser(user);

            user.addProfileComment(comment);

            userRepository.save(user);
            Comment savedComment = user.getProfileComments().get(user.getProfileComments().size() - 1);

            commentor.addPostedComment(savedComment);
            userRepository.save(commentor);

        }
    }

    public Long getUserIdFromUsername(String username) {
        return userRepository.getUserWithUsername(username);
    }

    public UpdateDetailsResponse updatePassword(Long userId, String password) {

        if (!isPasswordValid(password)) {
            return new UpdateDetailsResponse(false, "Password must be at least 10 characters long and contain a number");
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return new UpdateDetailsResponse(false, "Invalid user");
        }

        user.setPassword(password);
        userRepository.save(user);

        return new UpdateDetailsResponse(true, "Successfully updated password");
    }

    public UpdateDetailsResponse updateEmail(Long userId, String email) {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return new UpdateDetailsResponse(false, "Invalid user");
        }

        user.setEmail(email);
        user.setVerifiedEmail(false);
        userRepository.save(user);

        return new UpdateDetailsResponse(true, "Successfully updated Email");
    }

    public UpdateDetailsResponse verifyEmail(Long userId) {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return new UpdateDetailsResponse(false, "Invalid user");
        }

        user.setVerifiedEmail(true);
        userRepository.save(user);

        return new UpdateDetailsResponse(true, "Successfully verified email");
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user with userId: {}", userId);
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }

    public boolean verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && EncryptUtils.checkPassword(password, user.getPassword());
    }

}
