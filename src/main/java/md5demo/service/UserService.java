package md5demo.service;

import md5demo.model.User;

public interface UserService {
    User register(User user);
    User getUser(String username);
}
