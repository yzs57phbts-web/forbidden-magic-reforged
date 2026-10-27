"""Named legacy controls and corrected data; four user recipe imports remain authoritative."""
for lang in [ru,en]:
 for r in research:
  text=lang.get('fm.tag.'+r['key'],'');lang['fm.tagged.'+r['key']]='[FM] '+text
lang_key='item.forbiddenmagic.spork'
ru[lang_key]='Ложковилка';en[lang_key]='Spork'

def finalize_130_assets():
 p=OUT/'forbiddenmagic/cage-souls.json';souls=json.loads(p.read_text(encoding='utf8'))
 for r in souls['souls']:
  if r['entity']=='thaumcraft:thaumic_slime':r['aspect']='vitium'
 write(Path('forbiddenmagic/cage-souls.json'),souls)
 write(Path('data/forbiddenmagic/damage_type/blink_hellfire.json'),{'message_id':'fireball','scaling':'never','exhaustion':0.1,'effects':'burning'})
 write(Path('data/minecraft/tags/damage_type/is_fire.json'),{'replace':False,'values':['forbiddenmagic:blink_hellfire']})
 copy_asset('assets/forbidden/textures/items/spork.png',Path('assets/forbiddenmagic/textures/item/spork.png'))
 write(Path('assets/forbiddenmagic/models/item/spork.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'forbiddenmagic:item/spork'}})
 write(Path('assets/forbiddenmagic/models/item/diabolists_fork.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'forbiddenmagic:item/diabolists_fork'},'overrides':[{'predicate':{'forbiddenmagic:spork':1},'model':'forbiddenmagic:item/spork'}]})
 for p in (OUT/'data/forbiddenmagic/recipe').rglob('*.json'):
  data=json.loads(p.read_text(encoding='utf8'));conditions=data.setdefault('neoforge:conditions',[])
  features=[]
  for c in list(conditions):
   if c.get('type')=='neoforge:mod_loaded':features.append(c['modid'])
  if p.stem in ['livingwood_activation','dreamwood_activation','manasteel_activation','blood_rod_activation','terrasteel_caps']:features.append('conversion_wands')
  for feature in sorted(set(features)):
   condition={'type':'forbiddenmagic:feature_enabled','feature':feature}
   if condition not in conditions:conditions.append(condition)
  write(p.relative_to(OUT),data)
 (ROOT/'port-assets.json').write_text(json.dumps(asset_hashes,indent=2)+'\n')
