package net.digihippo.cryptnet.server;

public enum ErrorCodes
{
    NO_SUCH_SESSION("No session with that key exists", "NSS"),
    SESSION_NOT_ESTABLISHED("A session has not been established", "SNE"),
    GAME_REQUEST_FAILED("The request for a game failed", "GRF");

    @SuppressWarnings({"FieldCanBeLocal", "unused"}) // Here for documentation, not necessarily code use
    private final String description;
    private final String threeLetterCode;

    ErrorCodes(String description, String threeLetterCode)
    {
        this.description = description;
        this.threeLetterCode = threeLetterCode;
        assert threeLetterCode.length() == 3;
    }

    public String code()
    {
        return threeLetterCode;
    }
}
