"""First playable FM 0.575 batch. All replacements are explicit, only BM3 + Botania Revived mechanics."""
from pathlib import Path
import json,zipfile,hashlib
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'src/main/resources'
def write(path,data):
 p=OUT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def full(s):return s if ':' in s else 'forbiddenmagic:'+s
recipes=[];research=[];ru={'itemGroup.forbiddenmagic':'Апокрифы','tc.research_category.FORBIDDEN':'Апокрифы'};en={'itemGroup.forbiddenmagic':'Apocrypha','tc.research_category.FORBIDDEN':'Apocrypha'}
items={}
def item(id,texture,name,english=None):
 items[id]=texture;ru['item.forbiddenmagic.'+id]=name;en['item.forbiddenmagic.'+id]=english or name
def entry(key,icon,x,y,name,text,parents=(),a=None,complexity=2,mods=(),warp=0,auto=False,refs=()):
 research.append(dict(key=key,icon=full(icon),x=x,y=y,complexity=complexity,parents=list(parents),aspects=a or {},mods=list(mods),warp=warp,auto=auto,recipes=['FM:'+s for s in refs]))
 ru['fm.name.'+key]=name;en['fm.name.'+key]=name;ru['fm.tag.'+key]='Запретная тауматургия';en['fm.tag.'+key]='Forbidden thaumaturgy';ru['fm.page.'+key]=text;en['fm.page.'+key]=text
def recipe(key,research,typ,output,a=None,mods=(),**kwargs):
 recipes.append(dict(key='FM:'+key,research=research,type=typ,output=full(output),aspects=a or {},mods=list(mods),**kwargs))
def infusion(key,research,output,center,parts,a,instability,mods=()):recipe(key,research,'infusion',output,a,mods,center=full(center),inputs=[full(p) for p in parts],instability=instability)
def shapeless(key,research,output,parts,a,mods=()):recipe(key,research,'shapeless',output,a,mods,inputs=[full(p) for p in parts])
def shaped(key,research,output,pattern,symbols,a,mods=()):recipe(key,research,'shaped',output,a,mods,pattern=pattern,symbols={k:full(v) for k,v in symbols.items()})
def native(key,data,mods):
 data['neoforge:conditions']=[{'type':'neoforge:mod_loaded','modid':m} for m in mods];write(Path('data/forbiddenmagic/recipe')/(key+'.json'),data)
def mana(key,output,input,cost):native(key,{'type':'botania:mana_infusion','input':{'item':full(input)},'output':{'id':full(output),'count':1},'mana':cost},['botania'])
def altar(key,output,input,lp,tier,consume,drain,mods=('bloodmagic',)):
 native(key,{'type':'bloodmagic:altar','input':{'item':full(input)},'output':{'item':full(output)},'altarSyphon':lp,'upgradeLevel':tier-1,'consumptionRate':consume,'drainRate':drain},mods)
names={'wrath':'гнева','envy':'зависти','taint':'порчи','pride':'гордыни','lust':'страсти','sloth':'лени','greed':'жадности','gluttony':'чревоугодия'}
for n,name in names.items():item(n+'_shard',n+'shard','Осколок '+name,n.title()+' Shard')
item('tainted_blood_shard','tainted_blood_shard','Заражённый кровавый осколок','Tainted Blood Shard')
item('crystal_scribing_tools','crystalwell','Кристальные письменные принадлежности','Crystal Scribing Tools')
item('primal_scribing_tools','primewell','Первозданные письменные принадлежности','Primal Scribing Tools')
item('riding_crop','crop','Хлыст','Riding Crop')
for id,tex,name in [
 ('infernal_rod','wand_rod_infernal','Адский стержень'),('tainted_rod','wand_rod_tainted','Заражённый стержень'),
 ('blood_rod','wand_rod_blood','Кровавый стержень'),('inert_blood_rod','wand_rod_blood_inert','Неактивный кровавый стержень'),('blood_staff_core','wand_rod_blood_staff','Кровавое навершие посоха'),('alchemical_cap','wand_cap_alchemical','Алхимический наконечник'),
 ('livingwood_rod','wand_rod_livingwood','Стержень живой древесины'),('inert_livingwood_rod','wand_rod_livingwood_inert','Неактивный стержень живой древесины'),('dreamwood_rod','wand_rod_dreamwood','Стержень древесины грёз'),('inert_dreamwood_rod','wand_rod_dreamwood_inert','Неактивный стержень древесины грёз'),('dreamwood_staff_core','wand_rod_dreamwood_staff','Навершие посоха грёз'),('manasteel_cap','wand_cap_manasteel','Манастальной наконечник'),('inert_manasteel_cap','wand_cap_manasteel_inert','Неактивный манастальной наконечник'),('elementium_cap','wand_cap_elementium','Элементиевый наконечник'),('inert_elementium_cap','wand_cap_elementium_inert','Неактивный элементиевый наконечник'),
 ('bloody_scribing_tools','bloodwell','Кровавые письменные принадлежности'),('bound_scribing_tools','boundwell','Связанные письменные принадлежности'),('eldritch_blood_orb','divineorb','Потусторонняя кровавая сфера'),('rapier_of_the_strix','blood_rapier','Рапира Стрикса')]:item(id,tex,name)
