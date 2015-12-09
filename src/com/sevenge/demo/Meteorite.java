package com.sevenge.demo;

import java.util.Random;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.sevenge.SevenGE;
import com.sevenge.ecs.Entity;
import com.sevenge.ecs.PhysicsComponent;
import com.sevenge.ecs.PhysicsSystem;
import com.sevenge.ecs.PositionComponent;
import com.sevenge.ecs.SpriteComponent;
import com.sevenge.graphics.TextureRegion;

public class Meteorite {

	public Meteorite(Entity entity, World world) {
		Random rng = new Random();
		SpriteComponent cs = new SpriteComponent();
		PositionComponent cp = new PositionComponent();
		cp.rotation = rng.nextFloat() * 360.0f;
		cp.x = rng.nextFloat() * 10000f - 5000f;
		cp.y = rng.nextFloat() * 10000f - 5000f;
		cs.scale = 1.0f;
		int rnd = rng.nextInt(24) + 1;
		cs.textureRegion = (TextureRegion) SevenGE.getAssetManager().getAsset(
				"a" + rnd);
		PhysicsComponent physicsComponent = new PhysicsComponent();
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(PhysicsSystem.WORLD_TO_BOX * cp.x,
				PhysicsSystem.WORLD_TO_BOX * cp.y);
		Body body = world.createBody(bodyDef);
		body.setAngularDamping(0.1f);
		body.setLinearDamping(0.1f);
		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(cs.textureRegion.height / 2
				* PhysicsSystem.WORLD_TO_BOX);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = cs.textureRegion.height * 0.8f;
		fixtureDef.friction = 0.5f;
		fixtureDef.restitution = 0.5f;
		body.createFixture(fixtureDef);
		body.setUserData(entity);
		physicsComponent.setBody(body);
		entity.addComponent(physicsComponent, 4);
		entity.addComponent(cp, 0);
		entity.addComponent(cs, 1);
	}
}
