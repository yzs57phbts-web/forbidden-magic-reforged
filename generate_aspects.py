"""Curated semantic families. Registry IDs are checked against a real 1.21.1 server dump.
No runtime name heuristics; decorative derivatives stay with Thaumcraft's recipe system.
"""
import json
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parent
inventory = json.loads((ROOT / 'reference/baseline-registry.json').read_text(encoding='utf-8'))
items = {r['id']: r for r in inventory if r['kind'] != 'entity'}
entities = {r['id']: r for r in inventory if r['kind'] == 'entity'}
rules = []

def add(mod, name, ids, aspects, reason, kind='item'):
    ids = ids.split() if isinstance(ids, str) else ids
    ids = [x if ':' in x else mod + ':' + x for x in ids]
    available = entities if kind == 'entity' else items
    missing = set(ids) - available.keys()
    assert not missing, (name, sorted(missing))
    assert ids and 1 <= len(aspects) <= 5 and all(0 < n <= 16 for n in aspects.values()), name
    rules.append(dict(mod=mod, family=name, kind=kind, targets=ids, aspects=aspects, reason=reason))

def matching(mod, expression):
    import re
    return sorted(x for x in items if x.startswith(mod + ':') and re.fullmatch(expression, x.split(':')[1]))

def b(name, ids, a, why): add('botania', name, ids, a, why)
def w(name, ids, a, why): add('witchery', name, ids, a, why)
def m(name, ids, a, why): add('bloodmagic', name, ids, a, why)
def t(name, ids, a, why): add('twilightforest', name, ids, a, why)
def mob(mod, ids, a, why): add(mod, ids.replace(' ', '/'), ids, a, why, 'entity')

# Botania: resources first, so ordinary tools/decorations can inherit from recipes.
b('Мистические лепестки', matching('botania', r'(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black)_petal'), {'herba':1,'sensus':1,'praecantatio':1}, 'Один состав для всех цветов; цвет не создаёт новый источник редких аспектов.')
b('Мистические цветы', matching('botania', r'(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black)_mystical_flower'), {'herba':2,'sensus':1,'praecantatio':1}, 'Одинаковая природа всех цветовых вариантов.')
b('Живой камень', 'livingrock', {'terra':2,'victus':1}, 'Оживлённый камень, не источник души или порчи.')
b('Живая древесина', 'livingwood_log livingwood', {'arbor':4,'victus':1}, 'Древесина с жизненной силой.')
b('Древесина грёз', 'dreamwood_log dreamwood', {'arbor':4,'praecantatio':2}, 'Эльфийская магическая древесина.')
b('Манасталь', 'manasteel_ingot', {'metallum':3,'praecantatio':1}, 'Металл, пропитанный маной.')
b('Террасталь', 'terrasteel_ingot', {'metallum':4,'praecantatio':2,'victus':1}, 'Сложный сплав с жизненной энергией, без избыточной награды за ранг.')
b('Элементиевый слиток', 'elementium_ingot', {'metallum':3,'praecantatio':2}, 'Эльфийский магический металл.')
b('Алмаз маны', 'mana_diamond', {'vitreus':4,'lucrum':4,'praecantatio':1}, 'Состав алмаза и небольшая магическая добавка.')
b('Жемчуг маны', 'mana_pearl', {'alienis':3,'iter':2,'praecantatio':1}, 'Сохраняет природу эндер-жемчуга.')
b('Пыль маны', 'mana_powder', {'potentia':2,'praecantatio':1}, 'Порошок, проводящий ману.')
b('Драконий камень', 'dragonstone', {'vitreus':4,'praecantatio':2}, 'Магический камень, не кровь дракона.')
b('Дух Гайи', 'life_essence', {'spiritus':4,'praecantatio':4,'victus':2}, 'Дух природного стража; Auram не назначается всей мане подряд.')
b('Слиток духа Гайи', 'gaia_ingot', {'metallum':4,'spiritus':4,'praecantatio':4}, 'Металл и связанный дух.')
b('Ткань маны', 'manaweave_cloth mana_string', {'pannus':2,'praecantatio':1}, 'Магическое волокно.')
b('Лексика Ботании', 'lexicon', {'cognitio':4,'herba':1,'praecantatio':1}, 'Книга ботанических знаний.')
b('Планшет маны', 'mana_tablet', {'terra':2,'vacuos':2,'praecantatio':2}, 'Носитель маны; запас маны не умножает аспекты.')
b('Лотосы маны', 'black_lotus blacker_lotus', {'herba':2,'praecantatio':2,'potentia':2}, 'Один умеренный состав для двух вариантов лотоса.')
for id, extra in [('rune_water','aqua'),('rune_fire','ignis'),('rune_earth','terra'),('rune_air','aer')]:
    b('Стихийная руна '+id, id, {'praecantatio':2,extra:2}, 'Руна конкретной стихии.')