for n in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:
 ru['block.forbiddenmagic.'+n]={'euclidaisy':'Эвклидова ромашка','whisperweed':'Шептушник','tainthistle':'Заразочертополох','bloodthorn':'Кровавый шип'}[n];en['block.forbiddenmagic.'+n]=n.title()
 for lang in [ru,en]:lang['item.forbiddenmagic.'+n]=lang['block.forbiddenmagic.'+n]

entry('FM_ROOT','wrath_shard',0,0,'Апокрифы','Это первые страницы запретного искусства. В Нижнем мире сильные эмоции кристаллизуются в осколки. Интеграции этого переноса используют только Blood Magic и Botania Revived.',auto=True)
entry('FM_SHARDS','wrath_shard',-2,0,'Осколки грехов','В Аду: враждебные убийства дают шанс гнева; боссы — гордыню; добыча с Удачей и убийства с Добычей — жадность. Смерти без настоящего игрока могут оставить лень. Редкие зомбифицированные пиглины держат зависть. Еда может дать чревоугодие; удары хлыстом — страсть.',parents=['FM_ROOT'],auto=True)
entry('FM_TAINTSHARD','taint_shard',-4,0,'Кристаллизация порчи','На этом этапе переноса заражённый осколок получают в тигле из сбалансированного кристалла и 2 Vitium. Это адаптация до переноса исходной Лопаты очистителя.',parents=['FM_SHARDS','DISTILESSENTIA'],a={'vitium':3,'vitreus':2},refs=['TAINTSHARD'])
recipe('TAINTSHARD','FM_TAINTSHARD','crucible','taint_shard',{'vitium':2},center='thaumcraft:balanced_shard')
entry('FM_CROP','riding_crop',-2,2,'Хлыст','Хлыст подгоняет лошадей и свиней. В Нижнем мире каждый удар может отделить осколок страсти; убийство не требуется. На игроках и големах временно усиливает скорость, силу и регенерацию.',parents=['FM_SHARDS'],a={'instrumentum':2,'motus':2},refs=['CROP'])
shaped('CROP','FM_CROP','riding_crop',['  L',' SL','S  '],{'L':'minecraft:leather','S':'minecraft:stick'},{'aer':2})
entry('FM_CRYSTALWELL','crystal_scribing_tools',2,0,'Кристальные чернила','Расходуются как обычные чернила в исследовательском столе. Полностью исчерпанные принадлежности можно использовать ПКМ: каждая первичная стихия даст 4–7 очков исследований, а предмет станет пустыми обычными принадлежностями.',parents=['RESEARCH'],a={'cognitio':3,'vitreus':2,'praecantatio':1},refs=['CRYSTALWELL'])
shapeless('CRYSTALWELL','FM_CRYSTALWELL','crystal_scribing_tools',['thaumcraft:scribing_tools','minecraft:black_dye','thaumcraft:air_shard','thaumcraft:water_shard'],{'aqua':1,'ordo':1})
entry('FM_PRIMEWELL','primal_scribing_tools',4,0,'Первозданные чернила','Первозданная жемчужина делает запас чернил неисчерпаемым. Эти принадлежности работают в обычном исследовательском столе.',parents=['FM_CRYSTALWELL','PRIMPEARL'],a={'cognitio':3,'alienis':6,'fabrico':1},refs=['PRIMEWELL'])
shapeless('PRIMEWELL','FM_PRIMEWELL','primal_scribing_tools',['minecraft:feather','thaumcraft:primordial_pearl','minecraft:glass_bottle'],{a:50 for a in ['aer','terra','ignis','aqua','ordo','perditio']})
entry('ROD_fm_infernal','infernal_rod',-4,2,'Адский стержень','Вмещает 150 vis каждой стихии. Огонь восстанавливается до одной пятой ёмкости; в Аду остальные стихии — до одной десятой. В руке жезл защищает от огня и иссушения; при ношении гасит пламя и снимает иссушение.',parents=['FM_SHARDS','ROD_silverwood','INFUSION'],a={'infernus':4,'ignis':3,'instrumentum':1},warp=2,refs=['INFERNAL'])
infusion('INFERNAL','ROD_fm_infernal','infernal_rod','minecraft:blaze_rod',['thaumcraft:salis_mundus','pride_shard','pride_shard','minecraft:nether_bricks','minecraft:wither_skeleton_skull','minecraft:fire_charge','minecraft:ghast_tear'],{'infernus':32,'praecantatio':12,'superbia':12},5)
entry('ROD_fm_tainted','tainted_rod',-6,2,'Заражённый стержень','Вмещает 150 vis. В заражённых землях восстанавливает все первичные стихии до одной десятой ёмкости. При получении урона порчей с жезлом в основной руке добавляет по одному vis трём случайным первичным стихиям; урон сохраняется.',parents=['FM_TAINTSHARD','ROD_silverwood','INFUSION'],a={'vitium':5,'praecantatio':4,'instrumentum':2},warp=2,refs=['TAINTED'])
infusion('TAINTED','ROD_fm_tainted','tainted_rod','thaumcraft:taint_tendril',['thaumcraft:salis_mundus']+['taint_shard']*6,{'vitium':24,'praecantatio':12,'perditio':12},5)

