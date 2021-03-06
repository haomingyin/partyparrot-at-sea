package seng302.visualiser.cameras;

import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class IsometricCamera extends PerspectiveCamera implements RaceCamera {

    private final Double MIN_X = -120.0;
    private final Double MAX_X = 125.0;

    private final Double MIN_Y = 40.0;
    private final Double MAX_Y = 170.0;

    private final Double PAN_LIMIT = 160.0;
    private final Double NEAR_ZOOM_LIMIT = -30.0;
    private final Double FAR_ZOOM_LIMIT = -180.0;

    private Double horizontalPan;
    private Double verticalPan;
    private Double zoomFactor;

    private ObservableList<Transform> transforms;

    public IsometricCamera(Double cameraStartX, Double cameraStartY) {
        super(true);
        transforms = this.getTransforms();

        zoomFactor = FAR_ZOOM_LIMIT;
        horizontalPan = cameraStartX;
        verticalPan = cameraStartY;

        updateCamera();
    }

    /**
     * Moves the camera to a new position after some change (Zooming or Panning)
     */
    private void updateCamera() {
        transforms.clear();
        transforms.addAll(
            new Translate(horizontalPan, verticalPan, zoomFactor),
            new Rotate(30, new Point3D(1, 0, 0))
        );
    }

    /**
     * Adjusts the zoom amount (camera depth) by some adjustment value
     *
     * @param adjustment the adjustment to be made to the camera
     */
    private void adjustZoomFactor(Double adjustment) {
        if (zoomFactor + adjustment < NEAR_ZOOM_LIMIT && zoomFactor + adjustment > FAR_ZOOM_LIMIT) {
            zoomFactor = zoomFactor + adjustment;
            updateCamera();
        }
    }

    /**
     * Adjusts the Vertical Panning of the Camera
     * @param adjustment the adjustment to be made to the camera
     */
    private void adjustVerticalPan(Double adjustment) {
        if (verticalPan + adjustment >= MIN_Y && verticalPan + adjustment <= MAX_Y) {
            verticalPan += adjustment;
            updateCamera();
        }
    }

    /**
     * Adjusts the Horizontal Panning of the Camera.
     * @param adjustment the adjustment to be made to the camera
     */
    private void adjustHorizontalPan(Double adjustment) {
        if (horizontalPan + adjustment >= MIN_X && horizontalPan + adjustment <= MIN_Y) {
            this.horizontalPan += adjustment;
            updateCamera();
        }
    }

    @Override
    public void zoomIn() {
        adjustZoomFactor(-2.5);
    }

    @Override
    public void zoomOut() {
        adjustZoomFactor(2.5);
    }

    @Override
    public void panLeft() {
        adjustHorizontalPan(-2.5);
    }

    @Override
    public void panRight() {
        adjustHorizontalPan(2.5);
    }

    @Override
    public void panUp() {
        adjustVerticalPan(-2.5);
    }

    @Override
    public void panDown() {
        adjustVerticalPan(2.5);
    }
}
