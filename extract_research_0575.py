"""Extract registration metadata from the final 0.575 decompilation, without using older master."""
from pathlib import Path
import re,json
ROOT=Path(__file__).resolve().parent
def closing(s,start):
 pairs={'(':')','{':'}','[':']'};stack=[];quote=None;escape=False
 for i in range(start,len(s)):
  c=s[i]
  if quote:
   if escape:escape=False
   elif c=='\\':escape=True
   elif c==quote:quote=None
  elif c in '\"\'':quote=c
  elif c in pairs:stack.append(pairs[c])
  elif stack and c==stack[-1]:
   stack.pop()
   if not stack:return i
 raise ValueError(s[start:start+100])
def split(s):
 out=[];last=0;i=0
 while i<len(s):
  if s[i] in '({[':i=closing(s,i)+1;continue
  if s[i]=='"':
   i+=1
   while i<len(s) and (s[i]!='"' or s[i-1]=='\\'):i+=1
  if i<len(s) and s[i]==',':out.append(s[last:i].strip());last=i+1
  i+=1
 out.append(s[last:].strip());return out
rows=[]
for file in ['ForbiddenResearch.java','compat/ForbiddenBotany.java','compat/BloodMagic.java']:
 s=(ROOT/'reference/original/decompiled/fox/spiteful/forbidden'/file).read_text()
 for m in re.finditer(r'new DarkResearchItem\s*\(',s):
  end=closing(s,m.end()-1);args=split(s[m.end():end]);methods=[];pos=end+1
  while True:
   call=re.match(r'\s*\.\s*(\w+)\s*\(',s[pos:])
   if not call:break
   a=pos+call.end()-1;b=closing(s,a);methods.append((call[1],s[a+1:b].strip()));pos=b+1
  if len(args)==2:
   rows.append({'source':file,'original_key':json.loads(args[0]),'x':0,'y':0,'complexity':0,'aspect_expression':'new AspectList()','icon_expression':None,'methods':dict(methods)})
   continue
  assert len(args) in [7,8],args
  offset=1 if len(args)==8 else 0
  row={'source':file,'original_key':json.loads(args[0]),'x':int(args[3+offset]),'y':int(args[4+offset]),'complexity':int(args[5+offset]),'aspect_expression':args[2+offset],'icon_expression':args[6+offset], 'methods':dict(methods)}
  rows.append(row)
out=ROOT/'reference/original/research-0575-registrations.json';out.write_text(json.dumps(rows,indent=2)+'\n')
if __name__=='__main__':
 for r in rows:
  print(r['original_key'],r['x'],r['y'],r['complexity'],(r['icon_expression'] or 'virtual').replace('\n',' '),{k:v for k,v in r['methods'].items() if k!='setPages'})

