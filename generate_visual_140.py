"""Use item sprites, Botania's native item loader and vanilla trident hand transforms."""
for name,texture in {'tainted_sapling':'tainted_sapling','umbral_rose':'umbral_rose','umbral_rose_bush':'rose_upper',**{n:n for n in ['euclidaisy','whisperweed','tainthistle','bloodthorn']}}.items():
 write(Path(f'assets/forbiddenmagic/models/item/{name}.json'),{'parent':'minecraft:item/generated','textures':{'layer0':f'forbiddenmagic:block/{texture}'}})
for name in ['euclidaisy','whisperweed','tainthistle','bloodthorn']:
 write(Path(f'assets/forbiddenmagic/models/block/{name}.json'),{'parent':'botania:block/shapes/cross','render_type':'minecraft:cutout','textures':{'cross':f'forbiddenmagic:block/{name}'}})
 write(Path(f'assets/forbiddenmagic/models/item/floating_{name}.json'),{'parent':'minecraft:block/block','flower':{'parent':f'forbiddenmagic:block/{name}'},'loader':'botania:floating_flower'})
write(Path('assets/forbiddenmagic/models/item/rapier_of_the_strix.json'),{'parent':'minecraft:item/handheld','transform':{'scale':[-1,1,1],'origin':'center'},'textures':{'layer0':'forbiddenmagic:item/rapier_of_the_strix'}})
write(Path('assets/forbiddenmagic/models/item/fork_icon.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/trident'}})
write(Path('assets/forbiddenmagic/models/item/fork_hand.json'),{'parent':'minecraft:item/trident_in_hand','overrides':[]})
write(Path('assets/forbiddenmagic/models/item/diabolists_fork.json'),{'loader':'neoforge:separate_transforms','gui_light':'front','base':{'parent':'forbiddenmagic:item/fork_icon'},'perspectives':{hand:{'parent':'forbiddenmagic:item/fork_hand'} for hand in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']},'overrides':[{'predicate':{'forbiddenmagic:spork':1},'model':'forbiddenmagic:item/spork'}]})
# These standalone items were invented by the port. Keep saved IDs, hide from the tab,
# and render any saved stack as a component rather than as an unwrapped texture sheet.
for name,tex in {'profaned_rod':'wand_rod_profaned','neutronium_rod':'wand_rod_neutronium','neutronium_staff_core':'wand_rod_neutronium','orichalcum_cap':'wand_cap_orichalcum'}.items():
 cap=name.endswith('_cap');lo,hi=([6,4,6],[10,9,10]) if cap else ([7,0,7],[9,16,9])
 write(Path(f'assets/forbiddenmagic/models/item/{name}.json'),{'parent':'minecraft:block/block','render_type':'minecraft:cutout','gui_light':'front','textures':{'material':f'forbiddenmagic:models/{tex}','particle':f'forbiddenmagic:models/{tex}'},'elements':[{'from':lo,'to':hi,'faces':{side:{'texture':'#material','uv':[0,1,4,2] if cap else [0,5,1,13]} for side in ['up','down','north','south','east','west']}}],'display':{'gui':{'rotation':[15,0,-45],'scale':[.9,.9,.9]},'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1.1,3.2,1.1],'scale':[.68,.68,.68]},'ground':{'scale':[.5,.5,.5],'translation':[0,3,0]}}})
