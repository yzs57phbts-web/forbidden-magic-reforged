"""User-approved modern mechanics. Run after the preserved 0.575 book is restored."""
for r in recipes:
 if r['key']=='FM:TAINTTREE':r['center']='#forbiddenmagic:taintable_saplings'
write(Path('data/forbiddenmagic/tags/item/taintable_saplings.json'),{'replace':False,'values':['minecraft:'+s+'_sapling' for s in ['oak','spruce','birch','jungle','acacia','dark_oak','cherry']]+['minecraft:mangrove_propagule']})
# Deliberately enumerate natural spoil, never ores, building shapes or archaeology blocks.
write(Path('data/forbiddenmagic/tags/item/consuming_trash.json'),{'replace':False,'values':['#minecraft:dirt','#minecraft:sand']+['minecraft:'+s for s in ['gravel','cobblestone','cobbled_deepslate','deepslate','granite','diorite','andesite','tuff','calcite','basalt','smooth_basalt','blackstone','netherrack']]})
write(Path('data/forbiddenmagic/tags/block/greed_ores.json'),{'replace':False,'values':['minecraft:nether_quartz_ore','minecraft:nether_gold_ore','minecraft:gilded_blackstone']})
write(Path('data/minecraft/tags/item/axes.json'),{'replace':False,'values':['forbiddenmagic:morph_axe','forbiddenmagic:skull_axe']})
write(Path('data/minecraft/tags/item/swords.json'),{'replace':False,'values':['forbiddenmagic:morph_sword','forbiddenmagic:diabolists_fork']})
# Native TC infusion checks supported_items directly, rather than the NeoForge item hook.
for name in ['greedy','educational']:
 p=Path('data/forbiddenmagic/tags/item/enchantable')/(name+'.json')
 tag=json.loads((OUT/p).read_text(encoding='utf8'));tag['values'].append('forbiddenmagic:skull_axe');write(p,tag)
# New production goes straight to vanilla dye; retain the registry ID and conversion for old saves.
craft('black_dye','minecraft:black_dye',['umbral_rose'],2)
for code,lang in [('ru',ru),('en',en)]:
 notes={
  'FM_ENCH_CONSUMING':('Поглощение также убирает глубинный булыжник, глубинный сланец, гранит, диорит, андезит, туф, кальцит, базальт, гладкий базальт и чернит. Руды и обработанные строительные формы не включены. Список расширяется тегом forbiddenmagic:consuming_trash.','Consuming also removes cobbled deepslate, deepslate, granite, diorite, andesite, tuff, calcite, basalt, smooth basalt and blackstone. Ores and crafted building shapes are excluded. Extend forbiddenmagic:consuming_trash to configure it.'),
  'FM_SKULLAXE':('Секира использует бой современного топора и может отключать щит. Дерево по-прежнему рубит медленно. Пиглины могут оставить голову; кадавры и утопленники — голову зомби, зимогоры и болотники — череп скелета.','The axe uses modern axe combat and can disable shields. It remains slow at chopping wood. Piglins can drop a head; husks and drowned use zombie heads, strays and bogged use skeleton skulls.'),
  'FM_SHARDS':('Осколок жадности при добыче с Удачей доступен также из незерской золотой руды и позолоченного чернита. Проверка выполняется один раз на сломанный блок, только при получении его обычного ресурса; Шёлковое касание не подходит.','Fortune mining can also yield a Greed Shard from Nether gold ore and gilded blackstone. The check runs once per broken block and requires its ordinary resource drop; Silk Touch does not qualify.'),
  'FM_TAINTTREE':('В тигле подходят саженцы дуба, ели, берёзы, тропического дерева, акации, тёмного дуба, вишни и мангровый проросток. Результат один и тот же. Собственный заражённый саженец и адские грибы не принимаются.','The crucible accepts oak, spruce, birch, jungle, acacia, dark oak and cherry saplings, plus mangrove propagules. The result is unchanged. Tainted saplings and Nether fungi are excluded.'),
  'FM_ROSES':('Одна сумеречная роза сразу даёт два ванильных чёрных красителя. Старый пигмент из сохранений можно преобразовать в обычный краситель один к одному.','One Umbral Rose now directly crafts two vanilla black dyes. Legacy floral powder from existing saves still converts to vanilla dye one for one.'),
  'FM_CAGE':('Список душ дополнен обычными существами Minecraft и Twilight Forest при наличии мода. Боссы и служебные существа исключены. Водным видам нужна вода, наземным — опора, летающим — свободное место. Свет и биом не ограничивают магическую клетку.','The soul list includes additional ordinary Minecraft creatures and optional Twilight Forest creatures. Bosses and helper entities are excluded. Aquatic species need water, land species need support and flying species need clear space. Light and biome do not restrict the magical cage.')}
 for key,values in notes.items():
  note='fm.modern.'+key
  lang[note]=('§lПримечание для 1.21.1§r<BR><BR>' if code=='ru' else '§l1.21.1 adaptation§r<BR><BR>')+values[code=='en']
for r in research:
 if r['key'] in notes:r.setdefault('pages',[]).append({'type':'text','key':'fm.modern.'+r['key']})
