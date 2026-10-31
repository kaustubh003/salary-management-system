package com.acme.salary.service;

public interface IAuthService {

    /**
     * Checks the HR manager's credentials.
     *
     * @param username the username
     * @param password the password
     * @return a session token to send with later requests.
     * @throws com.acme.salary.exception.InvalidCredentialsException if the credentials are wrong.
     */
    String login(String username, String password);

    /**
     * @param token the session token
     * @return true if the token was issued by login and has not been logged out.
     */
    boolean isValidToken(String token);

    /**
     * Ends the session. An unknown token is ignored.
     *
     * @param token the session token
     */
    void logout(String token);

}