#!/usr/bin/env python3
"""Bounded HTTP and asset benchmark for a running SceneChain stack."""

import json, os, statistics, time, urllib.request
from pathlib import Path

BASE = os.environ.get("SCENECHAIN_URL", "http://localhost:8088")
SAMPLES = int(os.environ.get("SCENECHAIN_BENCHMARK_SAMPLES", "30"))

def timed(path: str):
    start=time.perf_counter();
    with urllib.request.urlopen(BASE+path, timeout=10) as response:
        body=response.read(); status=response.status
    return (time.perf_counter()-start)*1000, status, len(body)

def percentile(values, p):
    ordered=sorted(values); return ordered[min(len(ordered)-1, round((len(ordered)-1)*p))]

root=[]; status=[]
for _ in range(SAMPLES):
    root.append(timed("/")[0]); status.append(timed("/api/pack/status")[0])

assets=list((Path(__file__).parents[1]/"scene-pack/v1/delivery").glob("*.webp"))
thumbnails=list((Path(__file__).parents[1]/"scene-pack/v1/thumbnails").glob("*.webp"))
report={
  "samples":SAMPLES,
  "http":{
    "landing_ms":{"median":round(statistics.median(root),2),"p95":round(percentile(root,.95),2)},
    "pack_status_ms":{"median":round(statistics.median(status),2),"p95":round(percentile(status,.95),2)},
  },
  "formal_scene_assets":{"count":len(assets),"total_bytes":sum(p.stat().st_size for p in assets),
                         "median_bytes":round(statistics.median(p.stat().st_size for p in assets))},
  "gallery_thumbnails":{"count":len(thumbnails),"total_bytes":sum(p.stat().st_size for p in thumbnails),
                         "decoded_rgba_bytes":len(thumbnails)*480*320*4},
  "estimated_gallery_plus_selected_scene_decoded_rgba_bytes":len(thumbnails)*480*320*4+1920*1280*4,
}
print(json.dumps(report,indent=2))
