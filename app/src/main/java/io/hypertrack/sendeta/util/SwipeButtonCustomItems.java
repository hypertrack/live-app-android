package io.hypertrack.sendeta.util;

/**
 * Created by piyush on 25/08/16.
 */
public abstract class SwipeButtonCustomItems {
    //These are the default values if we don't choose to set them later:
    public int gradientColor1 = 0xFF58B8FC;
    public int gradientColor2 = 0xFF58B8FC;
    public int gradientColor2Width = 60;
    public int gradientColor3 = 0xFF008CEE;
    public int postConfirmationColor = 0xFF008CEE;
    public double actionConfirmDistanceFraction = 0.40;
    public double actionConfirmPercentageFraction = 0.70;
    public String buttonPressText = "SLIDE TO END TRIP >";

    public String actionConfirmText = "ENDING TRIP";

    public int getGradientColor1() {
        return gradientColor1;
    }

    public SwipeButtonCustomItems setGradientColor1(int gradientColor1) {
        this.gradientColor1 = gradientColor1;
        return this;
    }

    public int getGradientColor2() {
        return gradientColor2;
    }

    public SwipeButtonCustomItems setGradientColor2(int gradientColor2) {
        this.gradientColor2 = gradientColor2;
        return this;
    }

    public int getGradientColor2Width() {
        return gradientColor2Width;
    }

    public SwipeButtonCustomItems setGradientColor2Width(int gradientColor2Width) {
        this.gradientColor2Width = gradientColor2Width;
        return this;
    }

    public int getGradientColor3() {
        return gradientColor3;
    }

    public SwipeButtonCustomItems setGradientColor3(int gradientColor3) {
        this.gradientColor3 = gradientColor3;
        return this;
    }

    public double getActionConfirmDistanceFraction() {
        return actionConfirmDistanceFraction;
    }

    public SwipeButtonCustomItems setActionConfirmDistanceFraction(double actionConfirmDistanceFraction) {
        this.actionConfirmDistanceFraction = actionConfirmDistanceFraction;
        return this;
    }

    public double getActionConfirmPercentageFraction() {
        return actionConfirmPercentageFraction;
    }

    public SwipeButtonCustomItems setActionConfirmPercentageFraction(double actionConfirmPercentageFraction) {
        this.actionConfirmPercentageFraction = actionConfirmPercentageFraction;
        return this;
    }

    public String getButtonPressText() {
        return buttonPressText;
    }

    public SwipeButtonCustomItems setButtonPressText(String buttonPressText) {
        this.buttonPressText = buttonPressText;
        return this;
    }

    public String getActionConfirmText() {
        return actionConfirmText;
    }

    public SwipeButtonCustomItems setActionConfirmText(String actionConfirmText) {
        this.actionConfirmText = actionConfirmText;
        return this;
    }

    public int getPostConfirmationColor() {
        return postConfirmationColor;
    }

    public SwipeButtonCustomItems setPostConfirmationColor(int postConfirmationColor) {
        this.postConfirmationColor = postConfirmationColor;
        return this;
    }

    //These methods listed below can be overridden in the instance of SwipeButton and
    public void onButtonPress() {

    }

    public void onSwipeCancel() {

    }

    abstract public void onSwipeConfirm();
}