B=['botania'];M=['bloodmagic'];BM=B+M
entry('FM_BOTANY','botania:lexicon',0,4,'Ботаническая тауматургия','Ниже описаны связи с установленной Botania Revived. Это обычные жезлы Thaumcraft и настоящие цветы Botania; они работают с её бассейнами, распространителями и Лесным жезлом.',parents=['FM_ROOT'],mods=B,auto=True)
for name,material,x in [('livingwood','botania:livingwood_log',-2),('dreamwood','botania:dreamwood_log',2)]:
 key='ROD_fm_'+name
 entry(key,name+'_rod',x,6,'Стержень '+('живой древесины' if name=='livingwood' else 'древесины грёз'),'Вмещает 100 vis. Стержень должен лежать в горячей панели: каждые 40 тиков он восполняет до 1 vis каждой первичной стихии. Стоимость — 800 маны за полный vis, с манастальным/элементиевым наконечником — 600. Неактивный стержень активируется в бассейне за 10000 маны.',parents=['FM_BOTANY','ROD_silverwood','INFUSION']+(['ROD_fm_livingwood'] if name=='dreamwood' else []),a={'arbor':8,'praecantatio':4,'herba':6},mods=B,warp=2,refs=[name.upper()])
 infusion(name.upper(),key,'inert_'+name+'_rod',material,['botania:mana_diamond']+['botania:rune_'+r for r in ['mana','water','fire','earth','air','spring','summer','autumn','winter']],{'praecantatio':16,'arbor':32,'victus':16,'sensus':8,'herba':8},3,B)
 mana(name+'_activation',name+'_rod','inert_'+name+'_rod',10000)
