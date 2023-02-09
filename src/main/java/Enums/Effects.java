package Enums;

public enum Effects {
    IsAfterburner(1),
    InAsteroidField(2),
    InGasCloud(4),
    HasSuperfood(8),
    HasShield(16);

    public final Integer value;

    Effects(Integer value) {
        this.value = value;
    }

    public Integer getValue(){
        return value;
    }

    public static Effects valueOf(Integer value) {
        for (Effects effect : Effects.values()) {
            if (effect.value == value)
                return effect;
        }

        throw new IllegalArgumentException("Value not found");
    }
}
