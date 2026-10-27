"""Restore the final 0.575 graph while retaining current item/API identifiers and saved keys."""
import extract_research_0575 as original_research
mapping={r['original_research']:r['remake_research'] for r in json.loads((ROOT/'releases/1.0.0/COMPLETION-MATRIX.json').read_text(encoding='utf8'))['research']}
mapping.update(RINGFOOD='FM_RINGFOOD',ARCANECAKE='FM_ARCANECAKE')
current={r['key']:r for r in research}
from copy import deepcopy
for key in ['FM_RINGFOOD','FM_ARCANECAKE']:
 current[key]=deepcopy(current['FM_GLUTTONY']);current[key]['key']=key
for lang in [ru,en]:
 lang['fm.name.FM_RINGFOOD']='Кольцо питания' if lang is ru else 'Ring of Nutrition'
 lang['fm.name.FM_ARCANECAKE']='Мистический торт' if lang is ru else 'Arcane Cake'
 lang['fm.tag.FM_RINGFOOD']='Больше пользы от еды' if lang is ru else 'Getting more from your food'
 lang['fm.tag.FM_ARCANECAKE']='Почти бесконечный десерт' if lang is ru else 'A regenerating dessert'
 lang['fm.page.FM_RINGFOOD']='Кольцо добавляет 2 единицы пищи и насыщение 2,0 после еды. Наденьте его в слот кольца Curios; второе такое кольцо не удваивает действие.' if lang is ru else 'Worn in a Curios ring slot, adds2food and2.0 saturation after eating. Wearing two does not double the effect.'
 lang['fm.page.FM_ARCANECAKE']='Мистический торт содержит 12 порций по 2 единицы пищи с насыщением 1,0. Пока осталась хотя бы одна порция, случайные тики восстанавливают съеденные. Разрушение торта не возвращает предмет.' if lang is ru else 'Twelve slices, each granting2food and1.0 saturation. Random ticks regrow eaten slices while some cake remains. Breaking it yields no item.'
 lang['fm.name.FM_ROOT']='Магические искусства' if lang is ru else 'Magical Schools'
 lang['fm.page.FM_ROOT']='Тауматургия соприкасается с другими магическими искусствами. В этой части Апокрифов описаны связи с Blood Magic и Botania.' if lang is ru else 'Thaumaturgy meets other magical arts. These branches describe Blood Magic and Botania.'
aspect_source=(ROOT.parent/'SOURCE 1.21.1/thaumcraft-4-reforged-1.21.1/src/main/java/thaumcraft/aspect/Aspect.java').read_text()
aspect_tags=dict(__import__('re').findall(r'\b([A-Z_]+)\("([a-z]+)"',aspect_source))
dark={'NETHER':'infernus','WRATH':'ira','ENVY':'invidia','GLUTTONY':'gula','PRIDE':'superbia','LUST':'luxuria','SLOTH':'desidia'}
re=__import__('re')
flags={'setAutoUnlock':'auto','setRound':'round','setStub':'stub','setVirtual':'virtual','setConcealed':'concealed','setHidden':'hidden','setLost':'lost','setSecondary':'secondary','setSpecial':'special'}
original_warp={}
for name in ['ForbiddenResearch.java','compat/ForbiddenBotany.java','compat/BloodMagic.java']:
 source=(ROOT/'reference/original/decompiled/fox/spiteful/forbidden'/name).read_text()
 original_warp.update({k:int(n) for k,n in re.findall(r'addWarpToResearch\("([^"]+)",\s*(\d+)\)',source)})
