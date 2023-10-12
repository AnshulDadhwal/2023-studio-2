package com.csse3200.game.components.Weapons.SpecWeapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.SoundComponent;
import com.csse3200.game.components.Weapons.SpecWeapon.Projectile.ProjectileController;
import com.csse3200.game.components.Weapons.WeaponControllerComponent;
import com.csse3200.game.components.explosives.ExplosiveComponent;
import com.csse3200.game.components.explosives.ExplosiveConfig;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.WeaponConfig;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class NukeController extends WeaponControllerComponent {
    private final Vector2 clickPos;

    public NukeController(WeaponConfig config,
                                      Vector2 clickPos,
                                      Entity player, int attackNum) {
        super(config, 0, player, attackNum);
        this.clickPos = clickPos;
    }

    @Override
    public void create() {
        super.create();
        var explosiveConfig = new ExplosiveConfig();
        explosiveConfig.chainable = false;
        explosiveConfig.damage = 50;
        explosiveConfig.damageRadius = 50f;
        explosiveConfig.chainRadius = 80.0f;
        explosiveConfig.effectPath = "particle-effects/explosion/bigBallCombustion.effect";
        explosiveConfig.soundPath = "sounds/explosion/grenade.mp3";

        var explode = new ExplosiveComponent(explosiveConfig);
        entity.addComponent(explode);
        explode.create();
    }

    public int get_spawn_delay() {
        return 800;
    }

    /**
     * Function to add animations based on standard animation configurations (Overidable)
     */
    @Override
    protected void set_animations() {
        animator = entity.getComponent(AnimationRenderComponent.class);
        animator.addAnimation("ATTACK", 1.4f, Animation.PlayMode.NORMAL);
    }

    /**
     * Set initial rotation (defaults to attack direction)
     */
    @Override
    protected void initial_rotation() {
        return;
    }

    /**
     * Set initial position
     */
    @Override
    protected void initial_position() {
        entity.setPosition(clickPos.mulAdd(entity.getScale(), -0.5f));
    }

    @Override
    protected void initial_animation() {
        animator.startAnimation("ATTACK");
    }

    @Override
    protected void rotate() {
        return;
    }

    @Override
    protected void move() {
     var pos = entity.getCenterPosition();
     entity.setScale(entity.getScale().sub(0.006f, 0.006f));
     entity.setPosition(pos.mulAdd(entity.getScale(), -0.5f));
        return;
    }

    @Override
    protected void reanimate() {

    }


    @Override
    protected void despawn() {
        //entity.getEvents().trigger("playSound", "stop");
        animator.stopAnimation();
        entity.getEvents().trigger("explode");
    }

}