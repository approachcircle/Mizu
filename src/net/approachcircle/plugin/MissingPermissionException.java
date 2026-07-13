package net.approachcircle.plugin;

public class MissingPermissionException extends Exception {
    private final String permission;

    public MissingPermissionException(String message, String permission) {
        super(message);
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
