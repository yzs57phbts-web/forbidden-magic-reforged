"""Retain final0.575 Russian, translate genuine gaps, restore source page sequence."""
import extract_localization_0575 as original_localization
original_lang=original_localization.langs
missing_ru=json.loads((ROOT/'localization/ru-missing-0575.json').read_text(encoding='utf8'))
missing_required={k for k,v in original_localization.relevant.items() if not v['ru'] or not re.search('[А-Яа-яЁё]',v['ru'])}
assert set(missing_ru)==missing_required,(set(missing_ru)^missing_required)
localization_provenance={}
def adapted_markup(text):return text.replace('<IMG>forbidden:','<IMG>forbiddenmagic:')
for k,v in original_localization.relevant.items():
 translated=k in missing_required
 ru[k]=adapted_markup(missing_ru[k] if translated else v['ru']);en[k]=adapted_markup(v['en'])
 localization_provenance[k]={'russian':'translated_from_original_english' if translated else 'original_russian_verbatim','english':'original_english_verbatim'}

page_recipes={
 'Crystalwell':'FM:CRYSTALWELL','Primewell':'FM:PRIMEWELL','TransEmerald':'FM:EMERALD','BlackFlower':'FM:ROSE','BlackInk':'forbiddenmagic:black_dye','RidingCrop':'forbiddenmagic:riding_crop',
 'Consuming':'FM:ENCH_CONSUMING','Educational':'FM:ENCH_EDUCATIONAL','SubCollar':'FM:COLLAR','SkullAxe':'FM:SKULLAXE','RingFood':'FM:RING','ArcaneCake':'FM:CAKE','FocusBlink':'FM:BLINK',
 'MorphPick':'FM:MORPH_PICKAXE','MorphSword':'FM:MORPH_SWORD','MorphShovel':'FM:MORPH_SHOVEL','MorphAxe':'FM:MORPH_AXE','WandRodInfernal':'FM:INFERNAL',
 'Wrath':'FM:ENCH_WRATH','Greedy':'FM:ENCH_GREEDY','Corrupting':'FM:ENCH_CORRUPTING','Cluster':'FM:ENCH_CLUSTER','Impact':'FM:ENCH_IMPACT','Voidtouched':'FM:ENCH_VOIDTOUCHED',
 'Fork':'FM:FORK','WrathCage':'FM:CAGE','MobCrystal':'FM:SOUL','TaintShovel':'FM:TAINTSHOVEL','TaintPick':'FM:TAINTPICK','WandRodTainted':'FM:TAINTED','TaintTree':'FM:TAINTTREE','TaintPlank':'forbiddenmagic:planks','TaintStone':'FM:TAINTSTONE','TaintBrick':'forbiddenmagic:tainted_bricks',
 'livingwood_rod':'FM:LIVINGWOOD','manasteel_cap':'FM:MANASTEEL','dreamwood_staff':'FM:DREAMSTAFF','dreamwood_rod':'FM:DREAMWOOD','elementium_cap_inert':'FM:ELEMENTIUM_BLANK','elementium_cap':'FM:ELEMENTIUM','terrasteel_cap':'forbiddenmagic:terrasteel_caps',
 'euclid':'FM:EUCLIDAISY','whispercraft':'FM:WHISPERWEED','thistlecraft':'FM:TAINTHISTLE','blood_recipe':'FM:BLOODROD','blood_staff':'FM:BLOODSTAFF','alchemical_recipe':'FM:ALCHEMICAL','bloodwell_recipe':'FM:BLOODWELL','blood_rapier':'FM:RAPIER','eldritch_orb':'FM:ELDRITCHORB'
}
original_by_key={}
for r in original_research.rows:original_by_key.setdefault(r['original_key'],r)
for r in research:
 if not r.get('original_key') or r.get('virtual'):continue
 old_key=r['original_key'];old=original_by_key[old_key];r.pop('additional_text',None)
 ru['fm.name.'+r['key']]=ru['forbidden.research_name.'+old_key];en['fm.name.'+r['key']]=en['forbidden.research_name.'+old_key]
 ru['fm.tag.'+r['key']]=ru['forbidden.research_text.'+old_key];en['fm.tag.'+r['key']]=en['forbidden.research_text.'+old_key]
 pages=[];expression=old['methods']['setPages'];pos=0
 for match in re.finditer(r'new ResearchPage\s*\(',expression):
  end=original_research.closing(expression,match.end()-1);args=original_research.split(expression[match.end():end])
  if args[-1].startswith('"forbidden.research_page.'):
   text=json.loads(args[-1]);page={'type':'text','key':text}
   if len(args)==2:page.update(type='concealed',requires=mapping.get(json.loads(args[0]),json.loads(args[0])))
  elif 'new ItemStack(ForbiddenBlocks.taintLog' in args[0]:page={'type':'smelting','input':'forbiddenmagic:tainted_log','output':'forbiddenmagic:hexcoal'}
  else:
   recipe_name=re.search(r'recipes.get\("([^"]+)"\)',args[0]);name=recipe_name[1] if recipe_name else args[0]
   assert name in page_recipes,(old_key,name)
   recipe_key=page_recipes[name];page={'type':'recipe' if recipe_key.startswith('FM:') else 'crafting','recipe':recipe_key}
  pages.append(page)
 assert pages and pages[0]['type']=='text',old_key
 r['pages']=pages
 # Saved external links using the1.0 first-page key resolve to the original first text.
 for lang in [ru,en]:lang['fm.page.'+r['key']]=lang[pages[0]['key']]