b('Сезонные руны', 'rune_spring rune_summer rune_autumn rune_winter', {'praecantatio':2,'herba':2,'motus':1}, 'Единое семейство природного цикла без четырёх выдуманных наборов.')
b('Руна маны', 'rune_mana', {'praecantatio':3,'potentia':2}, 'Магическая энергия.')
for id, aspect in [('lust','luxuria'),('gluttony','gula'),('greed','lucrum'),('sloth','desidia'),('wrath','ira'),('envy','invidia'),('pride','superbia')]:
    b('Руна греха '+id, 'rune_'+id, {'praecantatio':2,aspect:2}, 'Прямое тематическое соответствие названия руны; жадность остаётся Lucrum.')
b('Распространители маны', 'mana_spreader redstone_spreader elven_spreader gaia_spreader', {'arbor':3,'motus':2,'praecantatio':2}, 'Одинаковая функция независимо от уровня.')
b('Бассейны маны', 'mana_pool diluted_pool fabulous_pool', {'terra':3,'vacuos':2,'praecantatio':2}, 'Один состав для обычного, разбавленного и декоративного бассейна; creative исключён.')
b('Пилоны', 'mana_pylon natura_pylon gaia_pylon', {'metallum':3,'praecantatio':3,'potentia':2}, 'Один умеренный состав для трёх пилонов.')
b('Рунический алтарь', 'runic_altar', {'terra':3,'praecantatio':3,'fabrico':2}, 'Ритуальное изготовление рун.')
b('Лепестковые аптекари', matching('botania', r'apothecary_.*'), {'terra':3,'herba':2,'fabrico':1}, 'Один состав для всех материалов аптекаря.')
b('Портал в Альфхейм', 'alfheim_portal', {'arbor':4,'iter':4,'praecantatio':3}, 'Межмировая торговля через деревянный портал.')
b('Катализаторы', 'alchemy_catalyst conjuration_catalyst', {'terra':2,'permutatio':3,'praecantatio':2}, 'Магическое преобразование; без аспектов пустоты за каждую копию предмета.')
b('Мана-искра', 'spark corporea_spark corporea_spark_master', {'aer':1,'motus':2,'praecantatio':2}, 'Магическая передача; сущности-искры отдельно не сканируются как мобы.')
b('Чистая маргаритка', 'pure_daisy floating_pure_daisy', {'herba':2,'permutatio':2,'praecantatio':1}, 'Преобразует камень и древесину; плавающая форма идентична.')
for name, ids, a, why in [
 ('Огненные цветы','endoflame floating_endoflame thermalily floating_thermalily',{'herba':2,'ignis':2,'praecantatio':1},'Питание топливом или лавой.'),
 ('Водогортензия','hydroangeas floating_hydroangeas',{'herba':2,'aqua':2,'praecantatio':1},'Питание водой.'),
 ('Пищевые цветы','gourmaryllis floating_gourmaryllis kekimurus floating_kekimurus',{'herba':2,'gula':2,'praecantatio':1},'Поглощают еду или торт; один набор для близкой функции.'),
 ('Роза Аркана','rosa_arcana floating_rosa_arcana',{'herba':2,'cognitio':2,'praecantatio':1},'Поглощает опыт.'),
 ('Рудоцветы','orechid floating_orechid orechid_ignem floating_orechid_ignem',{'herba':2,'perfodio':2,'permutatio':1},'Создают руды; адская форма не получает бесплатный Infernus только за место работы.'),
 ('Цветы исцеления','fallen_kanade floating_fallen_kanade',{'herba':2,'sano':2,'praecantatio':1},'Исцеляют окружающих.'),
 ('Цветы сбора','hopperhock floating_hopperhock hopperhock_chibi floating_hopperhock_chibi',{'herba':2,'vacuos':1,'motus':1},'Собирают предметы; мини- и плавающие формы не усиливаются.'),
]: b(name,ids,a,why)