entry('CAP_fm_manasteel','manasteel_cap',-4,6,'Манастальные наконечники','Расход vis — 90%. Для ботанических стержней снижает расход маны до 600 за полный vis. После изготовления неактивный наконечник надо зарядить в бассейне за 1000 маны.',parents=['ROD_fm_livingwood'],a={'auram':6,'praecantatio':4,'metallum':4},mods=B,refs=['MANASTEEL'])
shaped('MANASTEEL','CAP_fm_manasteel','inert_manasteel_cap',['NNN','N N'],{'N':'botania:manasteel_nugget'},{a:6 for a in ['aer','terra','ignis','aqua','ordo','perditio']},B);mana('manasteel_activation','manasteel_cap','inert_manasteel_cap',1000)
entry('ROD_fm_dreamwood_staff','dreamwood_staff_core',2,8,'Посох древесины грёз','Вмещает 250 vis каждой стихии. Зарядка маной сохраняет правила стержня грёз: только горячая панель, каждые 40 тиков. Для изготовления нужны два заряженных стержня грёз и драконий камень.',parents=['ROD_fm_dreamwood','ROD_silverwood_staff'],a={'arbor':16,'praecantatio':8,'alienis':6},mods=B,refs=['DREAMSTAFF'])
shaped('DREAMSTAFF','ROD_fm_dreamwood_staff','dreamwood_staff_core',['  D',' B ','B  '],{'D':'botania:dragonstone','B':'dreamwood_rod'},{a:26 for a in ['aer','terra','ignis','aqua','ordo','perditio']},B)
entry('CAP_fm_elementium','elementium_cap',4,8,'Элементиевые наконечники','Расход vis — 80%; расход маны ботанических стержней — 600 за vis. Неактивный наконечник пробуждается на алтаре наполнения.',parents=['ROD_fm_dreamwood_staff'],a={'praecantatio':12,'ignis':8,'aqua':8,'aer':8,'terra':8},mods=B,refs=['ELEMENTIUM_BLANK','ELEMENTIUM'])
shaped('ELEMENTIUM_BLANK','CAP_fm_elementium','inert_elementium_cap',['NNN','N N'],{'N':'botania:elementium_nugget'},{a:27 for a in ['aer','terra','ignis','aqua','ordo','perditio']},B)
infusion('ELEMENTIUM','CAP_fm_elementium','elementium_cap','inert_elementium_cap',['botania:life_essence']+['botania:rune_'+r for r in ['water','fire','earth','air']],{'terra':18,'ignis':18,'aqua':18,'aer':18},1,B)
entry('FM_EUCLIDAISY','euclidaisy',-4,9,'Эвклидова ромашка','Каждые 400 тиков расходует 9000 маны и создаёт эфирную эссенцию с 2 единицами аспекта. С вероятностью 40% выбирается Auram; иначе — случайный доступный аспект. Красный камень останавливает работу.',parents=['FM_BOTANY','INFUSION'],a={'herba':8,'praecantatio':4,'auram':12},mods=B,refs=['EUCLIDAISY'])
infusion('EUCLIDAISY','FM_EUCLIDAISY','euclidaisy','botania:pink_mystical_flower',['thaumcraft:salis_mundus','botania:mana_pearl','botania:pixie_dust','botania:pink_petal','botania:pink_petal','botania:rune_greed','botania:rune_gluttony'],{'auram':8,'alienis':10,'praecantatio':8},8,B)
entry('FM_WHISPERWEED','whisperweed',-2,10,'Шептушник','Каждые 300 тиков расходует 6000 маны на случайного игрока в радиусе 3 блоков и даёт очки исследований. Обычно выбирает первичные аспекты, изредка — составные. В 20% циклов даёт больше знаний и временное искажение. Без игрока мана не тратится. Красный камень отключает цветок.',parents=['FM_BOTANY'],a={'herba':8,'cognitio':10,'sensus':4},mods=B,refs=['WHISPERWEED'])
shapeless('WHISPERWEED','FM_WHISPERWEED','whisperweed',['minecraft:blue_orchid','botania:mana_diamond','botania:pixie_dust','thaumcraft:knowledge_fragment','botania:gray_petal','botania:purple_petal','botania:rune_envy','thaumcraft:zombie_brain'],{a:10 for a in ['aer','terra','ignis','aqua','ordo','perditio']},B)
entry('FM_TAINTHISTLE','tainthistle',0,10,'Заразочертополох','Раз в 80 тиков поглощает газообразный и жидкий флюкс в радиусе 4 блоков. Количество флюкса по современной шкале 1–8 превращается во время горения; запас ограничен 4000 тиками. Во время горения создаёт 3 маны каждые 2 тика и передаёт её привязанному распространителю. Буфер — 150 маны.',parents=['FM_BOTANY','INFUSION'],a={'herba':8,'vitium':10,'praecantatio':4},mods=B,refs=['TAINTHISTLE'])
infusion('TAINTHISTLE','FM_TAINTHISTLE','tainthistle','thaumcraft:taint_tendril',['botania:purple_petal']*3+['botania:rune_lust','botania:rune_mana'],{'vitium':8,'fames':4,'aer':4,'aqua':4},4,B)
entry('FM_BLOODTHORN','bloodthorn',2,10,'Кровавый шип','Каждые 40 маны накладывает Слабость III на 60 тиков на живое существо в радиусе 6 блоков. Игроки исключены. Уже ослабленное существо не расходует ману повторно. Заражённый моб, убитый игроком под Слабостью III, может дать заражённый кровавый осколок. Сам цветок получают из красного мистического цветка в Blood Altar IV за 15000 LP.',parents=['FM_BOTANY','FM_BLOODMAGIC'],a={'herba':6,'victus':4,'venenum':3},mods=BM)
altar('bloodthorn','bloodthorn','botania:red_mystical_flower',15000,4,25,50,BM)

