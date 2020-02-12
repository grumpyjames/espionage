package net.digihippo.cryptnet.api;

public final class GameObject {
    public final GameObjectIdentifier identifier;
    public final String type;

    public GameObject(GameObjectIdentifier identifier, String type) {
        this.identifier = identifier;
        this.type = type;
    }
}
