"""Remaining 0.575 survival content. Executed in generate_port's namespace."""
for id,tex,rus,eng in [
 ('morph_pickaxe','chameleonpick','Кирка-хамелеон','Chameleon Pickaxe'),('morph_axe','chameleonaxe','Топор-хамелеон','Chameleon Axe'),('morph_shovel','chameleonshovel','Лопата-хамелеон','Chameleon Shovel'),('morph_sword','chameleonsword','Меч-хамелеон','Chameleon Sword'),
 ('taint_pickaxe','taintpickaxe','Кирка разложения','Pickaxe of Distortion'),('taint_shovel','taintshovel','Лопата очистителя','Shovel of the Purifier'),('skull_axe','skullaxe','Секира собирателя черепов','Skull Axe'),('diabolists_fork','fork','Вилка демонолога','Diabolist’s Fork'),('soul_crystal','emptycrystal','Кристалл души','Mob Crystal'),('blink_focus','focus_blink','Набалдашник: Скачок','Wand Focus: Blink'),
 ('submissive_collar','collar','Ошейник подчинения','Submissive Collar'),('ring_of_nutrition','ring_nutrition','Кольцо питания','Ring of Nutrition'),('emerald_nugget','emerald_nugget','Кусочек изумруда','Emerald Nugget'),('black_dye','dye_powder_black','Чёрный пигмент','Black Floral Powder'),('tainted_fruit','taint_fruit','Заражённый плод','Tainted Fruit'),('hexcoal','taint_charcoal','Проклятый уголь','Hexcoal'),('arcane_cake','cake','Мистический торт','Arcane Cake'),('profane_rod','wand_rod_profane','Нечестивый стержень','Profane Rod'),('terrasteel_cap','wand_cap_terrasteel','Террастальные наконечники','Terrasteel Caps')]:item(id,tex,rus,eng)
# Creative parts reuse the authoritative original wand model textures, registered below as item models.
for id,rus in [('profaned_rod','Исчерпанный нечестивый стержень'),('neutronium_rod','Нейтрониевый стержень'),('neutronium_staff_core','Нейтрониевое навершие посоха'),('orichalcum_cap','Орихалковые наконечники')]:
 ru['item.forbiddenmagic.'+id]=rus;en['item.forbiddenmagic.'+id]=id.replace('_',' ').title()
block_names={'wrath_cage':('Клетка гнева','Wrath Cage'),'tainted_log':('Заражённое бревно','Tainted Log'),'tainted_planks':('Заражённые доски','Tainted Planks'),'tainted_leaves':('Заражённая листва','Tainted Leaves'),'tainted_sapling':('Заражённый саженец','Tainted Sapling'),'tainted_stone':('Заражённый камень','Tainted Stone'),'tainted_bricks':('Заражённые кирпичи','Tainted Bricks'),'nether_star_block':('Блок звёзд Нижнего мира','Nether Star Block'),'umbral_rose':('Сумеречная роза','Umbral Rose'),'umbral_rose_bush':('Куст сумеречной розы','Umbral Rose Bush'),'arcane_cake':('Мистический торт','Arcane Cake')}
for id,(rus,eng) in block_names.items():
 ru['block.forbiddenmagic.'+id]=rus;en['block.forbiddenmagic.'+id]=eng
 ru['item.forbiddenmagic.'+id]=rus;en['item.forbiddenmagic.'+id]=eng

# Replace the interim 0.2 taint recipe with its original shovel acquisition route.
recipes[:]=[r for r in recipes if r['key']!='FM:TAINTSHARD']
for r in research:
 if r['key']=='FM_TAINTSHARD':r['recipes']=[]
