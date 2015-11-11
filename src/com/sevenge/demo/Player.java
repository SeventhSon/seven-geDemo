package com.sevenge.demo;

import java.util.ArrayList;

import android.graphics.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.sevenge.SevenGE;
import com.sevenge.audio.Music;
import com.sevenge.audio.Sound;
import com.sevenge.ecs.Entity;
import com.sevenge.ecs.EntityManager;
import com.sevenge.ecs.PhysicsComponent;
import com.sevenge.ecs.PhysicsSystem;
import com.sevenge.ecs.PositionComponent;
import com.sevenge.ecs.SpriteComponent;
import com.sevenge.graphics.Emitter;
import com.sevenge.graphics.ParticleSystem;
import com.sevenge.graphics.TextureRegion;
import com.sevenge.utils.Vector3;

public class Player {

	Sound laserSfx;
	Music engineMusic;
	boolean isAccelerating;
	Body body;
	float thrust = 10000;
	Vector2 position;
	PositionComponent posc;
	ParticleSystem particleSystem;
	Emitter emiter;
	long globalStartTime;
	int height;
	Entity entity;
	EntityManager mEM;
	World world;
	ArrayList<Bullet> activeBullets;

	public Player(final Entity entity, EntityManager em, World world,
			ParticleSystem ps, ArrayList<Bullet> activeBullets) {
		SpriteComponent spritec = new SpriteComponent();
		this.activeBullets = activeBullets;
		posc = new PositionComponent();
		PhysicsComponent physc = new PhysicsComponent();
		position = new Vector2();
		posc.x = 0;
		posc.y = 0;
		this.entity = entity;
		mEM = em;
		this.world = world;

		spritec.scale = 1.0f;
		spritec.textureRegion = (TextureRegion) SevenGE.getAssetManager()
				.getAsset("Hull4.png");
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(PhysicsSystem.WORLD_TO_BOX * posc.x,
				PhysicsSystem.WORLD_TO_BOX * posc.y);
		body = world.createBody(bodyDef);
		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(spritec.textureRegion.height / 2
				* PhysicsSystem.WORLD_TO_BOX);
		height = spritec.textureRegion.height / 2;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = 100.0f;
		fixtureDef.friction = 0.5f;
		fixtureDef.restitution = 0.5f;
		body.createFixture(fixtureDef);
		physc.setBody(body);
		body.setLinearDamping((float) 0.1);
		body.setAngularDamping((float) 0.2);
		body.setUserData(entity);
		entity.addComponent(physc, 4);
		entity.addComponent(posc, 0);
		entity.addComponent(spritec, 1);

		engineMusic = (Music) SevenGE.getAssetManager().getAsset("engine");
		laserSfx = (Sound) SevenGE.getAssetManager().getAsset("laser");
		engineMusic.setLooping(true);
		particleSystem = ps;
		emiter = new Emitter(particleSystem, Color.rgb(5, 50, 255), 30, 2);
	}

	public void setAccelerating(boolean flag) {
		if (flag) {
			engineMusic.play();
		} else {
			engineMusic.stop();
		}
		isAccelerating = flag;
	}

	public void onAccelerate(float currentTime) {
		Vector2 force = new Vector2(1, 0);
		float angle = body.getAngle();// (float)Math.toRadians(spaceShipAngle);
		float X = (float) (Math.cos(angle) * force.x - Math.sin(angle)
				* force.y);
		float Y = (float) (Math.cos(angle) * force.y + Math.sin(angle)
				* force.x);
		force.set(X, Y);
		force.mul(thrust);
		body.applyForce(force, body.getPosition());
		Vector3 pos = new Vector3(position.x - X * height, position.y - Y
				* height, 0.0f);
		Vector3 direction = new Vector3(-X * 90, -Y * 90, 0.0f);
		emiter.addParticles(pos, direction, currentTime, 5);
	}

	public void onFire() {
		laserSfx.play(1f);
		activeBullets.add(new Bullet(mEM.createEntity(10), body.getAngle(),
				world, position.x, position.y));
	}

	public void onRotate(float angle) {
		body.setTransform(body.getPosition(), (float) Math.toRadians(angle));
	}

	public void update(float currentTime) {
		position.set(posc.x, posc.y);
		if (isAccelerating) {
			onAccelerate(currentTime);
		}
	}

	public Vector2 getPosition() {
		return position;
	}

	public Entity getEntity() {
		return entity;
	}
}