entry('FM_BLOODMAGIC','bloodmagic:sacrificialdagger',0,-4,'Кровавая тауматургия','Используется настоящая сеть душ Blood Magic 3.4.0. Предметы привязываются к UUID владельца; передача другому игроку не меняет сеть. Старые реагенты заменены доступными катализаторами и реагентами этого порта.',parents=['FM_ROOT'],mods=M,auto=True)
entry('ROD_fm_blood','blood_rod',-2,-6,'Кровавый стержень','Вмещает 100 vis. Каждые 100 тиков восполняет до 1 vis каждой первичной стихии за 500 LP; с алхимическим наконечником — 400 LP. При недостатке LP забирает 3 здоровья, пока остаётся больше 6 здоровья. Неактивный стержень нужно насытить в Blood Altar IV за 20000 LP.',parents=['FM_BLOODMAGIC','ROD_silverwood','INFUSION'],a={'victus':5,'aqua':4,'telum':3},mods=M,warp=2,refs=['BLOODROD'])
infusion('BLOODROD','ROD_fm_blood','inert_blood_rod','bloodmagic:masterbloodorb',['bloodmagic:arcaneashes','bloodmagic:infusedslate','bloodmagic:infusedslate','bloodmagic:rawcatalyst','bloodmagic:reagentsight','bloodmagic:reagentwater','bloodmagic:reagentlava','bloodmagic:reagentgrowth','bloodmagic:reagentair'],{'victus':32,'praecantatio':12,'aqua':16},2,M)
altar('blood_rod_activation','blood_rod','inert_blood_rod',20000,4,15,20)
entry('CAP_fm_alchemical','alchemical_cap',-4,-6,'Алхимические наконечники','Общий расход vis — 90%, воды — 80%. На кровавом жезле или посохе стоимость зарядки снижается с 500 до 400 LP за полный vis. Ближний удар кровавым жезлом с этим наконечником накладывает Слабость III.',parents=['ROD_fm_blood','CAP_gold'],a={'victus':4,'instrumentum':1,'aqua':3},mods=M,refs=['ALCHEMICAL'])
infusion('ALCHEMICAL','CAP_fm_alchemical','alchemical_cap','thaumcraft:gold_wand_cap',['bloodmagic:arcaneashes']*3,{'victus':12,'aqua':6},3,M)
entry('ROD_fm_blood_staff','blood_staff_core',-2,-8,'Кровавый посох','Исходный быстрый посох вмещает только 50 vis. Заряжается каждые 25 тиков, только в горячей панели. При нехватке LP забирает 3 здоровья и способен убить владельца. Перед использованием пополните сеть или уберите посох из горячей панели.',parents=['ROD_fm_blood','ROD_silverwood_staff'],a={'victus':8,'aqua':7,'telum':6},mods=M,refs=['BLOODSTAFF'])
shaped('BLOODSTAFF','ROD_fm_blood_staff','blood_staff_core',['  D',' B ','B  '],{'B':'blood_rod','D':'bloodmagic:defaultcrystal'},{a:26 for a in ['aer','terra','ignis','aqua','ordo','perditio']},M)
entry('FM_BLOODWELL','bloody_scribing_tools',2,-6,'Кровавые чернила','Всего 5 порций чернил. ПКМ привязывает к сети владельца. При ношении восполняет одну порцию за 25 LP; при нехватке LP может забрать 2 здоровья, если осталось больше 6. Исследовательский стол использует обычную механику чернил.',parents=['FM_BLOODMAGIC'],a={'victus':5,'cognitio':4,'sensus':3},mods=M,refs=['BLOODWELL'])
shapeless('BLOODWELL','FM_BLOODWELL','bloody_scribing_tools',['minecraft:feather','bloodmagic:life_essence_bucket','minecraft:glass_bottle','bloodmagic:weakbloodorb'],{'aqua':10,'terra':10},M)
entry('FM_BOUNDWELL','bound_scribing_tools',4,-6,'Связанные чернила','На алхимическом массиве свяжите кристальные принадлежности реагентом связывания Blood Magic. Когда 100 порций чернил израсходованы, ПКМ попытается списать 10000 LP через настоящую сеть Blood Magic; при её нехватке используется штатное кровавое повреждение. Выживший владелец получит свежие чернила и 4–7 очков каждой первичной стихии.',parents=['FM_CRYSTALWELL','FM_BLOODMAGIC'],a={'victus':5,'cognitio':4,'vinculum':3},mods=M)
native('bound_scribes',{'type':'bloodmagic:array','baseinput':{'item':'bloodmagic:reagentbinding'},'addedinput':{'item':'forbiddenmagic:crystal_scribing_tools'},'output':{'item':'forbiddenmagic:bound_scribing_tools'},'texture':'bloodmagic:textures/models/alchemyarrays/bindingarray.png'},M)
entry('FM_BLOODRAPIER','rapier_of_the_strix',2,-8,'Рапира Стрикса','Удар по игроку уничтожает максимум из 4000 LP и четверти его текущего запаса. Печать крови на 1200 тиков запрещает пополнение его сети. Физический урон мал; рапира сама восстанавливает 1 прочность в секунду. Искажение снаряжения: 2.',parents=['FM_BLOODMAGIC','INFUSION','VOIDMETAL'],a={'victus':16,'fames':12,'telum':12,'praecantatio':12},mods=M,warp=2,refs=['RAPIER'])
infusion('RAPIER','FM_BLOODRAPIER','rapier_of_the_strix','thaumcraft:void_sword',['thaumcraft:taint_tendril','wrath_shard','gluttony_shard','minecraft:feather','thaumcraft:zombie_brain'],{'victus':18,'fames':32,'telum':8},8,M)
entry('FM_ELDRITCHORB','eldritch_blood_orb',4,-8,'Потусторонняя кровавая сфера','Повышает предел сети Blood Magic до 80000000 LP и считается сферой VI ранга. Основа адаптирована с отсутствующей старой Transcendent Orb на современную Archmage Orb. Первозданная жемчужина в рецепте сохраняется. Искажение снаряжения: 5.',parents=['FM_BLOODMAGIC','INFUSION','PRIMPEARL','VOIDMETAL'],a={'victus':24,'vacuos':32,'vitreus':24,'alienis':12},mods=M,warp=3,refs=['ELDRITCHORB'])
infusion('ELDRITCHORB','FM_ELDRITCHORB','eldritch_blood_orb','bloodmagic:archmagebloodorb',['thaumcraft:primordial_pearl','thaumcraft:eldritch_eye','thaumcraft:balanced_shard','thaumcraft:void_seed','thaumcraft:void_seed','minecraft:nether_star','thaumcraft:void_seed','thaumcraft:void_seed','thaumcraft:balanced_shard','thaumcraft:eldritch_eye'],{'victus':64,'alienis':48,'tenebrae':32,'vacuos':64},2,M)
entry('FM_SANITY','bloodmagic:masterritualstone',0,-10,'Отсроченное безумие','Ритуал виден в ритуальном прорицателе Blood Magic как «Отсроченное безумие». Активация: 20000 LP, кристалл второго уровня, 28 ритуальных камней по исходной схеме. Каждые 600 тиков накладывает защиту от искажения на игроков в радиусе 15; стоимость 15000 LP за обновлённого игрока. Иногда добавляет временное искажение. Без LP защита истекает. Расширения Crystallos в современном Blood Magic нет.',parents=['FM_BLOODMAGIC','ELDRITCHMINOR'],a={'alienis':6,'sano':4,'praecantatio':4},mods=M)

