package at.gastronaut.android.classes.StepCounter;

// Will listen to step alerts
public interface StepListener {
    public void step(long timeNs);
}