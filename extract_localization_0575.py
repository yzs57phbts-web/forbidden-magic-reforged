from pathlib import Path
import zipfile,json,re
ROOT=Path(__file__).resolve().parent
with zipfile.ZipFile(ROOT/'reference/original/ForbiddenMagic-1.7.10-0.575.jar') as z:
 langs={}
 for short,name in [('ru','ru_RU'),('en','en_US')]:
  raw=z.read('assets/forbidden/lang/'+name+'.lang').decode('utf-8-sig')
  langs[short]={line.split('=',1)[0].strip():line.split('=',1)[1] for line in raw.splitlines() if '=' in line and not line.lstrip().startswith('#')}
rows=json.loads((ROOT/'reference/original/research-0575-registrations.json').read_text())
used={}
for r in rows:
 if r['original_key']=='ROD_dreamwood':continue
 used['forbidden.research_name.'+r['original_key']]=r['original_key'];used['forbidden.research_text.'+r['original_key']]=r['original_key']
 for key in re.findall(r'"(forbidden\.research_page\.[^"]+)"',r['methods'].get('setPages','')):used[key]=r['original_key']
relevant={k:{lang:d.get(k) for lang,d in langs.items()} for k in used}
(ROOT/'reference/original/localization-0575.json').write_text(json.dumps({'langs':langs,'research':relevant},ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':
 missing=[k for k,v in relevant.items() if not v['ru'] or not re.search('[А-Яа-яЁё]',v['ru'])]
 print('Research strings:',len(relevant),'missing/untranslated Russian:',len(missing))
 for k in missing:print(k,'=',relevant[k]['en'])
 print('Missing English:',[k for k,v in relevant.items() if not v['en']])