exec((ROOT/'generate_completion.py').read_text(encoding='utf8'),globals())
exec((ROOT/'generate_research_110.py').read_text(encoding='utf8'),globals())
for lang in [ru,en]:
 lang['effect.forbiddenmagic.blood_seal']='Печать крови' if lang is ru else 'Blood Seal'
 lang['ritual.forbiddenmagic.delayed_insanity']='Отсроченное безумие' if lang is ru else 'Plea of Delayed Insanity'
 for name in ['blood','livingwood','dreamwood','tainted','infernal']:
  lang['item.Wand.fm_'+name+'.rod']={'blood':'Кровавый','livingwood':'Живой','dreamwood':'Грёз','tainted':'Заражённый','infernal':'Адский'}[name]
 for name in ['alchemical','manasteel','elementium']:lang['item.Wand.fm_'+name+'.cap']={'alchemical':'Алхимический','manasteel':'Манастальной','elementium':'Элементиевый'}[name]
exec((ROOT/'generate_localization_111.py').read_text(encoding='utf8'),globals())
exec((ROOT/'generate_modernization_120.py').read_text(encoding='utf8'),globals())
exec((ROOT/'generate_recipes_121.py').read_text(encoding='utf8'),globals())
exec((ROOT/'generate_legacy_130.py').read_text(encoding='utf8'),globals())
write(Path('forbiddenmagic/port-data.json'),{'recipes':recipes,'research':research})
write(Path('assets/forbiddenmagic/lang/ru_ru.json'),ru);write(Path('assets/forbiddenmagic/lang/en_us.json'),en)
z=zipfile.ZipFile(ROOT/'reference/original/ForbiddenMagic-1.7.10-0.575.jar');asset_hashes=[]
def copy_asset(old,new):
 data=z.read(old);p=OUT/new;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data);asset_hashes.append({'from':old,'to':str(new),'sha256':hashlib.sha256(data).hexdigest()})
