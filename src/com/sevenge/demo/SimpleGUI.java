package com.sevenge.demo;

import static android.opengl.Matrix.orthoM;

import com.sevenge.SevenGE;
import com.sevenge.assets.AssetManager;
import com.sevenge.graphics.Sprite;
import com.sevenge.graphics.SpriteBatcher;
import com.sevenge.graphics.TextureRegion;

public class SimpleGUI {
	SpriteBatcher mSpriteBatch;
	float[] projectionMatrix;
	boolean isRotating, isFollowing = true;
	Sprite[] controls;
	AssetManager assetManager;

	public SimpleGUI() {
		controls = new Sprite[5];
		projectionMatrix = new float[16];
		assetManager = SevenGE.getAssetManager();
		mSpriteBatch = new SpriteBatcher(5);
		Sprite sMoveButton = new Sprite(
				(TextureRegion) assetManager.getAsset("shadedLight07.png"));
		sMoveButton.setPosition(40, 40);
		sMoveButton.setScale(1.2f, 1.2f);

		Sprite sAccelerateButton = new Sprite(
				(TextureRegion) assetManager.getAsset("shadedLight00.png"));
		sAccelerateButton.setPosition(SevenGE.getWidth() - 140, 40);
		sAccelerateButton.setScale(1.2f, 1.2f);

		Sprite sFireButton = new Sprite(
				(TextureRegion) assetManager.getAsset("shadedLight49.png"));
		sFireButton.setPosition(SevenGE.getWidth() - 100, 160);
		sFireButton.setScale(1.2f, 1.2f);

		Sprite sCameraSwitchButton1 = new Sprite(
				(TextureRegion) assetManager.getAsset("shadedLight48.png"));
		sCameraSwitchButton1.setPosition(SevenGE.getWidth() - 100, 300);
		sCameraSwitchButton1.setScale(1.2f, 1.2f);

		Sprite sCameraSwitchButton2 = new Sprite(
				(TextureRegion) assetManager.getAsset("shadedLight45.png"));
		sCameraSwitchButton2.setPosition(SevenGE.getWidth() - 100, 300);
		sCameraSwitchButton2.setScale(1.2f, 1.2f);

		controls[0] = sFireButton;
		controls[1] = sMoveButton;
		controls[2] = sAccelerateButton;
		controls[3] = sCameraSwitchButton1;
		controls[4] = sCameraSwitchButton2;
	}

	public void draw(float[] viewMatrix) {
		orthoM(projectionMatrix, 0, 0, SevenGE.getWidth(), 0,
				SevenGE.getHeight(), -1f, 1f);
		mSpriteBatch.begin();
		mSpriteBatch.setProjection(projectionMatrix);
		if (isRotating)
			mSpriteBatch.drawSprite(controls[1]);
		if (isFollowing) {
			mSpriteBatch.drawSprite(controls[0]);
			mSpriteBatch.drawSprite(controls[2]);
		}
		mSpriteBatch.drawSprite(controls[3]);
		mSpriteBatch.setProjection(viewMatrix);
		mSpriteBatch.end();
	}

	public void setJoystickPosition(int x, int y) {
		controls[1].setCenter(x, y);
		isRotating = true;

	}

	public void setIsRotating(boolean rotate) {
		isRotating = rotate;
	}

	public void setIsFollowing(boolean follow) {
		isFollowing = follow;
		Sprite temp = controls[3];
		controls[3] = controls[4];
		controls[4] = temp;
	}

	public boolean isFollowing() {
		return isFollowing;
	}

	public Sprite getControl(int i) {
		return controls[i];
	}

	public void setControl(Sprite control, int i) {
		controls[i] = control;
	}
}