ru['fm.page.FM_TAINTSHARD']='Заражённые осколки извлекаются Лопатой очистителя из блоков порчи, которые не оставляют обычной добычи. Базовый шанс — 2/31; Удача добавляет по 2/31 за уровень, Шёлковое касание — 6/31. Доступная порча Thaumcraft подходит; собственная листва исключена.'
en['fm.page.FM_TAINTSHARD']='Use the Shovel of the Purifier on taint blocks that yield no ordinary drops: chance 2/31, plus 2/31 per Fortune level or 6/31 for Silk Touch. Tainted tree leaves are excluded.'
for r in recipes:
 if r['key']=='FM:INFERNAL':r['inputs']=[full(i) for i in ['thaumcraft:salis_mundus','pride_shard','pride_shard','minecraft:soul_sand','minecraft:wither_skeleton_skull','minecraft:quartz','minecraft:blaze_powder']]
 if r['key']=='FM:CROP':r.update(pattern=['L','S','S'],symbols={'L':'minecraft:leather','S':'minecraft:stick'},aspects={})
entry('FM_TAINTSHOVEL','taint_shovel',-6,-2,'Лопата очистителя','ПКМ удаляет газ и жидкий флюкс в области 11×9×11 вокруг владельца; цена — до 15 прочности. При добыче пустых блоков порчи может получить заражённый осколок. Сохраняет материал стихийных инструментов и чинится таумием.',parents=['FM_ROOT','INFUSION'],a={'sano':4,'perfodio':3,'vitreus':3},refs=['TAINTSHOVEL'])
infusion('TAINTSHOVEL','FM_TAINTSHOVEL','taint_shovel','thaumcraft:thaumium_shovel',['thaumcraft:order_shard','thaumcraft:zombie_brain','minecraft:diamond','thaumcraft:silverwood_log'],{'sano':8,'vitreus':24,'perfodio':12},5)
entry('FM_TAINTPICK','taint_pickaxe',-8,0,'Кирка разложения','Скорость зависит от твёрдости: мягкие блоки добывает медленно, твёрдые — всё быстрее. Сохраняет исходную прочность 1500 и зачаровываемость 18; чинится таумием. Бедрок остаётся неразрушимым.',parents=['FM_TAINTSHARD','INFUSION'],a={'vitium':4,'perfodio':4},refs=['TAINTPICK'])
infusion('TAINTPICK','FM_TAINTPICK','taint_pickaxe','thaumcraft:thaumium_pickaxe',['thaumcraft:entropy_shard','taint_shard','minecraft:diamond','thaumcraft:greatwood_log'],{'perditio':8,'perfodio':8,'vitium':12},1)
entry('FM_TAINTTREE','tainted_sapling',-8,2,'Заражённое дерево','Саженец вырастает на почве в дерево высотой 4–6 блоков. Листва даёт саженцы и сбрасывает плоды; древесина перерабатывается в доски или проклятый уголь. Плод даёт 4 единицы пищи и насыщение 0,8, но приносит постоянное искажение и отравление порчей. Плоды выпадают при добыче или распаде листвы.',parents=['FM_TAINTSHARD'],a={'vitium':4,'arbor':4},refs=['TAINTTREE'])
recipe('TAINTTREE','FM_TAINTTREE','crucible','tainted_sapling',{'vitium':10,'venenum':4},center='minecraft:oak_sapling')
entry('FM_TAINTSTONE','tainted_stone',-10,2,'Заражённый камень','Из камня и заражённого осколка получается девять блоков. Четыре камня превращаются в четыре кирпича. Материалы сохраняют компактное семейство аспектов.',parents=['FM_TAINTTREE'],a={'vitium':2,'terra':2},refs=['TAINTSTONE'])
shaped('TAINTSTONE','FM_TAINTSTONE','tainted_stone',['SSS','SXS','SSS'],{'S':'minecraft:stone','X':'taint_shard'},{'perditio':2,'ordo':1});recipes[-1]['count']=9
entry('FM_MORPH','morph_pickaxe',6,0,'Инструменты-хамелеоны','Каждый инструмент хранит три независимых набора зачарований и имён. Shift+ПКМ переключает набор за 5 прочности. Наборы не копируются друг в друга. Ремонтируются осколком зависти; Улучшения наполнения открываются отдельными исследованиями.',parents=['FM_SHARDS','INFUSION'],a={'permutatio':8,'invidia':6,'instrumentum':4},refs=['MORPH_PICKAXE','MORPH_AXE','MORPH_SHOVEL','MORPH_SWORD'])
for tool in ['pickaxe','axe','shovel','sword']:
 infusion('MORPH_'+tool.upper(),'FM_MORPH','morph_'+tool,'thaumcraft:thaumium_'+tool,['envy_shard','envy_shard','minecraft:diamond','thaumcraft:alumentum','thaumcraft:silverwood_log'],{'permutatio':32,'invidia':16,'telum' if tool=='sword' else 'instrumentum':16},6)
