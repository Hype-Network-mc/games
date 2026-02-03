package dev.emortal.minestom.lobby.util;

public enum CustomModels {

    // https://github.com/emortalmc/Resourcepack/blob/master/pack/assets/minecraft/models/item/phantom_membrane.json
    TRUMPET("emortalmc:trumpet"),
    CUBE("emortalmc:emortalbox"),
    EMORTAL_BIRTHDAY_HAT("emortalmc:birthdayhat"),
    PUNCHER("emortalmc:blocksumo/puncher"),
    STREET_SIGN("emortalmc:streetsign"),
    EMOTES("emortalmc:emotes"),
    GUNGUN("emortalmc:lazertag/gungun");

    private final String modelId;
    CustomModels(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }
}
