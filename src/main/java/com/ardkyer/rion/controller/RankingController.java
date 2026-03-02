package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.service.UserService;
import com.ardkyer.rion.service.VideoService;
import com.ardkyer.rion.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RankingController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private FollowService followService;

    @GetMapping("/ranking")
    public String showRanking(Model model, Authentication authentication) {
        List<User> topUsers = userService.getTopUsersByFollowerCount(10);

        User currentUser = null;
        if (authentication != null) {
            currentUser = userService.findByUsername(authentication.getName());
        }

        // 배치 쿼리: 모든 top user의 최근 비디오를 한 번에 조회 (N+1 방지)
        List<Video> allRecentVideos = videoService.getRecentVideosByUsers(topUsers, 5);
        Map<Long, List<Video>> videosByUserId = allRecentVideos.stream()
                .collect(Collectors.groupingBy(v -> v.getUser().getId()));

        // 배치 쿼리: 팔로우 상태를 한 번에 조회 (N+1 방지)
        Set<Long> followedIds = Collections.emptySet();
        if (currentUser != null) {
            followedIds = followService.getFollowedUserIds(currentUser, topUsers);
        }

        for (User user : topUsers) {
            if (user.getAvatarUrl() == null) {
                user.setAvatarUrl("/default-avatar.png");
            }
            user.setRecentVideos(videosByUserId.getOrDefault(user.getId(), Collections.emptyList()));
            user.setFollowedByCurrentUser(followedIds.contains(user.getId()));
        }

        model.addAttribute("topUsers", topUsers);
        model.addAttribute("currentUser", currentUser);
        return "ranking";
    }
}
