public abstract class PowerUp {
    private String effect;
    private float duration;

    public PowerUp(String effect, float duration) {
        this.effect = effect;
        this.duration = duration;
    }
    public abstract void applyEffect();
}
