#!/usr/bin/env python3
"""Local-only crop and eligible-cell review application for SceneChain packs."""

from __future__ import annotations

import argparse
import json
import mimetypes
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]

HTML = r"""<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>SceneChain pack review</title><style>
:root{font-family:Inter,system-ui,sans-serif;color:#14211c;background:#f5f4ee}*{box-sizing:border-box}body{margin:0}button,select{font:inherit}
header{position:sticky;top:0;z-index:5;display:flex;gap:16px;align-items:center;justify-content:space-between;padding:16px 24px;background:#fff;border-bottom:1px solid #ccd2cc}
main{max-width:1500px;margin:auto;padding:24px}.layout{display:grid;grid-template-columns:minmax(700px,3fr) minmax(280px,1fr);gap:24px}.board{position:relative;aspect-ratio:3/2;background:#222;overflow:hidden}.board img{width:100%;height:100%;object-fit:contain}.grid{position:absolute;inset:0;display:grid;grid-template-columns:repeat(24,1fr);grid-template-rows:repeat(16,1fr)}.grid button{padding:0;border:1px solid #ffffff18;background:#7e171780}.grid button.on{background:#0b755a4d}.grid button:hover{box-shadow:inset 0 0 0 3px #fff}.panel{background:#fff;border:1px solid #d7dbd5;padding:20px}.counts{display:grid;grid-template-columns:repeat(4,1fr);gap:6px}.count{padding:8px;text-align:center;border-radius:5px;background:#e9eee9}.count.bad{background:#ffdcd5;color:#8a2619}.actions{display:grid;gap:10px;margin-top:18px}button{min-height:42px;border:1px solid #aeb8b0;border-radius:7px;background:#fff;padding:8px 12px;cursor:pointer}.primary{background:#0d5c46;color:#fff;border-color:#0d5c46}.danger{color:#9b281c}.status{font-weight:700}.muted{color:#68736d;line-height:1.5}code{background:#e9ece8;padding:2px 5px}.scene-nav{display:flex;align-items:center;gap:9px}.scene-nav select{min-height:42px;max-width:430px}.approved{color:#0d6a4e}.pending{color:#a34b18}@media(max-width:1000px){.layout{grid-template-columns:1fr}.board{min-width:900px}.board-scroll{overflow:auto}}
</style></head><body><header><strong>SceneChain pack review</strong><div class="scene-nav"><button id="prev">Previous</button><select id="scene"></select><button id="next">Next</button></div><span id="packStatus" class="status"></span></header>
<main><div class="layout"><div class="board-scroll"><div class="board"><img id="image" alt="Scene under review"><div id="grid" class="grid"></div></div></div><aside class="panel"><h1 id="title"></h1><p id="meta" class="muted"></p><h2>Window counts</h2><div id="counts" class="counts"></div><p id="cellTotal"></p><div class="actions"><button id="crop">Approve crop</button><button id="cells">Approve eligible cells</button><button id="save" class="primary">Save scene review</button><button id="approvePack" class="primary">Approve complete pack</button></div><p class="muted">Green cells are eligible. Every six-by-four window must contain 12–15 eligible cells. Crop and cells require separate approval. Pack approval is impossible until all 48 scenes pass.</p><p id="message" role="status"></p></aside></div></main>
<script>
let data, index=0, dirty=false; const $=id=>document.getElementById(id);
async function load(){data=await fetch('/api/state').then(r=>r.json()); data.scenes.forEach(s=>s.eligibleCells=s.recommendedCells||s.eligibleCells); $('scene').innerHTML=data.scenes.map((s,i)=>`<option value="${i}">${s.sceneId} · ${s.family} · ${s.title||'Untitled'}</option>`).join(''); render()}
function review(){return data.review[String(data.scenes[index].sceneId)]||={cropApproved:false,cellsApproved:false,notes:''}}
function counts(cells){const set=new Set(cells),out=[];for(let wr=0;wr<4;wr++)for(let wc=0;wc<4;wc++){let n=0;for(let r=wr*4;r<wr*4+4;r++)for(let c=wc*6;c<wc*6+6;c++)if(set.has(r*24+c))n++;out.push(n)}return out}
function render(){const s=data.scenes[index],r=review(),set=new Set(s.eligibleCells);$('scene').value=String(index);$('image').src=`/assets/${s.sceneId}.webp`;$('title').textContent=`${s.sceneId} · ${s.family}`;$('meta').textContent=`${s.source.institution} · ${s.source.url}`;$('grid').innerHTML='';for(let cell=0;cell<384;cell++){const b=document.createElement('button');b.className=set.has(cell)?'on':'';b.title=`row ${Math.floor(cell/24)+1}, column ${cell%24+1}, cell ${cell}`;b.onclick=()=>{set.has(cell)?set.delete(cell):set.add(cell);s.eligibleCells=[...set].sort((a,b)=>a-b);r.cellsApproved=false;dirty=true;render()};$('grid').appendChild(b)}const cs=counts(s.eligibleCells);$('counts').innerHTML=cs.map((n,i)=>`<span class="count ${n<12||n>15?'bad':''}">${i+1}: ${n}</span>`).join('');$('cellTotal').textContent=`${s.eligibleCells.length} eligible cells`;$('crop').textContent=r.cropApproved?'Crop approved':'Approve crop';$('crop').className=r.cropApproved?'approved':'';$('cells').textContent=r.cellsApproved?'Cells approved':'Approve eligible cells';$('cells').className=r.cellsApproved?'approved':'';$('packStatus').textContent=`${data.status} · ${Object.values(data.review).filter(x=>x.cropApproved&&x.cellsApproved).length}/48 reviewed`;$('packStatus').className=data.status==='approved'?'approved':'pending';$('message').textContent=dirty?'Unsaved changes':''}
function validCells(){const s=data.scenes[index],cs=counts(s.eligibleCells);return s.eligibleCells.length>=192&&s.eligibleCells.length<=240&&cs.every(n=>n>=12&&n<=15)}
$('crop').onclick=()=>{review().cropApproved=!review().cropApproved;dirty=true;render()};$('cells').onclick=()=>{if(!validCells()){alert('Fix cell totals and every window count first.');return}review().cellsApproved=!review().cellsApproved;dirty=true;render()};
$('save').onclick=async()=>{const s=data.scenes[index];const response=await fetch('/api/review',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({sceneId:s.sceneId,eligibleCells:s.eligibleCells,review:review()})});const result=await response.json();$('message').textContent=result.message;dirty=false;await load()};
$('approvePack').onclick=async()=>{const response=await fetch('/api/approve',{method:'POST'});const result=await response.json();$('message').textContent=result.message;await load()};
$('prev').onclick=()=>{if(dirty&&!confirm('Discard unsaved changes?'))return;index=(index+data.scenes.length-1)%data.scenes.length;dirty=false;render()};$('next').onclick=()=>{if(dirty&&!confirm('Discard unsaved changes?'))return;index=(index+1)%data.scenes.length;dirty=false;render()};$('scene').onchange=e=>{if(dirty&&!confirm('Discard unsaved changes?')){e.target.value=index;return}index=Number(e.target.value);dirty=false;render()};load();
</script></body></html>"""


