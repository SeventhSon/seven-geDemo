package com.sevenge.demo;

import com.badlogic.gdx.math.Vector2;
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

public class Bullet {
	Entity entity;

	public Bullet(Entity entity, float angle, World world, float x, float y) {
		this.entity = entity;
		PositionComponent pcBullet = new PositionComponent();
		SpriteComponent scBullet = new SpriteComponent();
		PhysicsComponent fcBullet = new PhysicsComponent();

		Vector2 v = new Vector2(150, 0);
		float X = (float) (v.x * Math.cos(angle) - v.y * Math.sin(angle));
		float Y = (float) (v.y * Math.cos(angle) + v.x * Math.sin(angle));
		v.set(X, Y);
		scBullet.textureRegion = (TextureRegion) SevenGE.getAssetManager()
				.getAsset("hull5Jet2.png");
		scBullet.scale = 1f;
		pcBullet.x = X + x;
		pcBullet.y = Y + y;
		pcBullet.rotation = angle;

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(PhysicsSystem.WORLD_TO_BOX * pcBullet.x,
				PhysicsSystem.WORLD_TO_BOX * pcBullet.y);
		Body body = world.createBody(bodyDef);

		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(scBullet.textureRegion.width
				* PhysicsSystem.WORLD_TO_BOX);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = 2.0f;
		fixtureDef.friction = 0.5f;
		fixtureDef.restitution = 0.5f;
		body.createFixture(fixtureDef);
		body.setUserData(entity);
		fcBullet.setBody(body);

		entity.addComponent(pcBullet, 0);
		entity.addComponent(fcBullet, 4);
		entity.addComponent(scBullet, 1);

		float angleRad = angle;
		fcBullet.getBody().setTransform(fcBullet.getBody().getPosition(),
				angleRad);
		body.applyLinearImpulse(v.mul(200), body.getPosition());
	}

	public Entity getEntity() {
		return entity;
	}
}
