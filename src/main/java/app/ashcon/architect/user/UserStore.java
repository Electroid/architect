package app.ashcon.architect.user;

import app.ashcon.architect.model.ModelStore;

public interface UserStore extends ModelStore<User> {

    /**
     * Login to the server and fetch the {@link User} model.
     *
     * @param uuid The ID of the player logging in.
     * @param username The username of the player logging in.
     * @return The user model.
     */
    User login(String uuid, String username);

}
