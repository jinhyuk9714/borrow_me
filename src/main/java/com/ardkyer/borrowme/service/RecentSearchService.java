package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.RecentSearch;
import com.ardkyer.borrowme.entity.User;

import java.util.List;

public interface RecentSearchService {
    List<RecentSearch> getRecentSearches(User user);
    void addOrUpdateRecentSearch(User user, String keyword);
    void deleteRecentSearch(User user, String keyword);
    void deleteAllRecentSearches(User user);
}