"""Original textures are copied unchanged; models are native 1.21 JSON."""
def model(id,data):write(Path('assets/forbiddenmagic/models/block')/(id+'.json'),data)
def state(id,variants):write(Path('assets/forbiddenmagic/blockstates')/(id+'.json'),{'variants':variants})
def blocktex(name,old=None):
 copy_asset('assets/forbidden/textures/blocks/'+(old or name)+'.png',Path('assets/forbiddenmagic/textures/block')/(name+'.png'));return 'forbiddenmagic:block/'+name
def drop(id):write(Path('data/forbiddenmagic/loot_table/blocks')/(id+'.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':full(id)}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for id,tex in {'tainted_planks':'taint_planks','tainted_stone':'taint_stone_smooth','tainted_bricks':'taint_stone_brick','nether_star_block':'starblock','wrath_cage':'spirit_box'}.items():
 model(id,{'parent':'minecraft:block/cube_all','render_type':'minecraft:cutout' if id=='wrath_cage' else 'minecraft:solid','textures':{'all':blocktex(id,tex)}});state(id,{'':{'model':'forbiddenmagic:block/'+id}});drop(id)
model('tainted_log',{'parent':'minecraft:block/cube_column','textures':{'side':blocktex('tainted_log','taint_log_side'),'end':blocktex('tainted_log_end','taint_log_bottom')}})
state('tainted_log',{'axis=y':{'model':'forbiddenmagic:block/tainted_log'},'axis=x':{'model':'forbiddenmagic:block/tainted_log','x':90,'y':90},'axis=z':{'model':'forbiddenmagic:block/tainted_log','x':90}});drop('tainted_log')
for n in [1,2]:model('tainted_log_gross'+str(n),{'parent':'minecraft:block/cube_column','textures':{'side':blocktex('tainted_log_gross'+str(n),'taint_log_side_gross'+('2' if n==2 else '')),'end':'forbiddenmagic:block/tainted_log_end'}})
state('tainted_log',{'axis='+axis:[{'model':'forbiddenmagic:block/'+name,'weight':weight,**rotation} for name,weight in [('tainted_log',6),('tainted_log_gross1',1),('tainted_log_gross2',1)]] for axis,rotation in [('y',{}),('x',{'x':90,'y':90}),('z',{'x':90})]})
model('tainted_leaves',{'parent':'minecraft:block/leaves','render_type':'minecraft:cutout_mipped','textures':{'all':blocktex('tainted_leaves','taint_leaves')}});state('tainted_leaves',{'':{'model':'forbiddenmagic:block/tainted_leaves'}})
for id,tex in {'tainted_sapling':'sapling_taint','umbral_rose':'flower_black'}.items():
 model(id,{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':blocktex(id,tex)}});state(id,{'':{'model':'forbiddenmagic:block/'+id}});drop(id)
for half in ['lower','upper']:model('umbral_rose_bush_'+half,{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':blocktex('rose_'+half,'umbral_rose_bottom' if half=='lower' else 'umbral_rose_top')}})
state('umbral_rose_bush',{'half='+h:{'model':'forbiddenmagic:block/umbral_rose_bush_'+h} for h in ['lower','upper']})
write(Path('data/forbiddenmagic/loot_table/blocks/umbral_rose_bush.json'),{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':full('umbral_rose_bush'),'properties':{'half':'lower'}}],'entries':[{'type':'minecraft:item','name':full('umbral_rose_bush')}]}]})
for t in ['top','side','bottom','inner']:blocktex('cake_'+t)
for bites in range(12):
 faces={d:{'texture':'#side','uv':[0,8,14,16]} for d in ['north','south','east','west']};faces['west']['texture']='#inner' if bites else '#side';faces.update(up={'texture':'#top','uv':[1+bites,1,15,15]},down={'texture':'#bottom','uv':[1+bites,1,15,15]})
 model('arcane_cake_'+str(bites),{'parent':'minecraft:block/block','textures':{t:'forbiddenmagic:block/cake_'+t for t in ['top','side','bottom','inner']}|{'particle':'forbiddenmagic:block/cake_side'},'elements':[{'from':[1+bites,0,1],'to':[15,8,15],'faces':faces}]})
state('arcane_cake',{'bites='+str(n):{'model':'forbiddenmagic:block/arcane_cake_'+str(n)} for n in range(12)})
write(Path('data/forbiddenmagic/loot_table/blocks/arcane_cake.json'),{'type':'minecraft:block','pools':[]})
# One sapling in sixty; a fruit in twenty, on harvest/decay as in BlockLeavesTainted.dropApple.
silk={'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}
shears={'condition':'minecraft:match_tool','predicate':{'items':'minecraft:shears'}}
intact={'condition':'minecraft:any_of','terms':[silk,shears]}
write(Path('data/forbiddenmagic/loot_table/blocks/tainted_leaves.json'),{'type':'minecraft:block','pools':[
 {'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':full('tainted_leaves'),'conditions':[intact]},{'type':'minecraft:item','name':full('tainted_sapling'),'conditions':[{'condition':'minecraft:table_bonus','enchantment':'minecraft:fortune','chances':[1/60,1/50,1/40,1/30,1/10]}]}]}]},
 {'rolls':1,'conditions':[{'condition':'minecraft:inverted','term':intact},{'condition':'minecraft:random_chance','chance':0.05}],'entries':[{'type':'minecraft:item','name':full('tainted_fruit')}]}]})
for id in block_names:
 if id=='arcane_cake':continue
 write(Path('assets/forbiddenmagic/models/item')/(id+'.json'),{'parent':'forbiddenmagic:block/'+('umbral_rose_bush_lower' if id=='umbral_rose_bush' else id)})
for tool,eye in [('pickaxe','pick'),('axe','axe'),('shovel','shovel'),('sword','sword')]:
 id='morph_'+tool;copy_asset('assets/forbidden/textures/items/eye'+eye+'.png',Path('assets/forbiddenmagic/textures/item')/(id+'_eyes.png'))
 write(Path('assets/forbiddenmagic/models/item')/(id+'.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'forbiddenmagic:item/'+id,'layer1':'forbiddenmagic:item/'+id+'_eyes'}})
copy_asset('assets/forbidden/textures/items/mobcrystal.png',Path('assets/forbiddenmagic/textures/item/soul_crystal_full.png'))
write(Path('assets/forbiddenmagic/models/item/soul_crystal_filled.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'forbiddenmagic:item/soul_crystal_full'}})
write(Path('assets/forbiddenmagic/models/item/soul_crystal.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'forbiddenmagic:item/soul_crystal'},'overrides':[{'predicate':{'forbiddenmagic:filled':1},'model':'forbiddenmagic:item/soul_crystal_filled'}]})
for id in ['taint_pickaxe','taint_shovel','skull_axe','diabolists_fork']:
 write(Path('assets/forbiddenmagic/models/item')/(id+'.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'forbiddenmagic:item/'+id}})
for n in ['profane','profaned','neutronium']:copy_asset('assets/forbidden/textures/models/wand_rod_'+n+'.png',Path('assets/forbiddenmagic/textures/models')/('wand_rod_'+n+'.png'))
for n in ['orichalcum','terrasteel']:copy_asset('assets/forbidden/textures/models/wand_cap_'+n+'.png',Path('assets/forbiddenmagic/textures/models')/('wand_cap_'+n+'.png'))
for id,texture in {'profaned_rod':'wand_rod_profaned','neutronium_rod':'wand_rod_neutronium','neutronium_staff_core':'wand_rod_neutronium','orichalcum_cap':'wand_cap_orichalcum'}.items():write(Path('assets/forbiddenmagic/models/item')/(id+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'forbiddenmagic:models/'+texture}})
copy_asset('assets/forbidden/textures/items/focus_blink.png',Path('assets/forbiddenmagic/textures/foci/blink.png'));copy_asset('assets/forbidden/textures/items/focus_blink_orn.png',Path('assets/forbiddenmagic/textures/foci/blink_ornament.png'))
copy_asset('assets/forbidden/textures/items/focus_blink_orn.png',Path('assets/forbiddenmagic/textures/item/blink_focus_ornament.png'))
write(Path('assets/forbiddenmagic/models/item/blink_focus.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'forbiddenmagic:item/blink_focus_ornament','layer1':'forbiddenmagic:item/blink_focus'}})
for n in ['hellfire','pandemonium']:copy_asset('assets/forbidden/textures/misc/'+n+'.png',Path('assets/forbiddenmagic/textures/foci')/(n+'.png'))
copy_asset('assets/forbidden/textures/models/heretic.png',Path('assets/forbiddenmagic/textures/entity/villager/profession/heretic.png'))
for n in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:
 id='floating_'+n;state(id,{'':{'model':'forbiddenmagic:block/'+id}});model(id,{'parent':'minecraft:block/block','flower':{'parent':'forbiddenmagic:block/'+n},'loader':'botania:floating_flower'});write(Path('assets/forbiddenmagic/models/item')/(id+'.json'),{'parent':'forbiddenmagic:block/'+id});drop(id)
 for target in [n,id]:
  path=Path('data/forbiddenmagic/loot_table/blocks')/(target+'.json');data=json.loads((OUT/path).read_text());data['neoforge:conditions']=[{'type':'neoforge:mod_loaded','modid':m} for m in (['botania','bloodmagic'] if n=='bloodthorn' else ['botania'])];write(path,data)
for kind in ['item','block']:
 for tag,values in {'floating_flowers':['floating_'+n for n in ['euclidaisy','whisperweed','tainthistle','bloodthorn']],'special_floating_flowers':['floating_'+n for n in ['euclidaisy','whisperweed','tainthistle','bloodthorn']],'special_flowers':['euclidaisy','whisperweed','tainthistle','bloodthorn'],'functional_special_flowers':['euclidaisy','whisperweed','bloodthorn'],'generating_special_flowers':['tainthistle'],'functional_floating_flowers':['floating_'+n for n in ['euclidaisy','whisperweed','bloodthorn']],'generating_floating_flowers':['floating_tainthistle']}.items():
  write(Path('data/botania/tags')/kind/(tag+'.json'),{'replace':False,'values':[{'id':full(v),'required':False} for v in values]})
(ROOT/'port-assets.json').write_text(json.dumps(asset_hashes,indent=2)+'\n')
for name in ['runecircle','consuming','educational','wrath','greedy','corrupting','lucrative','impact','voidtouched','hellfire','research']:
 copy_asset('assets/forbidden/textures/misc/'+name+'.png',Path('assets/forbiddenmagic/textures/misc')/(name+'.png'))
(ROOT/'port-assets.json').write_text(json.dumps(asset_hashes,indent=2)+'\n')
print('Completion assets:',len(asset_hashes),'original byte-identical assets')
