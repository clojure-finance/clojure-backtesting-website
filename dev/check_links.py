"""Checks every internal link and #anchor of the built site.

Run after `lein run`:

    python3 dev/check_links.py public/clojure-backtesting-website

Prints each link whose page or anchor is missing, and duplicate ids,
then a summary; exits 1 if there are problems.
"""
import sys, os, re
from html.parser import HTMLParser
root = sys.argv[1]            # public/clojure-backtesting-website
prefix = '/clojure-backtesting-website'
class P(HTMLParser):
    def __init__(s): super().__init__(); s.ids=[]; s.links=[]
    def handle_starttag(s, tag, attrs):
        a=dict(attrs)
        if 'id' in a: s.ids.append(a['id'])
        for k in ('href','src'):
            if k in a and a[k]: s.links.append(a[k])
pages={}
for d,_,fs in os.walk(root):
    for f in fs:
        if f.endswith('.html'):
            p=os.path.join(d,f); pr=P(); pr.feed(open(p,encoding='utf-8').read()); pages[p]=pr
problems=0
for p,pr in sorted(pages.items()):
    dup={i for i in pr.ids if pr.ids.count(i)>1}
    if dup: print('DUP-ID', p, dup); problems+=1
    for l in pr.links:
        if re.match(r'^(https?:|mailto:|//)', l): continue
        path, _, frag = l.partition('#')
        if path=='' : target=p
        else:
            if not path.startswith(prefix+'/'):
                print('NOPREFIX', p, l); problems+=1; continue
            rel=path[len(prefix):].lstrip('/')
            target=os.path.join(root, rel)
            if os.path.isdir(target) or rel.endswith('/') or rel=='': target=os.path.join(target,'index.html')
        if not os.path.exists(target):
            print('MISSING', p, l); problems+=1; continue
        if frag and target.endswith('.html'):
            if frag not in pages[target].ids:
                print('NOANCHOR', p, l); problems+=1
print('pages', len(pages), 'problems', problems)
sys.exit(1 if problems else 0)