# Blood Magic: slates, orbs and crystals are intentionally family-wide, not a tier ladder.
m('Таблички', 'blankslate reinforcedslate infusedslate demonslate etherealslate', {'terra':2,'victus':1,'praecantatio':1}, 'Все пять табличек имеют один состав. Новые уровни не открывают отдельные редкие аспекты.')
m('Кровавые сферы', 'weakbloodorb apprenticebloodorb magicianbloodorb masterbloodorb archmagebloodorb', {'vitreus':3,'victus':2,'praecantatio':2}, 'Один состав для всех рангов; ёмкость и текущие LP не считаются содержимым предмета.')
m('Воля демона', matching('bloodmagic',r'basemonstersoul(_(corrosive|destructive|steadfast|vengeful))?'), {'spiritus':2,'infernus':1}, 'Все оттенки воли одинаковы; не масштабируется от численного запаса.')
m('Кристаллы воли', 'defaultcrystal corrosivecrystal destructivecrystal steadfastcrystal vengefulcrystal', {'vitreus':3,'spiritus':1,'infernus':1}, 'Единый состав цветовых состояний воли.')
m('Кластеры воли', 'rawdemoncrystal corrosivedemoncrystal destructivedemoncrystal steadfastdemoncrystal vengefuldemoncrystal', {'vitreus':4,'spiritus':2,'infernus':1}, 'Материальный кристаллический кластер, без пяти отдельных редких наборов.')
m('Камни душ', 'soulgempetty soulgemlesser soulgemcommon soulgemgreater', {'vitreus':3,'vinculum':2,'praecantatio':1}, 'Носители воли. Пустому камню не приписывается содержимое или запас воли.')
m('Кровавый алтарь', 'altar', {'terra':4,'victus':3,'praecantatio':3}, 'Ритуальная обработка жизненной эссенции.')
m('Благовонный алтарь', 'incensealtar', {'terra':3,'herba':2,'praecantatio':2}, 'Растительные благовония и ритуал.')
m('Рунические блоки алтаря', matching('bloodmagic',r'(blankrune|(speed|sacrifice|selfsacrifice|altarcapacity|orbcapacity|bettercapacity|dislocation|acceleration|charging)rune2?)'), {'terra':2,'praecantatio':1,'victus':1}, 'Один небольшой состав для обычных и усиленных рун; функция учитывается в рецептах, без коллекции редких аспектов.')
m('Ритуальные камни', 'ritualstone masterritualstone airritualstone waterritualstone fireritualstone earthritualstone duskritualstone lightritualstone', {'terra':2,'praecantatio':2}, 'Единое семейство частей ритуала.')
m('Жертвенные ножи', 'sacrificialdagger daggerofsacrifice', {'metallum':2,'telum':2,'victus':1}, 'Инструмент жертвоприношения; без предположения о врождённой порче.')
m('Ловушка душ', 'soulsnare', {'vinculum':2,'spiritus':1,'praecantatio':1}, 'Связывание воли существа.')
m('Кузница адского огня', 'soulforge', {'terra':3,'fabrico':3,'infernus':2}, 'Демоническое изготовление, а не обычная печь.')
m('Демонические устройства', 'demoncrucible demoncrystallizer demonpylon', {'terra':3,'infernus':2,'praecantatio':2}, 'Единая группа устройств обработки демонической воли.')
m('Алхимические устройства', 'alchemytable alchemicalreactionchamber', {'fabrico':3,'permutatio':2,'praecantatio':2}, 'Алхимическая обработка.')
m('Адский слиток', 'ingot_hellforged', {'metallum':3,'infernus':1}, 'Демонический металл; декоративные блоки выводятся из рецептов.')
m('Демонит', 'rawdemonite', {'terra':1,'metallum':2,'infernus':1}, 'Сырой демонический металл.')
m('Магический пепел', 'arcaneashes', {'ignis':1,'praecantatio':2}, 'Ритуальная зола.')
m('Лавовый кристалл', 'lavacrystal', {'vitreus':2,'ignis':2,'potentia':1}, 'Топливный кристалл.')
m('Кристаллы активации', 'activationcrystalweak activationcrystalawakened', {'vitreus':2,'praecantatio':2,'potentia':1}, 'Одинаковая ритуальная активация; creative исключён.')
m('Кровь в ведре', 'life_essence_bucket', {'metallum':1,'victus':3,'aqua':1}, 'Физическое ведро жизненной эссенции; не приписывает объём LP.')
m('Телеустановщик', 'teleposer', {'machina':3,'iter':3,'praecantatio':2}, 'Перемещение блоков.')
m('Фокусы телеустановки', 'teleposerfocus enhancedteleposerfocus reinforcedteleposerfocus', {'vitreus':2,'iter':2,'praecantatio':1}, 'Один состав трёх радиусов действия.')
m('Сигилы видения', 'divinationsigil seersigil', {'sensus':2,'praecantatio':2,'terra':1}, 'Информация об алтаре и окружении.')
for id, aspect in [('watersigil','aqua'),('lavasigil','ignis'),('airsigil','aer'),('voidsigil','vacuos'),('bloodlightsigil','lux'),('growthsigil','herba'),('miningsigil','perfodio'),('icesigil','gelum'),('telepositionsigil','iter'),('sigilofmagnetism','motus'),('sigilofholding','vinculum'),('sigilofsuppression','perditio')]:
    m('Функциональный сигил '+id, id, {'terra':1,'praecantatio':2,aspect:2}, 'Аспект фактической функции сигила, а не его уровня.')
