package com.sevenge.demo;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glEnable;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.sevenge.GameState;
import com.sevenge.SevenGE;
import com.sevenge.ecs.AnimationSystem;
import com.sevenge.ecs.Entity;
import com.sevenge.ecs.EntityManager;
import com.sevenge.ecs.PhysicsComponent;
import com.sevenge.ecs.PhysicsSystem;
import com.sevenge.ecs.PositionComponent;
import com.sevenge.ecs.RendererSystem;
import com.sevenge.ecs.SpriteComponent;
import com.sevenge.assets.AssetManager;
import com.sevenge.assets.AudioLoader;
import com.sevenge.assets.SpriteSheetFTLoader;
import com.sevenge.assets.Texture;
import com.sevenge.assets.TextureLoader;
import com.sevenge.audio.Music;
import com.sevenge.audio.Sound;
import com.sevenge.graphics.Camera;
import com.sevenge.graphics.ParticleSystem;
import com.sevenge.graphics.ShaderUtils;
import com.sevenge.graphics.SpriteBatch;
import com.sevenge.graphics.SpriteBatcher;
import com.sevenge.graphics.TextureRegion;
import com.sevenge.input.Input;
import com.sevenge.utils.DebugLog;
import com.sevenge.utils.FixedSizeArray;

public class DemoGameState extends GameState {

	SpriteBatcher mSpriteBatch;
	Camera camera;
	AssetManager assetManager = SevenGE.getAssetManager();
	SimpleGUI gui;
	FixedSizeArray<Layer> maplayers;
	float[] matrix = new float[16];
	EntityManager mEM;
	RendererSystem rendererSystem;
	AnimationSystem animationSystem;
	PhysicsSystem physicsSystem;
	Player player;
	ParticleSystem particleSystem;
	long globalStartTime;
	FixedSizeArray<Explosion> explosions;
	private ArrayList<Bullet> activeBullets = new ArrayList<Bullet>(25);
	private ArrayList<Bullet> bulletsToRemove = new ArrayList<Bullet>(25);

	@Override
	public void dispose() {

	}

	@Override
	public void draw(float interpolationAlpha) {
		glClear(GL_COLOR_BUFFER_BIT);

		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		for (int i = 0; i < maplayers.getCount(); i++) {
			Layer layer = maplayers.get(i);
			for (int j = 0; j < layer.batches.getCount(); j++) {
				SpriteBatch sb = layer.batches.get(j);
				sb.setProjection(camera.getCameraMatrix(layer.parallaxFactor,
						matrix));
				sb.draw();
			}
		}
		rendererSystem.process(interpolationAlpha);
		float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
		particleSystem.draw(camera.getCameraMatrix(), currentTime);
		gui.draw(camera.getCameraMatrix());
	}

	@Override
	public void update() {
		SevenGE.getInput().process();
		mEM.assignEntities();
		physicsSystem.process();
		player.update((System.nanoTime() - globalStartTime) / 1000000000f);
		if (gui.isFollowing()) {
			Vector2 position = player.getPosition();
			camera.setPostion(position.x, position.y);
		}
		animationSystem.process();
		for (int i = 0; i < explosions.getCount(); i++) {
			if (!explosions.get(i).isPlaying()) {
				mEM.removeEntity(explosions.get(i).getID());
				explosions.remove(i);
				i--;
			}
		}

		for (Bullet bullet : activeBullets) {

			if (bulletsToRemove.contains(bullet)) {
				continue;
			}

			PositionComponent pcBullet = (PositionComponent) bullet.getEntity().mComponents[0];
			float dist = (float) Math
					.sqrt((pcBullet.x - player.getPosition().x)
							* (pcBullet.x - player.getPosition().x)
							+ (pcBullet.y - player.getPosition().y)
							* (pcBullet.y - player.getPosition().y));

			if (dist > 2000) {
				bulletsToRemove.add(bullet);
			}

		}

		for (Bullet bullet : bulletsToRemove) {
			activeBullets.remove(bullet);
			mEM.removeEntity(bullet.getEntity().mId);
			physicsSystem.getWorld().destroyBody(
					((PhysicsComponent) bullet.getEntity().mComponents[4])
							.getBody());
		}
		bulletsToRemove.clear();
	}

	@Override
	public void pause() {
		((Music) SevenGE.getAssetManager().getAsset("engine")).stop();
	}

	@Override
	public void resume() {
		DebugLog.i("DEMO", "resume");
	}