# Clearly labelled appendices for current APIs, not replacements of the original prose.
for lang in [ru,en]:
 for key in ['FM_BOUNDWELL','FM_BLOODPOTIONS','FM_SANITY']:
  lang['fm.note.'+key]=('§lПримечание для1.21.1§r<BR><BR>' if lang is ru else '§l1.21.1 note§r<BR><BR>')+lang['fm.page.'+key]
 lang['fm.note.FM_ELDRITCHORB']=('§lПримечание для1.21.1§r<BR><BR>Старый русский перевод описывал более раннюю версию сферы. Здесь её предел — 80 000 000 LP, как в финальном оригинале0.575. Для изготовления используется сфера архимага Blood Magic; ихор не требуется. Первозданная жемчужина в рецепте сохраняется.' if lang is ru else '§l1.21.1 note§r<BR><BR>The current orb holds80,000,000 LP, as in final0.575. The recipe uses the modern Blood Magic Archmage Blood Orb and returns its Primordial Pearl. Ichor is not required.')
 lang['fm.note.FM_TAINTHISTLE']=('§lПримечание для1.21.1§r<BR><BR>Современная порча представлена жидкостью. Цветок поглощает её настоящие порции и превращает их в ману; отдельного старого блока заражённого газа в этой версии нет.' if lang is ru else '§l1.21.1 note§r<BR><BR>Modern flux is a fluid. The flower consumes its actual quanta to generate mana; the separate old flux-gas block is absent.')
en['fm.note.FM_BOUNDWELL']='§l1.21.1 note§r<BR><BR>Use the Blood Magic binding array to bind Crystal Scribing Tools. Once their100 ink uses are exhausted, right-click to spend10,000 LP from the owner’s Soul Network and refill them.'
en['fm.note.FM_BLOODPOTIONS']='§l1.21.1 note§r<BR><BR>The Blood Seal and Taint effects are available through Blood Magic’s native alchemy flask recipes. Blood Seal temporarily prevents Life Points from entering the affected player’s Soul Network.'
en['fm.note.FM_SANITY']='§l1.21.1 note§r<BR><BR>The Plea of Delayed Insanity uses28 ritual stones and a tier2 activation crystal. Activation costs20,000 LP. Every600 ticks it grants Warp Ward to players within15 blocks for15,000 LP per refreshed player. It sometimes adds temporary warp. Without LP the protection expires. The old Crystallos extension is absent from modern Blood Magic.'
for lang in [ru,en]:
 for k in list(lang):
  if k.startswith('fm.note.'):lang[k]=lang[k].replace('для1.21.1','для 1.21.1')
for r in research:
 if r['key']=='FM_CRYSTALWELL':r['pages'].append({'type':'text','key':'fm.note.FM_BOUNDWELL','mods':['bloodmagic']})
 if r['key']=='FM_BLOODMAGIC':r['pages'].extend({'type':'text','key':'fm.note.'+k} for k in ['FM_BLOODPOTIONS','FM_SANITY'])
 if r['key'] in ['FM_ELDRITCHORB','FM_TAINTHISTLE']:r['pages'].append({'type':'text','key':'fm.note.'+r['key']})
 if r['key']=='FM_BOTANY':
  for lang in [ru,en]:lang['fm.original.bloodthorn']=adapted_markup(original_lang['ru' if lang is ru else 'en']['forbidden.lexicon.bloodthorn.0']).replace('<br>','<BR>').replace('&1','§1').replace('&0','§0').replace('&o','§o').replace('&r','§r')
  r['pages'].append({'type':'text','key':'fm.original.bloodthorn','mods':['botania','bloodmagic']})