m('Ритуальные инструменты', 'ritualdiviner ritualdivinerdusk ritualtinkerer', {'instrumentum':2,'praecantatio':2}, 'Одно семейство настройки ритуала.')
m('Живая броня', 'livinghelmet livingplate livingleggings livingboots', {'tutamen':3,'victus':2,'praecantatio':1}, 'Единый умеренный состав всех частей; боевая статистика не меняется.')

# Witchery: concentrate on actual reagents, rituals and creatures, not oxidation/colour forms.
for ids, a, why in [
 ('mandrake_root',{'herba':2,'spiritus':1},'Корень живого магического растения.'),
 ('belladonna_flower wolfsbane wormwood',{'herba':2,'venenum':1},'Ядовитые ритуальные травы.'),
 ('water_artichoke_globe',{'herba':2,'aqua':1},'Водное растение.'),
 ('rowan_berries garlic',{'herba':1,'victus':1},'Обычная растительная пища, без автоматической магии.'),
 ('wood_ash',{'ignis':1,'terra':1},'Древесная зола.'),
 ('wool_of_bat',{'pannus':1,'bestia':1,'volatus':1},'Материал летучей мыши.'),
 ('toe_of_frog tongue_of_dog',{'corpus':2,'bestia':1},'Часть тела животного.'),
 ('owlets_wing',{'volatus':2,'bestia':1},'Крыло совы.'),
 ('ent_twig',{'arbor':2,'victus':1},'Ветвь живого дерева.'),
 ('bottle_of_blood',{'vitreus':1,'victus':2,'corpus':1},'Кровь в стекле.'),
 ('demons_blood',{'victus':2,'infernus':2},'Явно демоническая кровь, а не всякая кровь.'),
 ('demon_heart',{'corpus':3,'victus':2,'infernus':2},'Сердце демона.'),
 ('spectral_dust phantom_vapor',{'spiritus':2,'praecantatio':1},'Призрачная субстанция.'),
 ('whiff_of_magic',{'aer':1,'praecantatio':2},'Магическое испарение.'),
 ('foul_fume',{'aer':1,'venenum':2},'Вредные испарения.'),
 ('breath_of_the_goddess',{'aer':2,'victus':1},'Жизненное дыхание.'),
 ('exhale_of_the_horned_one',{'aer':1,'bestia':1,'praecantatio':1},'Ритуальное дыхание Рогатого, без автоматической демонизации.'),
 ('hint_of_rebirth',{'victus':2,'sano':1},'Возрождение и восстановление.'),
 ('odor_of_purity tear_of_the_goddess',{'sano':2,'aqua':1},'Очищение и исцеление.'),
 ('oil_of_vitriol',{'venenum':2,'perditio':1},'Едкое алхимическое вещество.'),
 ('mutandis mutandis_extremis',{'herba':2,'permutatio':2},'Один состав преобразующей смеси независимо от усиления.'),
 ('attuned_stone',{'vitreus':3,'praecantatio':2},'Магически настроенный камень; заряд не увеличивает состав.'),
 ('necromantic_stone',{'terra':1,'exanimis':2,'spiritus':1},'Некромантический камень.'),
 ('infernal_animus',{'spiritus':2,'infernus':2},'Адская одушевляющая сущность.'),
 ('refined_evil',{'infernus':2,'praecantatio':2},'Очищенная демоническая сущность; не таумкрафтовская порча.'),
 ('soul_of_the_world',{'spiritus':3,'victus':2,'praecantatio':2},'Душа мира.'),
 ('spirit_of_otherwhere',{'spiritus':2,'iter':2},'Связь с иным местом.'),
 ('condensed_fear',{'sensus':2,'tenebrae':1},'Страх, а не отдельная выдуманная стихия.'),
 ('focused_will',{'cognitio':2,'praecantatio':1},'Сосредоточенная воля.'),
 ('flying_ointment',{'volatus':2,'praecantatio':1},'Полёт.'),
 ('taglock',{'vitreus':1,'vinculum':1},'Носитель связи; пустой образец не содержит чужой души.'),
 ('arthana',{'metallum':2,'telum':2,'praecantatio':1},'Ритуальный клинок.'),
 ('broom',{'arbor':2,'volatus':2,'praecantatio':1},'Летающая метла.'),
 ('waystone',{'terra':2,'iter':2,'praecantatio':1},'Камень привязки перемещения.'),
 ('seer_stone crystal_ball',{'vitreus':3,'sensus':2,'praecantatio':1},'Ясновидение.'),
 ('koboldite_ingot',{'metallum':3,'lucrum':1},'Кобольдит — металл хобгоблинов.'),
 ('demonic_contract',{'pannus':1,'vinculum':2,'infernus':2},'Демонический договор, не просто бумага.'),
 ('guidebook torn_page',{'cognitio':2,'praecantatio':1},'Семейство магических записей.'),
 ('ritual_chalk golden_ritual_chalk infernal_chalk otherwhere_chalk binding_chalk',{'terra':1,'praecantatio':2},'Все виды ритуального мела — один состав без лестницы редких аспектов.'),
 ('poppet armor_protection_poppet corrupt_poppet death_protection_poppet hunger_protection_poppet vampiric_poppet voodoo_poppet voodoo_protection_poppet',{'pannus':2,'vinculum':2,'praecantatio':1},'Одна семья кукол; привязка не создаёт физическую душу внутри.'),
 ('altar',{'terra':3,'praecantatio':3,'herba':1},'Природный ритуальный алтарь.'),
 ('cauldron',{'metallum':3,'aqua':2,'permutatio':2},'Ведьмин котёл для смешивания.'),
 ('distillery',{'metallum':3,'permutatio':2,'aer':1},'Дистилляция испарений.'),
 ('brazier censer',{'metallum':2,'ignis':2,'praecantatio':1},'Ритуальное горение и дым.'),
 ('spinning_wheel',{'arbor':3,'machina':2,'pannus':1},'Прядильный механизм.'),
 ('blood_crucible',{'terra':3,'victus':2,'praecantatio':2},'Сосуд крови.'),
 ('mirror',{'vitreus':3,'sensus':2,'praecantatio':1},'Магическое отражение; служебный dimensional_mirror не имеет собственного предмета.'),
 ('bear_trap critter_snare cage',{'metallum':2,'vinculum':2},'Удержание животных.'),
]: w(ids.replace(' ','/'),ids,a,why)
w('Ведьмины печи', matching('witchery',r'(iron|((waxed_)?(exposed_|weathered_|oxidized_)?copper))_witches_oven'), {'metallum':3,'ignis':2,'machina':1}, 'Один состав всех материалов и окислений печи.')
w('Медные котлы', matching('witchery',r'(waxed_)?(exposed_|weathered_|oxidized_)?copper_cauldron'), {'metallum':3,'aqua':2,'permutatio':2}, 'Одинаковые аспекты всех степеней окисления.')

