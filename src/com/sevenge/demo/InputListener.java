package com.sevenge.demo;

import android.view.MotionEvent;

import com.sevenge.SevenGE;
import com.sevenge.graphics.Camera;
import com.sevenge.graphics.Sprite;
import com.sevenge.input.GestureProcessor;
import com.sevenge.input.InputProcessor;
import com.sevenge.utils.AABB;

public class InputListener implements InputProcessor, GestureProcessor {

	Camera camera;
	SimpleGUI gui;
	int rotatingPointer;
	Player player;
	float lastScale;
	float firstSpan;
	private int acceleratingPointer;

	public InputListener(Camera camera, SimpleGUI gui, Player player) {
		this.camera = camera;
		this.gui = gui;
		this.player = player;
	}

	private boolean isButtonClicked(Sprite button, int x, int y) {
		AABB boundingbox = button.getAxisAlignedBoundingBox();
		if (x > boundingbox.x && x < boundingbox.x + boundingbox.width
				&& y > boundingbox.y && y < boundingbox.y + boundingbox.height)
			return true;
		return false;

	}

	@Override
	public boolean onScroll(MotionEvent me1, MotionEvent me2, float distX,
			float distY) {

		if (gui.isFollowing())
			return false;

		float me2x = me2.getX();
		float me2y = me2.getY();

		float[] coords = camera.unproject((int) (me2x + distX),
				(int) (me2y + distY), SevenGE.getWidth(), SevenGE.getHeight(),
				camera.getCameraMatrix());
		float x1 = coords[0];
		float y1 = coords[1];
		coords = camera.unproject((int) me2x, (int) me2y, SevenGE.getWidth(),
				SevenGE.getHeight(), camera.getCameraMatrix());
		float x2 = coords[0];
		float y2 = coords[1];

		com.sevenge.utils.Vector2 camPos = camera.getPosition();
		camPos.x -= x2 - x1;
		camPos.y -= y2 - y1;
		camera.setPostion(camPos.x, camPos.y);
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		y = SevenGE.getHeight() - y;

		if (gui.isFollowing()) {
			if (x > 0 && x < SevenGE.getWidth() / 2) {
				gui.setJoystickPosition(x, y);
				rotatingPointer = pointer;
			}
			if (isButtonClicked(gui.getControl(0), x, y)) {
				player.onFire();
			}
			if (isButtonClicked(gui.getControl(2), x, y)) {
				player.setAccelerating(true);
				acceleratingPointer = pointer;
			}
		}
		if (isButtonClicked(gui.getControl(3), x, y)) {
			if (gui.isFollowing()) {
				gui.setIsFollowing(false);
			} else {
				gui.setIsFollowing(true);
			}
		}
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {

		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {

		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {

		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScale(float currentSpan) {
		if (!gui.isFollowing()) {
			float scale = lastScale * firstSpan / currentSpan;
			camera.setZoom(Math.min(5.0f, Math.max(0.1f, scale)));
		}
		return true;
	}

	@Override
	public void onScaleEnd(float currentSpan) {
	}

	@Override
	public void onScaleBegin(float currentSpan) {
		if (!gui.isFollowing()) {
			lastScale = camera.getZoom();
			firstSpan = currentSpan;
		}
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (pointer == rotatingPointer) {
			gui.setIsRotating(false);
			rotatingPointer = -1;
		}
		if (pointer == acceleratingPointer) {
			player.setAccelerating(false);
			acceleratingPointer = -1;
		}
		return false;
	}

	@Override
	public boolean touchMove(int screenX, int screenY, int pointer) {
		screenY = SevenGE.getHeight() - screenY;
		if (gui.isRotating && rotatingPointer == pointer && gui.isFollowing()) {
			Sprite joystick = gui.getControl(1);
			float buttonY = joystick.getY()
					+ joystick.getAxisAlignedBoundingBox().height / 2;
			float buttonX = joystick.getX()
					+ joystick.getAxisAlignedBoundingBox().width / 2;
			double angle = Math.toDegrees(Math.atan2(screenY - buttonY, screenX
					- buttonX));
			player.onRotate((float) angle);
		}
		return false;
	}

}
