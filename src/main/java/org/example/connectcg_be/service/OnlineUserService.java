package org.example.connectcg_be.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    // Using a concurrent set to store online user IDs
    private final Set<Integer> onlineUsers = ConcurrentHashMap.newKeySet();

    public void addUser(Integer userId) {
        onlineUsers.add(userId);
    }

    public void removeUser(Integer userId) {
        onlineUsers.remove(userId);
    }

    public boolean isUserOnline(Integer userId) {
        return onlineUsers.contains(userId);
    }

    public Set<Integer> getOnlineUsers() {
        return onlineUsers;
    }
}