# Twilight Forest: unique resources and devices; building shapes stay recipe-derived.
for ids,a,why in [
 ('liveroot',{'herba':2,'victus':2},'Живой корень.'),
 ('raw_ironwood',{'metallum':2,'arbor':2,'victus':1},'Железодревесный материал.'),
 ('ironwood_ingot',{'metallum':3,'arbor':1,'praecantatio':1},'Сплав железа и живой древесины.'),
 ('steeleaf_ingot',{'metallum':3,'herba':2},'Прочный листовой материал.'),
 ('knightmetal_ingot',{'metallum':4,'tutamen':1},'Прочный рыцарский металл.'),
 ('fiery_ingot',{'metallum':3,'ignis':2},'Огненный сплав.'),
 ('fiery_blood fiery_tears',{'victus':2,'ignis':2},'Один состав огненных жидкостей; не адская кровь.'),
 ('naga_scale',{'bestia':2,'tutamen':2},'Защитная чешуя наги.'),
 ('arctic_fur alpha_yeti_fur',{'pannus':2,'gelum':2},'Один состав холодного меха.'),
 ('raven_feather',{'volatus':2,'bestia':1},'Перо птицы без выдуманной тёмной магии.'),
 ('torchberries',{'herba':1,'lux':2},'Светящиеся ягоды.'),
 ('carminite',{'vitreus':2,'machina':2,'praecantatio':1},'Магический компонент башенных механизмов.'),
 ('transformation_powder',{'permutatio':2,'praecantatio':1},'Изменение существ.'),
 ('magic_beans',{'herba':2,'victus':1,'praecantatio':1},'Магическое растение.'),
 ('magic_map_focus maze_map_focus',{'sensus':2,'cognitio':1,'praecantatio':1},'Одна группа компонентов карт.'),
 ('magic_map filled_magic_map maze_map filled_maze_map ore_map filled_ore_map',{'cognitio':2,'sensus':2,'iter':1},'Один состав пустых и заполненных карт, без награды за каждый вид карты.'),
 ('charm_of_life_1 charm_of_life_2',{'sano':2,'victus':2,'praecantatio':1},'Единая семья оберегов жизни.'),
 ('charm_of_keeping_1 charm_of_keeping_2 charm_of_keeping_3',{'vinculum':2,'vacuos':1,'praecantatio':1},'Единая семья удержания инвентаря.'),
 ('twilight_scepter',{'telum':2,'praecantatio':3},'Магический выстрел.'),
 ('lifedrain_scepter',{'telum':2,'victus':2,'fames':1},'Похищение жизненной силы.'),
 ('zombie_scepter',{'exanimis':2,'vinculum':2,'praecantatio':1},'Призыв и управление нежитью.'),
 ('fortification_scepter',{'tutamen':3,'praecantatio':2},'Защитный барьер.'),
 ('ore_magnet',{'perfodio':2,'motus':2},'Перемещение руд.'),
 ('ore_meter',{'perfodio':2,'sensus':2},'Поиск руд.'),
 ('crumble_horn',{'instrumentum':2,'perditio':2},'Разрушение породы.'),
 ('moonworm_queen',{'bestia':2,'lux':2},'Создаёт светящихся лунных червей.'),
 ('peacock_feather_fan',{'volatus':2,'motus':2},'Веер, создающий подъём.'),
 ('lamp_of_cinders',{'ignis':3,'instrumentum':2},'Сжигает терновник.'),
 ('cube_of_annihilation',{'vacuos':3,'perditio':3,'praecantatio':2},'Магическое уничтожение материи.'),
 ('ice_bomb',{'gelum':2,'perditio':1},'Замораживающий снаряд.'),
 ('uncrafting_table',{'arbor':3,'fabrico':2,'permutatio':2},'Разбор и перекрафт.'),
 ('time_log_core',{'arbor':4,'motus':2,'praecantatio':2},'Ускорение роста; отдельного аспекта времени в TC4 нет.'),
 ('transformation_log_core',{'arbor':4,'permutatio':2,'praecantatio':2},'Изменяет биом.'),
 ('mining_log_core',{'arbor':4,'perfodio':2,'praecantatio':2},'Перемещает руды.'),
 ('sorting_log_core',{'arbor':4,'ordo':2,'praecantatio':2},'Упорядочивает предметы.'),
]: t(ids.replace(' ','/'),ids,a,why)