for id,texture in items.items():
 old='assets/forbidden/textures/items/'+texture+'.png';new=Path('assets/forbiddenmagic/textures/item')/(id+'.png');copy_asset(old,new)
 if old+'.mcmeta' in z.namelist():copy_asset(old+'.mcmeta',Path(str(new)+'.mcmeta'))
 write(Path('assets/forbiddenmagic/models/item')/(id+'.json'),{'parent':'minecraft:item/handheld' if id in ['riding_crop','rapier_of_the_strix'] else 'minecraft:item/generated','textures':{'layer0':'forbiddenmagic:item/'+id}})
for name in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:
 copy_asset('assets/forbidden/textures/blocks/'+name+'.png',Path('assets/forbiddenmagic/textures/block')/(name+'.png'))
 write(Path('assets/forbiddenmagic/blockstates')/(name+'.json'),{'variants':{'':{'model':'forbiddenmagic:block/'+name}}})
 write(Path('assets/forbiddenmagic/models/block')/(name+'.json'),{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'forbiddenmagic:block/'+name}})
 write(Path('assets/forbiddenmagic/models/item')/(name+'.json'),{'parent':'forbiddenmagic:block/'+name})
 write(Path('data/forbiddenmagic/loot_table/blocks')/(name+'.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'forbiddenmagic:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for name in ['blood','livingwood','dreamwood','infernal','tainted']:
 copy_asset('assets/forbidden/textures/models/wand_rod_'+name+'.png',Path('assets/forbiddenmagic/textures/models')/('wand_rod_'+name+'.png'))
for name in ['alchemical','manasteel','elementium']:
 copy_asset('assets/forbidden/textures/models/wand_cap_'+name+'.png',Path('assets/forbiddenmagic/textures/models')/('wand_cap_'+name+'.png'))
copy_asset('assets/forbidden/textures/misc/forbidden.png',Path('assets/forbiddenmagic/textures/misc/forbidden.png'))
copy_asset('assets/forbidden/textures/items/tainted_blood_shard.png',Path('assets/forbiddenmagic/textures/mob_effect/blood_seal.png'))
(ROOT/'port-assets.json').write_text(json.dumps(asset_hashes,indent=2)+'\n')
print(f'{len(items)} item models, 4 flowers, {len(recipes)} native TC recipes, {len(research)} research entries')
exec((ROOT/'generate_completion_assets.py').read_text(encoding='utf8'),globals())
finalize_130_assets()
exec((ROOT/'generate_visual_140.py').read_text(encoding='utf8'),globals())