entry('FM_SKULLAXE','skull_axe',-6,4,'Секира черепов','Оружие собирает головы обычных и иссушённых скелетов, зомби, криперов и игроков. Добыча повышает исходные шансы. Это оружие с характеристиками стихийного меча; починка — таумий.',parents=['FM_SHARDS','INFUSION'],a={'ira':4,'telum':3,'mortuus':3},refs=['SKULLAXE'])
infusion('SKULLAXE','FM_SKULLAXE','skull_axe','thaumcraft:thaumium_axe',['wrath_shard','wrath_shard','minecraft:diamond','minecraft:wither_skeleton_skull'],{'ira':8,'telum':8,'infernus':8},1)
entry('FM_FORK','diabolists_fork',6,2,'Вилка демонолога','Убейте подходящего моба вилкой, держа пустой кристалл души в инвентаре. Первый пустой кристалл запомнит вид существа. Инвентарь, экипировка и произвольные данные моба не захватываются. ПКМ вилкой по Клетке гнева меняет выбранный вид топлива.',parents=['FM_SHARDS','INFUSION'],a={'infernus':3,'machina':3},refs=['FORK'])
infusion('FORK','FM_FORK','diabolists_fork','thaumcraft:thaumium_sword',['minecraft:quartz']*3+['minecraft:redstone'],{'infernus':8,'machina':8,'potentia':8},1)
entry('FM_CAGE','wrath_cage',8,2,'Клетка гнева','Вставьте заполненный кристалл души. Клетка принимает эссенцию выбранной стороны во внутренний запас до 64; всасывание 256. Для четырёх успешных появлений тратит 5 эссенции: сначала аспект вида, затем Ira, затем Desidia. Лень добавляет задержку 200 тиков. Красный камень останавливает спаун. Базовая задержка 200–299 тиков; максимум шесть существ того же вида рядом. Освещение и биом не ограничивают клетку, но коллизии, жидкость и граница мира учитываются. При разрушении душа возвращается.',parents=['FM_FORK','DISTILESSENTIA'],a={'ira':8,'bestia':8,'machina':4},warp=2,refs=['CAGE','SOUL'])
infusion('CAGE','FM_CAGE','wrath_cage','thaumcraft:thaumium_block',['wrath_shard']*4+['minecraft:diamond']*4+['thaumcraft:warded_jar']*3,{'ira':32,'praecantatio':32,'bestia':32,'machina':16},10)
recipe('SOUL','FM_CAGE','crucible','soul_crystal',{'cognitio':10,'potentia':10},center='minecraft:diamond')
entry('FM_COLLAR','submissive_collar',-6,6,'Ошейник подчинения','Украшение шеи хранит 250 vis каждой стихии и работает как вис-амулет Thaumcraft. Полученный урон создаёт по три vis за целую единицу урона в случайных первичных стихиях; хлыст добавляет три порции. В Аду боль может дать осколок страсти. ПКМ по игроку надевает ошейник, если слот шеи свободен.',parents=['FM_CROP','VISAMULET','INFUSION'],a={'luxuria':4,'vinculum':4,'corpus':3},refs=['COLLAR'])
infusion('COLLAR','FM_COLLAR','submissive_collar','thaumcraft:vis_amulet',['lust_shard']*3+['minecraft:lead','thaumcraft:mundane_belt'],{'luxuria':8,'vinculum':8,'infernus':4,'corpus':4},1)
entry('FM_GLUTTONY','ring_of_nutrition',-8,6,'Запретное насыщение','Кольцо добавляет 2 единицы пищи и насыщение 2,0 после еды; два кольца не удваивают действие. Мистический торт содержит 12 порций по 2 пищи с насыщением 1,0. Пока осталась хотя бы одна порция, случайные тики восстанавливают съеденные. Разрушение торта не возвращает предмет.',parents=['FM_SHARDS','INFUSION'],a={'gula':4,'fames':4,'fabrico':4},refs=['RING','CAKE'])
shaped('RING','FM_GLUTTONY','ring_of_nutrition',[' D ','SXS',' S '],{'D':'minecraft:diamond','S':'gluttony_shard','X':'thaumcraft:mundane_ring'},{'perditio':10,'aqua':20,'terra':20})
infusion('CAKE','FM_GLUTTONY','arcane_cake','minecraft:cake',['thaumcraft:salis_mundus','minecraft:sugar','minecraft:egg','minecraft:sugar','gluttony_shard','gluttony_shard'],{'gula':12,'fames':24,'fabrico':24},3)
entry('FM_BLINK','blink_focus',6,4,'Набалдашник: Скачок','Телепортирует к выбранной грани блока на расстоянии до 128 за 3 Perditio. Безопасное место и загруженный чанк обязательны. На втором ранге доступны Адское пламя или Пандемоний: пламя ранит и поджигает область назначения, пандемоний переносит врагов оттуда на прежнее место владельца. Они добавляют 1 Ignis или 1 Ordo к цене. Экономность, Мощность и Расширение используют штатный верстак набалдашников.',parents=['FM_SHARDS','INFUSION'],a={'iter':6,'desidia':4,'perditio':4},refs=['BLINK'])
infusion('BLINK','FM_BLINK','blink_focus','minecraft:ender_pearl',['minecraft:quartz','sloth_shard']*3,{'iter':25,'infernus':10,'desidia':10,'perditio':25},3)
entry('FM_ROSES','umbral_rose_bush',2,2,'Сумеречные розы','Алхимический куст разрастается маленькими чёрными розами. Из розы получаются два чёрных пигмента, пригодных для красителей и чернил. Плотность разрастания ограничена.',parents=['FM_ROOT'],a={'tenebrae':3,'victus':3},refs=['ROSE'])
recipe('ROSE','FM_ROSES','crucible','umbral_rose_bush',{'tenebrae':8,'victus':5},center='minecraft:rose_bush')
entry('FM_EMERALD','emerald_nugget',4,2,'Изумрудная трансмутация','Девять кусочков составляют изумруд; изумруд разбирается обратно. В тигле один кусочек и эссенция дают четыре кусочка.',parents=['CRUCIBLE'],a={'vitreus':3,'lucrum':3},refs=['EMERALD'])
recipe('EMERALD','FM_EMERALD','crucible','emerald_nugget',{'vitreus':2,'lucrum':2},center='emerald_nugget',count=4)
entry('ROD_fm_profane','profane_rod',0,2,'Нечестивый договор','Еретик получает профессию у куста сумеречной розы или блока звёзд Нижнего мира и торгует запретными материалами. За 8–12 изумрудов и палку продаёт заряженный нечестивый жезл. Он хранит 50 vis каждой стихии и по договору восполняет суммарно 25000 сантиvis. Восполнение приносит искажение; после истощения договора стержень становится неактивным.',parents=['FM_ROOT'],a={'praecantatio':3,'lucrum':3},auto=True)

