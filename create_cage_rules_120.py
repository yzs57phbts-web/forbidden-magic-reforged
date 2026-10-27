"""Explicit, reviewable soul catalogue; no registry-wide capture or boss fallback."""
from pathlib import Path
import json
root=Path(__file__).resolve().parent
rows=[]
def add(names,aspect,habitat='land',mod='minecraft'):
 for name in names.split():rows.append(dict(entity=mod+':'+name,aspect=aspect,habitat=habitat))
add('zombie husk drowned','corpus')
add('skeleton wither_skeleton stray bogged','mortuus')
add('creeper','ignis')
add('horse donkey mule pig cow cat ocelot wolf rabbit polar_bear panda fox goat camel llama armadillo sniffer skeleton_horse','bestia')
add('turtle frog','bestia','amphibious')
add('sheep','pannus');add('mooshroom','herba');add('chicken','volatus')
add('bat parrot bee','volatus','air');add('squid glow_squid','sensus','water')
add('cod salmon tropical_fish pufferfish dolphin axolotl','bestia','water')
add('guardian','sensus','water');add('spider','pannus');add('slime','limus')
add('ghast','infernus','air');add('zombified_piglin piglin','lucrum')
add('enderman endermite','alienis');add('cave_spider','venenum');add('silverfish','lucrum')
add('blaze','ignis','air');add('magma_cube','ignis');add('witch','praecantatio');add('villager','lucrum')
add('hoglin zoglin','bestia');add('pillager vindicator','telum');add('breeze','aer')
add('phantom','mortuus','air');add('strider','bestia','strider')
# Drowned can be conjured both in water and on supported dry ground.
next(r for r in rows if r['entity']=='minecraft:drowned')['habitat']='amphibious'
add('firebat','ignis','air','thaumcraft');add('wisp','auram','air','thaumcraft')
add('thaumic_slime','vitium',mod='thaumcraft')
add('brainy_zombie giant_brainy_zombie','cognitio',mod='thaumcraft')
add('taint_spider tainted_sheep tainted_cow tainted_pig tainted_chicken tainted_creeper tainted_villager','vitium',mod='thaumcraft')
add('taint_swarm','vitium','air','thaumcraft');add('pech','lucrum',mod='thaumcraft')
add('eldritch_guardian cultist_knight cultist_cleric eldritch_crab inhabited_zombie','alienis',mod='thaumcraft')
add('deer boar bighorn_sheep dwarf_rabbit squirrel penguin','bestia',mod='twilightforest')
add('tiny_bird raven','volatus','air','twilightforest')
add('kobold redcap redcap_sapper','lucrum',mod='twilightforest')
add('minotaur blockchain_goblin snow_guardian','telum',mod='twilightforest')
add('maze_slime slime_beetle','limus',mod='twilightforest')
add('swarm_spider hedge_spider king_spider helmet_crab pinch_beetle','bestia',mod='twilightforest')
add('towerwood_borer','arbor',mod='twilightforest');add('fire_beetle','ignis',mod='twilightforest')
add('mosquito_swarm','venenum','air','twilightforest')
add('hostile_wolf mist_wolf winter_wolf yeti troll','bestia',mod='twilightforest')
add('stable_ice_core unstable_ice_core ice_crystal','gelum',mod='twilightforest')
add('wraith','mortuus','air','twilightforest');add('death_tome','cognitio','air','twilightforest')
add('skeleton_druid','herba',mod='twilightforest');add('carminite_broodling','bestia',mod='twilightforest')
add('carminite_golem','machina',mod='twilightforest')
add('carminite_ghastguard carminite_ghastling','infernus','air','twilightforest')
add('adherent','praecantatio',mod='twilightforest')
exclusions={
 'minecraft':['ender_dragon','wither','elder_guardian','warden','ravager','evoker','piglin_brute','illusioner','giant','zombie_horse','trader_llama','wandering_trader','allay','vex','iron_golem','snow_golem'],
 'twilightforest':['naga','lich','minoshroom','hydra','knight_phantom','ur_ghast','alpha_yeti','snow_queen','plateau_boss','quest_ram','loyal_zombie','lich_minion','rising_zombie','upper_goblin_knight','lower_goblin_knight','giant_miner','armored_giant','harbinger_cube','roving_cube']}
assert len({r['entity'] for r in rows})==len(rows)
out=root/'src/main/resources/forbiddenmagic/cage-souls.json'
out.write_text(json.dumps({'souls':rows,'excluded':exclusions,'policy':'Explicit ordinary species only. Boss tags are also rejected at capture and spawn time. Excludes summoned/quest/debug entities and selected elite or coupled creatures.'},ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('Soul definitions:',len(rows))