# Keep names in recipes consistent with the original research terminology.
item_name_map={
 'skull_axe':'item.SkullAxe.name','arcane_cake':'tile.ArcaneCake.name','taint_shovel':'item.TaintShovel.name','taint_pickaxe':'item.TaintPickaxe.name','emerald_nugget':'item.FMResource.0.name','shadow_ink':'item.FMResource.1.name','manasteel_nugget':'item.FMResource.2.name','tainted_blood_shard':'item.FMResource.3.name','elementium_nugget':'item.FMResource.4.name','imprinting_crystal':'item.MobCrystalEmpty.name','diabolists_fork':'item.DiabolistFork.name','bloody_scribing_tools':'item.Bloodwell.name','eldritch_blood_orb':'item.EldritchOrb.name','crystal_scribing_tools':'item.Crystalwell.name','primal_scribing_tools':'item.Primewell.name','riding_crop':'item.RidingCrop.name','rapier_of_the_strix':'item.BloodRapier.name','bound_scribing_tools':'item.Boundwell.name','submissive_collar':'item.SubCollar.name','ring_of_nutrition':'item.RingNutrition.name','tainted_fruit':'item.TaintFruit.name','hexcoal':'item.TaintCoal.name','blink_focus':'item.BlinkFocus.name',
 'umbral_rose':'tile.InkFlower.name','wrath_cage':'tile.WrathCage.name','nether_star_block':'tile.StarBlock.name','umbral_rose_bush':'tile.blackRoseBush.name','tainted_log':'tile.TaintLog.name','tainted_planks':'tile.TaintPlank.name','tainted_leaves':'tile.TaintLeaves.name','tainted_sapling':'tile.TaintSapling.name','tainted_stone':'tile.TaintStone_Smooth.name','tainted_bricks':'tile.TaintStone_Brick.name'
}
for name in ['pickaxe','sword','shovel','axe']:item_name_map['morph_'+name]='item.Morph'+name.title()+'.name'
for name in ['wrath','envy','taint','pride','lust','sloth','greed','gluttony']:item_name_map[name+'_shard']='item.NetherShard.'+name+'.name'
for name in ['blood','livingwood','dreamwood','infernal','tainted']:item_name_map[name+'_rod']='item.WandCores.'+name+'.name'
for name in ['blood','livingwood','dreamwood']:item_name_map['inert_'+name+'_rod']='item.WandCores.'+name+'_inert.name'
for name in ['blood','dreamwood']:item_name_map[name+'_staff_core']='item.WandCores.'+name+'_staff.name'
for name in ['alchemical','manasteel','elementium','terrasteel']:item_name_map[name+'_cap']='item.WandCaps.'+name+'.name'
for name in ['manasteel','elementium']:item_name_map['inert_'+name+'_cap']='item.WandCaps.'+name+'_inert.name'
for name in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:item_name_map[name]='darkflower.'+name+'.name'
extra_missing={'item.Primewell.name':missing_ru['forbidden.research_name.PRIMEWELL'],'item.BlinkFocus.name':missing_ru['forbidden.research_name.FOCUSBLINK'],'item.TaintFruit.name':'Заражённый плод','item.TaintCoal.name':'Проклятый уголь','tile.TaintLog.name':'Заражённое бревно','tile.TaintPlank.name':'Заражённые доски','tile.TaintLeaves.name':'Заражённая листва','tile.TaintSapling.name':'Заражённый саженец','tile.TaintStone_Smooth.name':'Заражённый камень','tile.TaintStone_Brick.name':'Кирпичи из заражённого камня','item.WandCaps.terrasteel.name':'Террастальной наконечник'}
for item_id,old in item_name_map.items():
 for code,lang in [('ru',ru),('en',en)]:
  text=original_lang[code].get(old) or (extra_missing[old] if code=='ru' else None);assert text,(item_id,old,code)
  for kind in ['item','block']:
   key=kind+'.forbiddenmagic.'+item_id
   if key in lang:lang[key]=text
for name in ['wrath','cluster','greedy','consuming','educational','corrupting','voidtouched','impact']:
 for code,lang in [('ru',ru),('en',en)]:lang['enchantment.forbiddenmagic.'+name]=original_lang[code]['enchantment.'+name]
for code,lang in [('ru',ru),('en',en)]:
 for name in ['hellfire','pandemonium']:
  for suffix in ['name','text']:
   key='forbidden.upgrade.'+name+'.'+suffix
   fallback={'hellfire.name':'Адское пламя','hellfire.text':'Это улучшение высвобождает вспышку огня при телепортации в выбранную точку.','pandemonium.name':'Пандемоний','pandemonium.text':'Это улучшение при телепортации переносит всех окружающих врагов на Ваше прежнее место.'}
   lang['focus.upgrade.fm_'+name+'.'+suffix]=original_lang[code].get(key) or fallback[name+'.'+suffix]
write(Path('forbiddenmagic/localization-0575-baseline.json'),{'research':localization_provenance,'original_russian':len(localization_provenance)-len(missing_ru),'translated_russian':len(missing_ru),'source':'Final0.575 JAR ru_RU.lang/en_US.lang; markup image namespace adapted only.'})