enchants={
 'wrath':('Гнев',5,['#minecraft:enchantable/sword','#minecraft:axes'],['minecraft:diamond_sword'],8,{'ira':16,'telum':16,'infernus':8},['thaumcraft:salis_mundus','minecraft:iron_sword']+['wrath_shard']*3,'Добавляет 1,25 урона за уровень.'),
 'cluster':('Доходность',4,['forbiddenmagic:morph_pickaxe'],[],3,{'ignis':4,'metallum':4,'lucrum':4},['thaumcraft:elemental_pickaxe','thaumcraft:salis_mundus'],'Даёт шанс 20% + 7,5% за уровень превратить руду в самородный кластер Thaumcraft. Несовместима с Удачей.'),
 'greedy':('Алчность',1,['#minecraft:enchantable/sword'],[],4,{'infernus':16,'telum':8,'lucrum':16},['minecraft:golden_sword','minecraft:diamond','greed_shard','greed_shard','thaumcraft:salis_mundus'],'Убийство жителя даёт изумруд; враждебные мобы имеют шанс 3/35 на кусочек. Несовместима с Добычей и Образованием.'),
 'consuming':('Поглощение',1,['#minecraft:enchantable/mining'],[],3,{'vacuos':8,'instrumentum':8,'fames':8},['minecraft:iron_shovel','minecraft:ender_eye','thaumcraft:salis_mundus'],'Удаляет из добычи землю, песок, гравий, булыжник и адский камень. Несовместимо с Шёлковым касанием.'),
 'educational':('Образование',5,['#minecraft:enchantable/sword'],[],3,{'praecantatio':4,'telum':4,'cognitio':8},['thaumcraft:zombie_brain','minecraft:book','thaumcraft:salis_mundus'],'За каждый уровень даёт дополнительно тройной исходный опыт моба. Несовместимо с Добычей и Алчностью.'),
 'corrupting':('Искажение',1,['#minecraft:enchantable/mining'],[],4,{'infernus':16,'permutatio':16,'vitreus':8},['minecraft:nether_wart','minecraft:soul_sand','thaumcraft:salis_mundus'],'При добыче руды кристаллов Thaumcraft с шансом 1/3 заменяет каждый кристалл случайным осколком греха.'),
 'voidtouched':('Прикосновение пустоты',1,['forbiddenmagic:morph_'+t for t in ['pickaxe','axe','shovel','sword']],[],8,{'vacuos':16,'tenebrae':16,'invidia':24,'alienis':16},['thaumcraft:salis_mundus']+['thaumcraft:void_seed']*3+['envy_shard']*3,'Инструмент-хамелеон восстанавливает 1 прочность каждые 10 тиков. Искажение снаряжения: 1.'),
 'impact':('Ударная волна',1,['forbiddenmagic:morph_pickaxe','forbiddenmagic:morph_shovel'],[],4,{'perditio':16,'perfodio':16},['thaumcraft:salis_mundus','envy_shard','envy_shard','thaumcraft:elemental_shovel'],'Кирка или лопата-хамелеон добывает подходящие блоки в плоскости 3×3. Дополнительные блоки проходят штатные проверки прав и добычи; каждый тратит прочность.')}
