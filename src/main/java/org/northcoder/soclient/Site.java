package org.northcoder.soclient;

public enum Site {

    NONE("", "-- choose site --"),
    STACK_OVERFLOW("stackoverflow", "Stack Overflow"),
    SERVER_FAULT("serverfault", "Server Fault"),
    UNIX("unix", "Unix &amp; Linux"),
    SECURITY("security", "Information Security"),
    CRYPTO("crypto", "Cryptography"),
    SOFTWARE_ENGINEERING("softwareengineering", "Software Engineering");

    public final String value;
    public final String label;

    private Site(String value, String label) {
        // must match the site URL segment,
        // e.g. "crypto" in https://crypto.stackexchange.com/
        this.value = value;
        // the site name:
        this.label = label;
    }

    // check that the string provided matchs a Site value:
    public static boolean exists(String value) {
        boolean result = false;
        for (Site site : values()) {
            if (site.value.equals(value)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