	@Override
	public void load() {
		assetManager = SevenGE.getAssetManager();
		assetManager.addLoader("spriteSheet", new SpriteSheetFTLoader(
				assetManager));
		assetManager.addLoader("texture", new TextureLoader(assetManager));
		assetManager.addLoader("audio", new AudioLoader(assetManager));
		assetManager.loadAssets("package.pkg");
		mSpriteBatch = new SpriteBatcher(500);
		camera = new Camera(SevenGE.getWidth(), SevenGE.getHeight());
		camera.setPostion(0, 0);
		camera.setRotation(0.0f);
		camera.setZoom(1.1f);
		gui = new SimpleGUI();

		MapLoader maploader = new MapLoader();
		maploader.load("map.json");
		maplayers = maploader.getLayers();

		mEM = new EntityManager(500, 10);
		rendererSystem = new RendererSystem(500);
		rendererSystem.setCamera(camera);
		physicsSystem = new PhysicsSystem(500);
		animationSystem = new AnimationSystem(500);

		mEM.registerSystem(rendererSystem);
		mEM.registerSystem(physicsSystem);
		mEM.registerSystem(animationSystem);
		generateRandomPlanets();

		globalStartTime = System.nanoTime();
		Texture tex = (Texture) SevenGE.getAssetManager().getAsset("particle");
		particleSystem = new ParticleSystem(400, ShaderUtils.PARTICLE_SHADER,
				tex.glID);
		player = new Player(mEM.createEntity(10), mEM,
				physicsSystem.getWorld(), particleSystem, activeBullets);
		explosions = new FixedSizeArray<Explosion>(25, null);

		InputListener inputListener = new InputListener(camera, gui, player);
		Input input = SevenGE.getInput();
		input.addInputProcessor(inputListener);
		input.addGestureProcessor(inputListener);
		physicsSystem.getWorld().setContactListener(new ContactListener() {

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}

			@Override
			public void endContact(Contact contact) {

			}

			@Override
			public void beginContact(Contact contact) {
				Entity a = (Entity) contact.getFixtureA().getBody()
						.getUserData();
				Entity b = (Entity) contact.getFixtureB().getBody()
						.getUserData();

				if (a == player.getEntity() || b == player.getEntity()) {
					Vector2 contactPoint = contact.getWorldManifold()
							.getPoints()[0];
					explosions.add(new Explosion(mEM.createEntity(10),
							contactPoint.x * 30, contactPoint.y * 30));
					((Sound) assetManager.getAsset("explosion1")).play(1f);
				}
				for (Bullet bullet : activeBullets) {
					if (a == bullet.getEntity()) {
						PositionComponent posC = (PositionComponent) a.mComponents[0];
						explosions.add(new Explosion(mEM.createEntity(10),
								posC.x, posC.y));
						((Sound) assetManager.getAsset("explosion2")).play(1f);
						if (!bulletsToRemove.contains(bullet)) {
							bulletsToRemove.add(bullet);
						}

					} else if (b == bullet.getEntity()) {
						PositionComponent posC = (PositionComponent) b.mComponents[0];
						explosions.add(new Explosion(mEM.createEntity(10),
								posC.x, posC.y));
						((Sound) assetManager.getAsset("explosion2")).play(1f);
						if (!bulletsToRemove.contains(bullet)) {
							bulletsToRemove.add(bullet);
						}

					}
				}
			}
		});
	}

	private void generateRandomPlanets() {

		Random rng = new Random();
		for (int i = 0; i < 400; i++) {
			Entity entity = mEM.createEntity(10);
			SpriteComponent cs = new SpriteComponent();
			PositionComponent cp = new PositionComponent();
			cp.rotation = rng.nextFloat() * 360.0f;
			cp.x = rng.nextFloat() * 10000f - 5000f;
			cp.y = rng.nextFloat() * 10000f - 5000f;
			cs.scale = 1.0f;
			int rnd = rng.nextInt(24) + 1;
			cs.textureRegion = (TextureRegion) assetManager.getAsset("a" + rnd);
			PhysicsComponent physicsComponent = new PhysicsComponent();
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.DynamicBody;
			bodyDef.position.set(PhysicsSystem.WORLD_TO_BOX * cp.x,
					PhysicsSystem.WORLD_TO_BOX * cp.y);
			Body body = physicsSystem.getWorld().createBody(bodyDef);
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
}