conflicts={'cluster':['minecraft:fortune'],'greedy':['minecraft:looting','forbiddenmagic:educational'],'educational':['minecraft:looting','forbiddenmagic:greedy'],'consuming':['minecraft:silk_touch']}
for n,(rus,maxlevel,supported,unused,instability,a,parts,text) in enchants.items():
 tag='enchantable/'+n
 if n in ['consuming','corrupting']:
  supported=['#minecraft:pickaxes']+(['#minecraft:shovels'] if n=='consuming' else [])+['thaumcraft:primal_crusher']
 write(Path('data/forbiddenmagic/tags/item')/(tag+'.json'),{'replace':False,'values':supported})
 data={'description':{'translate':'enchantment.forbiddenmagic.'+n},'supported_items':'#forbiddenmagic:'+tag,'weight':1,'max_level':maxlevel,'min_cost':{'base':5,'per_level_above_first':11},'max_cost':{'base':25,'per_level_above_first':11},'anvil_cost':4,'slots':['mainhand'],'effects':{}}
 if n in conflicts:data['exclusive_set']=conflicts[n]
 write(Path('data/forbiddenmagic/enchantment')/(n+'.json'),data)
 if n=='wrath':write(Path('data/minecraft/tags/enchantment/in_enchanting_table.json'),{'replace':False,'values':['forbiddenmagic:wrath']})
 ru['enchantment.forbiddenmagic.'+n]=rus;en['enchantment.forbiddenmagic.'+n]=n.title()
 key='FM_ENCH_'+n.upper();entry(key,'morph_pickaxe' if n in ['cluster','impact','voidtouched'] else 'minecraft:enchanted_book',6+2*(list(enchants).index(n)%3),6+2*(list(enchants).index(n)//3),rus,text,parents=['FM_MORPH' if n in ['cluster','impact','voidtouched'] else 'FM_ROOT','INFUSIONENCHANTMENT'],a=a,refs=['ENCH_'+n.upper()])
 recipe('ENCH_'+n.upper(),key,'enchantment','minecraft:enchanted_book',a,inputs=[full(p) for p in parts],enchantment=n,instability=instability)

def craft(id,out,parts,count=1,pattern=None):
 data={'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':full(p)} for p in parts],'result':{'id':full(out),'count':count}}
 if pattern:data={'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{str(i):{'item':full(p)} for i,p in enumerate(parts)},'result':{'id':full(out),'count':count}}
 native(id,data,[])
craft('planks','tainted_planks',['tainted_log'],4);craft('tainted_bricks','tainted_bricks',['tainted_stone'],4,['00','00']);craft('black_dye','black_dye',['umbral_rose'],2)
craft('black_vanilla_dye','minecraft:black_dye',['black_dye']);craft('emerald_nuggets','emerald_nugget',['minecraft:emerald'],9);craft('emerald','minecraft:emerald',['emerald_nugget'],1,['000','000','000'])
craft('star_block','nether_star_block',['minecraft:nether_star'],1,['000','000','000']);craft('stars','minecraft:nether_star',['nether_star_block'],9)
native('hexcoal',{'type':'minecraft:smelting','category':'misc','ingredient':{'item':'forbiddenmagic:tainted_log'},'result':{'id':'forbiddenmagic:hexcoal'},'experience':0.15,'cookingtime':200},[])
write(Path('data/forbiddenmagic/tags/item/consuming_trash.json'),{'replace':False,'values':['#minecraft:dirt','#minecraft:sand','minecraft:gravel','minecraft:cobblestone','minecraft:netherrack']})
for tag,values in {'logs':['tainted_log'],'logs_that_burn':['tainted_log'],'planks':['tainted_planks'],'leaves':['tainted_leaves'],'saplings':['tainted_sapling'],'flowers':['umbral_rose','umbral_rose_bush'],'small_flowers':['umbral_rose'],'tall_flowers':['umbral_rose_bush']}.items():
 for kind in ['item','block']:write(Path('data/minecraft/tags')/kind/(tag+'.json'),{'replace':False,'values':[full(v) for v in values]})
write(Path('data/minecraft/tags/block/mineable/axe.json'),{'replace':False,'values':[full(v) for v in ['tainted_log','tainted_planks']]})
write(Path('data/minecraft/tags/block/mineable/pickaxe.json'),{'replace':False,'values':[full(v) for v in ['tainted_stone','tainted_bricks','wrath_cage','nether_star_block']]})
for tag,values in {'necklace':['submissive_collar'],'ring':['ring_of_nutrition']}.items():write(Path('data/curios/tags/item')/(tag+'.json'),{'replace':False,'values':[full(v) for v in values]})
write(Path('data/c/tags/item/dyes/black.json'),{'replace':False,'values':['forbiddenmagic:black_dye']})
entry('FM_BLOODPOTIONS','tainted_blood_shard',6,-8,'Алхимические настойки','Алхимический стол Blood Magic принимает простой катализатор и заражённый слизень Thaumcraft для настоя порчи на 450 тиков; заражённый кровавый осколок даёт Печать крови на 1200 тиков. Это настоящие эффекты во флягах Blood Magic, включая метательные и затяжные. Цена изготовления: 500 LP, время 200 тиков, стол второго уровня.',parents=['FM_BLOODMAGIC'],a={'victus':4,'venenum':4},mods=M)
for name,effect,duration,ingredient in [('taint','thaumcraft:taint_poison',450,'thaumcraft:tainted_goo'),('blood_seal','forbiddenmagic:blood_seal',1200,'forbiddenmagic:tainted_blood_shard')]:native('flask/'+name,{'type':'bloodmagic:flask_potioneffect','baseDuration':duration,'effect':effect,'input':[{'item':'bloodmagic:simplecatalyst'},{'item':ingredient}],'syphon':500,'ticks':200,'upgradeLevel':1},M)
entry('CAP_fm_terrasteel','terrasteel_cap',6,12,'Террасталь: сомнительная идея','Исходный шуточный наконечник: расход vis 180%. Не даёт скидки на зарядку ботанических стержней.',parents=['FM_BOTANY'],a={'metallum':2},mods=B,refs=['TERRASTEEL'])
shaped('TERRASTEEL','CAP_fm_terrasteel','terrasteel_cap',['NNN','N N'],{'N':'botania:terrasteel_nugget'},{},B)
for n in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:
 mods=BM if n=='bloodthorn' else B
 native('floating_'+n,{'type':'minecraft:crafting_shapeless','ingredients':[{'item':full(n)},{'item':'botania:white_floating_flower'}],'result':{'id':full('floating_'+n)},'neoforge:conditions':[{'type':'neoforge:mod_loaded','modid':m} for m in mods]},mods)
 ru['block.forbiddenmagic.floating_'+n]='Парящий '+ru['block.forbiddenmagic.'+n].lower();en['block.forbiddenmagic.floating_'+n]='Floating '+n.title()
for lang in [ru,en]:
 lang.update({'fm.cage.mode':'%s: %s / 64','fm.collar.owner':'Владелец: %s' if lang is ru else 'Owner: %s','entity.minecraft.villager.forbiddenmagic.heretic':'Еретик' if lang is ru else 'Heretic'})
 for name in ['profane','profaned','neutronium']:lang['item.Wand.fm_'+name+'.rod']={'profane':'Нечестивый','profaned':'Исчерпанный','neutronium':'Нейтрониевый'}[name]
 for name in ['orichalcum','terrasteel']:lang['item.Wand.fm_'+name+'.cap']=name.title()
 for name,title,text in [('hellfire','Адское пламя','Обжигает существ в области назначения.'),('pandemonium','Пандемоний','Переносит врагов на прежнее место владельца.')]:lang['focus.upgrade.fm_'+name+'.name']=title;lang['focus.upgrade.fm_'+name+'.text']=text

for tag,values in {"pickaxes":["morph_pickaxe","taint_pickaxe"],"shovels":["morph_shovel","taint_shovel"],"axes":["morph_axe"],"swords":["morph_sword","skull_axe","diabolists_fork"]}.items():write(Path("data/minecraft/tags/item")/(tag+".json"),{"replace":False,"values":[full(v) for v in values]})

# Preserve the original enchantability costs, including infusion XP derived from level-one cost.
for name in enchants:
 p=OUT/'data/forbiddenmagic/enchantment'/(name+'.json');d=json.loads(p.read_text())
 if name!='wrath':
  d['weight']=5 if name in ['consuming','educational'] else 1
  d['min_cost']={'base':20 if name in ['greedy','corrupting'] else 30 if name in ['impact','voidtouched'] else 11,'per_level_above_first':0 if name in ['greedy','corrupting','impact','voidtouched'] else 10}
  d['max_cost']={'base':61 if name in ['greedy','corrupting'] else 41 if name=='voidtouched' else 21 if name=='impact' else 16,'per_level_above_first':10}
  write(Path('data/forbiddenmagic/enchantment')/(name+'.json'),d)
for r in research:
 if r['key'] in ['FM_MORPH','FM_CAGE','FM_SKULLAXE','FM_TAINTTREE','FM_ENCH_WRATH','FM_ENCH_VOIDTOUCHED']:r['warp']={'FM_MORPH':4,'FM_CAGE':3,'FM_SKULLAXE':1,'FM_TAINTTREE':1,'FM_ENCH_WRATH':3,'FM_ENCH_VOIDTOUCHED':3}[r['key']]

entry('FM_HELLFIRE','minecraft:flint_and_steel',-2,-2,'Владыка адского пламени','Исследование открывается после знакомства с Infernus. Жезл без набалдашника зажигает огонь на верхней грани обсидиана или адского камня за 2 сантиvis Ignis. Так можно заново зажечь погасший портал.',parents=['FM_SHARDS'],a={'ignis':3,'infernus':4,'iter':2})
research[-1].update(hidden=True,triggers=['infernus'])
for r in research:
 if r['key']=='ROD_fm_profane':r.update(auto=False,hidden=True,complexity=0)
craft('riding_crop','riding_crop',['minecraft:leather','minecraft:stick'],1,['0','1','1'])
native('terrasteel_caps',{'type':'minecraft:crafting_shaped','pattern':['NNN','N N'],'key':{'N':{'item':'botania:terrasteel_nugget'}},'result':{'id':full('terrasteel_cap')}},B)
write(Path('data/minecraft/tags/block/beacon_base_blocks.json'),{'replace':False,'values':[full('nether_star_block')]})
write(Path('data/thaumcraft/tags/block/taint.json'),{'replace':False,'values':[full('tainted_leaves'),full('tainted_sapling')]})
for lang in [ru,en]:
 lang['itemGroup.forbiddenmagic.souls']='Кристаллы душ' if lang is ru else 'Mob Crystals'
 lang['fm.creative_only']='Только творческий режим' if lang is ru else 'Creative mode only'
 lang['fm.contract']='Остаток договора: %s сантиvis' if lang is ru else 'Contract remaining: %s centivis'
 lang['enchantment.forbiddenmagic.cluster']='Доходность' if lang is ru else 'Lucrative'
 lang['fm.page.FM_ROSES']=lang['fm.page.FM_ROSES'].replace('Плотность разрастания ограничена.','Костная мука также стимулирует разрастание.')
for r in recipes:
 if r['key']=='FM:CRYSTALWELL':r['inputs'][1]='#c:dyes/black'

# Contemporary Botania uses Patchouli instead of the old imperative Lexicon API.
original_lang=z if 'z' in globals() else None
for lang,strings in [('ru_ru',ru),('en_us',en)]:
 for flower in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:
  key='FM_'+flower.upper();text=strings['fm.page.'+key]
  entry_data={'name':strings['block.forbiddenmagic.'+flower],'category':'botania:'+('generating_flowers' if flower=='tainthistle' else 'functional_flowers'),'icon':full(flower),'pages':[{'type':'patchouli:text','text':text},{'type':'patchouli:text','text':'Рецепт и исследования: Таумономикон → Апокрифы. Парящий вариант создаётся из цветка и обычного парящего цветка Botania.' if lang=='ru_ru' else 'Research and recipes: Thaumonomicon → Apocrypha. Combine with a mundane floating flower for a floating variant.'}],'extra_recipe_mappings':{full(flower):0,full('floating_'+flower):0}}
  if flower=='bloodthorn':entry_data['flag']='mod:bloodmagic'
  write(Path('assets/botania/patchouli_books/lexicon')/lang/'entries/forbiddenmagic'/(flower+'.json'),entry_data)
ru['fm.page.FM_BLOODPOTIONS']=ru['fm.page.FM_BLOODPOTIONS'].replace('стол второго уровня','минимальный ранг сферы I')
en['fm.page.FM_BLOODPOTIONS']=ru['fm.page.FM_BLOODPOTIONS']
