package org.cs1331.gitsubmitter;

import java.util.Base64;
import java.util.Scanner;
import javax.security.auth.login.FailedLoginException;

public class AuthenticatedUser {

    public final String name;
    public final String base64Auth;
    public final String twoFactorCode;

    private AuthenticatedUser(String userName,
                              String base64Auth,
                              String twoFactorCode) {
        this.name = userName;
        this.base64Auth = base64Auth;
        this.twoFactorCode = twoFactorCode;
    }

    /**
     * @todo: dependency on user-interaction method should be injected
     */
    public static AuthenticatedUser create()
            throws Exception {
        AuthenticatedUser authUser = null;
        String user = "", password = "", base64Auth = "";
        String lines = "------------------------------------------";
        System.out.println();
        System.out.println(lines);
        System.out.println("    Logging in to Georgia Tech servers    ");
        System.out.println(lines);

        boolean success = true;

        do {
            if (!success) {
                System.out.println(lines);
                System.out.println("      Login failed, please try again");
                System.out.println(lines);
            }

            System.out.print("\tUsername: ");
            System.out.flush();
            user = System.console().readLine();
            System.out.print("\tPassword: ");
            System.out.flush();
            password = new String(System.console().readPassword());
            try {
                authUser = create(user, password);
                success = true;
            } catch (FailedLoginException e) {
                success = false;
            }
        } while (!success);
        return authUser;
    }

    public static AuthenticatedUser create(String user, String password) throws Exception {
        boolean success = false;
        String twoFactorCode = null;
        String base64Auth = Base64.getEncoder().encodeToString((user + ":" + password)
            .getBytes());

        try {
            success = Utils.testAuth(base64Auth);
        } catch (TwoFactorAuthException ex) {
            System.out.println("\tTwo-Factor Code:");
            twoFactorCode = new Scanner(System.in).nextLine();
            success = Utils.testTwoFactorAuth(base64Auth, twoFactorCode);
        }

        if (!success) {
            throw new FailedLoginException();
        }

        System.out.println("------------------------------------------");
        System.out.println("            Authenticated!");
        System.out.println("------------------------------------------");

        return new AuthenticatedUser(user, base64Auth, twoFactorCode);
    }



}
