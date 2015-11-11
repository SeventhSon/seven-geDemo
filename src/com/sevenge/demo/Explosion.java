package com.sevenge.demo;

import java.util.Arrays;

import com.sevenge.SevenGE;
import com.sevenge.ecs.AnimationComponent;
import com.sevenge.ecs.Entity;
import com.sevenge.ecs.PositionComponent;
import com.sevenge.ecs.SpriteComponent;
import com.sevenge.graphics.TextureRegion;

public class Explosion {

	Entity entity;
	AnimationComponent ca;

	public Explosion(Entity entity, float x, float y) {
		PositionComponent pcExplosion = new PositionComponent();
		SpriteComponent scExplosion = new SpriteComponent();
		pcExplosion.x = x;// - 128;
		pcExplosion.y = y; // - 128;
		pcExplosion.layer = 100;
		scExplosion.scale = 1f;// 2.0f;
		scExplosion.textureRegion = (TextureRegion) SevenGE.getAssetManager()
				.getAsset("slice_0_0.png");
		ca = new AnimationComponent();
		TextureRegion[] frames = new TextureRegion[48];
		this.entity = entity;
		for (int i = 0; i < 48; i++) {
			frames[i] = (TextureRegion) SevenGE.getAssetManager().getAsset(
					"slice_0_" + i + ".png");
		}

		ca.durations = new int[48];
		Arrays.fill(ca.durations, 16);
		ca.frameList = frames;
		ca.isLooping = false;
		ca.isPlaying = true;
		entity.addComponent(pcExplosion, 0);
		entity.addComponent(ca, 3);
		entity.addComponent(scExplosion, 1);
	}

	public boolean isPlaying() {
		return ca.isPlaying;
	}

	public int getID() {
		return entity.mId;
	}
}
