package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.enemies.EnemyName;

/**
 * Defines all NPC configs to be loaded by Related Factories.
 */
public class NPCConfigs {

  public SoundsConfig sound;
  public BotanistConfig botanist = new BotanistConfig();
  public AstroConfig Astro = new AstroConfig();
  public TutnpcConfig Tutnpc = new TutnpcConfig();
  public HellmanConfig Hellman = new HellmanConfig();
  public AstronautConfig astronautConfig = new AstronautConfig();

  public JailConfig Jail = new JailConfig();
  //   Enemies Factory
  public EnemyConfig redGhost = new EnemyConfig();
  public EnemyConfig roboMan = new EnemyConfig();
  public EnemyConfig chain = new EnemyConfig();
  public EnemyConfig necromancer = new EnemyConfig();
  public EnemyConfig Knight = new EnemyConfig();
  public EnemyConfig rangeBossPTE = new EnemyConfig();

  public EnemyConfig GetEnemyConfig(EnemyName name) {
      EnemyConfig config = null;
      switch (name) {
          case redGhost:
               config = redGhost;
              break;
          case chain:
              config = chain;
              break;
          case necromancer:
              config = necromancer;
              break;
          case roboMan:
              config = roboMan;
              break;
          case Knight:
              config = Knight;
              break;
          case rangeBossPTE:
              config = rangeBossPTE;
              break;
      }
      return config;
  }
}