mob('botania','pixie',{'aer':1,'volatus':2,'praecantatio':2},'Маленькое летающее магическое существо.')
mob('botania','doppleganger',{'spiritus':4,'praecantatio':4,'victus':2,'herba':2},'Дух-страж Гайи.')
mob('botania','pink_wither',{'exanimis':3,'victus':2,'praecantatio':2},'Особая форма иссушителя, без обычного разрушительного набора.')
mob('botania','manaseal_creeper',{'herba':2,'vinculum':2,'praecantatio':1},'Крипер подавления маны.')
for ids,a,why in [
 ('owl',{'bestia':2,'volatus':2,'sensus':1},'Ночная птица; не получает Tenebrae только за ночной образ жизни.'),
 ('ent',{'arbor':4,'victus':2,'bestia':1},'Одушевлённое дерево.'),
 ('mandrake',{'herba':2,'spiritus':1,'sensus':1},'Кричащий живой корень.'),
 ('coven_witch baba_yaga',{'humanus':2,'praecantatio':3,'cognitio':2},'Ведьмы и их знания; без автоматического Infernus всем ведьмам.'),
 ('demon mirror_demon',{'infernus':3,'spiritus':2,'praecantatio':2},'Явные демоны.'),
 ('imp',{'infernus':2,'bestia':2,'praecantatio':1},'Малый демон.'),
 ('poltergeist banshee spectre',{'spiritus':3,'exanimis':2,'motus':1},'Одно семейство духов; без множества наборов за разновидности.'),
 ('spectral_pig',{'spiritus':2,'bestia':2},'Призрачное животное.'),
 ('werewolf',{'bestia':3,'humanus':2,'ira':1},'Человек-зверь с яростью.'),
 ('vampire',{'exanimis':2,'humanus':2,'fames':2},'Нежить, питающаяся кровью.'),
 ('nightmare',{'bestia':2,'tenebrae':2,'sensus':2},'Воплощённый кошмар.'),
 ('parasitic_louse',{'bestia':1,'fames':2},'Паразит.'),
 ('death',{'mortuus':4,'spiritus':3,'alienis':2},'Воплощение смерти; не источник всех редких аспектов сразу.'),
 ('horned_huntsman',{'bestia':3,'telum':3,'praecantatio':2},'Магический охотник.'),
 ('lilith',{'infernus':3,'superbia':2,'praecantatio':3},'Демонический босс.'),
 ('leonard',{'infernus':3,'cognitio':2,'praecantatio':3},'Демонический покровитель колдовства.'),
 ('hunter',{'humanus':2,'telum':2},'Человек-охотник.'),
 ('hobgoblin',{'humanus':2,'lucrum':1,'perfodio':1},'Собиратель и рудокоп.'),
 ('mog gulg',{'humanus':2,'praecantatio':2,'telum':1},'Парные боссы хобгоблинов; классы MogEntity/GulgEntity наследуют AbstractHobgoblinBossEntity.'),
 ('elle',{'humanus':2,'praecantatio':2,'spiritus':1},'Сверхъестественная форма: настоящий ElleEntity превращается в Lilith после пребывания в Аду.'),
]: mob('witchery',ids,a,why)
for ids,a,why in [
 ('boar deer dwarf_rabbit squirrel',{'bestia':2,'victus':1},'Обычные лесные животные; кролики не получают Luxuria.'),
 ('bighorn_sheep',{'bestia':2,'pannus':2},'Шерстяное животное.'),
 ('tiny_bird raven penguin',{'bestia':2,'volatus':1},'Одна группа птиц, без набора за каждый цвет.'),
 ('hostile_wolf',{'bestia':3,'telum':1},'Враждебный волк.'),
 ('mist_wolf',{'bestia':3,'tenebrae':1,'sensus':1},'Волк с ослепляющим туманом.'),
 ('winter_wolf yeti',{'bestia':3,'gelum':2},'Холодолюбивые существа.'),
 ('alpha_yeti',{'bestia':4,'gelum':4,'telum':2},'Крупный ледяной босс.'),
 ('hedge_spider swarm_spider carminite_broodling',{'bestia':2,'venenum':1},'Одна группа небольших пауков.'),
 ('king_spider',{'bestia':4,'venenum':1,'superbia':1},'Крупный паук-вожак; умеренная отсылка к FM.'),
 ('fire_beetle',{'bestia':2,'ignis':2},'Огненный жук.'),
 ('slime_beetle',{'bestia':2,'limus':2},'Слизневый жук.'),
 ('pinch_beetle',{'bestia':3,'vinculum':2},'Захватывает жертву; Luxuria за захват не назначается.'),
 ('helmet_crab',{'bestia':2,'tutamen':1,'metallum':1},'Краб с металлической защитой.'),
 ('mosquito_swarm',{'bestia':2,'volatus':1,'fames':1},'Кровососущие насекомые.') ,
 ('kobold',{'humanus':2,'lucrum':1},'Мелкий гуманоид-собиратель.'),
 ('redcap redcap_sapper',{'humanus':2,'perfodio':1,'telum':1},'Одно семейство гоблинов-рудокопов.'),
 ('blockchain_goblin lower_goblin_knight upper_goblin_knight',{'humanus':2,'metallum':2,'telum':2},'Одно семейство вооружённых гоблинов.'),
 ('minotaur',{'bestia':3,'humanus':1,'telum':2},'Вооружённый человек-зверь.'),
 ('minoshroom',{'bestia':4,'herba':2,'telum':2},'Грибной минотавр.') ,
 ('troll giant_miner armored_giant',{'humanus':3,'terra':2,'telum':2},'Крупные гуманоиды; одна группа без отдельных наборов за броню.'),
 ('skeleton_druid',{'exanimis':3,'herba':1,'praecantatio':1},'Скелет с друидической магией.'),
 ('wraith knight_phantom',{'exanimis':3,'spiritus':2,'telum':1},'Призрачная нежить.'),
 ('lich',{'exanimis':4,'praecantatio':4,'spiritus':2},'Маг-некромант.') ,
 ('lich_minion loyal_zombie rising_zombie',{'exanimis':3,'humanus':1},'Один состав зомби независимо от способа появления.'),
 ('death_tome',{'cognitio':3,'volatus':1,'praecantatio':2},'Летающая заколдованная книга.'),
 ('maze_slime',{'limus':3,'aqua':1},'Лабиринтный слизень.'),
 ('towerwood_borer',{'bestia':1,'arbor':1},'Древоточец.'),
 ('carminite_golem',{'machina':3,'metallum':2,'praecantatio':1},'Башенный конструкт.') ,
 ('carminite_ghastguard carminite_ghastling',{'exanimis':2,'volatus':2,'ignis':1},'Башенные гасты; не объявляются адскими только из-за формы.') ,
 ('snow_guardian stable_ice_core unstable_ice_core ice_crystal',{'gelum':3,'praecantatio':2},'Одна группа ледяных магических стражей.'),
 ('snow_queen',{'gelum':4,'praecantatio':3,'superbia':1},'Ледяная королева.') ,
 ('naga',{'bestia':4,'tutamen':2,'motus':2},'Подвижный чешуйчатый босс.') ,
 ('hydra',{'bestia':4,'ignis':4,'victus':2},'Многоголовый огненный зверь.') ,
 ('ur_ghast',{'exanimis':4,'volatus':3,'ignis':3,'ira':1},'Гневный летающий босс башни.') ,
 ('quest_ram',{'bestia':3,'pannus':3,'praecantatio':2},'Магический баран, принимающий шерсть.') ,
 ('adherent',{'humanus':2,'praecantatio':3},'Магический гуманоид замка.'),
]: mob('twilightforest',ids,a,why)

