package com.connect.api.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileResponse {

    public Long userId;

    public String username;

    public String email;

    public Integer elo;

    public Boolean isOnline;

    public Integer pastGameCount;

    public List<PastGamesResponse> pastGames;

    public Boolean success;

    public Boolean isVerified;

    public String errorMessage;

    public byte[] image;

    public List<CommentResponse> comments;

}