class ReviewServer(ThreadingHTTPServer):
    def __init__(self, address: tuple[str, int], pack: Path):
        self.pack = pack
        self.manifest = pack / "manifest-draft.json"
        self.review_file = pack / "review-state.json"
        super().__init__(address, Handler)

    def state(self) -> tuple[dict, dict]:
        manifest = json.loads(self.manifest.read_text(encoding="utf-8"))
        review = json.loads(self.review_file.read_text(encoding="utf-8")) if self.review_file.exists() else {}
        return manifest, review


class Handler(BaseHTTPRequestHandler):
    server: ReviewServer

    def send_bytes(self, body: bytes, content_type: str, status: int = 200) -> None:
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.send_header("Content-Security-Policy", "default-src 'self'; img-src 'self'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; frame-ancestors 'none'")
        self.end_headers()
        self.wfile.write(body)

    def json_response(self, value: object, status: int = 200) -> None:
        self.send_bytes(json.dumps(value).encode(), "application/json", status)

    def do_GET(self) -> None:
        path = urlparse(self.path).path
        if path == "/":
            self.send_bytes(HTML.encode(), "text/html; charset=utf-8")
            return
        if path == "/api/state":
            manifest, review = self.server.state()
            self.json_response({**manifest, "review": review})
            return
        if path.startswith("/assets/"):
            scene_id = Path(path).stem
            if not scene_id.isdigit():
                self.send_error(404); return
            asset = self.server.pack / "delivery" / f"{scene_id}.webp"
            if not asset.is_file():
                self.send_error(404); return
            self.send_bytes(asset.read_bytes(), mimetypes.guess_type(asset.name)[0] or "application/octet-stream")
            return
        self.send_error(404)

    def read_json(self) -> dict:
        length = int(self.headers.get("Content-Length", "0"))
        if length <= 0 or length > 100_000:
            raise ValueError("invalid body")
        return json.loads(self.rfile.read(length))

    def do_POST(self) -> None:
        manifest, review = self.server.state()
        if self.path == "/api/review":
            try:
                body = self.read_json()
                scene_id = int(body["sceneId"])
                scene = next(item for item in manifest["scenes"] if item["sceneId"] == scene_id)
                cells = sorted({int(cell) for cell in body["eligibleCells"]})
                if any(cell < 0 or cell >= 384 for cell in cells): raise ValueError("invalid cell")
                scene["recommendedCells"] = cells
                scene["eligibleCells"] = list(range(384))
                state = body["review"]
                review[str(scene_id)] = {"cropApproved": bool(state["cropApproved"]), "cellsApproved": bool(state["cellsApproved"])}
                manifest["status"] = "draft-crops-and-generated-masks-require-human-review"
                self.server.manifest.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
                self.server.review_file.write_text(json.dumps(review, indent=2) + "\n", encoding="utf-8")
                self.json_response({"message": "Scene review saved."})
            except (KeyError, StopIteration, TypeError, ValueError, json.JSONDecodeError):
                self.json_response({"message": "Invalid review submission."}, 400)
            return
        if self.path == "/api/approve":
            ids = {str(scene["sceneId"]) for scene in manifest["scenes"]}
            complete = len(manifest["scenes"]) == 48 and all(review.get(scene_id, {}).get("cropApproved") and review.get(scene_id, {}).get("cellsApproved") for scene_id in ids)
            if not complete:
                self.json_response({"message": "Pack cannot be approved until all 48 crop and cell reviews pass."}, 409)
                return
            manifest["status"] = "approved"
            self.server.manifest.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
            self.json_response({"message": "Pack approved. Restart the application to load it."})
            return
        self.send_error(404)

    def log_message(self, message: str, *args: object) -> None:
        print(message % args)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--pack", type=Path, default=ROOT / "scene-pack/v1")
    parser.add_argument("--port", type=int, default=8091)
    args = parser.parse_args()
    server = ReviewServer(("127.0.0.1", args.port), args.pack.resolve())
    print(f"Scene review available at http://127.0.0.1:{args.port}")
    server.serve_forever()


if __name__ == "__main__":
    main()
