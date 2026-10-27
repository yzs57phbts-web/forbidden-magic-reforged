"""The four user exports are authoritative; preserve exact slot order and quantities."""
for record in json.loads((ROOT/'recipe-imports/1.2.1/manifest.json').read_text(encoding='utf8')):
 data=json.loads((ROOT/'recipe-imports/1.2.1'/record['file']).read_text(encoding='utf8'))
 assert data['kind']=='INFUSION' and data['owner']=='forbiddenmagic' and data['format']==1
 recipe=next(r for r in recipes if r['key']==data['key'])
 assert recipe['research']==data['research'] and recipe['instability']==data['instability'] and recipe['aspects']==data['aspects']
 assert recipe['output']==data['output']['item'] and data['output']['count']==1 and not data['output']['data']
 center=data['central'];assert center['type']=='stack' and center['stack']['count']==1 and not center['stack']['data'] and recipe['center']==center['stack']['item']
 parts=[]
 for slot in data['inputs']:
  assert slot['type']=='stack' and slot['stack']['count']==1 and not slot['stack']['data']
  parts.append(slot['stack']['item'])
 recipe['inputs']=parts