def renamed(keys):return [mapping.get(k,k) for k in re.findall(r'"([^"]+)"',keys)]
rows=[];seen=set();expected=[]
for old in original_research.rows:
 original_key=old['original_key']
 if original_key in seen:continue # normal/default enabled branch, duplicated fallback branches have the same registration metadata
 seen.add(original_key);key=mapping[original_key];r=deepcopy(current[key]);methods=old['methods']
 for field in ['auto','hidden','triggers','any_mods','icon_texture','additional_text','parents_hidden','siblings',*flags.values()]:r.pop(field,None)
 a={}
 for source,name,n in re.findall(r'\.(?:add|merge)\((Aspect|DarkAspects)\.([A-Z_]+),\s*(\d+)\)',old['aspect_expression']):a[(dark if source=='DarkAspects' else aspect_tags)[name]]=int(n)
 r.update(x=old['x'],y=old['y'],complexity=0 if 'setVirtual' in methods else max(1,min(3,old['complexity'])),aspects=a,parents=renamed(methods.get('setParents','')),parents_hidden=renamed(methods.get('setParentsHidden','')),siblings=renamed(methods.get('setSiblings','')),original_key=original_key)
 for method,field in flags.items():r[field]=method in methods
 r['warp']=original_warp.get(original_key,0)
 r['triggers']=[(dark if source=='DarkAspects' else aspect_tags)[name] for source,name in re.findall(r'(Aspect|DarkAspects)\.([A-Z_]+)',methods.get('setAspectTriggers',''))]
 texture=re.search(r'new ResourceLocation\("forbidden",\s*"([^"]+)"\)',old['icon_expression'] or '')
 if texture:r['icon_texture']='forbiddenmagic:'+texture[1];r.pop('icon',None)
 if original_key=='SCHOOLS':r['icon']='minecraft:bookshelf';r['any_mods']=['bloodmagic','botania']
 if original_key=='HELLFIRE':r['icon_texture']='forbiddenmagic:textures/misc/hellfire.png';r.pop('icon',None)
 if original_key=='BLACKFLOWER':r['icon']='forbiddenmagic:umbral_rose'
 if original_key=='TAINTSTONE':r['icon']='forbiddenmagic:tainted_bricks'
 if original_key=='RINGFOOD':r.update(icon='forbiddenmagic:ring_of_nutrition',recipes=['FM:RING'])
 if original_key=='ARCANECAKE':r.update(icon='forbiddenmagic:arcane_cake',recipes=['FM:CAKE'])
 if original_key=='ROD_dreamwood':r['recipes']=[]
 if original_key=='ROD_dreamwood_staff':r['recipes']=['FM:DREAMWOOD','FM:DREAMSTAFF'] # virtual sibling unlocks the rod recipe, both are documented on the original staff node
 rows.append(r);expected.append(deepcopy(r))
# API adaptations were information nodes in1.0. Move their pages onto the original branches.
merge_into={'FM_TAINTSHARD':'FM_TAINTSHOVEL','FM_BOUNDWELL':'FM_CRYSTALWELL','FM_BLOODTHORN':'FM_BOTANY','FM_BLOODPOTIONS':'FM_BLOODMAGIC','FM_SANITY':'FM_BLOODMAGIC'}
for old_key,target in merge_into.items():
 r=next(r for r in rows if r['key']==target);r.setdefault('additional_text',[]).append({'key':'fm.page.'+old_key,'mods':current[old_key].get('mods',[])})
 # Keep old keys resolvable for existing saves/external links, but outside the visible graph.
 alias=deepcopy(current[old_key]);alias.update(virtual=True,auto=False,hidden=False,concealed=False,round=False,stub=False,aspects={},parents=[],parents_hidden=[],siblings=[],x=0,y=0,complexity=0,recipes=[]);rows.append(alias)
legacy=deepcopy(current['FM_GLUTTONY']);legacy.update(virtual=True,auto=False,hidden=False,concealed=False,round=False,stub=False,aspects={},parents=[],x=0,y=0,complexity=0,recipes=[]);rows.append(legacy)
for r in recipes:
 if r['key']=='FM:RING':r['research']='FM_RINGFOOD'
 if r['key']=='FM:CAKE':r['research']='FM_ARCANECAKE'
research[:]=rows
write(Path('forbiddenmagic/research-0575-baseline.json'),{'background':'forbiddenmagic:textures/misc/runecircle.png','nodes':expected,'source':'Final ForbiddenMagic-1.7.10-0.575.jar decompilation; default enabled core/BM/Botania registrations.'})