seen=set()
for r in rules:
    for id in r['targets']:
        key=(r['kind'],id)
        assert key not in seen, ('duplicate',id)
        seen.add(key)

excluded = {
 'witchery:insanity': 'Служебное воплощение безумия: не отдельный источник сканирования.',
 'twilightforest:plateau_boss': 'Недоделанный/служебный босс не получает придуманный состав.',
 'twilightforest:harbinger_cube': 'Техническое/незавершённое существо, состав не выдуман.',
 'twilightforest:roving_cube': 'Технический механизм, а не обычный живой моб.',
}
payload={'schema':1,'rules':rules,'excluded_entities':excluded}
out=ROOT/'src/main/resources/forbiddenmagic/aspects.json'
out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
counts=Counter((r['mod'],r['kind']) for r in rules for _ in r['targets'])
print('Families',len(rules),'targets',len(seen),'counts',dict(counts))
text=['FORBIDDEN MAGIC: REFORGED — АСПЕКТЫ, ЭТАП 1',
 'Только Minecraft 1.21.1 / NeoForge. Это первый этап ремейка, не полный перенос механик FM.',
 'Ручные составы сгруппированы по смыслу; цвет, ранг, плавание, мини-форма и окисление не создают отдельные редкие наборы.',
 'Таблички Blood Magic: все 5 = Terra 2, Victus 1, Praecantatio 1.',
 'Остальное оформление, инструменты и производные предметы остаются штатному выводу Thaumcraft из рецептов.',
 'Назначения статические: LP/мана/воля/привязанный игрок не увеличивают ценность носителя.',
 'Blood Magic данного JAR не регистрирует живых мобов; выдуманных EntityType нет.',
 'Новые аспекты/текстуры не дублируются: включаются 7 уже предусмотренных аспектов FM в выбранном Thaumcraft.',
 'Vanilla/Thaumcraft назначения не меняются. Порча Vitium не ставится просто за тёмную магию.',
 'Это умеренная авторская интерпретация, не заявление об исходном балансе FM 1.7.10.', '']
for mod in ['botania','bloodmagic','witchery','twilightforest']:
    selected=[r for r in rules if r['mod']==mod]
    text += [mod.upper(),f"Семейств: {len(selected)}; предметов/блоков: {counts[mod,'item']}; мобов: {counts[mod,'entity']}",'']
    for r in selected:
        text += [r['family']+' ['+r['kind']+']', ', '.join(k+' '+str(v) for k,v in r['aspects'].items()),r['reason'],'  '+', '.join(r['targets']),'']
text+=['ИСКЛЮЧЕНИЯ']+[k+' — '+v for k,v in excluded.items()]
(ROOT/'ASPECTS-RU.txt').write_text('\n'.join(text)+'\n',encoding='utf-8')
